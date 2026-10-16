# MPlan – Gestione del progetto

**Documento 2 – Gestione del progetto** (Software Life Cycle, Configuration Management, Persone e organizzazione del team)
Corso di Ingegneria del Software, A.A. 2026/27

| Versione | Data | Modifiche |
|---|---|---|
| 0.1.0 | 15/10/2026 | Versione iniziale |

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

Descrive il processo **effettivamente seguito**. Le differenze rispetto a quanto previsto nel Project Plan (Documento 1) sono indicate in 2.6.

---

## 2. Ciclo di vita del software

### 2.1 Processo seguito

Il team segue **Scrum**, adattato a un gruppo di due persone. Il lavoro è diviso in **sprint** di una settimana, da un giovedì al giovedì successivo. Il giovedì il team si incontra di persona per pianificare lo sprint, fare il punto della situazione e iniziare il lavoro. All'inizio di ogni sprint ogni membro prende uno o più compiti, e al termine di ogni compito il lavoro è rivisto dall'altro membro.

### 2.2 Ruoli ed eventi di Scrum

| Ruolo | Chi | Compito |
|---|---|---|
| Product Owner | Matteo Poli | Rappresenta le esigenze del committente (MP elettric s.a.s), decide le priorità dei compiti e dei requisiti |
| Scrum Master | Michele Bisignano | Cura il rispetto del processo e rimuove gli ostacoli |
| Sviluppatori | Entrambi i membri | Svolgono i compiti dello sprint |

| Evento o artefatto di Scrum | Come lo svolgiamo |
|---|---|
| Sprint planning | Ogni giovedì, durante l'incontro di persona, si scelgono i compiti dello sprint e si assegnano le issue ai membri |
| Daily scrum | **[DA COMPLETARE: se e come, ad esempio messaggio o breve incontro]** |
| Sprint review | Ogni giovedì si fa il punto sul lavoro svolto; inoltre ogni compito è rivisto dall'altro membro con la pull request prima del merge (3.2) |
| Sprint retrospective | **[DA COMPLETARE: se durante l'incontro del giovedì si discute anche come migliorare il processo]** |
| Product backlog | Le issue della kanban board non ancora assegnate a uno sprint, ciascuna con gli ID dei requisiti che realizza. L'ordine segue le priorità MoSCoW della specifica dei requisiti (sezione 6) |
| Sprint backlog | Le issue assegnate ai membri nello sprint (3.2) |

### 2.3 Sprint e milestone

| Sprint | Periodo | Compiti | Responsabile |
|---|---|---|---|
| Sprint 1 | 08/10/2026 – 15/10/2026 **[DA COMPLETARE: verificare]** | Diagramma UML dei casi d'uso | Scrum Master |
| | | Specifica dei requisiti (Documento 3) | Product Owner |
| Sprint 2 | 15/10/2026 – 22/10/2026 **[DA COMPLETARE: verificare]** | Project Plan (Documento 1) | Scrum Master |
| | | Gestione del progetto (Documento 2, questo documento) | Product Owner |

| Milestone | Contenuto | Stato |
|---|---|---|
| M0 – Setup repository | Creazione e configurazione del repository | **[DA COMPLETARE: chiusa?]** |
| M1 – Ingegneria dei requisiti | Specifica dei requisiti, intervista, diagramma dei casi d'uso, Project Plan, gestione del progetto | In corso |
| Milestone successive | **[DA COMPLETARE: dal Project Plan, ad esempio Design, Implementazione, Testing]** | – |

### 2.4 Gestione di una modifica

Ogni modifica, che nasca da un commento di revisione o da una decisione del team, segue lo stesso percorso. La regola sull'ID del requisito è quella della specifica dei requisiti (sezione 1.7).

```mermaid
flowchart TD
    A([Richiesta di modifica]) --> B[Si apre una issue che cita l'ID del requisito o del documento]
    B --> C[Si crea un branch dedicato alla issue]
    C --> D[Si modifica e si fa commit con l'ID nel messaggio]
    D --> E[Si apre una pull request]
    E --> F{L'altro membro approva?}
    F -- no --> D
    F -- sì --> G[Merge sul ramo principale]
    G --> H[Si aggiorna la tabella delle versioni]
    H --> I[Si chiude la issue]
    I --> J([Fine])
```

*Figura 2.1 – Schema provvisorio della gestione di una modifica. La versione definitiva sarà un diagramma delle attività in StarUML.*

### 2.5 Approccio basato su modelli (MDA)

Il design parte dai diagrammi UML realizzati con StarUML. Dal diagramma delle classi si genera una prima versione del codice Java, poi completata a mano. Le classi generate e quelle modificate saranno indicate nella relazione **[DA COMPLETARE alla fase di implementazione]**.

### 2.6 Differenze rispetto al Project Plan

**[DA COMPLETARE:** il Project Plan è in redazione nello sprint 2. Quando è pronto, confrontare processo, ruoli e date e indicare qui ogni differenza.**]**

---

## 3. Configuration management

### 3.1 Strumenti

| Strumento | Uso |
|---|---|
| Git e GitHub | Versionamento del codice e dei documenti, issue, branch, pull request, revisioni e milestone |
| Kanban board | Gestione visiva dei compiti dello sprint e del product backlog **[DA COMPLETARE: strumento e colonne]** |
| StarUML | Modelli UML; il file di progetto è nel repository |
| Eclipse e Maven | Sviluppo e gestione delle dipendenze |

Il repository è condiviso con il docente e con l'esercitatrice (account `garganti` e `silviabonfanti`) **[DA COMPLETARE: verificare]**.

### 3.2 Flusso di lavoro

1. **Una issue per compito.** Ogni compito della settimana è una issue assegnata a una sola persona e collegata a una milestone.
2. **Un branch per issue.** Il nome del branch inizia con il numero della issue, seguito da un nome breve (ad esempio `12-nome-del-branch`). Una persona può avere più issue, quindi più branch attivi contemporaneamente.
3. **Commit.** Il lavoro è salvato con commit sul branch della issue.
4. **Pull request.** Al termine del compito si apre una pull request verso il ramo principale.
5. **Revisione.** L'altro membro del team rivede la pull request. Se servono correzioni, si tornano a fare commit sullo stesso branch.
6. **Merge e chiusura.** Dopo l'approvazione la pull request è unita al ramo principale e la issue è chiusa.

Il ramo principale `main` è protetto: non si fa push diretto e ogni modifica entra con una pull request approvata dall'altro membro. **[DA COMPLETARE:** verificare dopo aver attivato la protezione e indicare il tipo di merge usato.**]**

### 3.3 Comandi usati

| Operazione | Comando |
|---|---|
| Aggiornare il ramo principale | `git pull` |
| Creare il branch di una issue | `git switch -c <numero>-<nome-del-branch>` |
| Preparare e salvare le modifiche | `git add <file>` e `git commit -m "<messaggio con ID>"` |
| Pubblicare il branch | `git push -u origin <branch>` |
| Rivedere, approvare e unire | pull request su GitHub |

### 3.4 Regole sul repository

- Codice e documenti sono in cartelle separate (`code/` e `docs/`).
- Il `.gitignore` esclude i file generati ma non il file `.project`.
- Il messaggio di commit ha la forma `tipo: testo` (ad esempio `docs: …` per i documenti) e riporta l'ID del requisito o del documento a cui si riferisce, come previsto dalla specifica dei requisiti (sezione 1.7). Esempio: `docs: chiarisce le transizioni di stato (FUN.6.3)`. La regola sull'ID è applicata dal 15/10/2026; i commit precedenti usano solo il formato `tipo: testo`.

### 3.5 Statistiche

| Dato | Valore |
|---|---|
| Commit per membro | **[DA COMPLETARE]** |
| Issue aperte / chiuse | **[DA COMPLETARE]** |
| Pull request aperte / unite | **[DA COMPLETARE]** |
| Revisioni svolte per membro | **[DA COMPLETARE]** |

---

## 4. Persone e organizzazione del team

### 4.1 Il team

| Membro | Ruolo in Scrum | Ruolo di sviluppo | Attività svolte finora |
|---|---|---|---|
| Matteo Poli | Product Owner | Sviluppatore | Specifica dei requisiti; gestione del progetto |
| Michele Bisignano | Scrum Master | Sviluppatore | Diagramma UML dei casi d'uso; Project Plan |

### 4.2 Schema organizzativo

Il team è composto da due persone con responsabilità condivise: ogni compito ha un solo responsabile ed è sempre rivisto dall'altro membro. **[DA COMPLETARE:** indicare lo schema di organizzazione del team presentato nel libro (capitolo 5) a cui ci si rifà e perché è adatto a un team di due persone.**]**

### 4.3 Ripartizione del lavoro

Ogni sprint il lavoro si divide in compiti individuali, ciascuno con la propria issue. Un membro può svolgere più compiti nello stesso sprint. Le attività svolte sono quelle della tabella in 2.3.

### 4.4 Comunicazione

Il team si incontra di persona ogni giovedì (2.1). **[DA COMPLETARE:** altri canali usati tra un incontro e l'altro, ad esempio messaggi o commenti nelle issue.**]**
