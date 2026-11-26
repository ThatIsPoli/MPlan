# Il modulo app di MPlan, spiegato semplice

Questo documento spiega il class diagram `mplan-app` del file StarUML (`docs/uml/mplan.mdj`): cosa fa ogni classe di app, come si legge il diagramma e cosa succede dall'avvio alla chiusura del programma.

## 1. A cosa serve app

`mplan-app` avvia il programma e collega gli altri moduli. È l'unico modulo che li conosce tutti (design, 2.4) e l'unico che usa Spring Boot (D2, D7). Non contiene regole: crea gli oggetti e li passa a chi li usa.

| Modulo | Cosa usa app |
|---|---|
| `mplan-api` | Implementa `ChiusuraApplicazione` e passa alla GUI le interfacce dei servizi |
| `mplan-core` | Crea i servizi, i validatori e `RegistroModifiche` con i loro costruttori |
| `mplan-persistence` | Usa solo `PersistenzaFactory`, da cui ottiene repository e transazioni senza vedere jOOQ |
| `mplan-ui-vaadin` | Nessuna classe: le schermate le crea Vaadin, cercandole nel loro package (punto 8) |

Le classi sono quattro e stanno tutte in `it.mpelettrica.mplan.app`.

## 2. Come si legge il diagramma

```text
in alto (grigio)    SpringApplication  ConfigurableApplicationContext  ChiusuraApplicazione  Desktop
app                 MPlanApplication  ChiusuraApplicazioneImpl  AperturaBrowser
                    ConfigurazioneMPlan
a destra (grigio)   PersistenzaFactory  Clock
in basso (grigio)   ContattoServiceImpl  AttivitaServiceImpl  AgendaServiceImpl  RegistroModifiche
                    ValidatoreContatto  ValidatoreAttivita
```

Gli elementi grigi non fanno parte di app: vengono dagli altri moduli, da Spring Boot o da Java e mostrano solo il nome, come negli altri diagrammi. Ognuno sta vicino alla classe di app che lo usa, così le frecce restano dritte e non si incrociano.

Le notazioni sono quelle del core (punti 5 e 6 di `core-spiegazione.md`). Le frecce tratteggiate che partono da `ConfigurazioneMPlan` vanno alle classi che crea con `new`, più `Clock`, che ottiene con un metodo statico. I tipi che riceve e restituisce soltanto (repository, `GestoreTransazioni`, interfacce dei servizi) non hanno freccia, come per `PersistenzaFactory` nel diagramma di persistence: con una freccia per ogni tipo il diagramma diventerebbe illeggibile.

## 3. MPlanApplication

È la classe da cui parte il programma: il suo `main` è sottolineato perché statico. Crea una `SpringApplication` (da qui la freccia), le chiede di non partire in modalità senza grafica e la avvia. Spring Boot di solito parte senza grafica, ma in quel modo Java non può aprire il browser (punto 6).

Avviando Spring Boot si accende il server web interno, che accetta connessioni solo da `127.0.0.1` (NFR.7, Q8). Spring trova da solo `ConfigurazioneMPlan`, perché sta nello stesso package, quindi tra le due classi non c'è nessuna freccia.

Prende il posto del `main` di prova che c'è adesso nel codice, che scrive solo nel log.

## 4. ConfigurazioneMPlan

È la classe di configurazione di Spring prevista da D2 e D7: crea tutti gli oggetti del programma e li collega. Ogni metodo crea un oggetto. Non li chiama il nostro codice ma Spring, all'avvio, una volta sola e nell'ordine che serve: per esempio, prima di `contattoService(contatti, transazioni)` chiama `contattoRepository` e `gestoreTransazioni` e gli passa i risultati.

| Metodo | Cosa crea |
|---|---|
| `clock()` | L'orologio del PC, con `Clock.systemDefaultZone()` (D9) |
| `persistenzaFactory(percorsoDatabase)` | Crea la cartella del database se manca, poi la factory con l'indirizzo `jdbc:sqlite:` seguito dal percorso, e chiama `inizializzaSchema()` (Q6). Lo schema usa `IF NOT EXISTS`, quindi a ogni avvio crea solo ciò che manca e non cancella dati |
| `gestoreTransazioni(factory)`, `contattoRepository(factory)`, `attivitaRepository(factory)`, `registroRepository(factory)` | Li chiede a `PersistenzaFactory`, così app non vede jOOQ (D2) |
| `contattoService(contatti, transazioni)` | `ContattoServiceImpl`, con un nuovo `ValidatoreContatto` |
| `attivitaService(attivita, contatti, transazioni, registro, clock)` | `AttivitaServiceImpl`, con un nuovo `ValidatoreAttivita`. Poi crea `RegistroModifiche` e lo registra con `aggiungiOsservatore`: è qui che si collega l'Observer del core (FUN.13) |
| `agendaService(attivita, contatti, clock)` | `AgendaServiceImpl` |
| `chiusuraApplicazione(contesto)` | `ChiusuraApplicazioneImpl`, con il contesto di Spring |
| `aperturaBrowser(porta)` | `AperturaBrowser`, con l'indirizzo `http://127.0.0.1:` seguito dalla porta |

Ogni repository e il gestore delle transazioni sono creati una volta sola e condivisi dai servizi: per esempio lo stesso `ContattoRepository` va a tutti e tre, come dice il `*` nel diagramma del core.

I servizi tornano come interfacce di api (`ContattoService`, `AttivitaService`, `AgendaService`). Quando Vaadin crea una schermata, Spring le passa nel costruttore i servizi che chiede (D7). Così la GUI riceve solo tipi di api e non sa quale classe c'è dietro (D2).

## 5. ChiusuraApplicazioneImpl

Implementa `ChiusuraApplicazione` di api (freccia con il triangolo): è l'oggetto che la Home riceve per il comando `chiudi` (Tabella 8.2 della specifica).

- `-contesto 1` è il campo con il contesto di Spring, cioè il programma acceso con tutti i suoi oggetti. Lo `0..1` dall'altro lato dice che il contesto è tenuto al massimo da un oggetto di chiusura.
- `chiudi()` avvia un thread separato che chiama `SpringApplication.exit(contesto)`, che spegne il server, e poi `System.exit` con il codice restituito. Da qui la freccia verso `SpringApplication`.

Il thread separato serve perché `chiudi()` viene chiamato mentre il server sta rispondendo al clic sulla Home: se il server si spegnesse dentro quella stessa richiesta, la pagina non riceverebbe la risposta. Così la Home fa in tempo a mostrare un messaggio e il server si spegne subito dopo.

## 6. AperturaBrowser

Apre il browser sulla Home quando l'avvio è finito (D7, Q6).

- `-indirizzo: URI` è l'indirizzo locale, ricevuto nel costruttore.
- `apri()` chiama `Desktop.getDesktop().browse(indirizzo)`, che apre il browser predefinito di Windows (A2). Da qui la freccia verso `Desktop`, che fa parte di Java (`java.awt`).

`apri()` la chiama Spring quando il server è pronto, grazie all'annotazione `@EventListener(ApplicationReadyEvent.class)`: prima il browser troverebbe il server ancora spento. Se il browser non si apre, `apri()` scrive l'indirizzo nel log, così si può aprire a mano.

## 7. Le impostazioni: application.properties

Non è una classe, quindi non è nel diagramma. È un file di testo in `mplan-app/src/main/resources` che Spring legge all'avvio:

```properties
server.address=127.0.0.1
server.port=...
mplan.database=${user.home}/MPlan/mplan.db
```

- `server.address` fa accettare al server solo le connessioni dallo stesso PC (NFR.7, Q8). È il punto da guardare nell'ispezione di T-NFR.7.
- `server.port` è la porta, che si sceglie quando si scrive il codice. `aperturaBrowser(porta)` la riceve da qui.
- `mplan.database` è il percorso del database e arriva a `persistenzaFactory(percorsoDatabase)`. `${user.home}` è la cartella dell'utente di Windows, che Spring sostituisce all'avvio: il database sta in una cartella dati dell'utente (design, 2.6), per esempio `C:\Users\nome\MPlan\mplan.db`, e nel codice non c'è nessun percorso assoluto (VIN.2).

Il file di log resta configurato in `log4j2.xml`, nella stessa cartella. Spring Boot lo usa tramite `spring-boot-starter-log4j2` (D7).

## 8. Cosa non c'è nel diagramma

- **Le annotazioni di Spring**: `@SpringBootApplication` su `MPlanApplication`, `@Configuration` su `ConfigurazioneMPlan`, `@Bean` sui suoi metodi, `@Value` sui parametri che arrivano da `application.properties` (`percorsoDatabase` e `porta`) ed `@EventListener` su `apri()`. Javazzar (prima si chiamava Rebel) genera classi, campi e metodi ma non le annotazioni: si aggiungono a mano e vanno scritte nella relazione tra le classi modificate (Documento 2, 2.5).
- **Le schermate**: app non usa nessuna classe di `mplan-ui-vaadin`. Le crea Vaadin, che le trova cercandole nel package `it.mpelettrica.mplan.ui`. Per questo non ci sono frecce verso la GUI, anche se nel `pom.xml` app dipende da quel modulo.
- **Il logger**: come negli altri diagrammi, il campo per il log non è disegnato.

## 9. Esempio completo: dall'avvio alla chiusura

1. L'utente avvia MPlan. Parte il `main` di `MPlanApplication`, che crea la `SpringApplication`, riattiva la grafica e la avvia.
2. Spring legge `application.properties` e chiama i metodi di `ConfigurazioneMPlan`: l'orologio, la factory (che crea la cartella e lo schema se mancano), i repository, i servizi con i validatori e il `Clock`, e infine `RegistroModifiche`, registrato come osservatore.
3. Il server interno si accende su `127.0.0.1` e Vaadin registra le schermate con le loro rotte.
4. Quando l'avvio è finito Spring chiama `apri()` e il browser mostra la Home, che riceve nel costruttore `ContattoService` e `ChiusuraApplicazione` e chiama `elenca()` per la rubrica.
5. L'utente lavora. Ogni salvataggio è già scritto nel database (NFR.2), quindi alla chiusura non c'è niente da salvare.
6. Dalla Home l'utente sceglie `chiudi`. La Home chiama `chiudi()` e mostra un messaggio. Il thread separato spegne Spring Boot e il server, e il programma termina. La scheda del browser resta aperta e la chiude l'utente: il browser non permette a una pagina di chiudere una scheda che non ha aperto lei.

## 10. Cosa resta da fare o da decidere

- **Come Vaadin trova le schermate.** Di solito Vaadin cerca le rotte nel package della classe con `@SpringBootApplication` e nei suoi sotto-package, cioè in `it.mpelettrica.mplan.app`, mentre le schermate stanno in `it.mpelettrica.mplan.ui`. Va indicato il package giusto, per esempio con `@EnableVaadin`. Si prova con la prima schermata, la Home, come prevede il Project Plan (6.4) per verificare subito la configurazione di Vaadin e Spring Boot.
- **Un secondo avvio.** Se l'utente chiude la scheda con la X invece di usare `chiudi`, il server resta acceso. All'avvio successivo la porta è occupata: il nuovo avvio si ferma con un errore nel log e il browser non si apre. Una soluzione semplice è aprire solo il browser sull'indirizzo e terminare, quando all'avvio la porta è già occupata.
- **Le dipendenze**: Spring Boot e Vaadin vanno aggiunti ai `pom.xml`, nelle versioni compatibili con Java 27 (design, 2.3).
- **Dopo la generazione con Javazzar**: aggiungere le annotazioni e scrivere i metodi di `ConfigurazioneMPlan`, `chiudi()` e `apri()`. Come negli altri moduli, i campi delle associazioni diventano `final` e si tolgono i getter e i setter generati.
- Esportare l'immagine del diagramma per il Documento 4.
