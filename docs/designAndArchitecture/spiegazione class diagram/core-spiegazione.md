# Il modulo core di MPlan, spiegato semplice

Questo documento spiega cosa c'è dentro `mplan-core`: quali classi e interfacce ci sono, chi implementa cosa e quali pezzi arrivano da altri moduli. Corrisponde ai tre class diagram della sezione `Mplan-class-diagram` del file StarUML.

Le firme dei metodi di `mplan-api` sono provvisorie: vanno completate nel class diagram di api.

## A cosa serve il core

Il core è la "testa" dell'applicazione. Non disegna schermate e non sa nulla di SQL o di SQLite. Decide solo cosa succede quando l'utente fa qualcosa, per esempio "crea un'attività per questo contatto".

Il core è diviso in tre livelli. Ogni livello usa quello sotto, mai il contrario:

- livello 3, `servizi`: i servizi che la GUI chiama
- livello 2, `dominio`: i controlli sui dati inseriti
- livello 1, `repository`: le richieste alla persistenza

Un po' di terminologia: una **interfaccia** è un contratto, cioè un elenco di operazioni senza codice. Una **classe** è il codice vero. Quando una classe "implementa" un'interfaccia, promette di avere tutte le operazioni dell'elenco e le scrive per davvero.

## Livello 1: repository (`mplan.core.repository`)

Qui ci sono solo tre interfacce. Dicono cosa serve al core dalla persistenza, ma non come lo si fa.

**ContattoRepository** serve a leggere e scrivere i contatti. Sa trovare un contatto per id, elencarli tutti, aggiungerne uno nuovo e aggiornarne uno esistente.

**AttivitaRepository** serve a leggere e scrivere le attività. Sa trovare un'attività per id, trovare quelle di un contatto in un certo periodo, trovare quelle di un giorno, aggiungerne una e aggiornarne una. Non ha nessuna operazione per cancellare, perché le attività non si cancellano mai (requisito FUN.12.2): si possono solo annullare.

**GestoreTransazioni** serve a eseguire più operazioni come un blocco unico. Se una va male, vengono annullate tutte e il database resta com'era. Il core lo usa senza sapere come funziona una transazione SQL.

Queste tre interfacce **non sono implementate nel core**. Le implementa il modulo `mplan-persistence`, usando jOOQ e SQLite. È la parte del tuo collega.

## Livello 2: dominio (`mplan.core.dominio`)

Qui ci sono due classi vere, non interfacce.

**ValidatoreContatto** controlla i dati inseriti per un contatto. Riceve un `DatiContatto` e restituisce la lista degli errori trovati, uno per campo sbagliato. Se la lista è vuota i dati vanno bene.

**ValidatoreAttivita** fa lo stesso per le attività: riceve un `DatiAttivita` e restituisce la lista degli errori.

I due validatori non salvano niente e non hanno campi propri. Sono solo un insieme di controlli. Per le regole si guardano DAT.5 e FUN.4.2 della specifica.

## Livello 3: servizi (`mplan.core.servizi`)

Qui ci sono tre classi, e ognuna **implementa un'interfaccia che sta in `mplan-api`**. Sono le classi che la GUI usa, anche se la GUI le vede solo attraverso l'interfaccia.

**ContattoServiceImpl** implementa `ContattoService`. Si occupa della rubrica: elenca i contatti, ne trova uno, ne crea uno nuovo, lo modifica, e controlla i dati prima di salvare. Per farlo usa `ContattoRepository` per leggere e scrivere, `GestoreTransazioni` per salvare in modo sicuro e `ValidatoreContatto` per i controlli.

**AttivitaServiceImpl** implementa `AttivitaService`. Si occupa delle attività di un contatto: le crea, le modifica, le segna come parziali o completate, le annulla indicando il motivo, e mostra lo storico (anche in un certo periodo). Usa `AttivitaRepository`, `GestoreTransazioni`, `ValidatoreAttivita` e anche `Clock`.

**AgendaServiceImpl** implementa `AgendaService`. Dà le attività di un certo giorno. Usa `AttivitaRepository` e `Clock`.

Queste tre classi non creano da sole i loro collaboratori. Li ricevono già pronti quando vengono costruite, e a costruirle è il modulo `mplan-app`. Questo permette di sostituire i pezzi, per esempio con dei finti nei test.

## Chi implementa cosa, in breve

Le interfacce di api sono implementate dal core: `ContattoService` da `ContattoServiceImpl`, `AttivitaService` da `AttivitaServiceImpl`, `AgendaService` da `AgendaServiceImpl`.

Le interfacce del core (`ContattoRepository`, `AttivitaRepository`, `GestoreTransazioni`) sono implementate da `mplan-persistence`.

I validatori non implementano niente: sono classi normali.

## Cosa il core prende da fuori

Dal modulo `mplan-api` il core usa:

- le interfacce dei servizi: `ContattoService`, `AttivitaService`, `AgendaService`
- le entità: `Contatto`, `Attivita`, `StatoAttivita` (le quattro condizioni: da fare, parziale, completata, annullata)
- i dati in ingresso: `DatiContatto`, `DatiAttivita`
- `Periodo`, un intervallo di date
- `ErroreValidazione`, che dice quale campo è sbagliato e perché
- `PersistenzaException`, l'errore che i repository lanciano se la persistenza si guasta

Da Java il core usa `Clock` (pacchetto `java.time`), per sapere che giorno e che ora sono. Lo si riceve dall'esterno e non lo si chiede direttamente al sistema, così nei test si può "fermare l'orologio".

Il core **non usa mai** `mplan-persistence`, `mplan-ui-swing` o `mplan-app`. Non sa nemmeno che esistono.

## Chi usa il core

`mplan-persistence` usa le tre interfacce del livello 1 per implementarle, e le entità di api per restituire i dati. È un caso particolare: la persistenza dipende sia dal core sia da api.

`mplan-app` costruisce tutto: crea i repository di persistence, li passa ai servizi del core e poi passa i servizi alla GUI.

`mplan-ui-swing` non usa il core direttamente. Parla solo con le interfacce di api.

## Cosa significano le frecce nei diagrammi

Una **freccia tratteggiata con triangolo vuoto** vuol dire "implementa". Si trova nel livello 3, per esempio da `ContattoServiceImpl` a `ContattoService`.

Una **freccia tratteggiata con punta aperta** si chiama dipendenza e vuol dire "usa questo tipo nei suoi metodi, come parametro o come risultato". Non è un campo della classe. Si trova nei livelli 1 e 2, per esempio da `ContattoRepository` a `Contatto`.

Una **freccia continua con una croce all'inizio e la punta aperta alla fine** si chiama associazione unidirezionale e vuol dire "ha questo oggetto come campo e lo può usare". La croce dice che l'altro oggetto non conosce chi lo usa. Si trova nel livello 3, per esempio da `AttivitaServiceImpl` a `AttivitaRepository`.

I numeri sulle frecce continue sono le molteplicità. Il `1` dal lato del componente usato vuol dire che ogni servizio ne ha uno solo. L'asterisco dal lato del servizio vuol dire che lo stesso repository, la stessa `GestoreTransazioni` o lo stesso `Clock` possono essere condivisi da più servizi.

Gli elementi disegnati in grigio nei diagrammi non appartengono al core: sono riferimenti ad altri moduli (api e `java.time`).

## Esempio: l'utente crea un'attività

1. La GUI chiama `AttivitaService.crea(idContatto, dati)`. Non sa che dietro c'è `AttivitaServiceImpl`.
2. `AttivitaServiceImpl` chiede a `ValidatoreAttivita` di controllare i dati. Se ci sono errori, li restituisce alla GUI e si ferma.
3. Se i dati vanno bene, apre una transazione con `GestoreTransazioni`.
4. Dentro la transazione chiama `AttivitaRepository.aggiungi(...)`.
5. Il repository vero, in `mplan-persistence`, scrive nel database SQLite. Il core non sa come.
6. L'attività creata torna indietro fino alla GUI.

## Cose ancora da decidere

- Le firme di `mplan-api` sono provvisorie e vanno completate nel suo class diagram.
- I campi anagrafici di `Contatto` e `DatiContatto` sono da completare (DAT.1).
- Come si generano gli id (nel diagramma sono `UUID`) non è ancora stato scelto.
