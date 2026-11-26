# Note sul codice di `mplan-ui-vaadin`

Interfaccia grafica di MPlan in Vaadin 25.3 (tema base Aura) con Spring. Il modulo dipende solo da `mplan-api`, Vaadin, Spring e log4j-api (Design D2, D7).

## Cosa è stato fatto rispetto allo scheletro

Regole valide per tutti i file generati da Javazzar:

- resta solo l'intestazione Javazzar in testa al file; sono stati tolti tutti i marcatori;
- import espliciti al posto di `java.util.*` e `java.time.*`;
- tolti i getter e i setter privati generati;
- i riferimenti a servizi, navigatore e componenti diventano campi `final`, assegnati nel costruttore;
- i costruttori sono spezzati in metodi privati (`configuraPagina`, `creaIntestazione`, `creaForm`, `creaPulsanti`, `configuraBinder`, …);
- Javadoc in italiano su tutto ciò che è pubblico.

Le classi file per file:

- **`navigazione/Navigatore`.** È un bean `@Component @UIScope`, cioè uno per scheda del browser. Prima di ogni `vaiA…` mette in una pila l'indirizzo corrente; `indietro()` lo riprende, oppure va alla Home se la pila è vuota. `vaiAHome()` svuota la pila; `vaiAAgenda()` dimentica il giorno memorizzato, perché l'agenda si apre su oggi. `dataAgenda` conserva il giorno dell'agenda quando si torna indietro da un'attività. Aggiunte tre costanti pubbliche, usate nei modelli delle rotte: `PARAMETRO_CONTATTO`, `PARAMETRO_ATTIVITA` e `FORMATO_UUID`.
- **`infrastruttura/GestoreErroriUI`.** È un bean `@Component`, che Vaadin registra da solo come `VaadinServiceInitListener`; in ogni nuova sessione si imposta come `ErrorHandler`.
  - Per ogni eccezione scrive nel log un codice di 8 caratteri e mostra una Notification d'errore con un messaggio comprensibile.
  - Il messaggio dipende dalla causa, cercata lungo tutta la catena: `PersistenzaException`, `ValidazioneException`, `IllegalStateException`, `IllegalArgumentException` o altro.
  - Non rilancia mai eccezioni.
- **`componenti/EditorTelefoni`.** Ogni riga ha un campo e un pulsante per rimuoverla; un pulsante aggiunge una riga.
  - `getNumeri()` restituisce i numeri senza spazi iniziali e finali e senza le righe vuote.
  - `mostraErrore` accumula i messaggi sotto i campi, perché l'errore `telefoni` può arrivare più volte. Evidenzia i campi il cui numero è citato nel messaggio.
- **`componenti/ElencoAttivita`.** Usa una `VirtualList`, che disegna solo le righe visibili (NFR.3).
  - `mostraStorico` mostra data, ora, titolo e stato, e apre l'elenco in fondo con `scrollToEnd()` (FUN.9.1).
  - `mostraAgenda` mostra ora (o "In giornata"), contatto, titolo e stato.
  - Lo stato è un `Badge`; le righe annullate hanno il titolo barrato (GUI.8).
  - `creaRiga` riceve una voce già formattata (record privato `VoceElenco`) e restituisce il componente della riga.
  - La riga è una classe privata `Riga extends Div implements Focusable, HasAriaRole`: si raggiunge con il tasto Tab, ha ruolo "button" e si apre con un clic o con Invio.
- **`componenti/BarraStatoAttivita`.** Mostra il badge dello stato e solo i pulsanti delle transizioni ammesse (Tabella 8.1): da DA_FARE parziale, completata e annulla; da PARZIALE completata e annulla; da COMPLETATA e ANNULLATA nessuno.
- **`componenti/DialogoAnnullamento`.** È un `Dialog` con il campo del motivo (obbligatorio, al massimo 1000 caratteri) e il pulsante di conferma. Un `ValidazioneException` restituito dal servizio viene mostrato sul campo e la finestra resta aperta (GUI.6).
- **`schermate/HomeView`** (`""`).
  - Tabella dei contatti con colonne Nome, Cognome, Tipo, Telefono e Indirizzo; un clic su una riga apre la scheda.
  - Ricerca in memoria tramite `GridListDataView.setFilter`, su nome, cognome, telefoni, codice fiscale e partita IVA.
  - `Select` "Menu" con "Agenda giornaliera" e "Chiudi applicazione" (GUI.2.2); dopo ogni scelta torna vuoto.
  - Pulsante "Nuovo contatto".
- **`schermate/SchedaContattoView`** (`contatti/:idContatto`). Mostra i dati in una `Card` (era un `Div` nello scheletro), i pulsanti "Modifica contatto", "Nuova attività" e "Indietro", e lo storico.
- **`schermate/FormContattoView`** (`contatti/nuovo` e alias `contatti/:idContatto/modifica`).
  - `FormLayout` auto-responsive e `Binder<DatiContatto>` con `withNullRepresentation("")`.
  - Validazione con `ContattoService.valida`: ogni errore viene mostrato sul campo corrispondente, `telefoni` sull'editor, e il focus va sul primo campo in errore (DAT.5).
  - Dopo il salvataggio, `navigatore.indietro()`.
- **`schermate/FormAttivitaView`** (`contatti/:idContatto/nuova-attivita` e alias `attivita/:idAttivita`).
  - Campi: titolo, data (propone `agendaService.oggi()`), ora facoltativa, descrizione e note (al massimo 5000 caratteri).
  - Una data o un'ora scritte male vengono intercettate da `binder.validate()` e bloccano il salvataggio (FUN.11).
  - Per un'attività esistente mostra la barra dello stato e, se annullata, il motivo; un'attività ANNULLATA è in sola lettura.
- **`schermate/AgendaGiornalieraView`** (`agenda`). Frecce per il giorno precedente e il successivo, pulsante "Oggi" e selettore "Vai a…" (valore iniziale: il giorno visualizzato). Con una data vuota o scritta male resta sul giorno corrente (GUI.7.3).

Classi nuove, approvate:

- **`infrastruttura/VistaErrore`.** Implementa `HasErrorParameter<Exception>` e sostituisce la pagina d'errore di Vaadin, che mostrerebbe il testo dell'eccezione. Gestisce gli errori durante l'apertura di una schermata: messaggio comprensibile, codice per il log e pulsante "Torna alla Home".
- **`componenti/LocalizzazioneDate`.** Raccoglie la localizzazione italiana di `DatePicker` e `TimePicker`: mesi, giorni, lunedì come primo giorno, formato `gg/mm/aaaa`, passo di 15 minuti e messaggi per l'input non valido. Contiene anche la formattazione di date e ore.

Tema grafico:

- **`src/main/resources/META-INF/resources/mplan-tema/tema.css`.** È l'unico foglio di stile. Lo attiva `MPlanApplication` (mplan-app) con `@StyleSheet(Aura.STYLESHEET)`, `@StyleSheet("mplan-tema/tema.css")` e `@ColorScheme(LIGHT)`. Contiene, in ordine:
  1. `@font-face` di Inter, caricato localmente da `fonts/inter/InterVariable.woff2` (NFR.6: nessuna richiesta a Internet);
  2. i token `--mplan-*` presi da `DESIGN.md` (colori, tipografia, raggi 4/8/16 px, spaziatura 4–80 px, ombre);
  3. la mappatura sulle proprietà di Aura e sulle `--vaadin-*` di input, pulsanti, badge, card e dialog;
  4. le regole sui componenti: pulsanti maiuscoli alti 44 px, secondario con contorno, campi con bordo inchiostro al focus, tabella con hairline;
  5. le classi semantiche `mplan-*`.

## Scelte non ovvie

- **Nessuno stile inline.** Il codice Java usa solo `addClassName("mplan-…")` e varianti del tema (`ButtonVariant.PRIMARY`, `TERTIARY`, `ERROR`, `GridVariant.NO_BORDER`). Colori e font stanno solo nel blocco 2 del CSS: per cambiare aspetto basta modificare quello.
- **Regola duplicata della modificabilità.** "ANNULLATA è in sola lettura" e le transizioni ammesse si ripetono nella UI (`FormAttivitaView.aggiornaModificabilitaCampi`, `BarraStatoAttivita.aggiornaPulsanti`), con un commento che rimanda a `RegoleStatoAttivita`: la UI non può importare il core. In ogni caso il core rifiuta le operazioni non ammesse (`IllegalStateException`, gestita da `GestoreErroriUI`).
- **Data facoltativa nella UI.** Se l'utente svuota la data, la UI manda `null` e il core usa la data di oggi; per questo il campo non è obbligatorio.
- **Validazione doppia.** Il form chiama prima `valida()` (come prevede lo scheletro) e poi `crea` o `modifica` dentro un try/catch su `ValidazioneException`, nel caso i dati cambino nel frattempo.
- **Cambio di stato da un'attività aperta.** Si aggiornano barra, stato e modificabilità senza perdere i testi non ancora salvati; si rileggono anche i testi solo se l'attività è stata annullata.
- **Rotte con la regex completa di un UUID.** Un indirizzo con un identificativo malformato non corrisponde a nessuna rotta. Se l'identificativo è valido ma non esiste, la schermata mostra una Notification e passa alla Home con `forwardTo`.
- **`HomeView.nomeTipo` package-private.** Riusato dalla scheda e dal form del contatto. Allo stesso modo `ElencoAttivita.creaBadgeStato` è package-private e riusato da `BarraStatoAttivita`.
- **Nessuna conferma su "Indietro" nei form.** Le modifiche non salvate vengono scartate, come da Tabella 8.2.

## Deviazioni dal diagramma

- Classi aggiunte: `infrastruttura.VistaErrore` e `componenti.LocalizzazioneDate` (approvate).
- Rispetto allo scheletro sono cambiati solo membri privati:
  - `ElencoAttivita.creaRiga(UUID, String, StatoAttivita)` è diventato `creaRiga(VoceElenco)` e restituisce un `Component`;
  - `SchedaContattoView.riquadroDati` passa da `Div` a `Card`;
  - `Binder` è tipizzato;
  - `validaCampi` riceve i dati già letti;
  - `caricaScheda` e `caricaAttivita` restituiscono `boolean`;
  - nuovi metodi privati di supporto.
- Nuovi membri pubblici e package-private: le costanti di `Navigatore`; `HomeView.nomeTipo`, `ElencoAttivita.creaBadgeStato`, `GestoreErroriUI.nuovoCodice` e `GestoreErroriUI.messaggioPer` (package-private).
- Il tema non è attivato nel modulo ui ma su `MPlanApplication`, per decisione dell'utente.

## Verifica

- Simboli Vaadin 25.3 verificati con il server MCP di Vaadin (javadoc 25.3.5): `Badge`, `Card`, `VirtualList`, `FormLayout`, `Binder`, i18n di `DatePicker` e `TimePicker`, `HasAriaRole`, `Focusable`, `Shortcuts`, `GridVariant`, `NotificationVariant`, `HasErrorParameter`, `StyleSheet`, `ColorScheme` e `Aura.STYLESHEET`.
- Compilazione dei sorgenti con `javac --release 27` (JDK 27) sul classpath di Maven del modulo, più l'API servlet: nessun errore e nessun avviso di deprecazione.
- Nessun test, come stabilito.
