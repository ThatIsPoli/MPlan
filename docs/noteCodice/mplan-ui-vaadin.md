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

- **`navigazione/Navigatore`.** È un bean `@Component @UIScope`, cioè uno per scheda del browser.
  - Prima di ogni `vaiA…` mette in una pila l'indirizzo corrente; `indietro()` lo riprende, oppure va alla Home se la pila è vuota. `indietroOppureScheda(idContatto)` fa lo stesso ma, a pila vuota, apre la scheda del contatto: la usa il form dell'attività aperto scrivendone l'indirizzo.
  - `vaiAHome()` svuota la pila; `vaiAAgenda()` dimentica il giorno memorizzato, perché l'agenda si apre su oggi. `dataAgenda` conserva il giorno dell'agenda quando si torna indietro da un'attività.
  - Naviga per indirizzo e non importa le schermate, così il package `ui.navigazione` non dipende da `ui.schermate` (niente ciclo tra package). I modelli delle rotte sono costanti pubbliche (`ROTTA_HOME`, `ROTTA_SCHEDA_CONTATTO`, `ROTTA_NUOVO_CONTATTO`, `ROTTA_MODIFICA_CONTATTO`, `ROTTA_NUOVA_ATTIVITA`, `ROTTA_ATTIVITA`, `ROTTA_AGENDA`), usate anche nelle `@Route` e `@RouteAlias` delle schermate; l'indirizzo concreto si ottiene mettendo l'identificativo al posto del parametro. Restano le costanti `PARAMETRO_CONTATTO`, `PARAMETRO_ATTIVITA` e `FORMATO_UUID`.
- **`infrastruttura/GestoreErroriUI`.** È un bean `@Component`, che Vaadin registra da solo come `VaadinServiceInitListener`; in ogni nuova sessione si imposta come `ErrorHandler`.
  - Per ogni eccezione scrive nel log un codice di 8 caratteri e mostra una Notification d'errore con un messaggio comprensibile.
  - Il messaggio dipende dalla causa, cercata lungo tutta la catena: `PersistenzaException`, `ValidazioneException`, `IllegalStateException`, `IllegalArgumentException` o altro.
  - Il core lancia `IllegalStateException` in tre casi (transizione non ammessa, modifica di un'attività annullata, contatto sparito): la UI impedisce già queste operazioni, quindi accadono solo se i dati sono cambiati nel frattempo. Il messaggio, unico e valido per tutti, è "Operazione non consentita: i dati potrebbero essere cambiati nel frattempo. Torna alla schermata precedente e riprova."
  - Non rilancia mai eccezioni.
- **`componenti/EditorTelefoni`.** Ogni riga ha un campo e un pulsante per rimuoverla; un pulsante aggiunge una riga.
  - `getNumeri()` restituisce i numeri senza spazi iniziali e finali e senza le righe vuote.
  - `mostraErrore` accumula i messaggi sotto i campi, perché l'errore `telefoni` può arrivare più volte. Evidenzia i campi il cui numero è citato nel messaggio.
- **`componenti/ElencoAttivita`.** Usa una `VirtualList`, che disegna solo le righe visibili (NFR.3).
  - `mostraStorico` mostra data, ora, titolo e stato, e apre l'elenco in fondo con `scrollToEnd()` (FUN.9.1).
  - `mostraAgenda` mostra ora (o "In giornata"), contatto, titolo e stato.
  - Le righe sono una griglia CSS con colonne di larghezza fissa (classi `mplan-elenco-storico` e `mplan-elenco-agenda`), così restano allineate anche con titoli lunghi; sotto i 600 px lo stato scende sotto il titolo.
  - Con l'elenco vuoto mostra una `Card` con icona e testo.
  - Lo stato è un `Badge`; le righe annullate hanno il titolo barrato (GUI.8).
  - `creaRiga` riceve una voce già formattata (record privato `VoceElenco`) e restituisce il componente della riga.
  - La riga è una classe privata `Riga extends Div implements Focusable, HasAriaRole`: si raggiunge con il tasto Tab, ha ruolo "button" e si apre con un clic o con Invio.
- **`componenti/BarraStatoAttivita`.** Mostra il badge dello stato e solo i pulsanti delle transizioni ammesse (Tabella 8.1): da DA_FARE parziale, completata e annulla; da PARZIALE completata e annulla; da COMPLETATA e ANNULLATA nessuno.
- **`componenti/DialogoAnnullamento`.** È un `Dialog` con il campo del motivo (obbligatorio, con il focus all'apertura grazie a `setAutofocus`) e il pulsante di conferma. Il campo non ha `setMaxLength`: la lunghezza la controlla il servizio, e il suo `ValidazioneException` viene mostrato sul campo mentre la finestra resta aperta (GUI.6).
- **`schermate/HomeView`** (`Navigatore.ROTTA_HOME`).
  - Tabella dei contatti con colonne Nome, Cognome, Tipo, Telefono e Indirizzo (GUI.2). Una riga si apre con un clic oppure da tastiera, con le frecce e poi Invio o Spazio (GUI.2.3): Invio è una scorciatoia sulla tabella che apre il contatto della cella con il focus, ricordato con `addCellFocusListener`.
  - Ricerca in memoria tramite `GridListDataView.setFilter`, su nome, cognome, telefoni, codice fiscale e partita IVA. Con la tabella vuota compare uno stato vuoto con icona e testo (`setEmptyStateComponent`).
  - Nell'intestazione tre pulsanti sempre visibili: "Agenda giornaliera" (secondario, GUI.2.2), "Nuovo contatto" (primario, GUI.2.1) e "Chiudi applicazione" (pericolo, evento `chiudi` dalla Home della Tabella 8.2). Hanno sostituito il menu a tendina, su richiesta dell'utente.
- **`schermate/SchedaContattoView`** (`ROTTA_SCHEDA_CONTATTO`). In alto "Indietro", il nome come titolo, "Modifica contatto" e "Nuova attività"; sotto, in una `Card` (era un `Div` nello scheletro), solo i dati compilati, senza segnaposti per quelli vuoti (GUI.4); se non c'è nessun dato la card si nasconde. Subito sotto lo storico, a tutta larghezza.
- **`schermate/FormContattoView`** (`ROTTA_NUOVO_CONTATTO` e alias `ROTTA_MODIFICA_CONTATTO`).
  - Campi di DAT.1 in una `Card`, divisi in tre sezioni con titoletto: "Dati anagrafici", "Recapiti", "Dati fiscali". Ogni sezione è un `FormLayout` auto-responsive: due colonne su PC, una su schermo stretto.
  - `Binder<DatiContatto>` con `withNullRepresentation("")`.
  - Il tipo non ha valore predefinito: la `Select` ha la voce vuota "Non indicato" (`setEmptySelectionAllowed`), che serve anche a togliere un tipo già salvato.
  - Nessun `setMaxLength`: un testo troppo lungo viene segnalato dal servizio sotto il campo, invece di essere troncato in silenzio.
  - Validazione con `ContattoService.valida`: ogni errore viene mostrato sul campo corrispondente, `telefoni` sull'editor, e il focus va sul primo campo in errore (DAT.5).
  - Dopo il salvataggio, `navigatore.indietro()`.
  - Contiene alcuni metodi statici package-private riusati dal form dell'attività, per non duplicare il codice: `mostraErroriSuiCampi`, `pulisciErroriSuiCampi`, `creaTitoloSezione`, `creaSezione` e `creaRiquadroForm`.
- **`schermate/FormAttivitaView`** (`ROTTA_NUOVA_ATTIVITA` e alias `ROTTA_ATTIVITA`).
  - Campi di DAT.2 in una `Card`, in tre sezioni: "Quando" (data e ora), "Cosa" (titolo e descrizione), "Note e materiali". La data propone `agendaService.oggi()` (DAT.2.2).
  - L'ora è un `TextField` con un convertitore del `Binder` (`LocalizzazioneDate.convertitoreOra()`): accetta `hh:mm` o `hh.mm` con ore 0–23 e minuti 00–59, e per ogni altro testo (ad esempio `25:99`, `24:00`, `12:75`) mostra "Ora non valida: usa il formato hh:mm, ad esempio 08:30." e blocca il salvataggio (DAT.2.3, DAT.5).
  - Nessun `setMaxLength` su titolo, descrizione e note: i limiti li controlla il servizio.
  - Per un'attività esistente mostra in testa alla card la barra dello stato e, se annullata, il motivo; un'attività ANNULLATA è in sola lettura.
  - "Indietro" e il salvataggio tornano alla schermata di provenienza; se il form è stato aperto scrivendone l'indirizzo, tornano alla scheda del contatto dell'attività (`indietroOppureScheda`).
- **`schermate/AgendaGiornalieraView`** (`ROTTA_AGENDA`). In alto "Indietro" e il titolo; sotto, in una `Card`, freccia indietro, giorno, freccia avanti, "Vai a…" accanto alle frecce e "Oggi" (GUI.7, GUI.7.3). "Vai a…" è un `DatePicker` con il testo "Vai a…" come prefisso e il giorno visualizzato come valore iniziale. Ha la validazione manuale (`setManualValidation(true)`): con una data vuota o scritta male mostra "Data non valida: usa il formato gg/mm/aaaa." sul campo e resta sul giorno visualizzato (DAT.5); l'errore sparisce al cambio di giorno.

Classi nuove, approvate:

- **`infrastruttura/VistaErrore`.** Implementa `HasErrorParameter<Exception>` e sostituisce la pagina d'errore di Vaadin, che mostrerebbe il testo dell'eccezione. Gestisce gli errori durante l'apertura di una schermata: messaggio comprensibile, codice per il log e pulsante "Torna alla Home".
- **`infrastruttura/VistaPaginaNonTrovata`.** Implementa `HasErrorParameter<NotFoundException>` e sostituisce la pagina predefinita di Vaadin, in inglese, per gli indirizzi che non corrispondono a nessuna schermata. Serve una classe separata da `VistaErrore` perché Vaadin sceglie il gestore dell'eccezione più specifica: per `NotFoundException` vincerebbe `RouteNotFoundError` di Vaadin. Mostra "Pagina non trovata" e il pulsante "Torna alla Home", scrive nel log un WARN con il percorso e restituisce 404.
- **`componenti/LocalizzazioneDate`.** Raccoglie la localizzazione italiana del `DatePicker` (mesi, giorni, lunedì come primo giorno, formato `gg/mm/aaaa`, messaggi per l'input non valido), la preparazione del campo dell'ora (`applicaOra`) e il suo convertitore rigoroso (`convertitoreOra`). Contiene anche la formattazione di date e ore.

Tema grafico:

- **`src/main/resources/META-INF/resources/mplan-tema/tema.css`.** È l'unico foglio di stile. Lo attiva `MPlanApplication` (mplan-app) con `@StyleSheet(Aura.STYLESHEET)`, `@StyleSheet("mplan-tema/tema.css")` e `@ColorScheme(LIGHT)`. Contiene, in ordine:
  1. `@font-face` di Inter, caricato localmente da `fonts/inter/InterVariable.woff2` (NFR.6: nessuna richiesta a Internet);
  2. i token `--mplan-*` presi da `DESIGN.md` (colori, tipografia, raggi 4/8/16 px, spaziatura 4–80 px, ombre);
  3. la mappatura sulle proprietà di Aura e sulle `--vaadin-*` di input, pulsanti, badge, card, dialog e anello di focus (blu, 2 px);
  4. le regole sui componenti: fondo grigio chiaro (`#f7f7f7`) con i contenuti su superfici bianche (raggio 16 px, ombra leggera); pulsanti maiuscoli alti 44 px con gerarchia primario (blu pieno), secondario (contorno inchiostro), terziario (testo blu) e pericolo (contorno rosso); campi con bordo inchiostro al focus; tabella come superficie bianca con righe alte e hover azzurro;
  5. le classi semantiche `mplan-*`. I badge di stato hanno un contrasto testo/fondo di almeno 4,5:1; "Completata" è bianco su `#356373`. Il riquadro del motivo di annullamento manda a capo anche le parole lunghe (`overflow-wrap: anywhere`);
  6. un blocco `@media (max-width: 600px)` per le finestre strette: padding ridotto, pulsanti dell'intestazione a tutta larghezza, form su una colonna, righe di storico e agenda su più livelli. La pagina non scorre in orizzontale; la tabella dei contatti scorre al proprio interno.
- **`mplan-tema/icona.svg`.** Icona dell'applicazione (quadrato blu primario con una "M" bianca), servita localmente. La attiva `MPlanApplication.configurePage` con `addFavIcon` (mplan-app).
- Il segnaposto della ricerca nella Home è "Cerca contatti"; la spiegazione dei campi cercati è nel testo di aiuto (`setHelperText`).

## Scelte non ovvie

- **Nessuno stile inline.** Il codice Java usa solo `addClassName("mplan-…")` e varianti del tema (`ButtonVariant.PRIMARY`, `TERTIARY`, `ERROR`). Colori e font stanno solo nel blocco 2 del CSS: per cambiare aspetto basta modificare quello.
- **Ora in un `TextField` e non in un `TimePicker`.** Con il `TimePicker` di Vaadin 25.3 un'ora fuori scala come `25:99` viene interpretata dal connettore lato browser, che mostra `02:39`, mentre il valore resta vuoto e il campo non risulta non valido: l'attività si salverebbe senza ora mentre l'utente ne vede una. Il server non riceve né il testo né un errore, quindi non c'è un'API per accorgersene. Con un `TextField` e un convertitore del `Binder` il testo viene controllato tutto lato server. Si perde solo l'elenco a tendina degli orari.
- **Navigazione per indirizzo.** `Navigatore` usa `UI.navigate(String)` invece della navigazione tipizzata per classe, che lo costringerebbe a importare le schermate e creerebbe un ciclo tra i package. Gli indirizzi nascono dalle stesse costanti usate nelle `@Route`, quindi restano allineati.
- **Regola duplicata della modificabilità.** "ANNULLATA è in sola lettura" e le transizioni ammesse si ripetono nella UI (`FormAttivitaView.aggiornaModificabilitaCampi`, `BarraStatoAttivita.aggiornaPulsanti`), con un commento che rimanda a `RegoleStatoAttivita`: la UI non può importare il core. In ogni caso il core rifiuta le operazioni non ammesse (`IllegalStateException`, gestita da `GestoreErroriUI`).
- **Data facoltativa nella UI.** Se l'utente svuota la data, la UI manda `null` e il core usa la data di oggi; per questo il campo non è obbligatorio.
- **Validazione doppia.** Il form chiama prima `valida()` (come prevede lo scheletro) e poi `crea` o `modifica` dentro un try/catch su `ValidazioneException`, nel caso i dati cambino nel frattempo.
- **Cambio di stato da un'attività aperta.** Si aggiornano barra, stato e modificabilità senza perdere i testi non ancora salvati; si rileggono anche i testi solo se l'attività è stata annullata.
- **Rotte con la regex completa di un UUID.** Un indirizzo con un identificativo malformato non corrisponde a nessuna rotta. Se l'identificativo è valido ma non esiste, la schermata mostra una Notification e passa alla Home con `forwardTo`.
- **`HomeView.nomeTipo` package-private.** Riusato dalla scheda e dal form del contatto. Allo stesso modo `ElencoAttivita.creaBadgeStato` è package-private e riusato da `BarraStatoAttivita`.
- **Nessuna conferma su "Indietro" nei form.** Le modifiche non salvate vengono scartate, come da Tabella 8.2.

## Deviazioni dal diagramma

- Classi aggiunte: `infrastruttura.VistaErrore`, `infrastruttura.VistaPaginaNonTrovata` e `componenti.LocalizzazioneDate` (approvate).
- Rispetto allo scheletro sono cambiati solo membri privati:
  - `ElencoAttivita.creaRiga(UUID, String, StatoAttivita)` è diventato `creaRiga(VoceElenco)` e restituisce un `Component`;
  - `SchedaContattoView.riquadroDati` passa da `Div` a `Card`;
  - `Binder` è tipizzato;
  - `validaCampi` riceve i dati già letti;
  - `caricaScheda` e `caricaAttivita` restituiscono `boolean`;
  - nuovi metodi privati di supporto.
- Nuovi membri pubblici e package-private:
  - in `Navigatore`, le costanti `PARAMETRO_*`, `FORMATO_UUID` e `ROTTA_*` (i modelli delle rotte, usati anche nelle `@Route`) e il metodo `indietroOppureScheda(UUID)`;
  - `HomeView.nomeTipo`, `ElencoAttivita.creaBadgeStato`, `GestoreErroriUI.nuovoCodice` e `GestoreErroriUI.messaggioPer` (package-private);
  - in `FormContattoView`, i metodi statici package-private `mostraErroriSuiCampi`, `pulisciErroriSuiCampi`, `creaTitoloSezione`, `creaSezione` e `creaRiquadroForm`.
- `FormAttivitaView.campoOra` è un `TextField` invece del `TimePicker` del diagramma (vedi "Scelte non ovvie").
- Il tema non è attivato nel modulo ui ma su `MPlanApplication`, per decisione dell'utente.

## Verifica

- Simboli Vaadin 25.3 verificati con il server MCP di Vaadin (javadoc 25.3.5): `Badge`, `Card`, `VirtualList`, `FormLayout`, `Binder`, `Converter.from`, i18n di `DatePicker`, `Select.setEmptySelectionAllowed`, `Grid.setEmptyStateComponent` e `addCellFocusListener`, `setAutofocus`, `HasAriaRole`, `Focusable`, `Shortcuts`, `NotificationVariant`, `HasErrorParameter`, `StyleSheet`, `ColorScheme` e `Aura.STYLESHEET`. Il comportamento del `TimePicker` con le ore fuori scala è stato verificato sul sorgente del connettore (`timepickerConnector.ts` di `vaadin-time-picker-flow` 25.3.6).
- `jdeps` sulle classi compilate: `ui.navigazione` non dipende più da `ui.schermate`.
- Revisione con la skill `vaadin-usage-review`: la barra del giorno dell'agenda e il messaggio dell'elenco vuoto, disegnati a mano come card, ora sono `Card`.
- Compilazione dei sorgenti con `javac --release 27` (JDK 27) sul classpath di Maven del modulo, più l'API servlet: nessun errore e nessun avviso di deprecazione.
- Verifica a runtime (jar di mplan-app, database nella scratchpad, Edge headless con Playwright):
  - `/pagina-che-non-esiste` mostra "Pagina non trovata" in italiano e "Torna alla Home" riporta a `/`;
  - a 390 px Home, nuovo contatto e agenda hanno `scrollWidth` uguale alla larghezza della finestra, quindi nessuno scorrimento orizzontale;
  - a 1280 px l'aspetto non cambia;
  - `/mplan-tema/icona.svg` risponde 200 con tipo `image/svg+xml`.
- Verifica a runtime del restyling (app avviata con `-Djava.awt.headless=true`, database nuovo nella scratchpad, Edge headless con Playwright), tutte superate:
  - Home senza tendina, con i tre pulsanti;
  - tipo vuoto per un nuovo contatto, poi impostato su "Azienda" e di nuovo svuotato;
  - Tab, frecce e Invio aprono la scheda;
  - la scheda non mostra segnaposti per i dati vuoti;
  - le ore `25:99`, `24:00` e `12:75` mostrano l'errore e non salvano;
  - all'apertura del dialogo il focus è sul motivo;
  - un motivo con una parola lunghissima resta nel riquadro;
  - "Vai a…" con una data scritta male e con il campo vuoto mostra l'errore e lascia il giorno invariato;
  - con un titolo lungo la colonna del contatto resta allineata;
  - aperto da indirizzo diretto, il form dell'attività con "Indietro" va alla scheda del contatto;
  - nessuno scorrimento orizzontale a 390 px;
  - nessun errore nella console né risposte HTTP di errore (il 404 di `/favicon.ico` non c'è più).
- Nessun test, come stabilito.
