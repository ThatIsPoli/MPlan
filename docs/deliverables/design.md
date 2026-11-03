# MPlan – Design

**Documento 4 – Design** (Modelling, Software Architecture, Software Design)
Corso di Ingegneria del Software, A.A. 2026/27

| Versione | Data | Modifiche |
|---|---|---|
| 0.1.0 | 01/11/2026 | Prima bozza della sezione Software Architecture |
| 0.2.0 | 02/11/2026 | Architettura a 5 moduli Maven con `mplan-api`; eccezioni e `ErroreValidazione` spostati in `mplan-api`; salvaguardia dei costruttori delle entità; corretta la regola su cosa blocca il compilatore; aggiunti appunti non definitivi per la stesura nelle sezioni 1 e 3 |
| 0.2.1 | 03/11/2026 | Riduzione dei contenuti |


> **Stato:** bozza. È compilata solo la sezione 2 (architettura). Le sezioni 1 e 3 sono **[DA COMPLETARE]** e contengono solo appunti non definitivi, emersi durante la stesura dell'architettura, come base di partenza.

---

## Indice

1. Modelling **[DA COMPLETARE]**
2. Software Architecture
3. Software Design **[DA COMPLETARE]**

---

## 1. Modelling

**[DA COMPLETARE]**

> **Appunti per la stesura (non definitivi).** Emersi durante la sezione 2; non sono decisioni approvate.

- **Diagramma delle classi (StarUML).** Entità, oggetti di input e interfacce dei servizi stanno in `mplan-api`; le interfacce dei repository in `mplan-core` (D2, D3).
- **Macchina a stati di `Attivita`.** Dall'intervista emergono *da fare*, *parziale*, *completata* e *annullata*. Le transizioni consentite sono da definire e da confrontare con FUN.6.2 e FUN.6.3. Un'attività annullata non si elimina e non si modifica (FUN.12.2).
- **Diagrammi già previsti come segnaposto nella sezione 2:** package (2.5), componenti (2.6) e deployment (2.7).
- **Da verificare:** se il generatore di codice di StarUML produce i `record` Java (appunto di D3); altrimenti si usano classi con campi `final`.

---

## 2. Software Architecture

Questa sezione descrive la struttura su larga scala di MPlan e le decisioni che l'hanno determinata, in tre viste: moduli, componenti e connettori, allocazione.

### 2.1 Qualità e scenari

Le qualità seguono ISO/IEC 9126 e sono elencate in ordine di priorità. Secondo il metodo ADD (van Vliet, 11.2), per ciascuna è definito uno scenario con il relativo criterio di accettazione.


| ID | Caratteristica (ISO 9126) | Requisiti | Scenario (Stimolo e Contesto) | Risposta attesa (Criterio di verifica) |
|---|---|---|---|---|
| **Q1** | **Manutenibilità** *(Testabilità)* | NFR.5, VIN.5 | Esecuzione automatica dei test sui casi d'uso UC2–UC10 durante la build Maven. | Le funzioni su contatti, attività e agenda si eseguono da test JUnit senza avviare la GUI né accedere al disco, simulando date arbitrarie con `Clock` (D10). |
| **Q2** | **Manutenibilità** *(Modificabilità)* | NFR.5, VIN.1 | Modifica di un vincolo di validazione (DAT.5), estensione dell'interfaccia utente. | Si modifica un solo punto del modulo `mplan-core`; `mplan-ui-swing` e `mplan-persistence`. |
| **Q3** | **Manutenibilità** *(Sostituibilità)* | VIN.3, VIN.4 | Sostituzione del motore di database embedded. | Si scrive un nuovo modulo Maven implementando le interfacce dei repository; `mplan-api`, `mplan-core` e `mplan-ui-swing` rimangono invariati. |
| **Q4** | **Affidabilità** | NFR.2, FUN.15, DAT.5 | Chiusura forzata. | In caso di errore durante l'operazione, la transazione esegue il rollback. |
| **Q5** | **Efficienza** | NFR.3 | Apertura di una scheda contatto con uno storico di almeno 5.000 attività. | Tempo di risposta inferiore a 2,0 secondi su macchina di riferimento Windows 11 con DB H2 locale. |
| **Q6** | **Portabilità** | NFR.4, NFR.6, VIN.3 | Installazione e primo avvio dell'applicazione su un PC Windows 11 con la sola JVM Java 21 installata. | L'applicazione si avvia, crea automaticamente lo schema H2 su file locale ed esegue i casi d'uso UC2–UC10. |
| **Q7** | **Funzionalità** | FUN.12.2, FUN.15.1, DAT.3.1 | Un utente richiede l'annullamento di un'attività già registrata. | L'attività non viene eliminata dal database; resta nello storico con stato `ANNULLATA`, motivo obbligatorio e tracciamento nel registro (FUN.13). |
| **Q8** | **Qualità in uso** | NFR.1, NFR.1.1, NFR.1.2, NFR.7 | L'utente crea una nuova attività (UC7) o gestisce gli errori di digitazione. | Il metodo `valida()` restituisce gli errori per singolo campo; nessun dato esce dalla macchina locale (zero socket di rete aperti). |

### 2.3 Decisioni architetturali

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
| Decisione | Il sistema è diviso in cinque moduli Maven: `mplan-api` (contratto pubblico: interfacce dei servizi, entità, oggetti di input, errori ed eccezioni), `mplan-core` (implementazione dei servizi e interfacce dei repository), `mplan-persistence`, `mplan-ui-swing` e `mplan-app`. `mplan-ui-swing` dipende **solo** da `mplan-api`. Le dipendenze sono verificate dal compilatore |
| Stato | Approvato |
| Presupposti | Il team sa usare Maven con più moduli |
| Alternative | Modulo unico con package separati; quattro moduli, con la GUI che dipende da `mplan-core` |
| Motivazione | Con `mplan-api` il compilatore impedisce alla GUI sia di accedere al database (JDBC, H2) sia di usare i repository: può usare solo i servizi. Le interfacce dei servizi permettono inoltre di testare la GUI con servizi finti |
| Implicazioni | Maggiore configurazione Maven. Tutto ciò che la GUI deve vedere o intercettare (entità, input, `ErroreValidazione`, `ValidazioneException`, `PersistenzaException`) deve stare in `mplan-api`. Una classe di assemblaggio nel modulo `mplan-app` (D5) |

**D3 – Entità immutabili e dati di input separati**

| Elemento | Descrizione |
|---|---|
| Problema | Impedire che la GUI modifichi lo stato di contatti e attività aggirando le regole (FUN.6.2, FUN.6.3, FUN.12.2) |
| Decisione | Il core restituisce alla GUI entità di dominio **immutabili** (`Contatto`, `Attivita`), definite in `mplan-api`. Ogni cambiamento passa da un metodo del servizio. La GUI invia ciò che l'utente ha scritto in oggetti di input separati (`DatiContatto`, `DatiAttivita`), che possono contenere valori non validi. Come salvaguardia estrema, il costruttore di un'entità lancia una `ValidazioneException` (non controllata) se i dati violano le regole |
| Stato | Approvato |
| Presupposti | Gli oggetti sono piccoli e la loro copia non pesa su NFR.3 |
| Alternative | Entità modificabili con setter; DTO che duplicano le entità |
| Motivazione | Un'entità non può esistere in uno stato non valido, mentre un modulo compilato male sì: per questo l'input è separato. Nel flusso normale la GUI chiama prima il metodo `valida()` del servizio (D4), quindi l'eccezione del costruttore non viene mai scatenata e protegge solo da errori di programmazione. Le transizioni di stato sono nel dominio (`StatoAttivita`), quindi nessun altro punto può aggirarle |
| Implicazioni | Due o tre classi in più; gli identificativi sono assegnati dal core, quindi un'entità non esiste mai senza id |

**D4 – Validazione e gestione degli errori**

| Elemento | Descrizione |
|---|---|
| Problema | Applicare DAT.5 in un solo punto e gestire i guasti della persistenza senza coinvolgere il core in JDBC (Q2, Q4) |
| Decisione | La validazione vive solo nel core. L'interfaccia del servizio (in `mplan-api`) offre un metodo `valida()` che valida senza salvare e restituisce gli errori **per campo** (`ErroreValidazione`: campo e motivo); il salvataggio usa lo stesso validatore. I guasti della persistenza sono una eccezione non controllata (`PersistenzaException`) che incapsula ogni `SQLException`. `ErroreValidazione`, `ValidazioneException` e `PersistenzaException` sono definiti in `mplan-api`, perché la GUI deve poterli leggere e intercettare |
| Stato | Approvato |
| Presupposti | I moduli sono piccoli e la validazione è veloce |
| Alternative | Validazione anche nella GUI; eccezioni controllate; codici di errore |
| Motivazione | Un errore di validazione è un evento normale che la GUI sa mostrare, quindi è un risultato. Un guasto del database non è gestibile dal chiamante: serve solo annullare, avvisare e registrare, in un unico punto. Le eccezioni controllate legherebbero il JDBC al core |
| Implicazioni | Il compilatore non segnala gli errori non gestiti: si compensa con un solo gestore in cima nella GUI, con i test dei guasti e con la transazione che esegue sempre il rollback. La GUI può mostrare gli errori durante la digitazione chiamando `valida()` |

**Altre decisioni**

| ID | Decisione | Motivazione breve |
|---|---|---|
| D5 | Transazioni come astrazione nel core, con implementazione JDBC nella persistenza | Atomicità senza che il core conosca `Connection` (Q4) |
| D6 | Accesso ai dati con JDBC semplice dietro l'interfaccia repository | Codice trasparente e adatto alle ore disponibili; JPA richiederebbe studio e annotazioni |
| D7 | GUI con Swing | Scelta del team per semplicità e conoscenza |
| D8 | Test di unità sul core (repository in memoria) e test di integrazione su persistence (H2 in memoria); collaudo manuale per la GUI | Verifica di logica e query SQL (Q1, Q3) senza la fragilità dei test automatici su Swing |
| D9 | Orologio iniettabile (`Clock`) nei costruttori dei servizi. | Rende i test su date e agenda deterministici e indipendenti dall'ora del PC (Q1) |
| D10 | Pattern interno alla GUI (controller per schermata o altro): rimandato al Software Design | Non è una decisione architetturale |

### 2.4 Librerie esterne (VIN.4)

| Libreria | Uso |
|---|---|
| H2 | Database relazionale embedded (VIN.3) |
| log4j2 | Registrazione delle operazioni e degli errori nel file di log |
| JUnit 5 | Test di unità (VIN.5) |

Le versioni sono fissate nel `pom.xml`.

### 2.5 Vista a moduli

Punto di vista: **scomposizione e strati** (Tabella 11.3). Interessa a sviluppatori e manutentori.

| Modulo Maven | Package radice | Contenuto | Dipende da |
|---|---|---|---|
| `mplan-api` | `mplan.api` | Contratto pubblico: interfacce dei servizi, entità di dominio immutabili, oggetti di input, `ErroreValidazione`, `ValidazioneException`, `PersistenzaException` | nessun modulo |
| `mplan-core` | `mplan.core` | Implementazione dei servizi, regole di dominio e validatore, interfacce dei repository e delle transazioni | `mplan-api` |
| `mplan-persistence` | `mplan.persistence` | Implementazione JDBC/H2 dei repository e delle transazioni, creazione dello schema | `mplan-core`, `mplan-api` |
| `mplan-ui-swing` | `mplan.ui` | Schermate Swing (GUI.1) e loro logica di presentazione | `mplan-api` |
| `mplan-app` | `mplan.app` | `MPlanApplication`, configurazione, avvio | tutti i moduli |

**Regole di dipendenza:** `mplan-ui-swing` → `mplan-api` ← `mplan-core` ← `mplan-persistence`; `mplan-app` → tutti.

**Cosa blocca il compilatore:** poiché `mplan-ui-swing` dipende solo da `mplan-api`, la GUI non può compilare riferimenti né al database (JDBC, H2, `mplan-persistence`) né ai repository e alle altre classi di `mplan-core`: può usare solo le interfacce dei servizi e i tipi del contratto. Allo stesso modo `mplan-core` non può importare Swing né JDBC.

*[Spazio riservato: diagramma dei package, StarUML]*

### 2.6 Vista componenti e connettori: stile a strati

Punto di vista: **componenti e connettori** (Tabella 11.4), descritto con lo schema degli stili della sezione 11.4. Interessa a sviluppatori e manutentori.

| Voce | Descrizione |
|---|---|
| **Stile** | A strati (*layered*), con visibilità limitata e inversione delle dipendenze: a runtime ogni strato chiama quello immediatamente inferiore tramite interfacce; in compilazione le dipendenze puntano verso `mplan-api` e `mplan-core` |
| **Problema** | Le funzioni si possono organizzare gerarchicamente in presentazione, logica e dati, con un solo verso di chiamata |
| **Contesto** | Applicazione desktop in un solo processo, con un solo thread di controllo per le azioni dell'utente |
| **Modello di sistema** | Il sistema è una gerarchia di tre strati. La GUI vede solo il contratto pubblico (`mplan-api`), implementato dal core; la persistenza non è chiamata dalla GUI e implementa interfacce definite nel core |
| **Componenti** | *Presentazione*: schermate Swing (Home, Form contatto, Scheda contatto, Form attività, Agenda giornaliera). *Logica*: servizi dei contatti e delle attività (tipo *manager*), dominio con le regole di stato, validatore (tipo *computazionale*). *Persistenza*: repository JDBC (tipo *manager*) e database H2 (tipo *memoria*) |
| **Connettori** | Chiamata di procedura tra presentazione e servizi (interfacce di `mplan-api`) e tra servizi e repository (interfacce di `mplan-core`); chiamate SQL tramite JDBC tra repository e H2 |
| **Struttura di controllo** | Un solo thread di controllo, avviato dall'utente; le operazioni sono sincrone |
| **Varianti** | Visibilità limitata al solo strato adiacente. Qui la persistenza dipende dal core e non viceversa, a differenza dello strato classico |
| **Esempi** | Modello a sette strati ISO OSI (libro, 11.4) |

*[Spazio riservato: diagramma dei componenti, StarUML]*

### 2.7 Vista di allocazione: deployment

Punto di vista: **distribuzione** (Tabella 11.5), con le risorse hardware.

| Elemento | Descrizione |
|---|---|
| Nodo | Un PC con Windows 11 (A1, A2) |
| Processo | Una sola JVM (Java 21) che esegue `mplan-app` e i moduli `mplan-api`, `mplan-core`, `mplan-persistence` e `mplan-ui-swing` |
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
| VIN.1, VIN.2 | D2; vista a moduli |
| VIN.4 | Librerie esterne |

---

## 3. Software Design

**[DA COMPLETARE]**

> **Appunti per la stesura (non definitivi).** Emersi durante la sezione 2.

**Decisioni già prese**
- Il metodo che esegue un'operazione in transazione si chiama `inTransazione` (D6).
- Il costruttore di un'entità lancia `ValidazioneException` (non controllata) come salvaguardia; nel flusso normale la GUI chiama prima `valida()` (D3, D4).
- `valida()` sta nell'interfaccia del servizio (`mplan-api`) e restituisce gli errori per campo come `ErroreValidazione` (D4).
- I repository delle attività non hanno un metodo di eliminazione fisica (FUN.12.2).
- Il test dei servizi usa repository in memoria (D9).
- I servizi ricevono un'istanza di java.time.Clock tramite costruttore per test deterministici (D10).


**Da decidere**
- Nomi definitivi delle interfacce dei servizi e dei repository (D2).
- Pattern interno della GUI (D11).
- Tipi usati o implicati dalle firme dei servizi ma non ancora descritti: `DatiAttivita`, `Periodo`, `VoceRegistro`, `TipoModifica`. Va deciso quali stanno in `mplan-api` (se compaiono nelle firme dei servizi) e quali restano in `mplan-core`.
- Idea non decisa: un record di sola lettura per l'agenda, con attività e nome del contatto. Va deciso se serve o se basta il caricamento separato.

**Requisiti dell'intervista non ancora coperti dal design**
- Anagrafica del `Contatto`: flag privato/azienda (non fondamentale), più telefoni, codice fiscale e/o partita IVA.
- Calendario con viste giornaliera e mensile (nella scheda cliente e come calendario generale); l'agenda prevista finora copre solo il giorno.
- Trattamento dei lavori "Generale" e dei cantieri come contatti nella stessa lista (D2.4–D2.5 dell'intervista, ancora PARZIALE/PROPOSTA).

**Ordine di lavoro suggerito:** macchina a stati di `Attivita` → diagramma delle classi → diagrammi di sequenza dei casi d'uso principali (ad esempio annullamento e agenda).