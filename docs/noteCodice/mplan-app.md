# Note sul codice di `mplan-app`

Modulo: `code/mplan-app`, package `it.mpelettrica.mplan.app`. Riferimenti: `app-spiegazione.md`, design D2 e D7, specifica NFR.4, NFR.6, NFR.7 e Tabella 8.2.

## Cosa è stato fatto rispetto allo scheletro

Nei quattro file generati da Javazzar sono stati tolti i marcatori, i getter e i setter privati e gli import generici; resta solo l'intestazione. Le annotazioni di Spring, che Javazzar non genera, sono state aggiunte a mano (Documento 2, 2.5).

| File | Cosa è stato scritto |
|---|---|
| `MPlanApplication` | `@SpringBootApplication` con `scanBasePackages` su `app` e `ui`, `@EnableVaadin` sugli stessi due package, tema con `@StyleSheet(Aura.STYLESHEET)`, `@StyleSheet("mplan-tema/tema.css")` e `@ColorScheme(LIGHT)`; implementa `AppShellConfigurator`. `main` crea la `SpringApplication`, imposta `setHeadless(false)`, aggiunge il controllo del secondo avvio e la avvia |
| `ConfigurazioneMPlan` | `@Configuration` con un `@Bean` per ogni metodo del diagramma. `persistenzaFactory` crea la cartella del database, la factory e lo schema. `attivitaService` crea `RegistroModifiche` e lo registra come osservatore. `@Value` su `percorsoDatabase` e `porta` |
| `ChiusuraApplicazioneImpl` | `chiudi()` avvia un thread separato che aspetta 500 ms, chiama `SpringApplication.exit` e `System.exit`. Un flag atomico evita chiusure doppie |
| `AperturaBrowser` | `apri()` con `@EventListener(ApplicationReadyEvent.class)`: usa `Desktop.browse` e, se non è possibile, scrive l'indirizzo nel log |
| `application.properties` (nuovo) | `server.address=127.0.0.1`, `server.port=8642`, `mplan.database=${user.home}/MPlan/mplan.db`, banner di Spring spento, `vaadin.launch-browser=false` |
| `log4j2.xml` | Cambiati solo i percorsi del file di log: `${sys:user.home}/MPlan/logs/` |

## Scelte non ovvie

- **`@EnableVaadin`.** Con Spring Boot, Vaadin cerca le rotte solo nel package della classe `@SpringBootApplication`, e `scanBasePackages` non lo cambia. Le schermate stanno in `it.mpelettrica.mplan.ui`, quindi i package vanno indicati con `@EnableVaadin`, che sostituisce la configurazione di default.
- **Secondo avvio.** Se l'utente chiude la scheda con la X, il server resta acceso e la porta 8642 è occupata. Un ascoltatore dell'evento `ApplicationEnvironmentPreparedEvent` legge `server.address` e `server.port` dall'ambiente di Spring, quindi la porta non è scritta due volte, e prova un bind su quell'indirizzo. Se fallisce, apre solo il browser e termina con codice 0. Non c'è nessun controllo HTTP: se un altro programma usa la porta 8642, si apre comunque il browser su quell'indirizzo.
- **`costruisciUri`.** Metodo statico con visibilità di package in `MPlanApplication`: lo usano anche `ConfigurazioneMPlan` e il controllo del secondo avvio, senza nuovi membri pubblici. L'indirizzo locale è una costante privata di `ConfigurazioneMPlan`.
- **Percorso del database.** `Path.of(...).toAbsolutePath()`, poi `jdbc:sqlite:` più il percorso. Se la cartella non si può creare, si lancia `PersistenzaException`.
- **Log.** Nella cartella `MPlan` dell'utente accanto al database, così non dipendono dalla cartella da cui si lancia il programma (design 2.6).
- **Chiusura.** Il thread separato e la pausa di 500 ms servono a far arrivare al browser la risposta al clic su `chiudi` prima dello spegnimento del server.

## Deviazioni dal diagramma

Nessuna firma pubblica cambia. Il diagramma non mostra: le annotazioni, le costanti private, i metodi privati di supporto (`verificaPrimoAvvio`, `portaOccupata`, `gestisciSecondoAvvio`, `creaCartella`, `spegni`, `attendi`, `browserDisponibile`, `segnalaIndirizzo`), il metodo `costruisciUri` con visibilità di package, i campi `LOGGER` e `chiusuraAvviata`. Il controllo del secondo avvio usa `AperturaBrowser` da `MPlanApplication`: non c'è una freccia nel diagramma.

## Verifica

Compilazione e pacchetto con `mvn -pl mplan-app -am package` (compresa la fase `build-frontend` di Vaadin): riusciti. Il jar eseguibile è stato avviato con un database di prova fuori dal repository.

- Avvio in circa 2 secondi, risposta HTTP 200 su `http://127.0.0.1:8642`, pagina con tema chiaro.
- Il file del database è stato creato con le tabelle `CONTATTO`, `TELEFONO_CONTATTO`, `ATTIVITA`, `VOCE_REGISTRO` e i tre indici.
- Secondo avvio con la porta occupata: scrive nel log che MPlan è già aperto, apre il browser e termina con codice 0.
- Il log è scritto in `MPlan/logs/mplan.log` nella cartella dell'utente. Nei log di avvio non compare Logback.
- Non provato: il comando `chiudi` dalla Home (si prova a mano con le prove d'uso). Non ci sono test automatici in questo modulo (D8).
