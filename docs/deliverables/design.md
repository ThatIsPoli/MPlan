# MPlan – Design

**Documento 4 – Design** (Modelling, Software Architecture, Software Design)
Corso di Ingegneria del Software, A.A. 2026/27

| Versione | Data | Modifiche |
|---|---|---|
| 0.1.0 | 01/11/2026 | Prima bozza della sezione Software Architecture |
| 0.2.0 | 02/11/2026 | Architettura a 5 moduli Maven con `mplan-api`; eccezioni e `ErroreValidazione` spostati in `mplan-api`; salvaguardia dei costruttori delle entità; corretta la regola su cosa blocca il compilatore; aggiunti appunti non definitivi per la stesura nelle sezioni 1 e 3 |
| 0.2.1 | 03/11/2026 | Riduzione dei contenuti |
| 0.2.2 | 04/11/2026 | Sezione 2: numerazione dei paragrafi e dei riferimenti alle decisioni, scenari Q2, Q4 e Q7 allineati alla specifica dei requisiti 0.3.3, struttura dei progetti Eclipse e Maven |
| 0.3.0 | 05/11/2026 | Rivisitazione dell'organizzazione del modulo Maven |
| 0.3.1 | 05/11/2026 | Un solo modulo Maven con cinque package: aggiornati D2, vista dei package e verifica delle dipendenze; corretta la risposta di Q4 |
| 0.3.2 | 06/11/2026 | Sezione 2: ritorno a cinque moduli Maven; database SQLite con accesso ai dati tramite jOOQ al posto di H2 con JDBC |
| 0.3.3 | 08/11/2026 | Sezione 2: schema SQL unico per generazione jOOQ e creazione al primo avvio; tipo dei componenti per modulo; architettura interna degli strati (2.5.1); corretto refuso in 2.3 |
| 0.3.4 | 12/11/2026 | Allineamento alla specifica dei requisiti 0.4.0 (storico senza filtro per periodo, agenda con scelta della data); Q5, Q6 e 2.6 corretti da H2 a SQLite; aggiornati gli appunti delle sezioni 1 e 3 |
| 0.3.5 | 14/11/2026 | Sezione 2: generazione degli identificativi e record `VoceAgenda` per l'agenda (D3); validatore come interfaccia, controllo del contatto di un'attività ed eccezioni per le operazioni non ammesse (D4); aggiornate la vista a moduli e la tracciabilità |
| 0.3.6 | 16/11/2026 | Sezione 2 allineata ai diagrammi di core e persistence: scenari Q2 e Q3, sostituibilità e installabilità sotto la portabilità come nella specifica (Q3, Q6), `PersistenzaFactory` (D2, 2.4), regole di stato (D3), regole di validazione solo nel core e costruttori delle entità limitati ai campi obbligatori, motivo dell'annullamento (D3, D4), `Clock` (D9), package radice e verifica delle dipendenze (2.4); tolti gli appunti delle sezioni 1 e 3 |
| 0.3.7 | 19/11/2026 | GUI web con Vaadin al posto di Swing: scheda D7, scenari Q2, Q3, Q6 e Q8, D2, D4 e D8; librerie (2.3), vista a moduli con `mplan-ui-vaadin` (2.4), componenti e connettori (2.5), deployment (2.6) e tracciabilità (2.7) aggiornati |

> **Stato:** bozza. È compilata solo la sezione 2 (architettura). Le sezioni 1 e 3 sono **[DA COMPLETARE]**.

---

## Indice

1. Modelling **[DA COMPLETARE]**
2. Software Architecture
3. Software Design **[DA COMPLETARE]**

---

## 1. Modelling

**[DA COMPLETARE]**

---

## 2. Software Architecture

Questa sezione descrive la struttura su larga scala di MPlan e le decisioni che l'hanno determinata, in tre viste: moduli, componenti e connettori, allocazione.

### 2.1 Qualità e scenari

Le qualità seguono ISO/IEC 9126 e sono elencate in ordine di priorità. Secondo il metodo ADD (van Vliet, 11.2), per ciascuna è definito uno scenario con il relativo criterio di accettazione.


| ID | Caratteristica (ISO 9126) | Requisiti | Scenario (Stimolo e Contesto) | Risposta attesa (Criterio di verifica) |
|---|---|---|---|---|
| **Q1** | **Manutenibilità** *(Testabilità)* | NFR.5, VIN.5 | Esecuzione automatica dei test sui casi d'uso UC2–UC10 durante la build Maven. | Le funzioni su contatti, attività e agenda si eseguono da test JUnit senza avviare la GUI né accedere al disco, simulando date arbitrarie con `Clock` (D9). |
| **Q2** | **Manutenibilità** *(Modificabilità)* | NFR.5, VIN.1 | Modifica di un vincolo di validazione (DAT.5), estensione dell'interfaccia utente. | Si modifica un solo modulo: `mplan-core` per il vincolo di validazione, `mplan-ui-vaadin` per l'interfaccia; gli altri moduli rimangono invariati. |
| **Q3** | **Portabilità** *(Sostituibilità)* | NFR.5, VIN.3 | Sostituzione del motore di database embedded. | Si scrive un nuovo modulo Maven implementando le interfacce dei repository e delle transazioni; `mplan-api`, `mplan-core` e `mplan-ui-vaadin` rimangono invariati. |
| **Q4** | **Affidabilità** | NFR.2, FUN.15, DAT.5 | Chiusura forzata del programma subito dopo un salvataggio, oppure errore durante un'operazione di scrittura. | Alla riapertura tutte le attività salvate sono presenti (0 attività perse); un'operazione interrotta da un errore non lascia dati parziali (rollback della transazione). |
| **Q5** | **Efficienza** | NFR.3 | Apertura di una scheda contatto con uno storico di almeno 5.000 attività. | Tempo di risposta inferiore a 2,0 secondi su macchina di riferimento Windows 11 con database SQLite locale. |
| **Q6** | **Portabilità** *(Installabilità)* | NFR.4, NFR.6, VIN.3 | Installazione e primo avvio dell'applicazione su un PC Windows 11 con la sola JVM Java 27 installata e il browser già incluso nel sistema. | L'applicazione si avvia, crea automaticamente lo schema SQLite su file locale, apre l'interfaccia nel browser ed esegue i casi d'uso UC2–UC10, anche senza connessione a Internet. |
| **Q7** | **Funzionalità** | FUN.12.2, FUN.15.1, DAT.3.1 | Un utente richiede l'annullamento di un'attività già registrata. | L'attività non viene eliminata dal database; resta nello storico con stato `ANNULLATA`, motivo obbligatorio. |
| **Q8** | **Qualità in uso** | NFR.1, NFR.1.1, NFR.1.2, NFR.7 | L'utente crea una nuova attività (UC7) o gestisce gli errori di digitazione. | Il metodo `valida()` restituisce gli errori per singolo campo; nessun dato esce dalla macchina locale: il server interno accetta connessioni solo dall'indirizzo locale `127.0.0.1` (D7). |

### 2.2 Decisioni architetturali

**D1 – Architettura a strati con inversione delle dipendenze**

| Elemento | Descrizione |
|---|---|
| Problema | Strutturare il sistema in modo che sia modificabile e testabile senza GUI (Q1, Q2) |
| Decisione | Un unico programma eseguibile con tre strati: presentazione, logica applicativa (core), persistenza. La presentazione dipende solo dal contratto pubblico (`mplan-api`), che il core implementa; la persistenza implementa le interfacce dei repository definite nel core |
| Stato | Approvato |
| Presupposti | Un solo PC, un solo utente alla volta, nessun requisito in tempo reale (A1) |
| Alternative | Microservizi; monolite con accesso diretto ai dati dalla GUI |
| Motivazione | La separazione in strati rende il core testabile da solo e aumenta la manutenibilità |
| Implicazioni | Ogni azione dell'utente attraversa tutti gli strati, con un piccolo costo prestazionale irrilevante per NFR.3 |

**D2 – Contratto pubblico in `mplan-api` e cinque moduli Maven**

| Elemento | Descrizione |
|---|---|
| Problema | Impedire che core, GUI e persistenza si accoppino e che la GUI raggiunga i dati senza passare dai servizi (Q1, Q2, Q3) |
| Decisione | Il sistema è diviso in cinque moduli Maven: `mplan-api` (contratto pubblico: interfacce dei servizi, entità, oggetti di input, errori ed eccezioni), `mplan-core` (implementazione dei servizi e interfacce dei repository), `mplan-persistence`, `mplan-ui-vaadin` e `mplan-app`. `mplan-ui-vaadin` dipende **solo** da `mplan-api` tra i moduli del progetto. Le dipendenze sono verificate dal compilatore |
| Stato | Approvato |
| Presupposti | Il team sa usare Maven con più moduli |
| Alternative | Un solo modulo Maven con cinque package, con le dipendenze verificate da un controllo automatico; quattro moduli, con la GUI che dipende da `mplan-core` |
| Motivazione | Con `mplan-api` il compilatore impedisce alla GUI sia di accedere al database (jOOQ, driver SQLite) sia di usare i repository: può usare solo i servizi. Le interfacce dei servizi permettono inoltre di testare la GUI con servizi finti |
| Implicazioni | Maggiore configurazione Maven. Tutto ciò che la GUI deve vedere o intercettare (entità, input, `ErroreValidazione`, `ValidazioneException`, `PersistenzaException`) deve stare in `mplan-api`. La creazione e il collegamento degli oggetti (servizi, repository, transazioni) sono affidati a una classe di configurazione di Spring Boot nel modulo `mplan-app` (D7), che ottiene repository e transazioni da `PersistenzaFactory` di `mplan-persistence` senza usare jOOQ; `mplan-api`, `mplan-core` e `mplan-persistence` non usano Spring |

**D3 – Entità immutabili e dati di input separati**

| Elemento | Descrizione |
|---|---|
| Problema | Impedire che la GUI modifichi lo stato di contatti e attività aggirando le regole (FUN.6.2, FUN.6.3, FUN.12.2) |
| Decisione | Il core restituisce alla GUI entità di dominio **immutabili** (`Contatto`, `Attivita`), definite in `mplan-api`. Ogni cambiamento passa da un metodo del servizio. La GUI invia ciò che l'utente ha scritto in oggetti di input separati (`DatiContatto`, `DatiAttivita`), che possono contenere valori non validi. Le entità non contengono regole di validazione: il core le crea solo dopo aver validato i dati (D4) e il loro costruttore controlla soltanto che i campi obbligatori non siano `null` (`Objects.requireNonNull`), lanciando `NullPointerException` come per ogni errore di programmazione. Per l'agenda il core restituisce un record immutabile `VoceAgenda`, con l'attività e il nome del contatto (GUI.7.1): la GUI non deve cercare il contatto di ogni attività |
| Stato | Approvato |
| Presupposti | Gli oggetti sono piccoli e la loro copia non pesa su NFR.3 |
| Alternative | Entità modificabili con setter; DTO che duplicano le entità |
| Motivazione | Un modulo compilato male può contenere valori non validi, un'entità creata dal core no: per questo l'input è separato. La GUI non può salvare un'entità costruita da sé, perché i servizi ricevono solo gli oggetti di input. Il controllo dei `null` nel costruttore impedisce che un errore nel core o nella persistenza crei un'entità incompleta e non blocca mai la lettura dei dati salvati, perché riguarda gli stessi campi che nello schema sono `NOT NULL`. Le regole di DAT.5 stanno solo nel validatore del core (D4), quindi i dati già salvati si rileggono anche se una regola cambia (Q2). Le transizioni di stato sono controllate in un solo punto del dominio (`RegoleStatoAttivita`, nel core), quindi nessun altro punto può aggirarle |
| Implicazioni | Due o tre classi in più; gli identificativi sono assegnati dal core, quindi un'entità non esiste mai senza id. Il servizio li genera con `UUID.randomUUID()` nel metodo `crea`: non serve un generatore iniettabile come il `Clock` (D9), perché nessuna regola dipende dal valore dell'id e nei test l'id si legge dall'entità restituita. L'unicità è garantita dalla chiave primaria della tabella, senza un controllo dei doppioni nel codice |

**D4 – Validazione e gestione degli errori**

| Elemento | Descrizione |
|---|---|
| Problema | Applicare DAT.5 in un solo punto e gestire i guasti della persistenza senza coinvolgere il core in JDBC (Q2, Q4) |
| Decisione | La validazione vive solo nel core. L'interfaccia del servizio (in `mplan-api`) offre un metodo `valida()` che valida senza salvare e restituisce gli errori **per campo** (`ErroreValidazione`: campo e motivo); il salvataggio usa lo stesso validatore, che i servizi ricevono come interfaccia (`Validatore<T>`) e non come classe concreta. Se `crea` o `modifica` ricevono comunque dati non validi, o se `annulla` riceve un motivo vuoto o troppo lungo (DAT.3.1), il servizio lancia `ValidazioneException`, che la GUI mostra all'utente. Il core controlla anche che il contatto di una nuova attività esista (DAT.2.6): la regola si verifica con i test di unità sui repository in memoria (D8), quindi non può affidarsi alla chiave esterna del database. Le operazioni che la GUI non offre (attività su un contatto inesistente, modifica o transizione non ammessa di un'attività, FUN.6.2, FUN.6.3) sono errori di programmazione: il servizio lancia `IllegalArgumentException` o `IllegalStateException`, eccezioni standard di Java. I guasti della persistenza sono un'eccezione non controllata (`PersistenzaException`) che incapsula ogni errore di accesso ai dati (ad esempio `SQLException`). `ErroreValidazione`, `ValidazioneException` e `PersistenzaException` sono definiti in `mplan-api`, perché la GUI deve poterli leggere e intercettare |
| Stato | Approvato |
| Presupposti | I dati sono piccoli e la validazione è veloce |
| Alternative | Validazione anche nella GUI; eccezioni controllate; codici di errore |
| Motivazione | Un errore di validazione è un evento normale che la GUI sa mostrare, quindi è un risultato. Un guasto del database non è gestibile dal chiamante: serve solo annullare, avvisare e registrare, in un unico punto. Le eccezioni controllate legherebbero il JDBC al core |
| Implicazioni | Il compilatore non segnala gli errori non gestiti: si compensa con un solo gestore degli errori nella GUI (il gestore degli errori di Vaadin, D7), con i test dei guasti e con la transazione che in caso di errore esegue sempre il rollback. La GUI può mostrare gli errori durante la digitazione chiamando `valida()` |

**Altre decisioni**

| ID | Decisione | Motivazione breve |
|---|---|---|
| D5 | Transazioni come astrazione nel core, con implementazione nella persistenza (jOOQ e JDBC) | Atomicità senza che il core conosca `Connection` (Q4) |
| D6 | Accesso ai dati con jOOQ sopra il driver JDBC di SQLite, dietro l'interfaccia repository. Lo schema è descritto in un unico file `schema.sql` in `mplan-persistence`: Maven lo usa per generare le classi jOOQ (senza database acceso) e l'applicazione lo esegue al primo avvio per creare le tabelle | La logica non scrive SQL: le query sono codice Java con controllo dei tipi e le classi generate restano solo in `mplan-persistence`; una sola descrizione dello schema evita disallineamenti (Q6); JPA richiederebbe più studio e annotazioni |
| D7 | GUI web con Vaadin, usata dal browser dello stesso PC (scheda sotto la tabella) | Tutta la GUI in Java, nessun servizio da installare e la stessa interfaccia per cellulare e più dispositivi (FUT.1, FUT.2) |
| D8 | Test di unità sul core (repository in memoria) e test di integrazione su persistence (SQLite in memoria); collaudo manuale per la GUI | Verifica di logica e query SQL (Q1, Q3). La GUI contiene solo presentazione e si collauda con le prove d'uso della specifica (9.4); se resta tempo si aggiungono test JUnit delle schermate con i test di Vaadin senza browser |
| D9 | Orologio iniettabile (`Clock`) nei costruttori delle classi del core che usano la data (servizi delle attività e dell'agenda, registro delle modifiche) | Rende i test su date e agenda deterministici e indipendenti dall'ora del PC (Q1) |
| D10 | Pattern interno alla GUI (controller per schermata o altro): rimandato al Software Design | Non è una decisione architetturale |

**D7 – GUI web con Vaadin**

| Elemento | Descrizione |
|---|---|
| Problema | Scegliere la tecnologia della GUI rispettando il regolamento del corso (VIN.1, VIN.3, NFR.6) senza chiudere la strada all'uso da cellulare e da più dispositivi (FUT.1, FUT.2) |
| Decisione | La GUI è un'applicazione web scritta in Java con Vaadin (Flow), nel modulo `mplan-ui-vaadin`. Il programma avvia con Spring Boot un server web interno, in ascolto solo sull'indirizzo locale `127.0.0.1`, e l'utente usa le schermate dal browser dello stesso PC. Ogni schermata è una classe Java con una propria rotta (`@Route`) e riceve nel costruttore i servizi di `mplan-api` |
| Stato | Approvato; sostituisce la scelta iniziale di Swing |
| Presupposti | Sul PC c'è un browser aggiornato (A2); un solo utente alla volta (A1) |
| Alternative | Swing (scelta iniziale: GUI desktop senza server, da rifare per FUT.1 e FUT.2); JSP o servlet di Jakarta EE (pagine in HTML, con JavaScript per le parti dinamiche come gli errori mostrati durante la digitazione) |
| Motivazione | Schermate e logica di presentazione si scrivono solo in Java (VIN.1) e Vaadin è tra le tecnologie web indicate dal corso. Un'interfaccia web si usa anche da cellulare e da altri dispositivi: per FUT.1 e FUT.2 basterà aprire il server alla rete e aggiungere l'accesso (FUT.3), senza riscrivere la GUI. Il server è dentro il programma, quindi non c'è alcun servizio da installare (NFR.6, VIN.3). La GUI continua a dipendere solo da `mplan-api` (D2, NFR.5) |
| Implicazioni | Il server accetta connessioni solo dallo stesso PC (NFR.7, Q8). `mplan-app` diventa un'applicazione Spring Boot: `MPlanApplication` avvia il server e una classe di configurazione crea e collega gli oggetti (D2). Vaadin è una dipendenza di `mplan-ui-vaadin` e Spring Boot di `mplan-app`; Spring Boot registra i log con Logback, quindi si usa `spring-boot-starter-log4j2` per tenere log4j2. La build usa Node.js, che il plugin Maven di Vaadin scarica da solo: le schermate sono preparate durante la build e sul PC del committente basta Java (Q6). All'avvio il browser si apre sull'indirizzo locale dell'applicazione; il comando `chiudi` della Home (Tabella 8.2 della specifica) arresta il server e quindi l'applicazione. La navigazione tra le schermate (GUI.1.1) usa le rotte di Vaadin; il pattern interno alla GUI resta rimandato (D10) |

### 2.3 Librerie esterne (VIN.4)

| Libreria | Uso |
|---|---|
| SQLite (driver JDBC) | Database relazionale embedded, in un file locale (VIN.3) |
| jOOQ | Accesso ai dati da codice Java, con generazione delle classi dallo schema (D6) |
| jooq-meta-extensions | Solo in fase di build: permette a jOOQ di leggere `schema.sql` per generare le classi (D6) |
| log4j2 | Registrazione delle operazioni e degli errori nel file di log; con Spring Boot tramite `spring-boot-starter-log4j2` (D7) |
| Vaadin (Flow) | GUI web scritta in Java (D7) |
| Spring Boot | Avvio del programma con il server web interno e creazione e collegamento degli oggetti (D2, D7) |
| JUnit 5 | Test di unità (VIN.5) |

Le versioni sono fissate nei `pom.xml`. Vaadin e Spring Boot si aggiungono ai `pom.xml` con il codice della GUI, nelle versioni compatibili con Java 27.

### 2.4 Vista a moduli

Punto di vista: **scomposizione e strati** (Tabella 11.3). Interessa a sviluppatori e manutentori.

| Modulo Maven | Package radice | Contenuto | Tipo | Dipende da |
|---|---|---|---|---|
| `mplan-api` | `it.mpelettrica.mplan.api` | Contratto pubblico: interfacce dei servizi, entità di dominio immutabili, `VoceAgenda`, oggetti di input, `ErroreValidazione`, `ValidazioneException`, `PersistenzaException` | gestore | nessun modulo |
| `mplan-core` | `it.mpelettrica.mplan.core` | Implementazione dei servizi, regole di dominio e validatore, interfacce dei repository e delle transazioni | computazionale | `mplan-api` |
| `mplan-persistence` | `it.mpelettrica.mplan.persistence` | Implementazione dei repository e delle transazioni con jOOQ e SQLite, `schema.sql`, creazione dello schema e `PersistenzaFactory` | memoria | `mplan-core`, `mplan-api` |
| `mplan-ui-vaadin` | `it.mpelettrica.mplan.ui` | Schermate Vaadin con le loro rotte (GUI.1) e loro logica di presentazione | controllore | `mplan-api` |
| `mplan-app` | `it.mpelettrica.mplan.app` | `MPlanApplication` (avvio con Spring Boot e server web interno), configurazione, creazione e collegamento degli oggetti | controllore | tutti i moduli |

**Progetti Eclipse e struttura del repository:** ogni modulo è un progetto Maven e un progetto Eclipse (con il proprio `.project`), sotto la cartella `code/`; un POM padre li aggrega. I test di ogni modulo stanno in una cartella separata dai sorgenti (`src/test/java`), nella radice del repository non c'è alcun `.project` e i documenti stanno in `docs/` (VIN.2, VIN.8).

**Regole di dipendenza:** `mplan-ui-vaadin` → `mplan-api` ← `mplan-core` ← `mplan-persistence`; `mplan-app` → tutti.

**Cosa blocca il compilatore:** poiché `mplan-ui-vaadin` dipende solo da `mplan-api`, la GUI non può compilare riferimenti né al database (jOOQ, driver SQLite, `mplan-persistence`) né ai repository e alle altre classi di `mplan-core`: può usare solo le interfacce dei servizi e i tipi del contratto. Allo stesso modo `mplan-core` non può importare jOOQ né il driver SQLite, che sono dipendenze del solo `mplan-persistence`, né Vaadin o Spring Boot. JDBC (`java.sql`) fa parte del JDK e il compilatore non lo blocca: che resti in `mplan-persistence` si verifica con la revisione del codice.

*[Spazio riservato: diagramma dei package, StarUML]*

### 2.5 Vista componenti e connettori: stile a strati

Punto di vista: **componenti e connettori** (Tabella 11.4), descritto con lo schema degli stili della sezione 11.4. Interessa a sviluppatori e manutentori.

| Voce | Descrizione |
|---|---|
| **Stile** | A strati (*layered*), con visibilità limitata e inversione delle dipendenze: a runtime ogni strato chiama quello immediatamente inferiore tramite interfacce; in compilazione le dipendenze puntano verso `mplan-api` e `mplan-core` |
| **Problema** | Le funzioni si possono organizzare gerarchicamente in presentazione, logica e dati, con un solo verso di chiamata |
| **Contesto** | Applicazione in un solo processo sul PC, con un server web interno usato dal browser dello stesso PC; un solo utente alla volta (A1) |
| **Modello di sistema** | Il sistema è una gerarchia di tre strati. La GUI vede solo il contratto pubblico (`mplan-api`), implementato dal core; la persistenza non è chiamata dalla GUI e implementa interfacce definite nel core |
| **Componenti** | *Presentazione*: schermate Vaadin (Home, Form contatto, Scheda contatto, Form attività, Agenda giornaliera), eseguite nel server e mostrate dal browser, di tipo *controllore*. *Logica*: servizi dei contatti, delle attività e dell'agenda, dominio con le regole di stato e validatore, di tipo *computazionale*. *Persistenza*: repository con jOOQ e database SQLite, di tipo *memoria* |
| **Connettori** | Richieste HTTP all'indirizzo locale tra il browser e le schermate, gestite da Vaadin; chiamata di procedura tra presentazione e servizi (interfacce di `mplan-api`) e tra servizi e repository (interfacce di `mplan-core`); accesso ai dati con jOOQ e JDBC tra repository e SQLite |
| **Struttura di controllo** | Ogni azione dell'utente nel browser diventa una richiesta al server, che la esegue in modo sincrono attraverso schermate, servizi e repository; Vaadin esegue le richieste di una sessione una alla volta |
| **Varianti** | Visibilità limitata al solo strato adiacente. Qui la persistenza dipende dal core e non viceversa, a differenza dello strato classico |
| **Esempi** | Modello a sette strati ISO OSI (libro, 11.4) |


*[Spazio riservato: diagramma dei componenti, StarUML]*

### 2.6 Vista di allocazione: deployment

Punto di vista: **distribuzione** (Tabella 11.5), con le risorse hardware.

| Elemento | Descrizione |
|---|---|
| Nodo | Un PC con Windows 11 e un browser (A1, A2) |
| Processo | Una sola JVM (Java 27) che esegue il programma, formato dai package `app`, `api`, `core`, `persistence` e `ui`, con il server web interno di Spring Boot (D7) |
| Browser | Il browser del PC mostra le schermate e comunica con la JVM all'indirizzo locale |
| Dati | File del database SQLite in una cartella dati dell'utente, senza percorsi assoluti nel codice (VIN.2) |
| Log | File di log in una cartella dell'applicazione, scritto da log4j2 |
| Comunicazioni | Solo tra il browser e la JVM sullo stesso PC, all'indirizzo `127.0.0.1`; nessuna connessione verso altri computer o verso Internet (NFR.6, NFR.7) |

*[Spazio riservato: diagramma di deployment, StarUML]*

### 2.7 Tracciabilità requisiti → architettura

| Requisito | Elemento architetturale |
|---|---|
| NFR.5, VIN.5 | D1, D2, D9; Q1 |
| NFR.2, FUN.15 | D4, D5, D6; Q4 |
| NFR.3 | D6; Q5 |
| NFR.4, NFR.6, NFR.7, VIN.3 | D7; vista di allocazione; SQLite |
| DAT.5, DAT.2.6 | D4 |
| FUN.6.2, FUN.6.3, FUN.12.2 | D3 |
| GUI.7.1 | D3 |
| VIN.1, VIN.2 | D2, D7; vista a moduli |
| FUT.1, FUT.2 | D7 |
| VIN.4 | Librerie esterne |

---

## 3. Software Design

**[DA COMPLETARE]**
