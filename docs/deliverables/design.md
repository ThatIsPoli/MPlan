# MPlan – Design

**Documento 4 – Design** (Modelling, Software Architecture, Software Design)
Corso di Ingegneria del Software, A.A. 2026/27

| Versione | Data | Modifiche |
|---|---|---|
| 0.1.0 | 01/11/2026 | Prima bozza della sezione Software Architecture |

> **Stato:** bozza. Sono compilate solo le parti dell'architettura. Le parti **[DA COMPLETARE]** sono rimandate.

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

### 2.1 Scopo e requisiti che guidano l'architettura

Questa sezione descrive la struttura su larga scala di MPlan e le decisioni che l'hanno determinata, in due viste (moduli, componenti e connettori) più una di allocazione. I requisiti che pesano di più sulle scelte sono:

| Requisito | Effetto sull'architettura |
|---|---|
| NFR.5 | Le funzioni su contatti, attività e agenda devono poter girare da test automatici senza GUI: la logica non può dipendere dalla GUI |
| NFR.2, FUN.15 | Il salvataggio è persistente e atomico: un errore a metà non lascia dati parziali |
| NFR.3 | Apertura di una scheda contatto in meno di 2 secondi con 5000 attività |
| NFR.4, NFR.6, NFR.7, VIN.3 | Applicazione per Windows 11, offline, con database embedded e senza invio di dati in rete |
| FUN.6.2, FUN.6.3, FUN.12.2, DAT.5 | Le regole su stati, annullamento e validità dei dati devono valere in un solo punto |
| VIN.1, VIN.2, VIN.4, VIN.5 | Java 21, Maven, almeno una libreria esterna, test JUnit |

### 2.2 Qualità e scenari

Per le qualità si usa ISO/IEC 9126, come nel Project Plan (9.1). Ordine di importanza: **1. manutenibilità** (modificabilità e testabilità), **2. usabilità**, **3. efficienza**.

| ID | Qualità | Scenario | Risposta attesa |
|---|---|---|---|
| Q1 | Testabilità | Uno sviluppatore verifica UC2–UC10 | Si eseguono con test JUnit senza GUI né database, usando un repository in memoria (NFR.5) |
| Q2 | Modificabilità | Cambia un limite di validazione (ad esempio la lunghezza del titolo) | Si modifica un solo punto del core; la GUI non cambia |
| Q3 | Sostituibilità | Si sostituisce H2 con un altro archivio | Si scrive un nuovo modulo che implementa le interfacce dei repository; core e GUI non cambiano |
| Q4 | Affidabilità | Il salvataggio fallisce a metà | La transazione viene annullata, i dati restano com'erano, l'utente è avvisato e l'errore è nel log (NFR.2) |
| Q5 | Efficienza | Si apre una scheda contatto con 5000 attività | Risposta in meno di 2 secondi (NFR.3) |

### 2.3 Decisioni architetturali

Le decisioni principali sono nel formato della Tabella 11.1 del libro.

**D1 – Architettura a strati monolitica**

| Elemento | Descrizione |
|---|---|
| Problema | Strutturare il sistema in modo che sia modificabile e testabile senza GUI (Q1, Q2) |
| Decisione | Un unico programma eseguibile con tre strati: presentazione, logica applicativa (core), persistenza. Lo strato di presentazione dipende dal core e lo strato di persistenza implementa le interfacce del core |
| Stato | Approvato |
| Presupposti | Un solo PC, un solo utente alla volta, nessun requisito in tempo reale (A1) |
| Alternative | Microservizi; MVC come stile dell'intero sistema; monolite con accesso diretto ai dati dalla GUI |
| Motivazione | Il monolite è il più semplice da gestire in un progetto piccolo; la separazione in strati rende il core testabile da solo. I microservizi sono sproporzionati rispetto alle dimensioni e alle ore disponibili |
| Implicazioni | Ogni azione dell'utente attraversa tutti gli strati, con un piccolo costo prestazionale irrilevante per NFR.3 |
| Appunti | Il pattern interno allo strato di presentazione è rimandato al Software Design |

**D2 – Dipendenze tramite interfacce e moduli Maven separati**

| Elemento | Descrizione |
|---|---|
| Problema | Impedire che core, GUI e persistenza si accoppino (Q1, Q3) |
| Decisione | Il core definisce due famiglie di interfacce: i **servizi applicativi** (usati dalla GUI) e i **repository** (implementati dalla persistenza). Ogni strato è un modulo Maven; le dipendenze sono verificate dal compilatore |
| Stato | Approvato |
| Presupposti | Il team sa usare Maven con più moduli |
| Alternative | Modulo unico con package separati; solo interfacce per i repository |
| Motivazione | Con i moduli il core non può importare Swing né JDBC: la regola è garantita dalla compilazione e non solo dalla disciplina. Le interfacce dei servizi permettono di testare la GUI con servizi finti |
| Implicazioni | Un po' più di configurazione Maven; una classe di assemblaggio nel modulo `mplan-app` (D5) |
| Appunti | I nomi delle interfacce sono provvisori e sono definiti nel Software Design |

**D3 – Entità immutabili e dati di input separati**

| Elemento | Descrizione |
|---|---|
| Problema | Impedire che la GUI modifichi lo stato di contatti e attività aggirando le regole (FUN.6.2, FUN.6.3, FUN.12.2) |
| Decisione | Il core restituisce alla GUI entità di dominio **immutabili** (`Contatto`, `Attivita`). Ogni cambiamento passa da un metodo del servizio. La GUI invia ciò che l'utente ha scritto in oggetti di input separati (`DatiContatto`, `DatiAttivita`), che possono contenere valori non validi |
| Stato | Approvato |
| Presupposti | Gli oggetti sono piccoli e la loro copia non pesa su NFR.3 |
| Alternative | Entità modificabili con setter; DTO che duplicano le entità |
| Motivazione | Un'entità non può esistere in uno stato non valido, mentre un modulo compilato male sì: per questo l'input è separato. I DTO sarebbero classi doppie senza beneficio nello stesso processo. Le transizioni di stato sono nel dominio (`StatoAttivita`), quindi nessun altro punto può aggirarle |
| Implicazioni | Due o tre classi in più; gli identificativi sono UUID assegnati dal core, quindi un'entità non esiste mai senza id |
| Appunti | Da verificare se il generatore di codice di StarUML produce i `record`; altrimenti si usano classi con campi `final` |

**D4 – Validazione e gestione degli errori**

| Elemento | Descrizione |
|---|---|
| Problema | Applicare DAT.5 in un solo punto e gestire i guasti della persistenza senza coinvolgere il core in JDBC (Q2, Q4) |
| Decisione | La validazione vive solo nel core. Il servizio offre un metodo che valida senza salvare e restituisce gli errori **per campo** (campo e motivo); il salvataggio usa lo stesso validatore. I guasti della persistenza sono una eccezione non controllata (`PersistenzaException`) che incapsula ogni `SQLException` |
| Stato | Approvato |
| Presupposti | I moduli sono piccoli e la validazione è veloce |
| Alternative | Validazione anche nella GUI; eccezioni controllate; codici di errore |
| Motivazione | Un errore di validazione è un evento normale che la GUI sa mostrare, quindi è un risultato. Un guasto del database non è gestibile dal chiamante: serve solo annullare, avvisare e registrare, in un unico punto. Le eccezioni controllate farebbero conoscere JDBC al core |
| Implicazioni | Il compilatore non segnala gli errori non gestiti: si compensa con un solo gestore in cima, con i test dei guasti e con la transazione che esegue sempre il rollback. La GUI può mostrare gli errori durante la digitazione chiamando il metodo di validazione (priorità S) |
| Appunti | Il registro delle modifiche (FUN.13) è un dato di dominio, diverso dal log tecnico |

**Altre decisioni**

| ID | Decisione | Motivazione breve |
|---|---|---|
| D5 | Assemblaggio manuale: il `main` ha due righe (`new MPlanApplication().start()`) e `MPlanApplication`, nel modulo `mplan-app`, crea e collega i componenti | Un solo punto conosce tutti i moduli; nessun framework da imparare |
| D6 | Transazioni come astrazione nel core, con implementazione JDBC nella persistenza | Atomicità senza che il core conosca `Connection` (Q4) |
| D7 | Accesso ai dati con JDBC semplice dietro l'interfaccia repository | Codice trasparente e adatto alle ore disponibili; JPA richiederebbe studio e annotazioni |
| D8 | GUI con Swing | Scelta del team per semplicità e conoscenza |
| D9 | Test dei servizi con repository in memoria; pochi test del repository JDBC su H2 in memoria | Test veloci e coerenti con Q1 e Q3 |
| D10 | Orologio iniettabile per la data corrente: **rimandato (priorità C)**; i test usano la data attuale | Aspetto secondario |
| D11 | Pattern interno alla GUI (controller per schermata o altro): rimandato al Software Design | Non è una decisione architetturale |

### 2.4 Librerie esterne (VIN.4)

| Libreria | Uso |
|---|---|
| H2 | Database relazionale embedded (VIN.3) |
| log4j2 | Registrazione delle operazioni e degli errori nel file di log |
| JUnit 5 | Test di unità (VIN.5) |

Le versioni sono fissate nel `pom.xml`.

### 2.5 Vista a moduli

Punto di vista: **scomposizione e strati** (Tabelle 11.3). Interessa a sviluppatori e manutentori.

| Modulo Maven | Package radice | Contenuto |
|---|---|---|
| `mplan-core` | `mplan.core` | Dominio (`domain`), interfacce dei servizi e implementazioni (`service`), interfacce dei repository e delle transazioni (`repository`), validazione, `PersistenzaException` |
| `mplan-persistence` | `mplan.persistence` | Implementazione JDBC/H2 dei repository e delle transazioni, creazione dello schema |
| `mplan-ui-swing` | `mplan.ui` | Schermate Swing (GUI.1) e loro logica di presentazione |
| `mplan-app` | `mplan.app` | `MPlanApplication`, configurazione, avvio |

**Regole di dipendenza:** `mplan-app` → tutti; `mplan-ui-swing` → `mplan-core`; `mplan-persistence` → `mplan-core`; `mplan-core` → nessun modulo. La GUI usa solo le interfacce dei servizi e non vede mai i repository.

*[Spazio riservato: diagramma dei package, StarUML]*

### 2.6 Vista componenti e connettori: stile a strati

Punto di vista: **componenti e connettori** (Tabella 11.4), descritto con lo schema degli stili del capitolo 11.4. Interessa a sviluppatori e manutentori.

| Voce | Descrizione |
|---|---|
| **Stile** | A strati (*layered*), con visibilità limitata: ogni strato usa solo quello immediatamente sotto, tramite interfacce |
| **Problema** | Le funzioni si possono organizzare gerarchicamente in presentazione, logica e dati, con dipendenze in un solo verso |
| **Contesto** | Applicazione desktop in un solo processo, con un solo thread di controllo per le azioni dell'utente |
| **Modello di sistema** | Il sistema è una gerarchia di tre strati. La persistenza non è chiamata direttamente dalla GUI, e implementa interfacce definite nel core (inversione delle dipendenze) |
| **Componenti** | *Presentazione*: schermate Swing (Home, Form contatto, Scheda contatto, Form attività, Agenda giornaliera). *Logica*: servizi dei contatti e delle attività (tipo *manager*), dominio con le regole di stato, validatore (tipo *computazionale*). *Persistenza*: repository JDBC (tipo *manager*) e database H2 (tipo *memoria*) |
| **Connettori** | Chiamata di procedura tra presentazione e servizi, e tra servizi e repository, sempre tramite interfacce; chiamate SQL tramite JDBC tra repository e H2 |
| **Struttura di controllo** | Un solo thread di controllo, avviato dall'utente; le operazioni sono sincrone |
| **Varianti** | Visibilità limitata al solo strato adiacente. Qui la persistenza dipende dal core e non viceversa, a differenza dello strato classico |
| **Esempi** | Modello a sette strati ISO OSI (libro, 11.4) |

*[Spazio riservato: diagramma dei componenti, StarUML]*

### 2.7 Vista di allocazione: deployment

Punto di vista: **distribuzione** (Tabella 11.5), con le risorse hardware.

| Elemento | Descrizione |
|---|---|
| Nodo | Un PC con Windows 11 (A1, A2) |
| Processo | Una sola JVM (Java 21) che esegue `mplan-app`, i moduli core, GUI e persistenza |
| Dati | File del database H2 in una cartella dati dell'utente, senza percorsi assoluti nel codice (VIN.2) |
| Log | File di log in una cartella dell'applicazione, scritto da log4j2 |
| Comunicazioni | Nessuna connessione di rete (NFR.6, NFR.7) |

*[Spazio riservato: diagramma di deployment, StarUML]*

### 2.8 Tracciabilità requisiti → architettura

| Requisito | Elemento architetturale |
|---|---|
| NFR.5, VIN.5 | D1, D2, D9; Q1 |
| NFR.2, FUN.15 | D6, D4; Q4 |
| NFR.3 | D7; Q5 |
| NFR.4, NFR.6, NFR.7, VIN.3 | Vista di allocazione; H2 |
| DAT.5 | D4 |
| FUN.6.2, FUN.6.3, FUN.12.2 | D3 |
| VIN.2 | Vista a moduli |
| VIN.4 | Librerie esterne |

---

## 3. Software Design

**[DA COMPLETARE]**