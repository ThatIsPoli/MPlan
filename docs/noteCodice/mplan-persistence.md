# Note sul codice di mplan-persistence

Questo documento descrive cosa è stato fatto rispetto allo scheletro generato con Javazzar dal class diagram `mplan-persistence`, le scelte non ovvie e le deviazioni.

## 1. Scopo del modulo

Implementa i repository e le transazioni definiti nel core con SQLite e jOOQ. Verso l'esterno (`mplan-app`) si espone solo `PersistenzaFactory`, con tipi di `core` e `api`. Le classi jOOQ generate e `java.sql` restano dentro il modulo.

## 2. Modifiche rispetto allo scheletro, file per file

Tutti i file: tolti i marcatori Javazzar (resta l'intestazione), import espliciti, tolti i getter privati, campi delle associazioni `final` assegnati nel costruttore, Javadoc in italiano.

| File | Cosa è stato fatto |
|---|---|
| `PersistenzaFactory` | Costruttore che configura SQLite (`SQLiteConfig`: chiavi esterne attive, `busy_timeout` 5 secondi), il dialetto jOOQ, il `TraduttoreEccezioni` e crea `ContestoTransazione` e `InizializzatoreSchema`. I cinque metodi `crea...` e `inizializzaSchema` delegano ai collaboratori. |
| `infrastruttura/ContestoTransazione` | `ThreadLocal` inizializzato; `<T>` aggiunto a `eseguiConContesto`; al termine ripristina il canale precedente, quindi le chiamate annidate sono coerenti. |
| `infrastruttura/GestoreTransazioniJooq` | `<T>` aggiunto a `inTransazione`; usa `transactionResult` di jOOQ (annidamento nativo con savepoint). Le eccezioni dell'operazione passano invariate dopo il rollback; solo `DataAccessException` diventa `PersistenzaException`. |
| `infrastruttura/InizializzatoreSchema` | Legge `/schema.sql` dal classpath, toglie i commenti, divide sugli `;` ed esegue gli statement in un'unica transazione. |
| `infrastruttura/TraduttoreEccezioni` | Aggiunto `@Override`; registra l'errore nel log e sostituisce l'eccezione con una `PersistenzaException` che ha la causa. |
| `mapping/*Mapper` | Costruttore privato (classi di sole funzioni statiche). Date e ore come stringhe ISO. I dati corrotti letti dal database diventano `PersistenzaException`. `VoceRegistroMapper.aRecord` non imposta l'id (lo genera SQLite). |
| `repository/Jooq*Repository` | Ogni metodo chiede il canale a `ContestoTransazione.dslCorrente()`. Nessun ordinamento, nessuna cancellazione di attività. |

## 3. Scelte non ovvie

- **Formati.** Data `yyyy-MM-dd`; ora `HH:mm:ss` (senza frazioni di secondo); momento del registro in ISO con frazioni. L'ordine delle stringhe coincide con quello cronologico.
- **Database in memoria.** Per URL con `:memory:` si apre una sola connessione, tenuta aperta da un `DefaultConnectionProvider`, altrimenti il database sparirebbe. Per un file si usa `DataSourceConnectionProvider` su `SQLiteDataSource`. Il diagramma citava `DataSource`: è l'unica differenza.
- **Chiavi esterne.** `PRAGMA foreign_keys` si attiva con `SQLiteConfig` a ogni connessione.
- **Telefoni.** Stanno in `TELEFONO_CONTATTO` e si leggono con `ORDER BY rowid` (ordine di inserimento). `aggiorna` cancella e reinserisce i numeri. Nessuna deduplica: un doppione viola la chiave primaria e diventa `PersistenzaException`.
- **`tutti()`** usa due query complessive, non una per contatto.
- **`aggiorna` su id inesistente** lancia `IllegalArgumentException` (errore di programmazione).
- **Registro.** `trovaPerAttivita` ordina per `quando` e poi per `id`.
- **Errori.** Le istruzioni SQL fallite sono tradotte dal listener di jOOQ; gli errori di apertura connessione o commit da `GestoreTransazioniJooq`.
- **Limite noto.** Un errore di apertura connessione fuori da una transazione arriverebbe come `DataAccessException` di jOOQ; con SQLite su file locale è praticamente impossibile.

## 4. Deviazioni dal diagramma

Nessuna classe o metodo pubblico aggiunto. Unica differenza: `ConnectionProvider` al posto di un `DataSource` per il caso in memoria.

## 5. Verifiche

- **Compilazione.** `mvn -pl mplan-persistence -am generate-sources compile` riesce: le classi jOOQ si rigenerano da `schema.sql` e il modulo compila con api e core.
- **Prova temporanea** (programma usa e getta fuori dal repository, poi cancellato; nessun test nel progetto), eseguita su `jdbc:sqlite::memory:` e su un file, con 17 controlli superati su 17 in entrambi i casi:
  - schema creato due volte senza errori;
  - lettura e scrittura di contatti, attività e voci del registro, con ordine dei telefoni conservato;
  - `aggiorna` su id inesistente lancia `IllegalArgumentException`;
  - un telefono doppio e una chiave esterna violata diventano `PersistenzaException`, e l'`aggiungi` fallito non lascia dati parziali;
  - rollback con l'eccezione del core rilanciata invariata, e commit di attività e registro insieme;
  - savepoint annidato: l'operazione interna fallita viene annullata e quella esterna conferma il resto; se fallisce l'esterna si annulla anche l'interna;
  - dopo gli errori il canale torna quello di base e le operazioni fuori transazione funzionano.
- Sulla JDK 27 sqlite-jdbc stampa un avviso sull'accesso nativo (`--enable-native-access`): è solo un avviso.
