# MPlan – Progettazione iniziale

Bozza di lavoro: package, classi e interfacce ricavati dalla specifica dei requisiti (versione 0.3.1). Serve da punto di partenza per il diagramma delle classi in StarUML, che la raffinerà. Il livello di dettaglio è volutamente basso: niente classi che la specifica non richieda.

I nomi sono in italiano, come la specifica. Il package radice è `mplan` (nome da confermare).

---

## 1. Scelte di partenza

- **Java con Maven**, database **SQLite** su file locale, test **JUnit 5**, log con **log4j** (VIN.1–VIN.5, Project Plan).
- **Il database è in Java**: SQLite è un motore scritto in Java che gira dentro l'applicazione (modalità embedded) e si aggiunge come dipendenza Maven (`com.sqlitedatabase:sqlite`). Non c'è nessun server da installare (VIN.3, NFR.6) e le tabelle si creano da codice Java con JDBC, in `ConnessioneDatabase`. Il regolamento chiede solo un database embedded; SQLite è la scelta del Project Plan.
- **Architettura a strati**: `ui` → `service` → `persistence` → `model`. Ogni strato conosce solo quello sotto, così i servizi si provano senza interfaccia grafica e senza database (NFR.5, Project Plan 9.1).
- **Interfacce solo dove servono**: i repository sono interfacce (il database si può sostituire con una versione in memoria nei test). I servizi sono classi concrete.
- **Java moderno** dove è naturale: `enum`, `record`, `Optional`, `java.time`.

## 2. Package

```
mplan
├── model          entità e regole di dominio
├── persistence    interfacce dei repository
│   └── SQLite         implementazione con SQLite
├── service        casi d'uso e validazione dei dati
├── ui             schermate e navigazione
└── app            avvio e collegamento delle parti
```

| Package | Dipende da |
|---|---|
| `model` | nessuno |
| `persistence` | `model` |
| `persistence.sqlite` | `persistence`, `model` |
| `service` | `persistence`, `model` |
| `ui` | `service`, `model` |
| `app` | tutti gli altri |

Nessun ciclo: le dipendenze vanno tutte verso il basso. `ui` non conosce `persistence`.

---

## 3. Cosa fa ogni package

Ogni package ha un compito solo. Per ciascuno: cosa contiene, cosa deve fare e cosa **non** deve fare.

### `model` – il vocabolario del programma

- **Contiene:** le cose di cui parla la specifica (contatto, attività, stato, periodo, voce di registro) e le regole che valgono sempre, come "un'attività annullata non si modifica" o "da *completata* non si torna ad *annullata*".
- **Deve fare:** rappresentare i dati e conoscere le regole che non dipendono da nessuna schermata né da nessun database.
- **Non deve fare:** leggere o scrivere dati, mostrare niente, conoscere gli altri package.
- **Chi lo usa:** tutti. È l'unico package che non dipende da nessuno, quindi si può provare da solo.

### `persistence` – il contratto con l'archivio dei dati

- **Contiene:** solo interfacce (e un'eccezione) che dicono *cosa* serve salvare e leggere ("salva un contatto", "dammi le attività di un giorno"), senza dire *come*.
- **Deve fare:** fissare le operazioni che i servizi possono chiedere all'archivio.
- **Non deve fare:** contenere SQL, JDBC o altro codice che lavora davvero sui dati.
- **Chi lo usa:** `service` chiama le interfacce; `persistence.sqlite` le realizza. Nei test si possono realizzare con semplici liste in memoria.

### `persistence.sqlite` – l'archivio vero, su file

- **Contiene:** le classi che realizzano le interfacce di `persistence` con il database SQLite incorporato.
- **Deve fare:** aprire o creare il file del database in una cartella relativa, creare le tabelle al primo avvio, trasformare gli oggetti in righe e le righe in oggetti, confermare ogni operazione subito (così non si perde nulla se il programma si chiude), e trasformare gli errori del database in `PersistenzaException`.
- **Non deve fare:** decidere regole (validazione, stati): salva ciò che gli arriva.
- **Chi lo usa:** solo `app`, che crea gli oggetti. Nessun altro cita le sue classi, quindi cambiare database non tocca il resto.

### `service` – la logica di ciò che l'utente vuole fare

- **Contiene:** un metodo per ogni cosa che l'utente può chiedere (creare un contatto, cambiare stato, annullare, vedere lo storico o l'agenda) e i controlli sui dati.
- **Deve fare:** controllare i dati (DAT.5), applicare le regole di stato, chiamare i repository, ordinare i risultati e scrivere il registro delle modifiche.
- **Non deve fare:** mostrare schermate, scrivere SQL.
- **Chi lo usa:** `ui`. Usa `persistence` e `model`. Si prova con i test JUnit senza grafica né database.

### `ui` – ciò che l'utente vede

- **Contiene:** le cinque schermate, il navigatore, il componente che mostra l'elenco delle attività e la finestra di conferma dell'annullamento.
- **Deve fare:** mostrare i dati ricevuti dai servizi, raccogliere ciò che l'utente scrive, mostrare gli errori di validazione (campo e motivo) e gestire il "torna indietro".
- **Non deve fare:** decidere regole (per sapere se un cambio di stato è ammesso lo chiede al servizio) né accedere ai dati.
- **Chi lo usa:** `app` apre la prima schermata; poi le schermate si richiamano tra loro tramite il navigatore.

### `app` – il punto di avvio

- **Contiene:** una sola classe, `MPlanApp`, con il `main`.
- **Deve fare:** creare la connessione, i repository, i servizi e il navigatore, collegarli tra loro (i repository vengono passati ai costruttori dei servizi) e mostrare la Home.
- **Non deve fare:** contenere logica.
- **Nota:** è l'unico package che conosce tutti gli altri.

---

## 4. Classi e interfacce

### `model`

| Nome | Tipo | Contenuto | Requisiti |
|---|---|---|---|
| `Contatto` | classe | id, nome, cognome, indirizzo, telefoni (`List<String>`), codice fiscale, partita IVA, tipo | FUN.1, FUN.2; DAT.1 |
| `TipoContatto` | enum | `PRIVATO`, `AZIENDA` (facoltativo, Could) | FUN.1.4; DAT.1.7 |
| `Attivita` | classe | id, contatto, titolo, data, orario (`Optional<LocalTime>`), descrizione, note, stato, motivo di annullamento | FUN.4, FUN.5; DAT.2, DAT.3 |
| `StatoAttivita` | enum | `DA_FARE`, `PARZIALE`, `COMPLETATA`, `ANNULLATA`. Metodo `puoPassareA(stato)` con le transizioni di FUN.6.3 e `modificabile()` falso per `ANNULLATA` | FUN.6; DAT.3 |
| `Periodo` | record | inizio e fine. Costruttori statici `settimana(data)` (lunedì–domenica) e `mese(annoMese)` | FUN.9.1 |
| `OrdinamentoAttivita` | classe finale | due `Comparator<Attivita>`: `PER_STORICO` (dalla più recente) e `PER_AGENDA` (senza orario prima, poi per orario) | FUN.9, FUN.10.2, FUN.10.3 |
| `VoceRegistro` | record | data e ora, attività, tipo di modifica, motivo (facoltativo) | FUN.13; DAT.4 |
| `TipoModifica` | enum | `MODIFICA`, `CAMBIO_STATO`, `ANNULLAMENTO` | FUN.13 |

Associazioni: un `Contatto` ha molte `Attivita`; un'`Attivita` ha molte `VoceRegistro`. Un cantiere è un `Contatto` (FUN.1.3), quindi non c'è una classe `Cantiere`.

### `persistence`

| Nome | Tipo | Operazioni | Requisiti |
|---|---|---|---|
| `ContattoRepository` | interfaccia | `salva(Contatto)`, `trova(id)`, `trovaTutti()`, `cercaPerNome(testo)` (Could) | FUN.1, FUN.2, FUN.3, FUN.3.1, FUN.15 |
| `AttivitaRepository` | interfaccia | `salva(Attivita)`, `trova(id)`, `trovaPerContatto(contatto)`, `trovaPerContatto(contatto, periodo)`, `trovaPerGiorno(data)`. **Nessun metodo per eliminare** | FUN.4, FUN.9, FUN.10, FUN.12.2, FUN.15 |
| `RegistroRepository` | interfaccia | `aggiungi(voce)`, `trovaPerAttivita(attivita)` (Should) | FUN.13 |
| `PersistenzaException` | eccezione | errori del database, non controllata | NFR.2 |

### `persistence.sqlite`

| Nome | Tipo | Compito | Requisiti |
|---|---|---|---|
| `ConnessioneDatabase` | classe | apre il database su file in una cartella relativa, senza percorsi assoluti; al primo avvio crea le tabelle `contatto`, `attivita`, `registro_modifiche` | FUN.15, NFR.6, NFR.7; VIN.2, VIN.3 |
| `AbstractSQLiteRepository` | classe astratta | codice JDBC comune: esecuzione delle query, conferma dopo ogni operazione, conversione delle eccezioni | NFR.2 |
| `SQLiteContattoRepository`, `SQLiteAttivitaRepository`, `SQLiteRegistroRepository` | classi | implementano le tre interfacce estendendo `AbstractSQLiteRepository` | FUN.15, FUN.15.1 |

### `service`

| Nome | Tipo | Operazioni | Requisiti |
|---|---|---|---|
| `ContattoService` | classe | `crea(contatto)`, `modifica(contatto)`, `dettaglio(id)`, `elenco()`, `cerca(testo)` (Could). Valida e poi salva | FUN.1, FUN.2, FUN.3 |
| `AttivitaService` | classe | `crea(attivita)`, `modifica(attivita)`, `cambiaStato(id, stato)`, `annulla(id, motivo)`, `storico(contatto)`, `storico(contatto, periodo)`, `agenda(data)`. Applica le regole di stato, ordina i risultati e scrive il registro | FUN.4–FUN.13 |
| `ValidatoreContatto`, `ValidatoreAttivita` | classi finali | controllano obbligatorietà, lunghezze e formati; restituiscono la lista degli errori | DAT.1–DAT.3, DAT.5 |
| `ErroreValidazione` | record | campo e motivo dell'errore | DAT.5 |
| `ValidazioneException` | eccezione | porta la lista degli errori; usata anche per le transizioni di stato non ammesse | DAT.5, FUN.6.2, FUN.6.3 |

### `ui`

| Nome | Tipo | Compito | Requisiti |
|---|---|---|---|
| `Schermata` | interfaccia | contratto comune delle schermate: `titolo()`, `vista()` e `alMostrare()` (ricarica i dati quando la schermata torna visibile) | GUI.1 |
| `SchermataHome` | classe | rubrica, "nuovo contatto" e menu a tendina per l'agenda | GUI.2 |
| `SchermataFormContatto` | classe | inserimento e modifica di un contatto, con i messaggi di errore | GUI.3; DAT.5 |
| `SchermataScheda` | classe | dati del contatto e storico, con filtro per settimana o mese e pulsante "nuova attività" | GUI.4 |
| `SchermataFormAttivita` | classe | inserimento e modifica di un'attività; cambio di stato | GUI.5 |
| `SchermataAgenda` | classe | giorno scelto, frecce per cambiare giorno, elenco a scorrimento | GUI.7 |
| `Navigatore` | classe | pila delle schermate: `vaiA(schermata)`, `indietro()`, `tornaAllaHome()` | GUI.1.1 |
| `ElencoAttivita` | classe | componente usato da storico e agenda: orario, contatto, titolo; mostra le annullate in modo diverso | GUI.4.2, GUI.7.1, GUI.7.2, GUI.8 |
| `DialogoAnnullamento` | classe | chiede il motivo e la conferma | GUI.6 |

Il toolkit grafico (Swing o JavaFX) non è ancora scelto: `Schermata` resta generica finché non si decide.

### `app`

| Nome | Tipo | Compito |
|---|---|---|
| `MPlanApp` | classe | punto di avvio: crea connessione, repository, servizi e navigatore, poi mostra la Home. I collegamenti sono fatti a mano, con i repository passati ai costruttori dei servizi |

---

## 5. Cosa fa ogni interfaccia

Un'interfaccia dice **cosa si può chiedere** a una parte del programma, senza dire come viene fatto. Chi la usa dipende solo da quel contratto; chi la realizza può cambiare senza che il resto se ne accorga. Qui le interfacce sono quattro, tutte dove cambiare la parte sotto ha senso: l'archivio (tre) e le schermate (una). I servizi e i validatori sono classi normali perché oggi ne esiste una sola versione; se servirà, diventeranno interfacce.

### `ContattoRepository` – archivio dei contatti

Chiamata da `ContattoService`; realizzata da `SQLiteContattoRepository` (e da una versione in memoria nei test).

| Metodo | Cosa fa | Usato per |
|---|---|---|
| `salva(contatto)` | Registra un contatto nuovo (senza id) oppure aggiorna uno esistente; restituisce il contatto con l'id assegnato | creare e modificare (FUN.1, FUN.2) |
| `trova(id)` | Restituisce il contatto, o niente se non esiste (`Optional`) | aprire la scheda (GUI.2.3) |
| `trovaTutti()` | Restituisce tutti i contatti | la rubrica nella Home (FUN.3) |
| `cercaPerNome(testo)` | Restituisce i contatti il cui nome contiene il testo (Could) | ricerca (FUN.3.1) |

Non c'è un metodo per eliminare: la specifica non lo prevede.

### `AttivitaRepository` – archivio delle attività

Chiamata da `AttivitaService`; realizzata da `SQLiteAttivitaRepository`.

| Metodo | Cosa fa | Usato per |
|---|---|---|
| `salva(attivita)` | Registra un'attività nuova oppure aggiorna una esistente | creare, modificare, cambiare stato e annullare: sono tutti salvataggi di un'attività cambiata (FUN.4, FUN.5, FUN.6, FUN.12) |
| `trova(id)` | Restituisce l'attività, o niente se non esiste | caricarla prima di modificarla o annullarla |
| `trovaPerContatto(contatto)` | Restituisce tutte le attività di un contatto | storico completo (FUN.9) |
| `trovaPerContatto(contatto, periodo)` | Come sopra, ma solo quelle del periodo | storico di una settimana o di un mese (FUN.9.1) |
| `trovaPerGiorno(data)` | Restituisce le attività di tutti i contatti in quel giorno | agenda giornaliera (FUN.10) |

Il repository restituisce i dati senza ordinarli: l'ordine lo decide il servizio con `OrdinamentoAttivita`, così la regola sta in un posto solo. **Non c'è un metodo per eliminare**: è così che il progetto garantisce che nessuna attività si cancelli (FUN.12.2).

### `RegistroRepository` – archivio del registro delle modifiche (Should)

Chiamata da `AttivitaService`; realizzata da `SQLiteRegistroRepository`.

| Metodo | Cosa fa | Usato per |
|---|---|---|
| `aggiungi(voce)` | Aggiunge una voce in coda. Le voci non si modificano né si cancellano | tenere traccia di modifiche, cambi di stato e annullamenti (FUN.13) |
| `trovaPerAttivita(attivita)` | Restituisce le voci di un'attività in ordine di data | consultare o verificare la storia di un'attività |

### `Schermata` – contratto di ogni schermata

Realizzata dalle cinque schermate; usata da `Navigatore`.

| Metodo | Cosa fa |
|---|---|
| `titolo()` | Restituisce il titolo da mostrare nell'intestazione |
| `vista()` | Restituisce il contenuto grafico (il tipo dipende dal toolkit scelto) |
| `alMostrare()` | Viene chiamato ogni volta che la schermata torna visibile, anche con "indietro", per ricaricare i dati (ad esempio lo storico dopo un salvataggio) |

Grazie a questo contratto il `Navigatore` tiene una pila di schermate senza sapere quali siano, e può tornare alla schermata di provenienza o alla Home (GUI.1.1).

---

## 6. Due esempi di flusso

**Creare un'attività** (UC7)

1. L'utente compila `SchermataFormAttivita` e salva: la schermata costruisce un'`Attivita` e chiama `AttivitaService.crea`.
2. Il servizio chiede a `ValidatoreAttivita` di controllare i dati. Se ci sono errori lancia `ValidazioneException`: la schermata mostra campo e motivo, non si salva nulla e gli altri dati restano nel modulo (DAT.5).
3. Se è tutto valido il servizio imposta lo stato `DA_FARE` e chiama `AttivitaRepository.salva`, che `SQLiteAttivitaRepository` scrive sul file.
4. Il `Navigatore` torna alla scheda e `alMostrare()` ricarica lo storico.

**Annullare un'attività** (UC10)

1. `DialogoAnnullamento` chiede il motivo e la conferma, poi la schermata chiama `AttivitaService.annulla(id, motivo)`.
2. Il servizio carica l'attività con `trova`, controlla con `StatoAttivita.puoPassareA(ANNULLATA)` che sia ammesso e che il motivo non sia vuoto; altrimenti lancia `ValidazioneException`.
3. Imposta stato e motivo, chiama `salva` e aggiunge una voce con `RegistroRepository.aggiungi`.
4. La schermata ricarica l'elenco: l'attività resta visibile e `ElencoAttivita` la mostra come annullata (GUI.8).

---

## 7. Regole del dominio e dove stanno

- **Transizioni di stato** (FUN.6.3, FUN.6.2): in `StatoAttivita`; `AttivitaService` le applica e rifiuta le altre.
- **Annullamento** (FUN.12): `AttivitaService.annulla` richiede un motivo non vuoto (DAT.3.1) e non cancella mai (FUN.12.2).
- **Dati non validi** (DAT.5): i validatori trovano gli errori, il servizio non salva e lancia `ValidazioneException`; la schermata mostra campo e motivo e lascia gli altri dati nel modulo.
- **Ordinamenti** (FUN.9, FUN.10.2, FUN.10.3): comparatori in `OrdinamentoAttivita`.
- **Registro** (FUN.13): `AttivitaService` aggiunge una `VoceRegistro` a ogni modifica, cambio di stato o annullamento.

## 8. Design pattern candidati

Il regolamento chiede di applicarne un paio. Quelli che nascono dal progetto senza forzature:

- **Repository**: interfacce in `persistence`, implementazione in `persistence.sqlite`.
- **Strategy**: i due ordinamenti come `Comparator` intercambiabili.
- **State** (facoltativo): oggi lo stato è un `enum` con le transizioni. Se serve un pattern esplicito si può trasformare in classi, ma per ora non conviene.

## 9. Cosa non è progettato

- **Notifica di fine giornata** (FUN.14, Could): nessuna classe.
- **Eliminazione di contatti e attività**: non prevista dalla specifica.
- **Evoluzioni** (cellulare, più dispositivi): nessuna classe; basta che `service` resti indipendente da `ui` (NFR.5).

## 10. Da decidere nel diagramma delle classi

1. Nome del package radice e del progetto Eclipse.
2. Toolkit grafico.
3. Se i servizi diventano interfacce.
4. `Attivita` con riferimento al `Contatto` oppure con il solo id.
5. Se la validazione resta in classi a parte o entra nei servizi.
6. Schema delle tabelle e indici (NFR.3).
7. Quali pattern dichiarare nel Documento 4.

---

## 11. Requisiti → classi

| Requisiti | Classi |s
|---|---|
| FUN.1, FUN.1.1, FUN.1.2, FUN.1.3, FUN.2; DAT.1, DAT.1.1, DAT.1.2, DAT.1.3, DAT.1.4, DAT.1.5, DAT.1.6; GUI.3, GUI.3.1 | `Contatto`, `ContattoService`, `ValidatoreContatto`, `SchermataFormContatto` |
| FUN.1.4, DAT.1.7 | `TipoContatto` |
| FUN.3, FUN.3.1; GUI.2, GUI.2.1, GUI.2.2, GUI.2.3 | `SchermataHome`, `ContattoService`, `ContattoRepository` |
| FUN.4, FUN.4.1, FUN.4.2, FUN.4.3, FUN.5, FUN.5.1, FUN.7, FUN.8, FUN.8.1; DAT.2, DAT.2.1, DAT.2.2, DAT.2.3, DAT.2.4, DAT.2.5, DAT.2.6; GUI.4.1, GUI.5, GUI.5.1 | `Attivita`, `AttivitaService`, `ValidatoreAttivita`, `SchermataFormAttivita` |
| FUN.6, FUN.6.1, FUN.6.2, FUN.6.3; DAT.3; GUI.5.2 | `StatoAttivita`, `AttivitaService`, `SchermataFormAttivita` |
| FUN.12, FUN.12.1, FUN.12.2; DAT.3.1; GUI.6, GUI.8 | `AttivitaService`, `AttivitaRepository` (senza eliminazione), `DialogoAnnullamento`, `ElencoAttivita` |
| FUN.9, FUN.9.1; GUI.4, GUI.4.2 | `AttivitaService`, `Periodo`, `OrdinamentoAttivita`, `SchermataScheda`, `ElencoAttivita` |
| FUN.10, FUN.10.1, FUN.10.2, FUN.10.3, FUN.10.4, FUN.11; GUI.7, GUI.7.1, GUI.7.2 | `AttivitaService`, `OrdinamentoAttivita`, `SchermataAgenda`, `ElencoAttivita` |
| FUN.13; DAT.4 | `VoceRegistro`, `TipoModifica`, `RegistroRepository`, `AttivitaService` |
| FUN.15, FUN.15.1; NFR.2, NFR.6, NFR.7 | `ConnessioneDatabase`, `AbstractSQLiteRepository`, repository SQLite |
| DAT.5 | `ErroreValidazione`, `ValidazioneException`, i due validatori, le schermate dei moduli |
| GUI.1, GUI.1.1; NFR.1 | `Schermata`, `Navigatore` e le cinque schermate |
| FUN.14 | non progettata (Could) |
| NFR.3, NFR.5; VIN.1–VIN.5 | struttura a strati, repository come interfacce, `enum`/`record`/`Optional`, test dei servizi con repository in memoria |
