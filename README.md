# MPlan

MPlan è un sistema per la verifica delle attività e la gestione dei clienti per gli interventi elettrici, pensato per **MP elettrica s.a.s.**

Il progetto è sviluppato per il corso di Ingegneria del Software (Università degli Studi di Bergamo, A.A. 2026/27).

> **Stato:** in sviluppo, fase di implementazione.

## Cosa fa

- gestisce la rubrica dei contatti (clienti e cantieri), che è la schermata iniziale
- registra le attività di intervento di ogni contatto
- permette di segnare un'attività come parziale, completata o annullata, senza mai cancellarla
- mostra lo storico delle attività di un contatto e l'agenda di un giorno

L'elenco completo dei requisiti è nella specifica (Documento 3, cartella `docs/`).

## Tecnologie

- Java con Maven (progetto multi-modulo)
- interfaccia grafica web con Vaadin, usata dal browser del PC
- Spring Boot per avviare l'applicazione
- database SQLite, usato tramite jOOQ
- JUnit 5 per i test, log4j2 per i log
- StarUML per i diagrammi UML

**27** versione di Java richiesta; l'interfaccia si apre nel browser del PC.

## Struttura del progetto

```
code/    il codice Maven, diviso in moduli
docs/    i documenti del progetto e i diagrammi UML
```

I moduli Maven sono cinque, in un'architettura a strati:

- `mplan-api`: le interfacce dei servizi e i tipi condivisi
- `mplan-core`: la logica dell'applicazione (servizi, validazioni, interfacce dei repository)
- `mplan-persistence`: l'accesso al database SQLite
- `mplan-ui-vaadin`: l'interfaccia grafica (schermate Vaadin)
- `mplan-app`: avvia l'applicazione con Spring Boot e collega tra loro gli altri moduli

L'interfaccia grafica parla solo con `mplan-api`. Il core non conosce né la grafica né il database.

## Documenti

- **Documento 1**: Project Plan
- **Documento 2**: Gestione del progetto
- **Documento 3**: Specifica dei requisiti
- **Documento 4**: Design (architettura e design pattern)

I diagrammi UML sono in `docs/uml/` (file StarUML) e le immagini esportate in `docs/uml/export/`.

## Come compilare ed eseguire

Servono **Java 27** (JDK) e **Maven 3.9** o successivo. Non servono altri programmi: il database è un file SQLite creato al primo avvio e l'interfaccia funziona anche senza connessione a Internet.

**Compilare** (dalla cartella `code/`):

```
mvn package
```

La prima compilazione scarica le dipendenze e prepara l'interfaccia Vaadin, quindi richiede qualche minuto. Il risultato è il file `code/mplan-app/target/mplan-app-0.0.1-SNAPSHOT.jar`.

**Avviare** (dalla radice del repository):

```
java -jar code/mplan-app/target/mplan-app-0.0.1-SNAPSHOT.jar
```

- L'applicazione si apre nel browser all'indirizzo `http://127.0.0.1:8642` ed è raggiungibile solo dallo stesso PC.
- Se l'applicazione è già aperta, un secondo avvio apre solo il browser sulla finestra esistente.
- Per chiuderla si usa il pulsante "Chiudi applicazione" nella schermata iniziale.

**Dove sono i dati**:

- database: `%USERPROFILE%\MPlan\mplan.db`;
- log: `%USERPROFILE%\MPlan\logs\`.

Per provare l'applicazione senza toccare i propri dati si può indicare un altro file di database:

```
java -jar code/mplan-app/target/mplan-app-0.0.1-SNAPSHOT.jar --mplan.database=C:/percorso/prova.db
```

**Avviare da Eclipse**:

1. Registrare il JDK 27 in *Window → Preferences → Java → Installed JREs*.
2. *File → Import → Maven → Existing Maven Projects* e scegliere la cartella `code/`: si importano il progetto padre e i cinque moduli.
3. La prima volta eseguire `mvn package` sul progetto padre (*Run As → Maven build…*, goal `package`), poi *Maven → Update Project*. Il comando genera le classi jOOQ di `mplan-persistence`.
4. Avviare `it.mpelettrica.mplan.app.MPlanApplication` (modulo `mplan-app`) con *Run As → Java Application*. Per usare un database di prova si aggiunge `--mplan.database=...` in *Run Configurations → Arguments*.

Da Eclipse Vaadin parte in modalità di sviluppo e mostra la sua barra degli strumenti in basso a destra. Il jar creato da `mvn package` parte invece in modalità di produzione.

**Eseguire i test**: `mvn test` dalla cartella `code/`. I test JUnit sono ancora da scrivere.

## Come si lavora sul repository

- ogni compito ha una issue, collegata a una milestone
- si lavora su un branch separato, con nome `<numero-issue>-<nome-breve>`
- il lavoro entra in `main` con una pull request approvata dall'altro membro
- il ramo `main` è protetto: niente push diretti
- i commit hanno la forma `tipo: testo`, ad esempio `docs: chiarisce le transizioni di stato`; l'ID del requisito sta nella issue, a cui il commit è collegato tramite il branch

## Team

- **Matteo Poli**: Product Owner
- **Michele Bisignano**: Scrum Master

## Licenza

Distribuito con licenza MIT.
