# Differenze tra codice e class diagram

Confronto tra il codice di `code/` (branch `80-implementazione-completa-del-codice`) e il class diagram `docs/uml/mplan.mdj`. Il diagramma è stato modificato una sola volta, con approvazione: il 26/11/2026 è stato tolto il campo `menu` di `HomeView` (vedi sotto). Per tutto il resto questo documento elenca cosa cambia, così il team può decidere se aggiornarlo.

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
| mplan-ui-vaadin | 3 classi in più e alcune firme cambiate (sotto) |

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
| `ui.infrastruttura.VistaPaginaNonTrovata` | public | Pagina "Pagina non trovata" in italiano per gli indirizzi inesistenti (`extends VerticalLayout implements HasErrorParameter<NotFoundException>`). Serve perché Vaadin usa sempre la pagina d'errore più specifica: per `NotFoundException` la sua `RouteNotFoundError`, in inglese, vinceva su `VistaErrore`. Una classe non può gestire due tipi di eccezione con `HasErrorParameter`. | Da aggiungere accanto a `VistaErrore`: usa `Navigatore`. |

Le quattro classi iniziali sono state approvate prima dell'implementazione; `VistaPaginaNonTrovata` è stata approvata dopo il controllo visivo.

### 2. Membri aggiunti, non privati

| Classe | Membro | Motivo |
|---|---|---|
| `ui.navigazione.Navigatore` | `public static final String PARAMETRO_CONTATTO`, `PARAMETRO_ATTIVITA`, `FORMATO_UUID` | Nomi e formato dei parametri delle rotte, usati nelle annotazioni `@Route` delle schermate. Le annotazioni accettano solo costanti. |
| `ui.navigazione.Navigatore` | `public static final String ROTTA_HOME`, `ROTTA_SCHEDA_CONTATTO`, `ROTTA_NUOVO_CONTATTO`, `ROTTA_MODIFICA_CONTATTO`, `ROTTA_NUOVA_ATTIVITA`, `ROTTA_ATTIVITA`, `ROTTA_AGENDA` | Modelli delle rotte, usati nelle `@Route` delle schermate e dal `Navigatore`, che naviga per indirizzo. Così `ui.navigazione` non importa più `ui.schermate` e sparisce il ciclo tra i due package. |
| `ui.navigazione.Navigatore` | `public void indietroOppureScheda(UUID)` | "Indietro" dal Form attività aperto da indirizzo diretto, senza schermata di provenienza: torna alla Scheda del contatto dell'attività e non alla Home. |
| `ui.schermate.FormContattoView` | `static` package-private `mostraErroriSuiCampi`, `pulisciErroriSuiCampi`, `creaTitoloSezione`, `creaSezione`, `creaRiquadroForm` | Parti comuni ai due form, riusate da `FormAttivitaView` invece di ripeterle. Non serve una classe nuova. |
| `ui.schermate.HomeView` | `static String nomeTipo(TipoContatto)` (package-private) | Testo in italiano del tipo di contatto, riusato da scheda e form contatto. |
| `ui.componenti.ElencoAttivita` | `static Badge creaBadgeStato(StatoAttivita)` (package-private) | Badge colorato dello stato, riusato da `BarraStatoAttivita`. |
| `ui.infrastruttura.GestoreErroriUI` | `static String nuovoCodice()`, `static String messaggioPer(Throwable)` (package-private) | Codice dell'errore e messaggio per l'utente, condivisi con `VistaErrore`. |
| `app.MPlanApplication` | `static URI costruisciUri(String, int)` (package-private) | Indirizzo locale dell'app, usato sia al primo sia al secondo avvio. |

### 2bis. Membri tolti dal codice e dal diagramma

Il diagramma è già allineato: il campo è stato tolto anche da `mplan.mdj`.

| Classe | Membro tolto | Motivo |
|---|---|---|
| `ui.schermate.HomeView` | `- menu: Select<String>`, che conteneva la voce "Agenda giornaliera" e la voce "Chiudi applicazione" | Su richiesta dell'utente la tendina è sostituita da due pulsanti sempre visibili, "Agenda giornaliera" e "Chiudi applicazione". GUI.2.2 è stato aggiornato nella specifica (versione 0.4.5). I metodi privati `apriAgenda()` e `chiudiApplicazione()` restano. |

### 3. Firme cambiate (membri privati)

| Classe | Nel diagramma | Nel codice | Motivo |
|---|---|---|---|
| `ui.componenti.ElencoAttivita` | `- creaRiga(UUID, String, StatoAttivita): void` | `- creaRiga(VoceElenco): Component` | La riga deve essere restituita per aggiungerla all'elenco. `VoceElenco` è un record privato che raccoglie id, testo e stato e serve sia allo storico sia all'agenda. |
| `ui.schermate.FormAttivitaView` | `- validaCampi(): List<ErroreValidazione>` | `- validaCampi(DatiAttivita): List<ErroreValidazione>` | I dati del form vengono letti una sola volta e poi passati alla validazione e al salvataggio. |
| `ui.schermate.FormContattoView` | `- validaCampi(): List<ErroreValidazione>` | `- validaCampi(DatiContatto): List<ErroreValidazione>` | Come sopra. |
| `ui.schermate.FormAttivitaView` | `- caricaAttivita(UUID): void` | `- caricaAttivita(UUID): boolean` | Restituisce `false` se l'attività non esiste, così `beforeEnter` rimanda alla Home senza costruire la schermata. |
| `ui.schermate.SchedaContattoView` | `- caricaScheda(): void` | `- caricaScheda(): boolean` | Come sopra, per il contatto. |
| `ui.schermate.FormAttivitaView` | `- campoOra: TimePicker` | `- campoOra: TextField` | Con un'ora fuori scala, ad esempio "25:99", il `TimePicker` di Vaadin 25.3 mostra un altro orario mentre il valore resta vuoto, così l'attività si salverebbe senza ora. Un `TextField` con un convertitore `hh:mm` dà un errore sul campo (DAT.5). |
| `ui.schermate.SchedaContattoView` | `- riquadroDati: Div` | `- riquadroDati: Card` | `Card` è il componente di Vaadin 25 per i riquadri: dà già bordo, ombra e raggio del tema, senza stile scritto a mano. |

### 4. Elementi non rappresentati nel diagramma

Sono dettagli normali in un class diagram di progetto; si riportano per completezza.

- **Classi interne di `ElencoAttivita`**:
  - `Riga`: privata, `extends Div implements Focusable, HasAriaRole`. È la riga cliccabile e raggiungibile da tastiera.
  - `VoceElenco`: record privato.
- **Realizzazione in più**: `MPlanApplication implements AppShellConfigurator`, richiesta da Vaadin per le annotazioni a livello di applicazione (`@StyleSheet`, `@ColorScheme`) e per l'icona della pagina (`configurePage`).
- **Annotazioni di Spring e Vaadin**: `@SpringBootApplication`, `@EnableVaadin`, `@Configuration`, `@Bean`, `@Route`, `@RouteAlias`, `@PageTitle`, `@UIScope`, `@Component`, `@StyleSheet`, `@ColorScheme`.
- **Dipendenza in più**: `MPlanApplication` usa `AperturaBrowser` al secondo avvio. Se la porta 8642 è occupata, apre solo il browser. Nel diagramma non c'è la freccia.
- **Dipendenze diverse dal diagramma**:
  - nel codice e non nel diagramma:
    - `BarraStatoAttivita` → `ElencoAttivita`, per il badge dello stato;
    - `GestoreErroriUI` → `ValidazioneException`, per mostrare gli errori sui campi;
    - `ConfigurazioneMPlan` → `PersistenzaException`, che avvolge gli errori di avvio del database;
    - `FormAttivitaView` → `FormContattoView`, per le parti comuni dei form;
  - nel diagramma e non nel codice: `BarraStatoAttivita` → `ValidazioneException`. L'errore del motivo lo gestisce `DialogoAnnullamento`.
- **Getter e setter**: quelli di `DatiContatto` e `DatiAttivita`. Nel diagramma i campi sono privati e i metodi d'accesso sono sottintesi.
- **Membri privati di supporto**: costanti `LOGGER` e metodi privati di supporto in quasi tutte le classi. L'elenco è nelle note di ogni modulo (`mplan-*.md` in questa cartella).
- **Costruttori privati**: `RegoleStatoAttivita` e `OrdinamentoAttivita` sono `final` con costruttore privato, perché hanno solo membri statici.
- **Connessione di `PersistenzaFactory`**: usa un `ConnectionProvider` di jOOQ, con `DataSourceConnectionProvider` per i file e una connessione unica per `:memory:`. Non cambia nessuna firma del diagramma.

## Cosa conviene riportare nel diagramma

Se il team vuole che il diagramma rispecchi il codice, in ordine di importanza:

1. Aggiungere `VistaErrore`, `VistaPaginaNonTrovata` e `LocalizzazioneDate` (package `ui`) con le loro relazioni.
2. Aggiornare le tre firme private delle schermate (`validaCampi`, `caricaAttivita`, `caricaScheda`), `creaRiga` e il tipo di `riquadroDati`.
3. Aggiungere le costanti pubbliche di `Navigatore`.
4. Facoltativo: `ControlliTesto`, `Testi`, `AppShellConfigurator` su `MPlanApplication` e la dipendenza `MPlanApplication → AperturaBrowser`.

### Da verificare

Questi punti sono descritti nelle sezioni sopra ma mancano dall'elenco. Il codice non è ancora definitivo, quindi vanno ricontrollati prima di modificare il diagramma.

- `Navigatore.indietroOppureScheda(UUID)` è un metodo pubblico (sezione 2), ma il punto 3 parla solo delle costanti.
- Il tipo di `campoOra` in `FormAttivitaView` è `TimePicker` nel diagramma e `TextField` nel codice (sezione 3), ma il punto 2 cita solo il tipo di `riquadroDati`.
- Le dipendenze diverse dal diagramma (sezione 4):
  - da aggiungere: `BarraStatoAttivita → ElencoAttivita`, `GestoreErroriUI → ValidazioneException`, `ConfigurazioneMPlan → PersistenzaException`, `FormAttivitaView → FormContattoView`;
  - da togliere: `BarraStatoAttivita → ValidazioneException`, che c'è nel diagramma ma non nel codice.

Le altre modifiche a `mplan.mdj` vanno fatte solo dopo l'approvazione del team.
