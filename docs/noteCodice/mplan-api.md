# Note sul codice di mplan-api

Il modulo contiene il contratto tra GUI e resto del programma, nel package `it.mpelettrica.mplan.api`. Non dipende da altri moduli, solo da Java. Le note descrivono cosa è stato cambiato rispetto al codice generato con Javazzar, file per file.

## Interventi comuni a tutti i file

- Tolti tutti i marcatori di Javazzar (`// ----------- << ... >>`). Nei file generati resta solo l'intestazione in testa.
- Import espliciti al posto di `java.util.*` e `java.time.*`.
- Riscritta tutta la Javadoc, in italiano: i Javadoc provvisori ("Firme provvisorie", "[DA COMPLETARE]") sono stati sostituiti.
- Tolti i getter e i setter privati generati.

## File per file

| File | Intervento |
|---|---|
| `StatoAttivita`, `TipoContatto` | Solo Javadoc dell'enumerazione e di ogni costante. Nessuna logica: le transizioni di stato stanno nel core |
| `ErroreValidazione` | Da classe a record. Costruttore compatto con `Objects.requireNonNull` su `campo` e `motivo`. La Javadoc elenca i nomi dei campi usati |
| `Contatto` | Da classe a record. Obbligatori `id`, `nome`, `telefoni`; `telefoni` copiata con `List.copyOf` |
| `Attivita` | Da classe a record. Obbligatori `id`, `idContatto`, `titolo`, `data`, `stato` (i campi `NOT NULL` di `schema.sql`) |
| `VoceAgenda` | Da classe a record `VoceAgenda(String nomeContatto, Attivita attivita)`, entrambi obbligatori. Tolto il setter generato |
| `DatiContatto` | Costruttore vuoto esplicito, getter e setter pubblici. `telefoni` parte come lista vuota; `setTelefoni` copia in lista immutabile e tratta `null` come lista vuota, così il `Binder` non rompe l'invariante |
| `DatiAttivita` | Costruttore vuoto esplicito, getter e setter pubblici, nessun controllo. La Javadoc di `getData` dice che con data `null` i servizi usano oggi |
| `PersistenzaException` | I due costruttori ora chiamano `super(...)`. Aggiunto `serialVersionUID` |
| `ValidazioneException` | Un solo `getErrori()` pubblico (quello privato generato è stato tolto). Campo `errori` `final`, assegnato nel costruttore con `List.copyOf`. Il messaggio dell'eccezione è composto da un metodo privato `creaMessaggio`. Aggiunto `serialVersionUID` |
| `ContattoService`, `AttivitaService`, `AgendaService`, `ChiusuraApplicazione` | Firme invariate, Javadoc completa con `@throws` (`ValidazioneException`, `IllegalArgumentException` per id inesistente, `IllegalStateException` per transizioni o modifiche non ammesse, `PersistenzaException`), ordinamenti delle liste e regola della data predefinita |

## Scelte non ovvie

- I record controllano solo i `null`: nessuna regola su lunghezze o formati, che sono del core.
- `ValidazioneException` accetta una lista vuota, senza controlli: è responsabilità di chi la lancia.
- Gli elementi `null` nelle liste fanno lanciare la `NullPointerException` standard di `List.copyOf`.
- Costruttori vuoti dei Dati, `serialVersionUID` e `creaMessaggio` non sono nel class diagram: sono dettagli privati o impliciti, il contratto non cambia.

## Deviazioni dal diagramma

Nessuna: firme, nomi e ordine dei componenti dei record sono quelli dello scheletro e di `mplan.mdj`.

## Verifiche

- Compilazione: `mvn -q -B -pl mplan-api compile` da `code`, senza errori.
- Prova temporanea (in una cartella fuori dal repo, poi cancellata), compilata con `javac -Xlint:all` ed eseguita. Ha controllato:
  - `NullPointerException` con messaggio in italiano sui campi obbligatori di `Contatto`, `Attivita`, `VoceAgenda`, `ErroreValidazione`;
  - copia difensiva e immutabilità di `telefoni` in `Contatto` e in `DatiContatto` (lista iniziale vuota, `setTelefoni(null)` che dà lista vuota);
  - copia e immutabilità di `getErrori()` e messaggio di `ValidazioneException`, con lista vuota e con lista `null`;
  - messaggio e causa dei due costruttori di `PersistenzaException`.
- Il compilatore con `-Xlint:all` segnala solo il warning `[serial]` sul campo `errori` di `ValidazioneException`. Non è un errore, perché `List.copyOf` restituisce una lista serializzabile.
- Nessun test è stato aggiunto al repository.
