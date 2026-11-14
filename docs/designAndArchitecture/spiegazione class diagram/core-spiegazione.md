# Il modulo core di MPlan, spiegato semplice

Questo documento spiega il class diagram `mplan-core` del file StarUML (`docs/uml/mplan.mdj`): cosa c'è dentro il core, come si legge il diagramma, perché ogni classe è fatta così e come il core si collega agli altri moduli. Le scelte sono quelle concordate nella PR #58 e nella issue #54.

Le firme dei metodi di `mplan-api` sono ancora provvisorie: vanno completate nel class diagram di api.

## 1. Dove sta il core

Il programma è diviso in cinque moduli Maven. Questa è l'unica architettura del progetto (design, sezione 2, D1 e D2).

```
   mplan-ui-swing        schermate
         │ usa solo
         ▼
     mplan-api           contratto: interfacce dei servizi, dati, errori
         ▲
         │ implementa
     mplan-core          logica: servizi, regole, interfacce dei repository
         ▲
         │ implementa le interfacce dei repository
   mplan-persistence     database SQLite con jOOQ

   mplan-app             crea tutti gli oggetti e li collega
```

| Modulo | Cosa fa |
|---|---|
| `mplan-api` | Dice cosa si può chiedere al programma: `ContattoService`, `AttivitaService`, `AgendaService`, i dati (`Contatto`, `Attivita`, `DatiAttivita`, ...) e gli errori |
| `mplan-core` | Fa il lavoro: controlla i dati, applica le regole, decide cosa salvare |
| `mplan-persistence` | Salva e legge dal database, implementando le interfacce del package `repository` del core |
| `mplan-ui-swing` | Mostra le schermate e parla solo con le interfacce di api |
| `mplan-app` | Avvia il programma: crea database, repository e servizi, li collega e apre la Home |

Il core non conosce né Swing né il database. Questo permette di provarlo con JUnit usando repository finti in memoria (D8).

## 2. Nomi dei package

Tutto il codice sta sotto `it.mpelettrica.mplan`. In Java i package iniziano con il dominio internet al contrario di chi possiede il software, per non scontrarsi con le librerie di altri. Il dominio è `mpelettrica.it`, il sito del committente. Lo stesso nome è già usato come `groupId` nei `pom.xml` e nel package di `MPlanApplication`, ed è stato confermato nella PR #58.

Nel file StarUML questo si vede come una catena di package `it` > `mpelettrica` > `mplan`, con dentro `api` e `core`. Rebel genera le cartelle Java seguendo la stessa catena.

## 3. Tre package, non tre livelli

Dentro `mplan-core` le classi sono divise in tre package:

| Package | Contiene | Può usare |
|---|---|---|
| `core.servizi` | le classi che la GUI chiama attraverso api | `dominio`, `repository`, api |
| `core.dominio` | regole e controlli, senza database e senza GUI | solo api |
| `core.repository` | le interfacce di cui il core ha bisogno per salvare e leggere | api e i tipi del registro |

Nessuno usa `servizi`, quindi non ci sono giri di dipendenze.

All'inizio questi pacchetti erano chiamati "livello 1, 2 e 3" e avevano un diagramma ciascuno. Nella review della PR #58 Michele ha fatto notare che così sembravano una seconda architettura, mentre sono solo un modo di ordinare le classi dentro un modulo: il compilatore controlla i confini tra moduli, non quelli tra package dello stesso modulo. Per questo adesso:

- c'è un solo diagramma per modulo Maven (`mplan-core`, e poi `mplan-persistence`, ecc.);
- i tre package sono disegnati come blocchi dentro lo stesso diagramma;
- la descrizione dei package va nel design alla sezione 3 (Software Design), non tra le decisioni architetturali.

## 4. Come leggere il diagramma

Il diagramma è a fasce, dall'alto verso il basso:

```
api (grigio)       ContattoService  AttivitaService  AgendaService
servizi            ContattoServiceImpl  AttivitaServiceImpl  AgendaServiceImpl     Clock (java.time)
                   OsservatoreAttivita  RegistroModifiche
repository         ContattoRepository  GestoreTransazioni  AttivitaRepository  RegistroRepository
dominio            ValidatoreContatto  Validatore  ValidatoreAttivita  RegoleStatoAttivita
                   OrdinamentoAttivita  VoceRegistro  TipoModifica
api (grigio)       Contatto  DatiContatto  ErroreValidazione  DatiAttivita  StatoAttivita  Attivita
```

Gli elementi grigi non fanno parte del core: sono riferimenti ad altri moduli (api) o a Java (`Clock`). Mostrano solo il nome perché la loro descrizione completa sta nel diagramma del loro modulo.

Il package `api` è disegnato due volte. Sopra ci sono le interfacce dei servizi, ognuna sopra la classe che la implementa. Sotto ci sono i tipi usati da repository e dominio, ognuno sotto la classe che lo usa. In questo modo le frecce restano corte e non attraversano tutto il diagramma. UML permette di mostrare lo stesso package più volte nello stesso diagramma.

## 5. Le frecce

| Disegno | Nome | Significato | Esempio | In Java |
|---|---|---|---|---|
| tratteggiata con triangolo vuoto | realizzazione | implementa un'interfaccia | `AttivitaServiceImpl` verso `AttivitaService` | `implements` |
| tratteggiata con punta aperta | dipendenza | usa quel tipo in un metodo (parametro, risultato o variabile locale), ma non lo tiene come campo | `AttivitaRepository` verso `Attivita` | un `import` e l'uso dentro i metodi |
| continua con punta aperta da un lato e X dall'altro | associazione navigabile | ha un campo di quel tipo | `ContattoServiceImpl` verso `ContattoRepository` | `private final ContattoRepository contatti;` |
| continua con triangolo vuoto | generalizzazione | estende una classe | non c'è nel core | `extends` |

Come si legge un'associazione, per esempio `ContattoServiceImpl  X──── -contatti 1 ───>  ContattoRepository`:

- la punta della freccia vuol dire che `ContattoServiceImpl` può usare `ContattoRepository`;
- la X dal lato di `ContattoServiceImpl` vuol dire che il repository non sa chi lo usa;
- `-contatti` è il nome del campo (il meno vuol dire privato). Il nome del ruolo si scrive vicino alla classe che quel ruolo indica, cioè dal lato della punta, come nelle slide del corso (esempio `Student` e `Professor` con il ruolo `+lecturer`): il campo però appartiene alla classe dal lato della X;
- `1` dal lato del repository: il servizio ne ha esattamente uno;
- `*` dal lato del servizio: lo stesso repository può essere usato da più servizi;
- `osservatori *` dal lato di `OsservatoreAttivita`: il servizio ha una lista di osservatori.

Per questo molte classi non hanno attributi nel riquadro. Quando il tipo di un campo è una classe disegnata nel diagramma, il campo si mostra come associazione e non si ripete nel riquadro: è la forma che le slide indicano come preferibile. Rebel genera i campi proprio dalle associazioni. Le interfacce invece non possono avere campi, e i validatori e `RegoleStatoAttivita` non ne hanno bisogno.

## 6. Notazioni particolari

**Sottolineato.** Un attributo o un'operazione sottolineati sono statici (slide "Variabile di classe e operazione di classe"): appartengono alla classe e si usano senza creare un oggetto, per esempio `RegoleStatoAttivita.puoPassareA(da, a)`. Nel core sono statici solo `puoPassareA`, `modificabile`, `PER_STORICO` e `PER_AGENDA`, perché sono regole fisse senza stato.

**{readOnly}.** Il valore non si può cambiare. `PER_STORICO` e `PER_AGENDA` diventano costanti `public static final`.

**Il rettangolino tratteggiato con la T.** È il parametro di tipo di una classe generica, come in `List<T>`. `Validatore<T>` vuol dire "valida dati di un tipo che sceglie chi lo implementa":

```java
interface Validatore<T> {
    List<ErroreValidazione> valida(T dati);
}
class ValidatoreContatto implements Validatore<DatiContatto> { ... }   // T = DatiContatto
class ValidatoreAttivita implements Validatore<DatiAttivita> { ... }   // T = DatiAttivita
```

Lo stesso vale per `inTransazione(operazione: Supplier<T>): T`, dove `T` è il tipo del risultato. In StarUML `T` è dichiarato come parametro template dell'operazione, altrimenti Rebel genererebbe una classe `T.java` inesistente.

**Optional.** `trova(id)` restituisce `Optional<...>` perché l'id potrebbe non esistere: chi lo chiama deve gestire il caso "non trovato".

## 7. Package `repository`

Contiene solo interfacce. Dicono cosa serve al core per salvare e leggere, senza dire come. Le implementa `mplan-persistence` con SQLite e jOOQ (D6). Nel diagramma del core non si vedono le implementazioni perché stanno in un altro modulo.

**ContattoRepository**: `trova(id)`, `tutti()`, `aggiungi(contatto)`, `aggiorna(contatto)`. `aggiungi` serve per i contatti nuovi e `aggiorna` per quelli già salvati.

**AttivitaRepository**: `trova(id)`, `perContatto(idContatto)` per lo storico, `perGiorno(giorno)` per l'agenda, `aggiungi(attivita)`, `aggiorna(attivita)`. Non c'è un metodo per cancellare: è così che il progetto garantisce che le attività non si eliminano mai (FUN.12.2). `perContatto` riceve solo l'id del contatto, perché con la specifica 0.4.0 lo storico non ha più il filtro per periodo. Un overload `trova(idContatto)` accanto a `trova(id)` non è possibile: tutti e due riceverebbero un solo `UUID` e avrebbero la stessa firma.

**RegistroRepository**: `aggiungi(voce)` aggiunge una voce al registro delle modifiche (FUN.13), che si scrive solo in coda. `trovaPerAttivita(idAttivita)` rilegge le voci di un'attività: nessuna schermata la usa, ma serve ai test per controllare che il registro venga davvero scritto.

**GestoreTransazioni**: esegue più operazioni come un blocco unico. Se una fallisce, si annullano tutte (D5). La versione con `Runnable` serve quando non c'è niente da restituire, quella con `Supplier<T>` quando serve un risultato:

```java
Attivita a = transazioni.inTransazione(() -> {
    attivita.aggiorna(nuova);
    return nuova;
});
```

## 8. Package `dominio`

Qui stanno le regole che valgono sempre, senza database e senza GUI.

**Validatore<T>** è l'interfaccia comune dei due validatori. È stata aggiunta perché i due controlli hanno la stessa forma e perché il regolamento chiede di usare interfacce e generics (VIN.1).

**ValidatoreContatto** e **ValidatoreAttivita** controllano obbligatorietà, lunghezze e formati (DAT.1, DAT.2, DAT.5). Ricevono i dati scritti dall'utente e restituiscono la lista degli errori, uno per campo. Se la lista è vuota i dati vanno bene.

**RegoleStatoAttivita** contiene la macchina a stati delle attività (FUN.6.2, FUN.6.3, Figura 8.1 della specifica). `puoPassareA(COMPLETATA, ANNULLATA)` restituisce `false`, `modificabile(ANNULLATA)` restituisce `false`. Sta nel core e non in `StatoAttivita` di api: così api resta un contratto senza logica.

**OrdinamentoAttivita** contiene i due modi di ordinare le attività:

- `PER_STORICO`: dalla più vecchia alla più recente come in una chat, e nello stesso giorno prima quelle senza orario (FUN.9, FUN.9.2);
- `PER_AGENDA`: prima quelle senza orario, poi per orario (FUN.10.2, FUN.10.3).

Si usano così: `lista.sort(OrdinamentoAttivita.PER_AGENDA)`. Il repository restituisce i dati senza ordinarli, così la regola sta in un posto solo.

**VoceRegistro** è una riga del registro delle modifiche: quando, quale attività, che tipo di modifica, motivo. **TipoModifica** dice il tipo: `MODIFICA`, `CAMBIO_STATO`, `ANNULLAMENTO` (DAT.4). Stanno nel core perché la GUI non li usa.

## 9. Package `servizi`

Le tre classi `...ServiceImpl` implementano le interfacce di api. Non contengono regole: le chiedono a `dominio` e salvano tramite `repository`.

Hanno un costruttore esplicito che riceve tutti i pezzi di cui hanno bisogno, per esempio `AttivitaServiceImpl(attivita, transazioni, validatore, clock)`. Il servizio non crea da solo i suoi collaboratori: glieli passa `mplan-app`. Nei test invece si passano un repository finto e un `Clock` fermo su una data, così il risultato è sempre lo stesso (D8, D9). Il costruttore esplicito serve anche a Rebel per generare i campi `private final`.

**ContattoServiceImpl**: `elenca()`, `trova(id)`, `crea(dati)`, `modifica(id, dati)`, `valida(dati)`. Usa `ContattoRepository`, `GestoreTransazioni` e `ValidatoreContatto`.

**AttivitaServiceImpl**:

| Metodo | Parametri e scopo |
|---|---|
| `crea(idContatto, dati)` | `idContatto` è il contatto della scheda aperta, `dati` è ciò che l'utente ha scritto. Restituisce l'attività creata, con stato *da fare* |
| `modifica(id, dati)` | Aggiorna i testi, per esempio la descrizione scritta dopo il lavoro |
| `segnaParziale(id)`, `segnaCompletata(id)` | Basta l'id: lo stato di arrivo è già nel nome |
| `annulla(id, motivo)` | Il motivo è obbligatorio (DAT.3.1). L'attività resta nello storico |
| `storico(idContatto)` | Tutte le attività del contatto, già ordinate con `PER_STORICO` |
| `valida(dati)` | La GUI la chiama prima di salvare per mostrare gli errori campo per campo (D4) |
| `aggiungiOsservatore(osservatore)` | Registra chi deve essere avvisato dei cambiamenti (vedi il punto 10) |

Usa `AttivitaRepository`, `GestoreTransazioni`, `ValidatoreAttivita`, `Clock` (per la data proposta, che è oggi), e chiede a `RegoleStatoAttivita` e `OrdinamentoAttivita`.

**AgendaServiceImpl**: `attivitaDelGiorno(giorno)` restituisce le attività di tutti i contatti in quel giorno, ordinate con `PER_AGENDA`. La GUI la chiama con le frecce e con "Vai a...". `oggi()` dice qual è il giorno corrente, perché l'agenda si apre su oggi (FUN.10.4). Così la GUI non legge l'orologio del PC e tutto resta provabile con un `Clock` fermo.

## 10. Il pattern Observer

Il registro delle modifiche (FUN.13) è fatto con il pattern Observer:

| Ruolo | Classe |
|---|---|
| soggetto | `AttivitaServiceImpl`: tiene la lista `osservatori` e li avvisa a ogni modifica, cambio di stato o annullamento |
| osservatore (interfaccia) | `OsservatoreAttivita`: `attivitaModificata(attivita)`, `statoCambiato(attivita, precedente)`, `attivitaAnnullata(attivita, motivo)` |
| osservatore concreto | `RegistroModifiche`: crea la `VoceRegistro` con l'ora presa dal `Clock` e la salva con `RegistroRepository.aggiungi` |

`mplan-app` crea `RegistroModifiche` e lo registra con `aggiungiOsservatore`. La notifica avviene dentro la stessa transazione del salvataggio, quindi l'attività e la voce del registro si salvano insieme o non si salvano affatto.

Perché questa scelta: il servizio non deve sapere come si scrive il registro, deve solo avvisare. È anche la risposta alla domanda di Michele sulla singola responsabilità: `AttivitaServiceImpl` coordina i passi di un caso d'uso, le regole stanno in `dominio`, il salvataggio nei repository e il registro in `RegistroModifiche`. Se in futuro servisse un altro effetto, per esempio la notifica serale di FUN.14, basterebbe aggiungere un altro osservatore senza toccare il servizio.

Degli altri pattern visti a lezione:

- **Singleton**: non serve nel core, perché i servizi ricevono i collaboratori dal costruttore per poterli sostituire nei test. Ha senso invece per il `Navigatore` della GUI, che è uno solo; comparirà nel diagramma di `mplan-ui-swing`;
- **Abstraction-Occurrence**: servirebbe un'entità "lavoro" che raggruppa le attività di più giorni, ma la specifica sceglie apposta un'attività per giorno (FUN.7);
- **General Hierarchy**: nel progetto non c'è nessuna struttura ad albero;
- **Player-Role**: servirebbero ruoli separati per privato, azienda e cantiere, mentre la specifica vuole un solo contatto con un tipo facoltativo (FUN.1.3, DAT.1.7).

## 11. Esempio completo: l'utente annulla un'attività

1. La GUI chiama `attivitaService.annulla(id, "cliente assente")`. Vede solo l'interfaccia di api.
2. `AttivitaServiceImpl` carica l'attività con `attivita.trova(id)`.
3. Controlla `RegoleStatoAttivita.puoPassareA(statoAttuale, ANNULLATA)`. Se la risposta è no, si ferma con un errore.
4. Dentro `transazioni.inTransazione(...)` salva l'attività con `attivita.aggiorna(...)` e avvisa gli osservatori con `attivitaAnnullata(attivita, motivo)`.
5. `RegistroModifiche` crea una `VoceRegistro` di tipo `ANNULLAMENTO` e la salva con `registro.aggiungi(voce)`.
6. Il repository vero, in `mplan-persistence`, scrive nel database. Il core non sa come.

## 12. Cosa resta da fare

- `Contatto` e `DatiContatto` in api hanno solo `id` e `nome`: vanno aggiunti gli altri campi di DAT.1 nel diagramma di api.
- Il diagramma `mplan-persistence` va affiancato a `mplan-core`, con le classi nel package `it` > `mpelettrica` > `mplan` > `persistence`. Le interfacce del package `repository` vanno trascinate dal Model Explorer e non ricreate.
- Il modo in cui si generano gli id (nel diagramma sono `UUID`) non è ancora stato scelto.

---

## 13. Analisi critica e punti di miglioramento (review architetturale e di design)

Questa sezione raccoglie una verifica dei principi di design (van Vliet, Cap. 11–12) e dei pattern applicati sul diagramma attuale di `mplan-core` (`docs/uml/mplan.mdj`), insieme ai problemi emersi e alle possibili soluzioni.

### 13.1 Aspetti positivi già ben impostati

- **Dipendenze acicliche tra package e stabilità (Cap. 12):** i tre package di `mplan-core` formano un grafo rigorosamente aciclico (`servizi` → `repository`, `dominio`, `api`; `repository` → `dominio`, `api`; `dominio` → `api`). Nessun package inferiore dipende da `servizi` e non ci sono *Factory* interne ai sottopackage che invertano le dipendenze: l'assemblaggio delle istanze concrete resta correttamente fuori dal core, in `mplan-app`.
- **Alta coesione, basso accoppiamento e testabilità (Q1, D8, D9):** l'uso di costruttori espliciti con iniezione delle dipendenze (`Repository`, `GestoreTransazioni`, `Clock`) permette di provare il core con JUnit senza GUI e senza database su disco.
- **Composizione/Delegazione e Legge di Demetra:** nessuna gerarchia di ereditarietà forzata; i servizi coordinano i casi d'uso delegando regole di stato, ordinamenti e persistenza ai rispettivi collaboratori diretti.
- **Pattern Observer (`AttivitaServiceImpl` → `OsservatoreAttivita` ← `RegistroModifiche`):** scelta molto pulita per il principio di singola responsabilità (SRP). `AttivitaServiceImpl` non conosce `RegistroRepository` né `VoceRegistro`, e rimane aperto a future estensioni (es. notifica serale `FUN.14`) senza modificare il servizio.
- **Assenza di antipattern gravi:** *Blob / God Class* evitato (anche grazie alla separazione dei tre servizi e di `RegistroModifiche`), *Swiss Army Knife* evitato (interfacce mirate per ruolo), *Singleton* giustamente non usato nel core (avrebbe impedito l'isolamento nei test).

---

### 13.2 Problemi individuati e soluzioni proposte

#### 1. Servizi accoppiati alle classi concrete di validazione invece che a `Validatore<T>`
- **Cosa succede oggi (`mplan.mdj`):** in `core.dominio` è definita l'interfaccia generica `Validatore<T>`, ma in `core.servizi`:
  - `ContattoServiceImpl` dichiara l'associazione `-validatore` e il parametro del costruttore con tipo concreto `ValidatoreContatto`;
  - `AttivitaServiceImpl` dichiara l'associazione `-validatore` e il parametro del costruttore con tipo concreto `ValidatoreAttivita`.
- **Perché è un problema:** nessun chiamante usa l'interfaccia `Validatore<T>`. Dal punto di vista del design (Cap. 12 e slide sui pattern), un'interfaccia che non viene referenziata da nessun cliente perde la sua utilità polimorfica e rischia di apparire aggiunta solo per soddisfare formalmente il vincolo `VIN.1` (lieve rischio di *Golden Hammer* / astrazione superflua). Inoltre, impedisce di sostituire facilmente il validatore con uno stub nei test unitari dei servizi.
- **Soluzione proposta:**
  - Far puntare le associazioni `-validatore` e i parametri dei costruttori di `ContattoServiceImpl` e `AttivitaServiceImpl` alle interfacce **`Validatore<DatiContatto>`** e **`Validatore<DatiAttivita>`** invece che alle classi concrete.
  - In questo modo `mplan-app` continua a passare `new ValidatoreContatto()` e `new ValidatoreAttivita()`, ma i servizi dipendono solo dall'astrazione (*Dependency Inversion* e *Information Hiding* rispettati).

#### 2. Manca il controllo sull'esistenza del contatto in `AttivitaServiceImpl.crea` (`DAT.2.6`)
- **Cosa succede oggi (`mplan.mdj`):** `AttivitaServiceImpl` ha un'associazione verso `AttivitaRepository`, `GestoreTransazioni`, `ValidatoreAttivita`, `Clock` e `OsservatoreAttivita`, ma **non ha alcun riferimento a `ContattoRepository`**.
- **Perché è un problema:** il requisito **`DAT.2.6`** stabilisce che il contatto di un'attività deve essere un *"riferimento obbligatorio a un contatto esistente"*. Senza `ContattoRepository`:
  - nei test unitari con repository finti in memoria (`D8`), `crea(idContatto, dati)` accetterebbe anche un `UUID` inesistente senza accorgersene;
  - in produzione su SQLite, il controllo ricadrebbe sul vincolo di chiave esterna (`FOREIGN KEY`) del database, trasformando un errore di dominio/input in una `PersistenzaException` (errore tecnico di sistema invece di una violazione di regola applicativa).
- **Soluzione proposta:**
  - Aggiungere `ContattoRepository` tra i collaboratori (associazione navigabile + parametro nel costruttore) di `AttivitaServiceImpl`.
  - All'interno di `crea(idContatto, dati)` (e dentro la transazione), verificare prima `contatti.trova(idContatto)`: se il contatto non esiste, interrompere con un errore di dominio/validazione esplicito prima di chiamare `attivita.aggiungi(...)`.

#### 3. Nome del contatto nell'Agenda giornaliera (`GUI.7.1`)
- **Cosa succede oggi (`mplan.mdj`):** `AgendaServiceImpl` dipende solo da `AttivitaRepository` e `Clock`, e il metodo `attivitaDelGiorno(giorno: LocalDate)` restituisce `List<Attivita>`. L'entità `Attivita` contiene `idContatto: UUID`, non il nome del contatto.
- **Perché è un problema:** il requisito **`GUI.7.1`** (e la Figura 8.4 della specifica) richiede che ogni riga dell'agenda mostri **orario, contatto e titolo** (es. `08:30 Cancello - Rossi Mario`). Con la firma attuale, la GUI dovrebbe fare una chiamata separata a `ContattoService.trova(id)` per ciascuna attività del giorno (oppure caricare l'intera rubrica ogni volta), spostando logica di composizione nella presentazione e peggiorando le prestazioni. Il punto è già annotato come dubbio aperto anche in `design.md` (§3, riga 222).
- **Soluzione proposta:**
  - Introdurre in `mplan-api` un piccolo tipo di sola lettura per la vista agenda (es. `VoceAgenda` con `Attivita` + nome/denominazione del contatto, applicando il pattern *Read-Only Interface / Immutable* delle slide), oppure restituire direttamente un DTO dedicato da `AgendaService.attivitaDelGiorno`.
  - Aggiungere `ContattoRepository` ad `AgendaServiceImpl`, così è il core (in un'unica operazione lato servizio) a risolvere i nomi dei contatti associati alle attività del giorno senza gravare sulla GUI.

#### 4. Strategia di generazione degli `UUID` non ancora definita (testabilità `Q1`)
- **Cosa succede oggi (`mplan.mdj`):** `Contatto` e `Attivita` richiedono un `id: UUID` già alla creazione (D3: *"un'entità non esiste mai senza id"*), mentre `DatiContatto` e `DatiAttivita` non contengono l'id e i costruttori di `ContattoServiceImpl` e `AttivitaServiceImpl` non ricevono alcun generatore di id.
- **Perché è un problema:** se i servizi chiamassero direttamente `UUID.randomUUID()` al loro interno, gli identificativi generati non sarebbero deterministici durante i test di unità (`Q1`), rendendo più scomode le asserzioni sui risultati e sulle voci di registro salvate (problema analogo a quello risolto brillantemente con l'iniezione di `Clock` per le date in `D9`).
- **Soluzione proposta:**
  - Iniettare nel costruttore di `ContattoServiceImpl` e `AttivitaServiceImpl` un fornitore di identificativi (es. `Supplier<UUID>` o una piccola interfaccia funzionale `GeneratoreId`), esattamente come fatto con `Clock`.
  - In produzione (`mplan-app`) si passa `UUID::randomUUID`; nei test JUnit si passa una sequenza deterministica e controllabile.

#### 5. Completamento degli elementi pendenti in `mplan-api` collegati al core e alla persistenza
- **Cosa manca oggi in `mplan.mdj` (sezione `api`):**
  - `Contatto` e `DatiContatto` espongono solo `id` e `nome` (mancano `cognome`, `indirizzo`, lista di telefoni `DAT.1.4`, `codiceFiscale`, `partitaIva`, `tipo` privato/azienda di `DAT.1`).
  - Manca la classe `PersistenzaException` in `mplan-api` (prevista da `design.md` D2 e D4 come eccezione non controllata che attraversa il core per essere intercettata nella GUI).
  - Non sono ancora esplicitate le eccezioni di dominio/stato lanciate dai servizi quando un'operazione viola le guardie (es. `modifica` o `annulla` su attività già `ANNULLATA` secondo `FUN.6.2`/`FUN.6.3`, o contatto non trovato): conviene chiarire nel contratto di `mplan-api` quali eccezioni usa il core (`ValidazioneException`, `IllegalStateException` o un'eccezione dedicata di `api`) affinché la GUI possa gestirle in modo uniforme.
- **Nota minore su `AgendaServiceImpl`:** la classe è molto sottile (delega a `AttivitaRepository.perGiorno` + `OrdinamentoAttivita.PER_AGENDA` e `LocalDate.now(clock)`). Non è un *Poltergeist* perché risponde a una chiara separazione di responsabilità rispetto ad `AttivitaService` (`UC6` vs `UC7`–`UC10`); adottando la soluzione 3 (risoluzione dei nomi contatto per l'agenda), acquisisce inoltre un ruolo di coordinamento ancora più netto.
