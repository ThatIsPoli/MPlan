# DOCUMENTO 1: PROJECT PLAN

# Progetto MPlan

## Storico delle revisioni

| Versione | Data |  Descrizione delle modifiche |
| --- | --- | --- |
| **0.1.0** | 15/10/2026 | Prima bozza del Project Plan. |
| **0.1.1** | 17/10/2026 | Correzione di alcune imprecisioni. |

---

## Indice

1. Introduction
2. Process model
3. Organization of the project
4. Standards, guidelines, procedures
5. Management activities
6. Risks
7. Staffing
8. Methods and techniques
9. Quality assurance
10. Resources
11. Budget and schedule
12. Changes
13. Delivery

## 1\. Introduction

### 1.1 Background e contesto

MPlan nasce nel corso di Ingegneria del Software (A.A. 2026/27). Il committente, MP elettrica s.a.s., esegue interventi presso clienti privati e cantieri e oggi non ha uno strumento per tenere traccia di cosa è stato fatto, cosa resta da fare e quali materiali servono per le visite successive.

### 1.2 Obiettivi del progetto

1. Realizzare la versione 1 di MPlan: applicazione per PC (Windows) con archivio dati locale, per gestire clienti, cantieri e attività, con storico per contatto e agenda giornaliera.
2. Consegnare entro gennaio 2027 i documenti e i diagrammi UML richiesti dal corso (13 consegne, scomposte al punto 11.1).
3. Applicare in modo documentato le tecniche del corso: requisiti, modellazione, progettazione, test, manutenzione e gestione del progetto.

### 1.3 Risultati attesi (Deliverable)

I principali risultati attesi sono:

- Codice sorgente Java organizzato in un progetto Maven.
- Repository GitHub condiviso per la collaborazione e il controllo delle versioni.
- Documentazione dei requisiti e della progettazione, accompagnata dai diagrammi UML.
- Database relazionale embedded H2.
- Suite di test automatizzati basata su JUnit 5 e relativo report di copertura.
- Interfaccia grafica locale per la dimostrazione delle funzionalità principali.
- Documentazione tecnica finale articolata nei sei documenti previsti dal progetto.
- Release finale identificata dal tag Git `v1.0.0`.
- Presentazione per l'esame orale.

### 1.4 Responsabili del progetto

I responsabili del progetto sono **Matteo Poli** e **Michele Bisignano**, che condividono le attività di sviluppo, verifica e documentazione.

### 1.5 Sintesi

Progetto di due persone, da svolgere nel primo semestre, con circa 40 ore a testa pianificate. Processo iterativo a settimane, con 6 milestone su GitHub. Il codice si ferma il 9 dicembre; dicembre e gennaio sono dedicati a documenti finali, manutenzione e revisione.

---

## 2\. Process Model

### 2.1 Modello di sviluppo adottato

Il progetto adotta un approccio agile ispirato a **SCRUM**, con iterazioni settimanali e rilascio incrementale delle funzionalità.

Per la gestione operativa delle attività viene utilizzata una board Kanban su GitHub Projects. La board costituisce uno strumento di visualizzazione del lavoro e non modifica l'organizzazione iterativa adottata.

### 2.2 Organizzazione degli sprint

- **Durata dello sprint:** una settimana.
- **Incontro settimanale:** ogni giovedì, con una sessione congiunta di tre ore dedicata alla pianificazione, alla revisione del codice e alla retrospettiva.
- **Daily Scrum:** allineamento quotidiano di 15 minuti, dalle 10:15 alle 10:30, compatibilmente con gli impegni accademici dei componenti del team.
- **Gestione del lavoro:** assegnazione delle attività e monitoraggio dell'avanzamento tramite issue e board Kanban.
- **Revisione:** verifica congiunta degli incrementi realizzati e aggiornamento delle priorità per lo sprint successivo.

### 2.3 Milestone di progetto

Le milestone sono su GitHub con relativa scadenza. Le consegne settimanali sono **issue** assegnate alla milestone, con le etichette `documento`, `codice`, `test` (tipo).

| Milestone | Inizio | Scadenza | Si considera raggiunta quando |
|---|---|---|
| M0 – – Setup del repository | 01/10/2026 |08/10/2026 | Creazione e configurazione del repository GitHub. |
| M1 – Ingegneria dei requisiti | 8/10/2026 | 22/10/2026 | Specifica dei requisiti pubblicato nel repository |
| M2 – Design | | 22/10/2026 |26/11/2026 | Tutti i diagrammi UML e il Documento di Design sono nel repository e approvati dall'altro membro |
| M3 – Sviluppo del codice | 19/11/2026 | 09/12/2026 | UC2–UC10 funzionano da GUI; il codice è unito in `main` e compila con Maven |
| M4 – Sviluppo dei test | 19/11/2026 | 16/12/2026 | Test JUnit superati e Documento di Testing con tracciabilità |
| M5 – Manutenzione | 16/12/2026 | 23/12/2026 | Modifica applicata, test ancora superati e Documento di Maintenance |
| M6 – Revisione dei documenti e consegna | 23/12/2026 | 01/01/2027 | Revisione incrociata completata, tracciabilità controllata, consegna effettuata |


---

## 3\. Organization of the Project

### 3.1 Organizzazione di riferimento

- **Organizzazione accademica:** Università degli Studi di Bergamo.
- **Docenti di riferimento:** Prof. A. Gargantini e Dott.ssa S. Bonfanti.
- **Organizzazione utente:** MP elettrica s.a.s.
- **Referente operativo:** rappresentante dell'azienda coinvolto nell'elicitazione e nella convalida dei requisiti.

### 3.2 Ruoli nel team

**Matteo Poli**

- Product Owner / Customer Representative: raccolta e chiarificazione dei requisiti, definizione delle priorità e supporto alla validazione funzionale.
- Developer: implementazione delle funzionalità, test e documentazione.

**Michele Bisignano**

- Scrum Master / Agile Coordinator: coordinamento delle attività, gestione della board Kanban, monitoraggio della roadmap e verifica dei vincoli temporali e accademici.
- Developer: implementazione delle funzionalità, test e documentazione.

Entrambi i componenti partecipano allo sviluppo Java, alla progettazione UML, alle revisioni del codice e alla stesura della documentazione tecnica.

I ruoli organizzativi sono adattati alle dimensioni ridotte del team e non implicano una separazione rigida delle responsabilità.

---

## 4\. Standards, Guidelines and Procedures

### 4.1 Convenzioni di codifica

Il codice Java deve rispettare le convenzioni di denominazione e formattazione comunemente adottate nell'ecosistema Java.

**Codice Java**:
- Classi e interfacce: `UpperCamelCase`.
- Metodi e variabili: `lowerCamelCase`.
- Costanti: `UPPER_SNAKE_CASE`.
- Packages: nomi in minuscolo.
- Attributi: visibilità privata, salvo necessità progettuali motivate.
- Commenti: Javadoc su tutti i membri ed elementi pubblici (classi, interfacce, metodi e costanti).
- Incapsulamento e information hiding applicati in modo rigoroso.
- Metodi e classi con responsabilità chiare e ben definite.
- Gestione esplicita delle condizioni di errore e delle eccezioni.

### 4.2 Gestione del versionamento

Il codice e la documentazione vengono gestiti mediante Git e GitHub.

Le procedure adottate sono:

- Utilizzo di branch dedicati per lo sviluppo delle funzionalità.
- Apertura di Pull Request per integrare le modifiche nel branch principale.
- Revisione obbligatoria da parte dell'altro componente del team.
- Esecuzione dei controlli di compilazione e test prima del merge.
- Aggiornamento della documentazione quando vengono modificate funzionalità, interfacce o architettura.
- Utilizzo di tag Git per identificare le versioni significative.

Il file `.gitignore` deve escludere i file temporanei e gli artefatti di compilazione, tra cui `target/` e `*.class`. I file di configurazione del progetto necessari alla collaborazione devono essere versionati in modo consapevole.

### 4.3 Standard della documentazione

La documentazione viene mantenuta in formato Markdown all'interno del repository.

I principali riferimenti sono:

- IEEE 830 come riferimento storico per la struttura della specifica dei requisiti software.
- UML per la modellazione strutturale e comportamentale.
- Convenzioni Java per il codice sorgente.
- Convenzioni Maven per la struttura del progetto e la gestione delle dipendenze.

La versione finale dei documenti deve essere consolidata entro cinque giorni dall'appello d'esame.

Ogni documento è sottoposto a revisione incrociata per verificarne correttezza, completezza e coerenza con il sistema implementato.

---

## 5\. Management Activities

### 5.1 Gestione delle priorità

Il progetto è vincolato da una disponibilità complessiva di 80 ore di lavoro, pari a circa 40 ore per componente del team.

Le priorità vengono stabilite considerando:

- Rispetto della scadenza dell'esame.
- Completamento dei requisiti funzionali essenziali.
- Correttezza della logica di business.
- Qualità della progettazione e della documentazione.
- Affidabilità della persistenza e dei test.
- Disponibilità di tempo per la convalida finale.

In caso di ritardi, le funzionalità non essenziali possono essere ridimensionate o rinviate, senza compromettere i requisiti fondamentali.

### 5.2 Monitoraggio dell'avanzamento

L'avanzamento viene monitorato mediante GitHub Projects, utilizzando issue e una board Kanban.

### 5.3 Verifica e chiusura delle attività

Le issue vengono chiuse dopo il completamento del lavoro e la verifica dei relativi criteri di accettazione.

Le Pull Request vengono sottoposte a revisione da parte dell'altro componente del team. La revisione settimanale consente di verificare gli incrementi realizzati, aggiornare la board e pianificare lo sprint successivo.

---

## 6\. Risks Potential

### 6.1 Ampliamento incontrollato del perimetro (Scope Creep)

**Descrizione:** il dominio della gestione delle attività di una società può includere numerose funzionalità aggiuntive non necessarie agli obiettivi iniziali.

**Impatto:** ritardi, aumento della complessità e riduzione del tempo disponibile per test e documentazione.

**Mitigazione:** Formalizzazione delle nuove richieste tramite issue e assegnazione delle priorità in funzione del tempo residuo.

### 6.2 Disallineamento tra modelli UML e codice

**Descrizione:** modifiche implementative possono rendere obsoleti i diagrammi di progettazione.

**Impatto:** documentazione non attendibile e maggiori difficoltà di manutenzione.

**Mitigazione:** aggiornamento dei diagrammi in caso di modifiche architetturali e controllo della coerenza durante le Pull Request.

### 6.3 Mancata validazione dei requisiti con il committente

**Descrizione:** presenza di dubbi e punti aperti sui requisiti (in particolare quelli indicati nella sezione 11 del Documento 3) non ancora formalmente chiariti e approvati dal committente.

**Impatto:** progettazione e sviluppo basati su assunzioni errate, con conseguente necessità di rework pesante del codice e del design di dettaglio.

**Mitigazione:** risoluzione e chiarimento dei punti aperti entro l'apertura della milestone M3 – Sviluppo del codice.

### 6.4 Mancanza di familiarità con i framework

**Descrizione:** limitata esperienza pratica dei membri del team nell'utilizzo delle API e dei layout manager di alcune librerie Java.

**Impatto:** rallentamento dello sviluppo, codice disordinato o poco manutenibile e potenziale mancato rispetto delle stime di tempo per l'interfaccia.

**Mitigazione:** mantenimento codice essenziale.

---

## 7\. Staffing

Il team è composto da due persone, con un impegno stimato di circa 40 ore per ciascun componente, per un totale di 80 ore.

---

## 8\. Methods and Techniques

### 8.1 Analisi dei requisiti

I requisiti vengono raccolti mediante un'intervista strutturata al referente dell'organizzazione utente e successivamente formalizzati in un documento dedicato.

### 8.2 Progettazione

La progettazione viene realizzata utilizzando StarUML v5 (use case, class, state, activity, sequence, package, component, communication/timing); architettura a strati con interfacce tra GUI, servizi e repository

### 8.3 Implementazione

Il sistema viene implementato in Java, utilizzando Eclipse come ambiente di sviluppo, Apache Maven per la compilazione e la gestione delle dipendenze, H2 embedded per la gestione del database.

Le funzionalità vengono integrate in modo incrementale, verificando la compilazione e il comportamento del sistema a ogni passaggio significativo.


### 8.4 Controllo della configurazione

Git e GitHub vengono utilizzati per gestire versioni, modifiche e integrazione del codice.

La documentazione tecnica e i diagrammi UML vengono aggiornati quando cambiano requisiti, interfacce o struttura architetturale.

---

## 9\. Quality Assurance

### 9.1 Obiettivi di qualità

Gli obiettivi di qualità sono definiti secondo le caratteristiche di qualità del prodotto dello standard ISO/IEC 9126.

**Funzionalità**

- *Accuratezza*: il sistema deve memorizzare correttamente gli attività.
- *Adeguatezza*: il sistema deve soddisfare i requisiti specificati.
- *Integrità dei dati*: le operazioni di aggiornamento delle attività devono preservare la coerenza dei dati e impedire modifiche incompatibili con le regole di business.

**Affidabilità**

- *Tolleranza ai guasti*: il sistema deve gestire gli input non validi e le condizioni di errore prevedibili senza terminazioni anomale non controllate.

**Manutenibilità**

- *Modificabilità*: l'architettura deve favorire la separazione delle responsabilità e il basso accoppiamento.
- *Testabilità*: le classi di dominio e i servizi devono poter essere verificati indipendentemente dall'interfaccia grafica e, quando possibile, dai dettagli del database.

**Portabilità**

- *Sostituibilità*: i componenti devono poter essere sostituiti attraverso interfacce dedicate.

### 9.2 Organizzazione della qualità

La qualità viene verificata congiuntamente dai due componenti.

La persona che sviluppa una modifica non può essere l'unico revisore della relativa Pull Request. L'altro componente deve verificare il codice e i criteri di accettazione prima del merge.

---

## 10\. Resources

### 10.1 Risorse software

| Categoria | Strumento | Utilizzo |
| --- | --- | --- |
| Ambiente di sviluppo | Eclipse IDE | Scrittura, debug e verifica del codice Java |
| Linguaggio e runtime | Java Development Kit | Compilazione ed esecuzione |
| Build automation | Apache Maven | Compilazione e gestione delle dipendenze |
| Modellazione | StarUML v5 | Creazione dei diagrammi UML |
| Database | H2 Database Engine | Persistenza relazionale embedded |
| Testing | JUnit 5 | Test unitari e di integrazione |
| Analisi statica | SonarLint | Individuazione di problemi nel codice |
| Versionamento | Git | Gestione delle versioni |
| Collaborazione | GitHub | Repository, issue, Pull Request e board Kanban |
| Documentazione | Markdown | Redazione dei documenti tecnici |

### 10.2 Risorse umane

Le risorse umane sono costituite dai due sviluppatori del progetto, che condividono le attività di analisi, progettazione, implementazione, testing e documentazione.

### 10.3 Risorse economiche

Nessun costo previsto

### 10.4 Risorse hardware

Un pc windows 11 per persona.

---

## 11\. Budget and Schedule

### 11.1 Allocazione dello sforzo

Il budget complessivo previsto è pari a 80 ore, ripartite tra i due componenti del team.

| Area di attività | Percentuale | Ore stimate |
| --- | --- | --- |
| Analisi, specifica, modellazione e documentazione | 70% | 56 |
| Codifica Java, integrazione e test | 30% | 24 |
| **Totale** | **100%** | **80** |


### 11.2 Pianificazione temporale

Il progetto adotta sprint settimanali con pianificazione e revisione il giovedì.

Le attività sono organizzate secondo le milestone nella sezione 2.3 di questo documento. 

La pianificazione dettagliata degli sprint viene mantenuta su GitHub Projects ricorrendo alle issues. Il codice e la documentazione devono essere congelati almeno cinque giorni prima dell'appello.

---

## 12\. Changes

Le richieste di modifica e i nuovi requisiti emersi durante lo sviluppo vengono gestiti in modo controllato.

La procedura prevede:

1. Apertura di una issue su GitHub con etichetta.
2. Valutazione dell'impatto: si indicano requisiti, UML, codice, test.
3. Approvazione da parte di entrambi i membri.
4. Implementazione e verifica attraverso Pull Request.
5. Aggiornamento dei documenti e dei diagrammi interessati.
6. Chiusura della issue.

---

## 13\. Delivery

### 13.1 Consegna per l'esame

La consegna accademica comprende:

- Repository GitHub aggiornato.
- Release finale identificata dal tag `v1.0.0`.
- Codice sorgente Java e configurazione Maven.
- Documentazione tecnica completa.
- Diagrammi UML e relativi file sorgente StarUML.
- Suite di test JUnit 5 e report di copertura, se generato.
- Presentazione finale in formato PPTX o PDF.

La release deve essere preparata almeno cinque giorni prima dell'appello.

Durante l'esame orale verrà effettuata una dimostrazione dal vivo delle funzionalità principali, accompagnata dalla presentazione del progetto e dall'esecuzione della suite di test, nel rispetto del limite di 20 minuti previsto.

### 13.2 Consegna al committente

La consegna operativa a MP elettrica s.a.s. è prevista come attività distinta dalla consegna accademica e subordinata alla disponibilità di un pacchetto eseguibile verificato.


- **Installazione e configurazione**: Installazione assistita sul dispositivo usato dal committente.
- **Formazione e affiancamento**: È prevista una breve formazione,dedicata alle operazioni principali.