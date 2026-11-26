# I pattern di MPlan, in breve

Promemoria dei pattern visti a lezione (dispensa del prof e slide del tutorato) e di dove stanno nel codice e nel class diagram.

## Usati

- **Observer** (`core.servizi`): `OsservabileAttivita` è il soggetto astratto, con la lista `osservatori`, `aggiungiOsservatore` e i metodi `notifica...`. `AttivitaServiceImpl` lo estende e avvisa a ogni modifica, cambio di stato e annullamento. `OsservatoreAttivita` è l'interfaccia; `RegistroModifiche` la implementa e scrive il registro. Il collegamento si fa in `ConfigurazioneMPlan`.
- **Immutable** (`api`, `core.dominio`): i record `Contatto`, `Attivita`, `VoceAgenda`, `ErroreValidazione` e `VoceRegistro` ricevono i valori solo nel costruttore, e le liste sono copiate con `List.copyOf`. Per cambiare un'attività se ne crea una nuova (`copiaConStato`).
- **Delegation** (tutti i moduli): metodi che chiamano solo un collaboratore. Esempi: `ContattoServiceImpl.valida` → `validatore.valida`, `trova` → `repository.trova`, `AgendaServiceImpl.oggi` → `LocalDate.now(clock)`; i repository usano i `*Mapper`; le schermate usano `Navigatore` ed `ElencoAttivita`.
- **Adapter** (`persistence`, `app`): `Jooq*Repository` e `GestoreTransazioniJooq` implementano le interfacce del core e dentro usano jOOQ. `ChiusuraApplicazioneImpl` implementa `ChiusuraApplicazione` e dentro usa Spring.
- **Façade** (`api`, `persistence`, `ui`): le interfacce dei servizi sono l'unico accesso della GUI al core; `PersistenzaFactory` è l'unico accesso di `mplan-app` alla persistenza; `Navigatore` nasconde alle schermate la navigazione di Vaadin.
- **Factory, forma semplice** (`persistence`): i metodi `PersistenzaFactory.crea...()` restituiscono le interfacce dei repository e creano dentro le classi jOOQ. Manca l'interfaccia «Factory» generica della forma completa.
- **Strategy**, dalle slide del tutorato (`core.dominio`): `OrdinamentoAttivita.PER_STORICO` e `PER_AGENDA` sono due `Comparator` intercambiabili.

Nella GUI l'Observer c'è anche tramite Vaadin, con i listener (`addClickListener`, `BeforeEnterObserver`, `ErrorHandler`) e le callback (`alClick`, `alCambioStato`).

## Non usati

- **Singleton**: nel codice non c'è nessun `getInstance()`. `Navigatore` lo crea Spring, uno per ogni scheda del browser (`@UIScope`).
- **Read-Only Interface**: le entità sono già immutabili per tutti.
- **Proxy**: non c'è caricamento pigro, perché i dati sono pochi.
- **Abstraction-Occurrence**: c'è un'attività per ogni giorno di lavoro (FUN.7).
- **General Hierarchy / Composite**: nei dati non c'è nessun albero. Lo usa solo Vaadin per i componenti grafici.
- **Player-Role**: privato o azienda è solo il valore `TipoContatto`.
