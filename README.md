# MPlan

MPlan è un sistema per la verifica delle attività e la gestione dei clienti per gli interventi elettrici, pensato per **MP elettrica s.a.s.**

Il progetto è sviluppato per il corso di Ingegneria del Software (Università degli Studi di Bergamo, A.A. 2026/27).

> **Stato:** in sviluppo, fase di design.

## Cosa fa

- gestisce la rubrica dei contatti (clienti), che è la schermata iniziale
- registra le attività di intervento di ogni contatto
- permette di segnare un'attività come parziale, completata o annullata, senza mai cancellarla
- mostra lo storico delle attività di un contatto e l'agenda di un giorno

L'elenco completo dei requisiti è nella specifica (Documento 3, cartella `docs/`).

## Tecnologie

- Java con Maven (progetto multi-modulo)
- interfaccia grafica Swing
- database SQLite, usato tramite jOOQ
- JUnit 5 per i test, log4j2 per i log
- StarUML per i diagrammi UML

**27** versione di Java richiesta.

## Struttura del progetto

```
code/    il codice Maven, diviso in moduli
docs/    i documenti del progetto e i diagrammi UML
```

I moduli Maven sono cinque, in un'architettura a strati:

- `mplan-api`: le interfacce dei servizi e i tipi condivisi
- `mplan-core`: la logica dell'applicazione (servizi, validazioni, interfacce dei repository)
- `mplan-persistence`: l'accesso al database SQLite
- `mplan-ui-swing`: l'interfaccia grafica
- `mplan-app`: avvia l'applicazione e collega tra loro gli altri moduli

L'interfaccia grafica parla solo con `mplan-api`. Il core non conosce né la grafica né il database.

## Documenti

- **Documento 1**: Project Plan
- **Documento 2**: Gestione del progetto
- **Documento 3**: Specifica dei requisiti
- **Documento 4**: Design (architettura e design pattern)

I diagrammi UML sono in `docs/uml/` (file StarUML) e le immagini esportate in `docs/uml/export/`.

## Come compilare ed eseguire

**[DA COMPLETARE]** comandi per compilare, eseguire i test e avviare l'applicazione (si aggiungeranno quando il codice sarà pronto).

## Come si lavora sul repository

- ogni compito ha una issue, collegata a una milestone
- si lavora su un branch separato, con nome `<numero-issue>-<nome-breve>`
- il lavoro entra in `main` con una pull request approvata dall'altro membro
- il ramo `main` è protetto: niente push diretti
- i commit hanno la forma `tipo: testo (ID del requisito)`, ad esempio `docs: chiarisce le transizioni di stato (FUN.6.3)`

## Team

- **Matteo Poli**: Product Owner
- **Michele Bisignano**: Scrum Master

## Licenza

Distribuito con licenza MIT.
