# Il modulo persistence di MPlan, spiegato semplice

Questo documento spiega l'organizzazione e il class diagram `mplan-persistence` del file StarUML (`docs/uml/mplan.mdj`): quali package contiene, perché ogni classe è progettata così, come dialoga con il database SQLite tramite jOOQ e come si collega a `mplan-core`, `mplan-api` e `mplan-app`.

Le scelte seguono l'architettura definita in `design.md` (D1–D8), le convenzioni StarUML/Rebel già usate in `core-spiegazione.md` e i principi di progettazione del corso (van Vliet, Cap. 11–12; slide sui pattern).

---

## 1. Dove sta `persistence` e da chi dipende

Nell'architettura a cinque moduli Maven di MPlan:

```text
   mplan-ui-swing        schermate (vede solo api)
         │ usa
         ▼
     mplan-api           contratto pubblico: servizi, entità, input, eccezioni
         ▲
         │ implementa i servizi / usa entità ed eccezioni
     mplan-core          logica: servizi, regole, interfacce repository e transazioni
         ▲
         │ implementa le interfacce repository e gestisce le transazioni
   mplan-persistence     database SQLite con jOOQ e schema.sql

   mplan-app             avvia il programma: crea e collega i moduli
```

| Modulo | Cosa vede `mplan-persistence` | Perché serve la dipendenza |
|---|---|---|
| `mplan-core` | `core.repository` e `core.dominio` (`VoceRegistro`, `TipoModifica`) | Per implementare le interfacce dei repository e delle transazioni e per salvare/leggere le voci del registro |
| `mplan-api` | `Contatto`, `Attivita`, `StatoAttivita`, `PersistenzaException` | I repository salvano e restituiscono le entità di `api` e convertono i guasti del database in `PersistenzaException` |

Questa doppia dipendenza verso `mplan-core` e `mplan-api` è già prevista in `design.md` (§2.4) e nel `pom.xml` di `mplan-persistence`. Resta invece vero che `mplan-ui-swing` non vede né `core` né `persistence`, e che `mplan-api` non dipende da nessuno.

---

## 2. Organizzazione dei package (non "livelli")

Come in `mplan-core`, tutto il codice sta sotto `it.mpelettrica.mplan.persistence`. Nel file StarUML questo corrisponde alla catena di package `it` > `mpelettrica` > `mplan` > `persistence`.

I sottopackage interni **non sono "livelli architetturali"**: sono un modo ordinato per raggruppare le responsabilità dentro un unico diagramma (`mplan-persistence`).

```text
it.mpelettrica.mplan.persistence
├── PersistenzaFactory        (nella radice: punto d'ingresso per mplan-app e test)
├── repository                (implementazioni jOOQ dei 3 repository del core)
├── mapping                   (conversione statica tra oggetti Java e righe delle tabelle)
└── infrastruttura            (schema all'avvio, transazioni, canale DB, errori SQL)
```

| Package | Contiene | Può usare |
|---|---|---|
| `persistence` *(radice)* | `PersistenzaFactory` | `repository`, `infrastruttura`, `api`, `core.repository`, `javax.sql`, `org.jooq` |
| `persistence.repository` | `JooqContattoRepository`, `JooqAttivitaRepository`, `JooqRegistroRepository` | `infrastruttura`, `mapping`, `core.repository`, `core.dominio`, `api`, classi generate jOOQ |
| `persistence.mapping` | `ContattoMapper`, `AttivitaMapper`, `VoceRegistroMapper` | `api`, `core.dominio`, record generati jOOQ |
| `persistence.infrastruttura` | `InizializzatoreSchema`, `ContestoTransazione`, `GestoreTransazioniJooq`, `TraduttoreEccezioni` | `core.repository` (`GestoreTransazioni`), `api` (`PersistenzaException`), `org.jooq` |

### Perché le dipendenze non formano cicli

- `persistence` *(radice)* guarda verso `repository` e `infrastruttura`, ma **nessun sottopackage guarda verso la radice**.
- `repository` usa `infrastruttura` e `mapping`.
- `infrastruttura` e `mapping` **non dipendono l'uno dall'altro** né da `repository`.

*(Nota: se `PersistenzaFactory` fosse stata messa dentro `infrastruttura`, si sarebbe creato un ciclo `infrastruttura ↔ repository`, perché `PersistenzaFactory` istanzia i repository mentre i repository usano `ContestoTransazione`. Tenerla nella radice `persistence` elimina il ciclo e nasconde i sottopackage interni a `mplan-app`.)*

---

## 3. Come leggere il diagramma e gli elementi grigi

Il diagramma `mplan-persistence` è organizzato a fasce dall'alto verso il basso:

```text
core.repository (grigio)    ContattoRepository  AttivitaRepository  RegistroRepository  GestoreTransazioni
persistence (radice)        PersistenzaFactory
persistence.repository      JooqContattoRepository   JooqAttivitaRepository   JooqRegistroRepository
persistence.mapping         ContattoMapper           AttivitaMapper           VoceRegistroMapper
persistence.infrastruttura  ContestoTransazione      GestoreTransazioniJooq   InizializzatoreSchema   TraduttoreEccezioni
esterni / contratti (grigi) api (Contatto, Attivita, StatoAttivita, PersistenzaException) · core.dominio (VoceRegistro, TipoModifica)
                            package generato jOOQ (tabelle/record) · org.jooq (DSLContext, ExecuteListener, ExecuteContext) · javax.sql (DataSource)
```

### Regole StarUML / Rebel seguite

- **Elementi grigi:** non vanno ricreati; si trascinano dal Model Explorer (per `core` e `api`) oppure si aggiungono nei rispettivi package esterni (`org.jooq`, `javax.sql`, package generato jOOQ) accanto a `java.time`.
- **Frecce:**
  - tratteggiata con triangolo vuoto (`..|>`): realizzazione di un'interfaccia (`JooqAttivitaRepository ..|> AttivitaRepository`);
  - continua con punta aperta e `X` dal lato del possessore (`X── -campo 1 ──>`): associazione navigabile, cioè un campo `private final` generato da Rebel senza ripeterlo nel riquadro degli attributi;
  - tratteggiata con punta aperta (`..>`): uso temporaneo in un metodo o chiamata a metodi statici (es. dai repository verso i `*Mapper`).
- **Costruttori espliciti:** tutte le classi istanziate hanno il costruttore nel diagramma, così Rebel genera i campi e le dipendenze vengono passate dall'esterno (*Dependency Injection*).
- **Operazioni sottolineate:** indicano metodi `static`, usati nei tre `*Mapper`.
- **Parametro template `<T>`:** usato in `ContestoTransazione.eseguiConContesto` (come in `GestoreTransazioni.inTransazione` del core) per evitare che Rebel generi una classe `T.java` inesistente.

---

## 4. Schema del database (`schema.sql` e modello ER)

Come deciso in `design.md` (D6), la struttura delle tabelle è descritta in **un unico file `schema.sql`** collocato in `mplan-persistence/src/main/resources/schema.sql`:

1. **In compilazione (Maven):** il plugin `jooq-meta-extensions` legge `schema.sql` e genera automaticamente le classi Java delle tabelle e dei record (senza bisogno di un database già acceso).
2. **A runtime:** il file `schema.sql` è **incorporato dentro il JAR / eseguibile** dell'applicazione (non è un file esterno da copiare a mano accanto al programma). All'avvio, `InizializzatoreSchema` lo legge dal classpath e crea tabelle e indici se non esistono ancora (Q6).

### Tabelle e relazioni (modello ER)

| Tabella | Chiavi e relazioni | Contenuto principale (da `DAT.1`–`DAT.4`) |
|---|---|---|
| `CONTATTO` | PK `id` | `nome` (obbligatorio), `cognome`, `indirizzo`, `codice_fiscale`, `partita_iva`, `tipo` (`PRIVATO`/`AZIENDA`, facoltativo). Clienti e cantieri stanno nella stessa tabella (`FUN.1.3`). |
| `TELEFONO_CONTATTO` | FK `id_contatto` → `CONTATTO(id)` (**1:N**) | `numero` (max 20 caratteri). Realizza `DAT.1.4` (*uno o più numeri facoltativi per contatto*) mantenendo la prima forma normale (1NF). |
| `ATTIVITA` | PK `id`; FK `id_contatto` → `CONTATTO(id)` (**1:N**) | `titolo` (obbl.), `data` (obbl.), `ora` (facoltativa), `descrizione`, `note`, `stato` (`DA_FARE`, `PARZIALE`, `COMPLETATA`, `ANNULLATA`), `motivo_annullamento`. Con indici su `(id_contatto, data)` e `(data)` per rispettare `NFR.3`/`Q5` (< 2 secondi con 5.000+ attività). Nessuna cancellazione fisica (`FUN.12.2`). |
| `VOCE_REGISTRO` | PK `id`; FK `id_attivita` → `ATTIVITA(id)` (**1:N**) | `quando`, `tipo` (`MODIFICA`, `CAMBIO_STATO`, `ANNULLAMENTO`), `motivo`. Realizza il registro applicativo delle modifiche (`FUN.13`, `DAT.4`) in modo interrogabile per attività e atomico col rollback; è distinto dal file di log tecnico `log4j2`. |

---

## 5. Radice `persistence`: `PersistenzaFactory`

È la **Factory / Façade** con cui `mplan-app` (in produzione) e i test di integrazione (`D8`) avviano l'intero strato di persistenza senza conoscere né jOOQ, né i convertitori, né la gestione interna delle transazioni (*Information Hiding*).

| Elemento | Dettaglio |
|---|---|
| **Associazioni** | `-contesto 1 → ContestoTransazione`, `-inizializzatore 1 → InizializzatoreSchema` |
| **Costruttore** | `PersistenzaFactory(urlJdbc: String)` — configura SQLite (incluso `PRAGMA foreign_keys = ON` per far rispettare i collegamenti tra tabelle, e gestione sicura degli URL `:memory:` nei test affinché il DB in RAM non venga distrutto tra inizializzazione e query), collega `TraduttoreEccezioni` alla configurazione jOOQ e prepara `ContestoTransazione` e `InizializzatoreSchema` |
| **Operazioni** | `inizializzaSchema(): void`, `creaGestoreTransazioni(): GestoreTransazioni`, `creaContattoRepository(): ContattoRepository`, `creaAttivitaRepository(): AttivitaRepository`, `creaRegistroRepository(): RegistroRepository` |
| **Dipendenze (`..>`)** | `TraduttoreEccezioni`, `GestoreTransazioniJooq`, `JooqContattoRepository`, `JooqAttivitaRepository`, `JooqRegistroRepository`, `DataSource` |

---

## 6. Package `persistence.infrastruttura`

Raccoglie le quattro classi infrastrutturali, ciascuna con una singola responsabilità chiara:

### `InizializzatoreSchema`
- **Responsabilità:** legge `schema.sql` incorporato nel JAR e lo esegue all'avvio per creare tabelle e indici se mancanti (`CREATE TABLE IF NOT EXISTS` / `CREATE INDEX IF NOT EXISTS`, così non sovrascrive né cancella dati esistenti).
- **Associazione:** `-dsl 1 → DSLContext` *(configurato con `TraduttoreEccezioni`, così anche eventuali errori di avvio diventano `PersistenzaException`)*.
- **Operazioni:** `InizializzatoreSchema(dsl: DSLContext)`, `inizializza(): void`.

### `ContestoTransazione`
- **Responsabilità:** coordina il canale jOOQ (`DSLContext`) da usare in ogni momento. Se è attiva una transazione aperta da `GestoreTransazioniJooq` sul thread corrente, fornisce il canale transazionale; altrimenti fornisce il canale base.
- **Associazione:** `-dslBase 1 → DSLContext`.
- **Operazioni:**
  - `ContestoTransazione(dslBase: DSLContext)`
  - `dslCorrente(): DSLContext`
  - `eseguiConContesto(dsl: DSLContext, op: Runnable): void`
  - `eseguiConContesto<T>(dsl: DSLContext, op: Supplier<T>): T`

### `GestoreTransazioniJooq` *(`..|> GestoreTransazioni`)*
- **Responsabilità:** **Adapter** che realizza l'interfaccia `GestoreTransazioni` del core usando le transazioni di jOOQ (`Q4`, `D5`).
- **Associazione:** `-contesto 1 → ContestoTransazione`.
- **Operazioni:** `GestoreTransazioniJooq(contesto: ContestoTransazione)`, `inTransazione(op: Runnable): void`, `inTransazione<T>(op: Supplier<T>): T`.
- **Comportamento chiave:**
  - Se tutte le operazioni nel blocco terminano bene, conferma il salvataggio; se una fallisce, annulla tutto (rollback).
  - Supporta chiamate annidate senza aprire transazioni incoerenti.
  - Se il codice del core dentro il blocco lancia un'eccezione applicativa (es. `ValidazioneException` o errore di stato), il database fa rollback ma l'eccezione del core viene **lasciata passare invariata**; solo gli errori tecnici di accesso al DB (es. guasto in apertura connessione o al commit) vengono trasformati in `PersistenzaException`.

### `TraduttoreEccezioni` *(`..|> ExecuteListener`)*
- **Responsabilità:** intercettore collegato alla configurazione di jOOQ. Ogni volta che una query SQL fallisce, registra il dettaglio tecnico sul file di log (`log4j2`) e trasforma l'errore SQLite/JDBC in una `PersistenzaException` di `mplan-api`.
- **Operazione:** `exception(ctx: ExecuteContext): void`.
- **Perché è separata:** evita di duplicare blocchi `try/catch` in tutti e tre i repository e mantiene `ContestoTransazione` focalizzato solo sulle transazioni (SRP), senza che `mplan-core` debba mai importare eccezioni SQL o JDBC (`D4`).

---

## 7. Package `persistence.mapping`

Le tre classi di questo package hanno **un solo compito**: tradurre i dati tra gli oggetti immutabili di `api`/`core.dominio` e i record generati da jOOQ per le tabelle SQLite (conversioni di `UUID`, date `LocalDate`, orari `LocalTime`, timestamp `LocalDateTime`, enum `StatoAttivita` / `TipoModifica` e numeri di telefono).

Tutte le loro operazioni sono **statiche** (sottolineate in StarUML), sullo stesso modello già adottato nel core per `RegoleStatoAttivita` e `OrdinamentoAttivita`: in questo modo i repository non devono conservare campi inutili né creare oggetti senza stato.

| Classe | Operazioni statiche | Tipi collegati (`..>`) |
|---|---|---|
| **`ContattoMapper`** | `daRecord(record: ContattoRecord, telefoni: List<String>): Contatto`<br>`aRecord(contatto: Contatto): ContattoRecord`<br>`aRecordTelefoni(idContatto: UUID, telefoni: List<String>): List<TelefonoContattoRecord>` | `Contatto`, `PersistenzaException`, `ContattoRecord`, `TelefonoContattoRecord` |
| **`AttivitaMapper`** | `daRecord(record: AttivitaRecord): Attivita`<br>`aRecord(attivita: Attivita): AttivitaRecord` | `Attivita`, `StatoAttivita`, `AttivitaRecord` |
| **`VoceRegistroMapper`** | `daRecord(record: VoceRegistroRecord): VoceRegistro`<br>`aRecord(voce: VoceRegistro): VoceRegistroRecord` | `VoceRegistro`, `TipoModifica`, `VoceRegistroRecord` |

*(Nota sulla salvaguardia dei costruttori `D3`: se nel leggere dal database si trovassero per ipotesi dati corrotti che fanno fallire il costruttore di `Contatto` o `Attivita`, il mapper/repository segnala un errore tecnico di `PersistenzaException` anziché farlo sembrare un errore di digitazione dell'utente.)*

---

## 8. Package `persistence.repository`

Le tre classi implementano i contratti di `core.repository` tramite **composizione e delegazione** (nessuna superclasse comune `AbstractSQLiteRepository`). Tutte ricevono nel costruttore solo `-contesto: ContestoTransazione` e delegano la conversione dati al rispettivo `*Mapper`.

### `JooqContattoRepository` *(`..|> ContattoRepository`)*
- **Costruttore:** `JooqContattoRepository(contesto: ContestoTransazione)`.
- **Operazioni:**
  - `trova(id: UUID): Optional<Contatto>` — carica la riga di `CONTATTO` e i suoi numeri da `TELEFONO_CONTATTO`.
  - `tutti(): List<Contatto>` — legge tutti i contatti e tutti i numeri di telefono in **due query complessive** (raggruppati per `id_contatto`), evitando di eseguire una query separata per ogni contatto della rubrica.
  - `aggiungi(contatto: Contatto): void` — inserisce il contatto e gli eventuali numeri di telefono in modo atomico.
  - `aggiorna(contatto: Contatto): void` — aggiorna i dati anagrafici e sincronizza i numeri di telefono del contatto in modo atomico (anche se chiamato fuori da `GestoreTransazioni`).

### `JooqAttivitaRepository` *(`..|> AttivitaRepository`)*
- **Costruttore:** `JooqAttivitaRepository(contesto: ContestoTransazione)`.
- **Operazioni:**
  - `trova(id: UUID): Optional<Attivita>`
  - `perContatto(idContatto: UUID): List<Attivita>` — sfrutta l'indice su `id_contatto` per rispettare `NFR.3`/`Q5`. Non ordina i risultati: l'ordinamento `PER_STORICO` sta in `OrdinamentoAttivita` dentro `mplan-core`.
  - `perGiorno(giorno: LocalDate): List<Attivita>` — sfrutta l'indice su `data`; l'ordinamento `PER_AGENDA` resta nel core.
  - `aggiungi(attivita: Attivita): void`
  - `aggiorna(attivita: Attivita): void`
- **Nessuna eliminazione:** coerentemente con `AttivitaRepository` e `FUN.12.2`, non esiste alcun metodo per cancellare un'attività.

### `JooqRegistroRepository` *(`..|> RegistroRepository`)*
- **Costruttore:** `JooqRegistroRepository(contesto: ContestoTransazione)`.
- **Operazioni:**
  - `aggiungi(voce: VoceRegistro): void` — aggiunge una voce in coda alla tabella `VOCE_REGISTRO`.
  - `trovaPerAttivita(idAttivita: UUID): List<VoceRegistro>` — recupera tutte le voci dell'attività richiesta usando l'indice su `id_attivita` (ordinate cronologicamente).

---

## 9. Scelte di design, pattern e controllo antipattern

- **Principi di design (Cap. 12):**
  - *Astrazione e Information Hiding:* `mplan-app` vede solo `PersistenzaFactory`, e `mplan-core` vede solo le interfacce di `core.repository`; jOOQ, SQL e struttura delle tabelle restano confinati nel modulo.
  - *Singola Responsabilità (SRP) e Alta Coesione:* connessione/avvio schema (`PersistenzaFactory`, `InizializzatoreSchema`), coordinamento transazioni (`ContestoTransazione`, `GestoreTransazioniJooq`), intercettazione errori (`TraduttoreEccezioni`), conversione dati (`mapping`) e query (`repository`) sono separati.
  - *Dipendenze Acicliche e Stabilità di Martin:* nessun ciclo tra i package (`persistence` → `repository` → `infrastruttura`, `mapping`).
  - *Composizione invece di ereditarietà e Legge di Demetra:* nessuna classe base condivisa per i repository; ogni classe parla solo con i propri collaboratori diretti.
- **Pattern utilizzati (dalle slide del corso):**
  - **Adapter:** i tre `Jooq*Repository` e `GestoreTransazioniJooq` adattano le API di jOOQ ai contratti di `core.repository`.
  - **Factory + Façade:** `PersistenzaFactory` nasconde la configurazione interna di SQLite/jOOQ e costruisce i collaboratori già collegati.
  - **Delegation:** i repository delegano traduzione dati ai `*Mapper` e gestione del canale/transazione a `ContestoTransazione`.
  - **Interceptor / Observer (nativo jOOQ):** `TraduttoreEccezioni` ascolta gli errori SQL e li converte centralmente.
  - **Singleton non usato:** intenzionalmente evitato per permettere nei test JUnit l'avvio di più database indipendenti in memoria (`D8`).
- **Antipattern evitati (Cap. 12):** nessun *God Object / Blob* (evitato un unico `DatabaseManager` tuttofare), nessun *Poltergeist* (accorpata la configurazione SQLite in `PersistenzaFactory` e resi statici i convertitori), nessun *Golden Hammer* (niente interfacce inutili sui mapper) e nessun *Swiss Army Knife*.

---

## 10. Esempio completo: salvataggio atomico di un annullamento

Riprendendo l'esempio del §11 di `core-spiegazione.md` dal punto di vista della persistenza:

1. `AttivitaServiceImpl` chiama `transazioni.inTransazione(...)`.
2. `GestoreTransazioniJooq` apre una transazione jOOQ su SQLite e la registra in `ContestoTransazione`.
3. Il core chiama `attivita.aggiorna(annullata)`: `JooqAttivitaRepository` chiede a `ContestoTransazione` il canale attivo, converte l'oggetto con `AttivitaMapper.aRecord` ed esegue l'`UPDATE` su `ATTIVITA` dentro quella transazione.
4. Nella stessa transazione, `RegistroModifiche` chiama `registro.aggiungi(voce)`: `JooqRegistroRepository` ottiene lo stesso canale da `ContestoTransazione`, usa `VoceRegistroMapper.aRecord` e inserisce la riga in `VOCE_REGISTRO`.
5. **Se tutto riesce:** `GestoreTransazioniJooq` esegue il commit e pulisce `ContestoTransazione`. Entrambe le tabelle vengono salvate su disco insieme (`Q4`).
6. **Se SQLite fallisce** (ad esempio durante l'inserimento in `VOCE_REGISTRO`): `TraduttoreEccezioni` registra l'errore tecnico su `log4j2` e lancia `PersistenzaException`; `GestoreTransazioniJooq` annulla l'intera transazione (rollback anche dell'`UPDATE` su `ATTIVITA`), lasciando il database coerente. L'eccezione attraversa `mplan-core` senza che il core importi JDBC e viene gestita dalla GUI per avvisare l'utente (`D4`).

---

## 11. Cosa resta da fare

- Disegnare il diagramma `mplan-persistence` in `docs/uml/mplan.mdj` seguendo la struttura descritta sopra, trascinando in grigio le interfacce di `core.repository`, i tipi di `api` / `core.dominio` e i tipi esterni.
- Completare in `mplan-api` (diagramma `mplan-api`) i campi di `Contatto` / `DatiContatto` previsti da `DAT.1` (compresi i telefoni multipli `DAT.1.4` e l'eventuale `TipoContatto`) e aggiungere `PersistenzaException` (`D4`).
- Creare `code/mplan-persistence/src/main/resources/schema.sql` con le 4 tabelle e gli indici, e configurare `jooq-meta-extensions` nel `pom.xml` per generare il package jOOQ in fase di build Maven (`D6`).
