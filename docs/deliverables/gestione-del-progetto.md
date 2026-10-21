# MPlan – Gestione del progetto

**Documento 2 – Gestione del progetto** (Software Life Cycle, Configuration Management, Persone e organizzazione del team)
Corso di Ingegneria del Software, A.A. 2026/27

| Versione | Data | Modifiche |
|---|---|---|
| 0.1.0 | 15/10/2026 | Versione iniziale |
| 0.1.1 | 17/10/2026 | Aggiornamento rispetto al Project Plan, alla specifica dei requisiti e al modo di lavorare sulle issue |
| 0.1.2 | 18/10/2026 | Milestone dettagliate, sprint backlog in tabella, procedura di lavoro sulle issue |
| 0.1.3 | 18/10/2026 | Piccole correzioni e specificazione di MDA |

> **Stato:** bozza. I punti segnati **[DA COMPLETARE]** richiedono dati che il team deve ancora inserire.

---

## Indice

1. Introduzione
2. Ciclo di vita del software
3. Configuration management
4. Persone e organizzazione del team

---

## 1. Introduzione

Questo documento descrive come il team gestisce lo sviluppo di **MPlan**: il processo seguito, gli strumenti con cui si tiene sotto controllo il codice e i documenti, e l'organizzazione del lavoro tra le persone.

---

## 2. Ciclo di vita del software

### 2.1 Processo seguito

Il team segue un approccio agile ispirato a **Scrum**, adattato a un gruppo di due persone, con iterazioni settimanali e rilascio incrementale (Project Plan, sezione 2.1). Il lavoro è diviso in **sprint** di una settimana, da un giovedì al giovedì successivo. Il giovedì il team si incontra di persona per pianificare lo sprint, fare il punto della situazione e iniziare il lavoro. All'inizio di ogni sprint ogni membro prende uno o più compiti come responsabile, e al termine di ogni compito il lavoro è rivisto dall'altro membro.

### 2.2 Ruoli ed eventi di Scrum

| Ruolo | Chi | Compito |
|---|---|---|
| Product Owner / Customer Representative | Matteo Poli | Rappresenta le esigenze del committente (MP elettrica s.a.s.), decide le priorità dei compiti e dei requisiti |
| Scrum Master / Agile Coordinator | Michele Bisignano | Cura il rispetto del processo, gestisce la board Kanban e rimuove gli ostacoli |
| Sviluppatori | Entrambi i membri | Svolgono i compiti dello sprint |


| Evento o artefatto di Scrum | Come lo svolgiamo |
|---|---|
| Sprint planning | Ogni giovedì, nella sessione congiunta di tre ore (Project Plan, 2.2), si scelgono i compiti dello sprint e si assegnano le issue ai membri |
| Daily scrum | Allineamento quotidiano di 15 minuti, dalle 10:15 alle 10:30, compatibilmente con gli impegni accademici (Project Plan, 2.2)  |
| Sprint review | Ogni giovedì si fa il punto sul lavoro svolto; inoltre ogni compito è rivisto dall'altro membro con la pull request prima del merge (3.2) |
| Sprint retrospective | Svolta nella sessione del giovedì (Project Plan, 2.2) |
| Backlog | Come backlog si usa la specifica dei requisiti, ordinata con le priorità MoSCoW (sezione 6). Lo sprint backlog di ogni sprint è la tabella di 2.3, compilata man mano con i compiti scelti dalla specifica |

### 2.3 Milestone

Le milestone sono su GitHub con le loro scadenze (Project Plan, 2.3):

- **M0 – Setup del repository** (01/10/2026 – 08/10/2026): creazione e configurazione del repository GitHub, con la struttura delle cartelle `code/` e `docs/`, il `.gitignore`, la licenza MIT e il `README`.
- **M1 – Ingegneria dei requisiti** (08/10/2026 – 22/10/2026): intervista al committente e documenti che la raccolgono; specifica dei requisiti (Documento 3), con casi d'uso e diagramma UML in StarUML, pubblicata nel repository. Nello stesso periodo si produce anche il Project Plan.
- **M2 – Design** (22/10/2026 – 26/11/2026): tutti i diagrammi UML richiesti (casi d'uso, classi, stati, sequenza, comunicazione o timing, attività, componenti, package) e il Documento di Design, con architettura (almeno due viste) e design pattern, nel repository e approvati dall'altro membro.
- **M3 – Sviluppo del codice** (19/11/2026 – 09/12/2026): i casi d'uso UC2–UC10 funzionano dall'interfaccia grafica; il codice, con una prima versione generata dal diagramma delle classi con StarUML e completata a mano, è unito in `main` e compila con Maven.
- **M4 – Sviluppo dei test** (19/11/2026 – 16/12/2026): test JUnit di unità superati e Documento di Testing con la descrizione dei test, la misura di copertura (se generata) e la tracciabilità requisito → test (specifica, 9.4).
- **M5 – Manutenzione** (16/12/2026 – 23/12/2026): una modifica applicata al software con la procedura di 3.2, test ancora superati e Documento di Maintenance, con le attività di refactoring svolte.
- **M6 – Revisione dei documenti e consegna** (23/12/2026 – 01/01/2027): revisione incrociata di tutti i documenti, tracciabilità controllata, release finale con tag `v1.0.0` e consegna. La release deve essere pronta almeno cinque giorni prima dell'appello (Project Plan, 13.1).

Lo sprint backlog di ogni sprint è una tabella, compilata all'inizio dello sprint scegliendo i compiti dalla specifica e aggiornata man mano. Dagli sprint di sviluppo ogni compito cita gli ID dei requisiti che realizza.

## 2.4 Sprint Backlog

| Sprint | Periodo | Sprint backlog | Responsabile | Stato |
|---|---|---|---|---|
| Sprint 1 | 08/10/2026 – 15/10/2026 | Diagramma UML dei casi d'uso | Scrum Master | Completato |
| | | Specifica dei requisiti (Documento 3) | Product Owner | Completato |
| Sprint 2 | 15/10/2026 – 22/10/2026 | Project Plan (Documento 1) | Scrum Master | Completato |
| | | Gestione del progetto (Documento 2, questo documento) | Product Owner | Completato |
| Sprint 3 e successivi | Dal 22/10/2026, ogni giovedì | Da compilare all'inizio di ogni sprint | Assegnato a inizio sprint | – |

### 2.5 Approccio basato su modelli (MDA)

Per questo progetto utilizziamo il modello MDA basato sullo sviulppo della documentazione in maniera circolare.

Il design parte dai diagrammi UML realizzati con StarUML v5.1.0 con estensione Rebel per la conversione in codice Java . Dal diagramma delle classi si genera una prima versione del codice Java, poi completata a mano. Le classi generate e quelle modificate saranno indicate nella relazione.

Se il codice generato presenta problemi strutturali, di progettazione o di requisiti, il team deve aggiornare la varia documentazione al fine di raggiungere gli obbiettivi prestabiliti ed infine ricreare il codice.


---

## 3. Configuration management

### 3.1 Strumenti

| Strumento | Uso |
|---|---|
| GitHub | Versionamento del codice e dei documenti, issue, branch, pull request, revisioni e milestone |
| GitHub Projects | Kanban board per i compiti dello sprint (Project Plan, 2.1) |
| StarUML | Modelli UML; il file di progetto è nel repository |
| Eclipse e Maven | Sviluppo e gestione delle dipendenze |
| GitHub Desktop | Fetch, Pull, Push della repository |

Il repository è condiviso con il docente e con l'esercitatrice.

### 3.2 Flusso di lavoro

1. **Issue.** Per ogni compito, o richiesta di modifica, si crea una issue con un'etichetta di tipo (`documento`, `codice` o `test`), collegata a una milestone. Una delle due persone ne è la responsabile; l'altra può lavorarci e dare consigli.
2. **Branch.** Si lavora sul branch della issue, il cui nome inizia con il numero della issue seguito da un nome breve (ad esempio `12-nome-del-branch`). Una persona può avere più issue, quindi più branch attivi contemporaneamente.
3. **Commit.** Il lavoro è salvato con commit sul branch. Anche l'altro membro può aggiungerne, per piccole correzioni.
4. **Pull request.** Quando il lavoro è finito si apre una pull request verso il ramo principale e si indica l'altro membro come reviewer.
5. **Revisione.** Il reviewer scrive gli eventuali commenti nella pull request; se non servono commenti, si completa insieme il lavoro rimasto. Il confronto può avvenire anche nei commenti della issue.
6. **Merge e chiusura.** Dopo l'approvazione la pull request è unita al ramo principale e la issue è chiusa.

Lo stato di ciascun requisito (proposto, validato, implementato, verificato) è tracciato nelle issue, come previsto dalla specifica dei requisiti (sezione 1.7).

Il ramo principale `main` è protetto: non si fa push diretto e ogni modifica entra con una pull request approvata dall'altro membro. **[DA COMPLETARE:** verificare dopo aver attivato la protezione e indicare il tipo di merge usato.**]**

### 3.3 Comandi usati

| Operazione | Comando |
|---|---|
| Aggiornare il workspace | `git pull` |
| Creare il branch di una issue | `git switch -c <numero>-<nome-del-branch>` |
| Caricare sullo stage | `git add <file>` e `git commit -m "<messaggio con ID>"` |
| Caricare sul repository remoto le modifiche | `git push -u origin <branch>` |
| Rivedere, approvare e unire | pull request su GitHub |

In alternativa si può utilizzare l'applicativo GitHub Desktop con le sue azioni equivalenti.

### 3.4 Regole sul repository

- Codice e documenti sono in cartelle separate (`code/` e `docs/`).
- Il `.gitignore` esclude i file generati ma non il file `.project`.
- Il messaggio di commit ha la forma `tipo: testo` (ad esempio `docs: …` per i documenti) e riporta l'ID del requisito o del documento a cui si riferisce, come previsto dalla specifica dei requisiti (sezione 1.7). Esempio: `docs: chiarisce le transizioni di stato (FUN.6.3)`. La regola sull'ID è applicata dal 15/10/2026; i commit precedenti usano solo il formato `tipo: testo`.
- Le versioni significative sono identificate con tag Git; la release finale avrà il tag `v1.0.0` (Project Plan, 13.1).


### 3.5 Statistiche

| Dato | Valore |
|---|---|
| Commit per membro | **[DA COMPLETARE]** |
| Issue aperte / chiuse | **[DA COMPLETARE]** |
| Pull request aperte / unite | **[DA COMPLETARE]** |
| Revisioni svolte per membro | **[DA COMPLETARE]** |

### 3.6 Comunicazioni con il docente

Le richieste al docente si fanno con una issue a lui assegnata, come previsto dal regolamento: ad esempio l'approvazione del Project Plan, che va condiviso almeno un mese prima dell'esame.

---

## 4. Persone e organizzazione del team

### 4.1 Il team

| Membro | Ruolo in Scrum | Ruolo di sviluppo | Attività svolte finora |
|---|---|---|---|
| Matteo Poli | Product Owner / Customer Representative | Sviluppatore | Specifica dei requisiti; gestione del progetto |
| Michele Bisignano | Scrum Master / Agile Coordinator | Sviluppatore | Diagramma UML dei casi d'uso; Project Plan |

### 4.2 Schema organizzativo

Il team è composto da due persone con responsabilità condivise: ogni compito ha un solo responsabile ed è sempre rivisto dall'altro membro, ma entrambi possono contribuire alla stessa issue. I ruoli organizzativi sono adattati alle dimensioni ridotte del team e non implicano una separazione rigida delle responsabilità (Project Plan, 3.2).

### 4.3 Ripartizione del lavoro

Ogni sprint il lavoro si divide in compiti individuali, ciascuno con la propria issue, come nella tabella di 2.3. Un membro può svolgere più compiti nello stesso sprint.

### 4.4 Comunicazione

Il team si incontra di persona ogni giovedì (2.1) e prevede un allineamento quotidiano di 15 minuti (2.2) svolto di persona in Università. Lo strumento di comunicazione virtuale è GitHub tramite i commenti delle issue e delle pull request.
