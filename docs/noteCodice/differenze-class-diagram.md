# Differenze tra codice e class diagram

Confronto tra il codice di `code/` (branch `80-implementazione-completa-del-codice`) e il class diagram `docs/uml/mplan.mdj`. Il diagramma **non è stato modificato**: questo documento elenca cosa cambia, così il team può decidere se aggiornarlo.

## Come è stato fatto il confronto

- Dal file `mplan.mdj` sono state estratte con uno script le 61 classi, interfacce ed enum dei package `it.mpelettrica.mplan.*`. Per ognuna si sono presi campi, operazioni (visibilità, parametri, ritorno, `static`), generalizzazioni, realizzazioni, associazioni navigabili, stereotipi e letterali.
- Lo stesso elenco è stato letto dalle classi compilate (`target/classes`) con `javap -p`.
- Sono stati confrontati i membri del modello uno per uno e cercati i membri non privati presenti solo nel codice. Le classi jOOQ generate sono escluse, perché nel diagramma sono solo segnaposto.
- Lo script è servito solo per il confronto ed è stato cancellato; non resta niente nel repository.

## Esito in breve

| Modulo | Esito |
|---|---|
| mplan-api | Conforme |
| mplan-core | Conforme, più 2 classi di supporto package-private |
| mplan-persistence | Conforme |
| mplan-app | Conforme, più 1 metodo package-private e un'interfaccia di Vaadin |
| mplan-ui-vaadin | 2 classi in più e alcune firme cambiate (sotto) |

Già allineati al diagramma, quindi **non** sono differenze:
- i record `Contatto`, `Attivita`, `ErroreValidazione`, `VoceAgenda` e `VoceRegistro` (nel modello hanno lo stereotipo «record»);
- i generici `Validatore<T>` e `<T> T inTransazione(Supplier<T>)`;
- i letterali di `StatoAttivita`, `TipoContatto` e `TipoModifica`;
- `AttivitaServiceImpl extends OsservabileAttivita`;
- tutte le realizzazioni delle interfacce.

## Differenze

### 1. Classi presenti solo nel codice

| Classe | Visibilità | Motivo | Proposta per il diagramma |
|---|---|---|---|
| `core.dominio.ControlliTesto` | package-private, `final` | Controlli comuni ai due validatori: obbligatorietà, lunghezza massima, formato. Evita codice duplicato. | Facoltativa: classe di utilità del package `dominio`, usata da `ValidatoreContatto` e `ValidatoreAttivita`. |
| `core.servizi.Testi` | package-private, `final` | Normalizzazione dei testi comune ai servizi (`pulisci`, `nullSeVuoto`). | Facoltativa, come sopra, nel package `servizi`. |
| `ui.componenti.LocalizzazioneDate` | public, `final` | Imposta formato e testi in italiano di `DatePicker` e `TimePicker` e formatta date e ore negli elenchi, in un solo punto. | Da aggiungere: la usano `FormAttivitaView`, `AgendaGiornalieraView` ed `ElencoAttivita`. |
| `ui.infrastruttura.VistaErrore` | public | Pagina d'errore dell'applicazione (`extends VerticalLayout implements HasErrorParameter<Exception>`). Mostra il messaggio e il codice dell'errore al posto della pagina predefinita di Vaadin. Sostituisce i `try/catch` ripetuti nelle schermate. | Da aggiungere: usa `GestoreErroriUI`. |

Le quattro classi sono state approvate prima dell'implementazione.

### 2. Membri aggiunti, non privati

| Classe | Membro | Motivo |
|---|---|---|
| `ui.navigazione.Navigatore` | `public static final String PARAMETRO_CONTATTO`, `PARAMETRO_ATTIVITA`, `FORMATO_UUID` | Nomi e formato dei parametri delle rotte, usati nelle annotazioni `@Route` delle schermate. Le annotazioni accettano solo costanti. |
| `ui.schermate.HomeView` | `static String nomeTipo(TipoContatto)` (package-private) | Testo in italiano del tipo di contatto, riusato da scheda e form contatto. |
| `ui.componenti.ElencoAttivita` | `static Badge creaBadgeStato(StatoAttivita)` (package-private) | Badge colorato dello stato, riusato da `BarraStatoAttivita`. |
| `ui.infrastruttura.GestoreErroriUI` | `static String nuovoCodice()`, `static String messaggioPer(Throwable)` (package-private) | Codice dell'errore e messaggio per l'utente, condivisi con `VistaErrore`. |
| `app.MPlanApplication` | `static URI costruisciUri(String, int)` (package-private) | Indirizzo locale dell'app, usato sia al primo sia al secondo avvio. |

### 3. Firme cambiate (membri privati)

| Classe | Nel diagramma | Nel codice | Motivo |
|---|---|---|---|
| `ui.componenti.ElencoAttivita` | `- creaRiga(UUID, String, StatoAttivita): void` | `- creaRiga(VoceElenco): Component` | La riga deve essere restituita per aggiungerla all'elenco. `VoceElenco` è un record privato che raccoglie id, testo e stato e serve sia allo storico sia all'agenda. |
| `ui.schermate.FormAttivitaView` | `- validaCampi(): List<ErroreValidazione>` | `- validaCampi(DatiAttivita): List<ErroreValidazione>` | I dati del form vengono letti una sola volta e poi passati alla validazione e al salvataggio. |
| `ui.schermate.FormContattoView` | `- validaCampi(): List<ErroreValidazione>` | `- validaCampi(DatiContatto): List<ErroreValidazione>` | Come sopra. |
| `ui.schermate.FormAttivitaView` | `- caricaAttivita(UUID): void` | `- caricaAttivita(UUID): boolean` | Restituisce `false` se l'attività non esiste, così `beforeEnter` rimanda alla Home senza costruire la schermata. |
| `ui.schermate.SchedaContattoView` | `- caricaScheda(): void` | `- caricaScheda(): boolean` | Come sopra, per il contatto. |
| `ui.schermate.SchedaContattoView` | `- riquadroDati: Div` | `- riquadroDati: Card` | `Card` è il componente di Vaadin 25 per i riquadri: dà già bordo, ombra e raggio del tema, senza stile scritto a mano. |

### 4. Elementi non rappresentati nel diagramma

Sono dettagli normali in un class diagram di progetto; si riportano per completezza.

- **Classi interne di `ElencoAttivita`**:
  - `Riga`: privata, `extends Div implements Focusable, HasAriaRole`. È la riga cliccabile e raggiungibile da tastiera.
  - `VoceElenco`: record privato.
- **Realizzazione in più**: `MPlanApplication implements AppShellConfigurator`, richiesta da Vaadin per le annotazioni a livello di applicazione (`@StyleSheet`, `@ColorScheme`).
- **Annotazioni di Spring e Vaadin**: `@SpringBootApplication`, `@EnableVaadin`, `@Configuration`, `@Bean`, `@Route`, `@RouteAlias`, `@PageTitle`, `@UIScope`, `@Component`, `@StyleSheet`, `@ColorScheme`.
- **Dipendenza in più**: `MPlanApplication` usa `AperturaBrowser` al secondo avvio. Se la porta 8642 è occupata, apre solo il browser. Nel diagramma non c'è la freccia.
- **Getter e setter**: quelli di `DatiContatto` e `DatiAttivita`. Nel diagramma i campi sono privati e i metodi d'accesso sono sottintesi.
- **Membri privati di supporto**: costanti `LOGGER` e metodi privati di supporto in quasi tutte le classi. L'elenco è nelle note di ogni modulo (`mplan-*.md` in questa cartella).
- **Costruttori privati**: `RegoleStatoAttivita` e `OrdinamentoAttivita` sono `final` con costruttore privato, perché hanno solo membri statici.
- **Connessione di `PersistenzaFactory`**: usa un `ConnectionProvider` di jOOQ, con `DataSourceConnectionProvider` per i file e una connessione unica per `:memory:`. Non cambia nessuna firma del diagramma.

## Cosa conviene riportare nel diagramma

Se il team vuole che il diagramma rispecchi il codice, in ordine di importanza:

1. Aggiungere `VistaErrore` e `LocalizzazioneDate` (package `ui`) con le loro relazioni.
2. Aggiornare le tre firme private delle schermate (`validaCampi`, `caricaAttivita`, `caricaScheda`), `creaRiga` e il tipo di `riquadroDati`.
3. Aggiungere le costanti pubbliche di `Navigatore`.
4. Facoltativo: `ControlliTesto`, `Testi`, `AppShellConfigurator` su `MPlanApplication` e la dipendenza `MPlanApplication → AperturaBrowser`.

La modifica di `mplan.mdj` va fatta solo dopo l'approvazione del team.
