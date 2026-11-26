# Spiegazione e Decisioni Architetturali del Class Diagram: `mplan-ui-vaadin`

Documento di dettaglio architetturale, progettuale e di design pattern per il modulo `mplan-ui-vaadin`, a corredo del diagramma delle classi contenuto in `docs/uml/mplan.mdj`.

---

## 1. Panoramica del Modulo e Confini Architetturali

Il modulo Maven **`mplan-ui-vaadin`** (`it.mpelettrica.mplan:mplan-ui-vaadin`) costituisce il layer di presentazione dell'applicazione MPlan. Implementa l'interfaccia grafica per desktop/browser basata su **Vaadin Flow 24** (Java puro, component-oriented, server-side execution via WebSocket / HTTP RPC).

### Regola Fondamentale di Disaccoppiamento (DIP e Modularità)
In stretta aderenza ai vincoli di capitolato e ai principi di architettura esagonale/a livelli del progetto MPlan:
1. **Dipendenza Unidirezionale verso `mplan-api`:** `mplan-ui-vaadin` dipende **esclusivamente** dai contratti di servizio e dai Data Transfer Object (DTO) definiti nel modulo `mplan-api` (`ContattoService`, `AttivitaService`, `AgendaService`, `ChiusuraApplicazione`, entità immutabili, DTO di validazione ed eccezioni).
2. **Isolamento da `mplan-core` e `mplan-persistence`:** La UI **non conosce né importa** le classi di implementazione dei servizi (`*ServiceImpl`), i repository jOOQ, le entità di dominio interne né le classi di accesso al database SQLite. L'iniezione dei servizi avviene a tempo di avvio tramite Spring Boot nel modulo radice `mplan-app`.
3. **Metriche sui Package (Robert C. Martin):**
   - Afferent Coupling $C_a = 0$ (nessun modulo di backend dipende dalla UI).
   - Efferent Coupling $C_e > 0$ (dipende solo da `api` e dai package framework `com.vaadin.flow.*`).
   - Instabilità $I = \frac{C_e}{C_a + C_e} = 1.0$ (massima flessibilità del layer più esterno; cambiamenti nella UI non propagano impatti verso il core).

---

## 2. Organizzazione dei Package

Il modulo è strutturato internamente in 4 package funzionali ad alta coesione e dipendenze acicliche (ADP):

```
it.mpelettrica.mplan.ui/
├── schermate/          # Viste principali mappate con @Route su VerticalLayout
├── componenti/         # Componenti grafici e dialoghi riutilizzabili / delegati
├── navigazione/        # Coordinatore di routing, URL e gestione cronologia (Indietro)
└── infrastruttura/     # Intercettazione globale errori runtime e integrazione Vaadin
```

---

## 3. Schermate dell'Applicazione (`ui.schermate`)

In accordo con la **FSM di Navigazione GUI (Sezione 8.2 dei requisiti)** e gli schemi a blocchi GUI (Sezioni 8.3 e 8.4 della Specifica Requisiti), la UI è articolata in **5 schermate separate**, ciascuna implementata come classe che estende `VerticalLayout` e realizza l'interfaccia `BeforeEnterObserver` di Vaadin Flow.

### 3.1. `HomeView`
- **Responsabilità:** Schermata di ingresso dell'applicazione (`Route("")`). Mostra l'elenco riassuntivo dei contatti in una tabella (`Grid<Contatto>`), la barra di ricerca filtrante per nome/cognome (`FUN.3.1`) e la barra dei comandi principali (`+ Nuovo contatto`, `Agenda Giornaliera`, `Chiudi applicazione`).
- **Attributi principali:**
  - `tuttiContatti: List<Contatto>` (cache locale dei contatti caricati per filtraggio istantaneo client-side/in-memory).
  - `campoRicerca: TextField` (input con debounce per ricerca incrementale).
  - `tabellaContatti: Grid<Contatto>` (griglia con colonne `Nome`, `Cognome`, `Tipo`, `Telefoni`; doppio clic o clic riga apre la scheda contatto).
- **Associazioni e Collaborazioni:**
  - `contattoService: ContattoService` (caricamento iniziale dell'elenco contatti).
  - `chiusuraApplicazione: ChiusuraApplicazione` (chiusura controllata del contesto Spring e arresto JVM, requisito `GUI.1.4`).
  - `navigatore: Navigatore` (transizione verso `FormContattoView`, `SchedaContattoView` e `AgendaGiornalieraView`).
- **Ciclo di vita:** Il metodo `beforeEnter(BeforeEnterEvent)` invoca `aggiornaElenco()` ricaricando i dati freschi ad ogni ingresso o ritorno sulla schermata.

### 3.2. `FormContattoView`
- **Responsabilità:** Schermata per la creazione di un nuovo contatto o la modifica di un contatto esistente (`GUI.3`). Gestisce il binding bidirezionale dei campi anagrafici e coordina la validazione preventiva e di backend.
- **Attributi principali:**
  - `idContatto: UUID` (valorizzato in modifica, `null` in creazione).
  - `campoNome`, `campoCognome`, `campoIndirizzo`, `campoCodiceFiscale`, `campoPartitaIva: TextField`.
  - `campoTipo: Select<TipoContatto>`.
  - `binder: Binder<DatiContatto>` (gestione binding Vaadin, stato dirty e validazioni sincrone sui campi).
  - `editorTelefoni: EditorTelefoni` (componente delegato per la gestione dell'elenco dinamico dei recapiti telefonici).
- **Flusso di Validazione e Salvataggio (Requisiti DAT.5 e A9):**
  1. L'utente compila i campi e preme "Salva".
  2. `validaCampi()` estrae i valori correnti (compresi i recapiti telefonici da `editorTelefoni.getNumeri()`).
  3. Invoca la validazione del servizio di backend `contattoService.valida(dati)`.
  4. Se emergono errori (`List<ErroreValidazione>`), vengono evidenziati puntualmente sui rispettivi campi e il focus viene riposizionato, **preservando integralmente l'input inserito dall'utente** (requisito `DAT.5`).
  5. Se la validazione passa, invoca `crea(...)` o `aggiorna(...)` e delega a `navigatore.indietro()` il ritorno al contesto chiamante. In caso di `ValidazioneException` non catturata a monte, `mostraErrori(...)` visualizza i dettagli.

### 3.3. `SchedaContattoView`
- **Responsabilità:** Schermata di consultazione di dettaglio di un singolo contatto (`GUI.4`).
- **Struttura a due pannelli:**
  - **Pannello Superiore (`riquadroDati: Div`):** Esposizione in sola lettura dei dati anagrafici del contatto (nome, cognome, indirizzo, CF, P.IVA, tipo e lista recapiti telefonici), con pulsante `[Modifica]` che porta a `FormContattoView(id)`.
  - **Pannello Inferiore:** Sezione cronologica delle attività del contatto, con pulsante `[+ Nuova attività]` e il componente `elencoAttivita: ElencoAttivita`.
- **Collaborazioni:**
  - Carica l'anagrafica da `ContattoService.leggi(id)`.
  - Carica lo storico ordinato dal più recente al più vecchio da `AttivitaService.elencaPerContatto(id)`.
  - Connette il callback di selezione riga di `ElencoAttivita` a `navigatore.vaiAApriAttivita(idAttivita)`.

### 3.4. `FormAttivitaView`
- **Responsabilità:** Schermata di creazione o modifica/avanzamento di un'attività (`GUI.5`). È il punto focale del workflow operativo dell'utente.
- **Campi e Controlli:**
  - `campoTitolo: TextField`, `campoData: DatePicker`, `campoOra: TimePicker`, `campoDescrizione: TextArea`, `campoNote: TextArea`.
  - `binder: Binder<DatiAttivita>`.
  - Pulsantiera di stato dedicata: `btnSalva`, `btnParziale`, `btnCompletata`, `btnAnnulla`.
- **Logica delle Transizioni di Stato (FSM Attività e Regole di Dominio):**
  - Il metodo privato `aggiornaStatoControlli()` interroga lo `statoCorrente: StatoAttivita`:
    - Se l'attività è in stato terminale (`COMPLETATA` o `ANNULLATA`), tutti i campi di testo e i pulsanti di transizione vengono disabilitati (sola lettura).
    - Se è `DA_FARE`, abilita `btnSalva`, `btnParziale`, `btnCompletata` e `btnAnnulla`.
    - Se è `PARZIALE`, abilita `btnSalva`, `btnCompletata` e `btnAnnulla`.
  - Per l'annullamento (`btnAnnulla`), la vista apre la modale specializzata `DialogoAnnullamento`, impedendo l'annullamento senza motivazione esplicita.
- **Integrazione Temporale e Testabilità (Decisione D9):**
  - In fase di creazione nuova attività, la data predefinita non è ottenuta da un generico `LocalDate.now()` di sistema, ma tramite `agendaService.oggi()`, garantendo determinismo e testabilità rispetto al `Clock` configurato nel core.

### 3.5. `AgendaGiornalieraView`
- **Responsabilità:** Schermata di consultazione dell'agenda giornaliera delle attività (`GUI.2`).
- **Controlli di Navigazione Temporale:**
  - Intestazione con data corrente formattata in italiano (`etichettaGiorno: Span`).
  - Tasti rapidi di scorrimento: `[< Giorno precedente]`, `[Giorno successivo >]`.
  - Selettore diretto `selettoreData: DatePicker` per saltare a qualsiasi data specifica.
- **Contenuto:** Mostra le voci dell'agenda tramite `elencoAttivita.mostraAgenda(voci)` ottenute da `agendaService.agendaGiorno(giornoCorrente)`.
- **Ritorno Contestuale:** Ogni volta che cambia giorno, la vista lo passa al `Navigatore` con `aggiornaDataAgenda(giorno)`, che lo salva nel campo `dataAgenda`. Quando l'utente apre un'attività e poi torna indietro, Vaadin crea una nuova `AgendaGiornalieraView`: in `beforeEnter` la vista usa `navigatore.dataAgenda()` se c'è, altrimenti `agendaService.oggi()`. `vaiAAgenda()`, usato dalla Home, azzera `dataAgenda`: così l'agenda aperta dalla Home parte sempre da oggi (`FUN.10.4`), mentre tornando dal form si ritrova il giorno che si stava guardando (`GUI.1.1`).

---

## 4. Componenti Grafici Riutilizzabili (`ui.componenti`)

Per rispettare il principio di singola responsabilità (SRP) e massimizzare la leggibilità del codice, porzioni grafiche complesse e comportamenti specialistici sono stati estratti in componenti autonomi.

### 4.1. `EditorTelefoni`
- **Ruolo:** Gestisce l'elenco dinamico dei numeri di telefono all'interno del form contatto (`GUI.3.2`).
- **Struttura:** Estende `VerticalLayout`. Mantiene una lista `campiNumero: List<TextField>`.
- **Comportamento Dinamico:**
  - Ogni riga presenta un campo di testo per il numero, un pulsante `[-] Rimuovi` e, in coda, il pulsante `[+ Aggiungi recapito]`.
  - Espone `getNumeri(): List<String>` per restituire la lista ordinata dei numeri non vuoti al form chiamante.
  - Espone `mostraErrore(messaggio: String)` e `pulisciErrore()` per visualizzare errori di validazione puntuali sul formato del recapito senza delegare logica anagrafica all'esterno.

### 4.2. `ElencoAttivita`
- **Ruolo:** Componente di visualizzazione tabellare / a lista cronologica riutilizzato sia in `SchedaContattoView` (storico attività del contatto) sia in `AgendaGiornalieraView` (attività della giornata).
- **Pattern Adapter / Renderer:**
  - Riceve in ingresso DTO eterogenei: `mostraStorico(List<Attivita>)` oppure `mostraAgenda(List<VoceAgenda>)`.
  - Il metodo privato `creaRiga(...)` normalizza la presentazione grafica applicando i badge cromatici di stato in accordo con il requisito `GUI.8` (es. `DA_FARE` neutro/blu, `PARZIALE` giallo/arancio, `COMPLETATA` verde, `ANNULLATA` grigio barrato).
  - Gestisce l'interazione al clic delegando a un callback funzionale `alClick: Consumer<UUID>`.

### 4.3. `DialogoAnnullamento`
- **Ruolo:** Finestra di dialogo modale (`Dialog` di Vaadin) dedicata all'acquisizione della motivazione obbligatoria per l'annullamento di un'attività (`GUI.6`).
- **Incapsulamento della Regola di Business:**
  - Contiene un campo `campoMotivo: TextArea`.
  - Non consente la conferma se il motivo è vuoto o composto solo da spazi.
  - Al click su "Conferma Annullamento", invoca direttamente `servizioAttivita.annulla(idAttivita, motivo)`.
  - In caso di successo, chiude il dialogo ed esegue il callback `alCompletamento: Runnable` per far ricaricare la schermata sottostante; in caso di errore, visualizza la segnalazione all'interno del dialogo stesso.

---

## 5. Navigazione e Gestione Cronologia (`ui.navigazione`)

### Il Componente `Navigatore` (Façade + Delegation)
Vaadin Flow utilizza il routing basato sull'interfaccia `UI.getCurrent().navigate(...)`. Tuttavia, cablare le destinazioni e i parametri di rotta direttamente all'interno dei pulsanti delle singole viste comporterebbe:
1. Accoppiamento diretto tra le viste (ogni schermata dovrebbe conoscere la classe Java o l'URL target di tutte le altre).
2. Mancanza di memoria della schermata di provenienza per il pulsante generico `[Indietro]` presente in tutte le viste secondarie (FSM 8.2).

Per risolvere tali problemi è stata introdotta la classe **`Navigatore`**:
- **Ambito di Iniezione:** Definito con `@VaadinSessionScope` o `@UIScope` via Spring DI, assicurando un'unica istanza isolata per ciascuna sessione utente/scheda browser aperta.
- **Pila delle Rotte (`pilaRotte: Deque<String>`):**
  - Ogni transizione verso una vista figlia (es. `vaiASchedaContatto`, `vaiANuovoContatto`, `vaiAApriAttivita`) effettua il push della rotta di partenza sulla pila interna prima di eseguire `UI.navigate(...)`.
  - Il metodo `indietro()` effettua il `pop()` della rotta precedente e naviga verso di essa; se la pila è vuota, esegue il fallback sicuro verso `HomeView` (`""`).
- **Giorno dell'agenda (`dataAgenda: LocalDate`):** il `Navigatore` ricorda l'ultimo giorno mostrato dall'agenda, perché la vista viene ricreata a ogni navigazione. `aggiornaDataAgenda(data)` lo salva, `dataAgenda()` lo restituisce e `vaiAAgenda()` lo azzera (vedi 3.5). Il `Navigatore` non legge l'orologio del PC: il giorno corrente lo dà `agendaService.oggi()` (D9).
- **Disaccoppiamento Completo:** Le viste dipendono solo dall'interfaccia semantica di `Navigatore` (`navigatore.vaiAAgenda()`, `navigatore.indietro()`), senza mai maneggiare stringhe di rotta o riferimenti incrociati ad altre schermate.

---

## 6. Gestione Infrastrutturale degli Errori (`ui.infrastruttura`)

### `GestoreErroriUI` (Adapter / Observer)
I requisiti non funzionali e di affidabilità (Decisioni D4 e D7) stabiliscono che:
- Gli errori di validazione devono essere intercettati a livello di form e segnalati inline senza interrompere l'applicazione.
- Gli errori gravi e imprevisti (es. fallimento disco, corruzione DB SQLite, eccezioni di `PersistenzaException`) non devono far comparire la schermata rossa di errore interno di Vaadin ("Internal Server Error") né causare crash non gestiti.

La classe `GestoreErroriUI` realizza:
1. `VaadinServiceInitListener`: aggancia globalmente il gestore all'avvio del servizio Vaadin su ogni nuova istanza di sessione.
2. `ErrorHandler`: implementa il metodo `error(ErrorEvent)`. Quando viene sollevata un'eccezione non gestita nei thread di evento della UI, il gestore:
   - Isola la causa scatenante (`findCause()`).
   - Se l'errore deriva da `PersistenzaException`, visualizza una notifica non bloccante ma visibile all'utente (`Notification.show(..., Type.ERROR)`), informandolo dell'impossibilità di completare l'operazione su disco.
   - Traccia l'errore sui log applicativi (Log4j2) e previene la chiusura anomala della sessione grafica.

---

## 7. Analisi dei Design Pattern Adottati

Il design del modulo applica rigorosamente il catalogo dei pattern illustrato nel testo del corso e nel documento `criteri-design-e-pattern.md`, evitando pattern inappropriati (*Martello d'Oro*):

| Pattern | Ruolo nel Modulo | Motivazione ed Efficacia |
|---|---|---|
| **Façade** | `Navigatore` | Offre alle schermate un punto di accesso unificato e semantico per tutte le operazioni di navigazione, nascondendo le API di basso livello `com.vaadin.flow.component.UI`. |
| **Delegation** | `HomeView` $\rightarrow$ `Navigatore`<br>`FormContattoView` $\rightarrow$ `EditorTelefoni`<br>`SchedaContattoView` $\rightarrow$ `ElencoAttivita`<br>`FormAttivitaView` $\rightarrow$ `DialogoAnnullamento` | Sostituisce l'ereditarietà con la composizione (`has-a`). Ogni vista delega blocchi di presentazione o comportamento specialistico a componenti dedicati. |
| **Adapter** | `ElencoAttivita` | Adatta due DTO eterogenei del backend (`Attivita` per lo storico e `VoceAgenda` per l'agenda) a un layout visuale uniforme con badge di stato e click handler. |
| **Observer / Listener** | `GestoreErroriUI` (`ErrorHandler`), `BeforeEnterObserver` | Permette al framework Vaadin di notificare alle schermate l'ingresso nella rotta per ricaricare i dati e al gestore di errori di ricevere eventi di crash in modo disaccoppiato. |

### Valutazione sul Pattern Singleton (Perché NON usare `getInstance()` statico)
Nelle indicazioni iniziali era stato suggerito di valutare l'uso del pattern Singleton per il coordinamento della UI/navigazione. L'analisi critica condotta alla luce di `criteri-design-e-pattern.md` (§3) e del libro di testo (p. 16) ha portato alla seguente **decisione consapevole**:
- **Rifiuto del Singleton statico globale:** Implementare `Navigatore.getInstance()` come istanza statica globale violerebbe l'isolamento delle sessioni in ambiente web (un utente sovrascriverebbe la `pilaRotte` di un altro) e impedirebbe il test automatico isolato o concorrente (stato globale mutabile condiviso tra test suite).
- **Soluzione conforme (Managed Scope Singleton):** `Navigatore` è gestito come singleton di sessione/UI tramite il container di Dependency Injection di Spring (`@VaadinSessionScope` o `@UIScope`). In questo modo ogni scheda del browser ha il suo ciclo di vita deterministico e isolato, senza ricorrere a costruttori privati o campi statici globali non iniettabili.

---

## 8. Valutazione Critica del Principio di Singola Responsabilità (SRP)

Tutte le 10 classi introdotte sono state analizzate singolarmente rispetto all'SRP:

1. **`HomeView`:** Un solo motivo di cambiamento: l'organizzazione della vista principale dell'applicazione. Non contiene logica di persistenza (delegata a `ContattoService`), né di chiusura (delegata a `ChiusuraApplicazione`), né di instradamento (delegata a `Navigatore`).
2. **`FormContattoView`:** Gestisce il binding e la validazione dei dati anagrafici del contatto. La gestione della lista dinamica dei recapiti telefonici è interamente delegata a `EditorTelefoni`.
3. **`SchedaContattoView`:** Gestisce la presentazione in sola lettura dell'anagrafica. La cronologia delle attività è delegata a `ElencoAttivita`.
4. **`FormAttivitaView` (Trade-off consapevolmente accettato):**
   - Presenta un numero più elevato di controlli poiché il requisito `GUI.5` impone la compresenza di dati descrittivi dell'attività e azioni di cambio stato.
   - Per non violare l'SRP, la logica modale di annullamento con motivazione è stata scorporata in `DialogoAnnullamento`.
   - Separare ulteriormente i pulsanti di cambio stato in un micro-componente autonomo è stato scartato perché avrebbe introdotto un forte accoppiamento di controllo (*Control Coupling*) con la form, dato che lo stato dell'attività blocca e sblocca contestualmente sia i campi sia i pulsanti.
5. **`AgendaGiornalieraView`:** Gestisce la consultazione del giorno e il cambio data; delega il rendering delle voci a `ElencoAttivita`.
6. **`EditorTelefoni`:** Gestisce solo le righe dinamiche dei numeri di telefono. Non conosce contatti, ID o logiche di persistenza.
7. **`ElencoAttivita`:** Gestisce solo la resa visiva delle righe attività e il feedback al clic.
8. **`DialogoAnnullamento`:** Incapsula l'acquisizione del motivo e la chiamata atomica `annulla(...)`.
9. **`Navigatore`:** Gestisce unicamente la mappa degli URL e la cronologia dello stack `Indietro`.
10. **`GestoreErroriUI`:** Incapsula la cattura globale degli errori non gestiti e la loro traduzione in notifiche visuali. L'unione di `VaadinServiceInitListener` e `ErrorHandler` nella medesima classe evita la proliferazione di classi effimere con un solo metodo (*Poltergeist*).

---

## 9. Convenzioni Grafiche e di Routing nel Diagramma UML

Il diagramma delle classi `mplan-ui-vaadin` in `docs/uml/mplan.mdj` adotta criteri formali rigorosi per garantire leggibilità, eleganza e conformità alle specifiche StarUML v5.1.0:

### Disposizione a Fasce Funzionali
- **Fascia 1 ($Y \approx 98$):** Servizi del modulo `mplan-api` e interfacce listener del framework Vaadin.
- **Fascia 2 ($Y \approx 540$):** Schermate applicative (`ui.schermate`) e classe di infrastruttura (`ui.infrastruttura`).
- **Fascia 3 ($Y \approx 1358$):** Navigatore (`ui.navigazione`), componenti delegati (`ui.componenti`) e controlli Vaadin (`Binder`, `VerticalLayout`, `Dialog`, `Notification`).
- **Fascia 4 ($Y \approx 2011$):** DTO, entità ed eccezioni del modulo `mplan-api` e nota illustrativa.

### Routing Ortogonale delle Relazioni
- **Zero incroci impropri:** Nessuna linea attraversa riquadri di classi, interfacce o note. I passaggi tra le fasce avvengono attraverso ampi corridoi orizzontali e vicoli verticali di larghezza $\ge 150\text{ px}$.
- **Zero sovrapposizioni tra linee parallele:** Ogni segmento orizzontale e verticale ha una corsia dedicata distanziata da tutte le altre.
- **Porte di connessione ordinate:** Le connessioni sui bordi superiore e inferiore delle classi sono distribuite equamente da sinistra verso destra in base alla coordinata X del target, eliminando sovrapposizioni e torsioni.
- **Etichette posizionate senza conflitti:** I nomi di ruolo (`-contattoService`, `-navigatore`, ecc.) e le molteplicità (`1`, `*`) sono calcolati con offset espliciti, evitando collisioni reciproche o sovrapposizioni con i testi delle classi.

### Coerenza Cromatica a Livello di Progetto
Per garantire uniformità visiva con i diagrammi preesistenti (`mplan-core`, `mplan-persistence`, `mplan-api`, `mplan-app`):
- `mplan-api`: **`#49ce49`** (verde, identico in tutti i diagrammi).
- `ui.infrastruttura`: **`#ffd8b8`** (pesca tenue, identico a `persistence.infrastruttura`).
- Componenti esterni Vaadin (`com.vaadin.flow.*`): **`#adadff`** (lilla chiaro, identico a librerie esterne come jOOQ, Java e JDBC), con elementi interni in grigio neutro **`#e3e3e3`**.
- Nuovi package esclusivi del layer UI (colori pastello distintivi e non collidenti):
  - `ui.schermate`: **`#fff2a6`** (giallo pastello tenue).
  - `ui.componenti`: **`#b8e8ff`** (azzurro cielo tenue).
  - `ui.navigazione`: **`#e2c6ff`** (lavanda tenue).
