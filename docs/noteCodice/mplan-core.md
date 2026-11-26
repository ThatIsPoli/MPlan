# Note sul codice di `mplan-core`

Modulo: `code/mplan-core`, package `it.mpelettrica.mplan.core`. Riferimenti: `core-spiegazione.md` (sezione 12), design D3, D4, D5 e D9, specifica DAT.1-DAT.5, FUN.4-FUN.13 e Tabella 8.1.

## Cosa è stato fatto rispetto allo scheletro

Nei file generati da Javazzar sono stati tolti i marcatori, le annotazioni segnaposto, i getter e i setter privati e gli import generici (`java.util.*`, `java.time.*`); resta solo l'intestazione. I campi delle associazioni sono `final` e si assegnano nel costruttore. Il core importa solo JDK, `mplan-api` e `log4j-api`.

### Package `dominio`

| File | Cosa è stato scritto |
|---|---|
| `Validatore<T>` | Aggiunti import e Javadoc |
| `ValidatoreContatto` | `implements Validatore<DatiContatto>`. Nome obbligatorio (100); cognome (100), indirizzo (200) facoltativi; ogni telefono non vuoto, al massimo 20 caratteri e senza doppioni (come la chiave primaria di `TELEFONO_CONTATTO`); codice fiscale di 16 caratteri alfanumerici o 11 cifre; partita IVA di 11 cifre. Il tipo non genera errori |
| `ValidatoreAttivita` | `implements Validatore<DatiAttivita>`. Titolo obbligatorio (100); data obbligatoria; descrizione e note facoltative (5000). L'ora non genera errori |
| `RegoleStatoAttivita` | Classe `final` con costruttore privato. Tabella delle transizioni in una `EnumMap`: da fare verso parziale, completata e annullata; parziale verso completata e annullata; tutto il resto vietato. `modificabile` è falso solo per annullata: la completata si modifica nei testi (Tabella 8.1) |
| `OrdinamentoAttivita` | `PER_AGENDA` e `PER_STORICO` come `public static final Comparator<Attivita>`. Ordine per ora con le attività senza ora prima; lo storico ordina prima per data. In fondo c'è uno spareggio per `id`, così l'ordine è identico tra due letture |
| `VoceRegistro` | Da classe a record; i campi obbligatori sono controllati con `Objects.requireNonNull`, il motivo può essere `null` |
| `TipoModifica` | Solo Javadoc |
| `ControlliTesto` (nuova) | Vedi sotto |

### Package `repository`

Solo import, Javadoc e `<T>` davanti a `inTransazione(Supplier<T>)`. Documentato il contratto: le eccezioni non controllate dell'operazione passano invariate dopo l'annullamento della transazione.

### Package `servizi`

| File | Cosa è stato scritto |
|---|---|
| `ContattoServiceImpl` | `crea` e `modifica` validano, poi scrivono in transazione; l'id è `UUID.randomUUID()`. `elenca` ordina per nome e cognome con un `Collator` italiano (strength secondaria: senza distinzione di maiuscole, con le lettere accentate vicine a quelle senza accento), cognome assente per ultimo. Il `Collator` non è sicuro tra thread, quindi se ne crea uno a ogni chiamata. Il codice fiscale si scrive in maiuscolo con `Locale.ROOT`. Un contatto inesistente in `modifica` dà `IllegalArgumentException` |
| `AttivitaServiceImpl` | Il ciclo carica, controlla, scrive e notifica sta in un'unica transazione. `crea` non notifica; `modifica`, cambi di stato e `annulla` sì. Se la data manca si usa oggi dal `Clock` (in `valida`, `crea` e `modifica`) |
| `AgendaServiceImpl` | `attivitaDelGiorno` ordina con `PER_AGENDA` e cerca il nome di ogni contatto una sola volta; `oggi` usa il `Clock` |
| `OsservabileAttivita` | Lista `final` di osservatori (`CopyOnWriteArrayList`, perché i servizi sono condivisi tra più sessioni); tolte le annotazioni e l'import di Jackson che Javazzar aveva generato e che non compilavano |
| `RegistroModifiche` | Una voce per modifica, cambio di stato (con il testo "Stato cambiato da ... a ...") e annullamento (con il motivo); data e ora dal `Clock` |
| `OsservatoreAttivita` | Solo import e Javadoc |
| `Testi` (nuova) | Vedi sotto |

## Scelte non ovvie

- **Ordine dei controlli.** `crea` valida i dati (`ValidazioneException`) e poi, in transazione, controlla il contatto (`IllegalArgumentException`). `modifica` valida i dati prima di tutto; poi, in transazione, carica l'attività (id inesistente: `IllegalArgumentException`) e controlla che non sia annullata (`IllegalStateException`). I cambi di stato non hanno dati da validare: caricano l'attività e controllano la transizione. `annulla` controlla la transizione prima del motivo (`ValidazioneException`, campo `motivo`).
- **Eccezioni e transazioni.** Le eccezioni sono lanciate dentro la transazione, che le rilancia invariate dopo l'annullamento: lo richiede l'implementazione di `GestoreTransazioni` in `mplan-persistence`.
- **Normalizzazione.** Il validatore non modifica i dati. Il servizio, quando crea l'entità, toglie gli spazi, trasforma in `null` i facoltativi vuoti e scrive il codice fiscale in maiuscolo. Il validatore valuta lo stesso testo già privato degli spazi.
- **Motivo di annullamento.** Controllato in un metodo privato di `AttivitaServiceImpl` (obbligatorio, al massimo 1000 caratteri): `ControlliTesto` sta in `dominio` ed è visibile solo lì.
- **Registro.** Nessuna voce alla creazione. La notifica avviene nella stessa transazione, quindi attività e voce si salvano insieme o non si salvano.
- **Messaggi di errore.** I messaggi dei testi hanno una forma che non dipende da genere e numero del campo: "Note: al massimo 5000 caratteri", "Nome: campo obbligatorio".
- **Modifica senza cambiamenti.** `modifica` salva sempre, ma notifica il registro solo se l'attività risultante è diversa da quella letta (confronto tra record). Un Salva senza cambiamenti non scrive voci (FUN.13).
- **Lunghezza dei testi.** Si conta con `String.length()`, cioè in unità UTF-16: un carattere fuori dal piano base, come un'emoji, vale 2. È un limite noto, non corretto.
- **Storico.** `storico` non controlla che il contatto esista: restituisce una lista vuota.
- **Log.** Logger di log4j-api nei tre servizi; si scrivono solo gli identificativi, mai nomi, telefoni o codici fiscali.

## Nomi dei campi in `ErroreValidazione`

Costanti private nei validatori e in `AttivitaServiceImpl`. Contatto: `nome`, `cognome`, `indirizzo`, `telefoni`, `codiceFiscale`, `partitaIva`. Attività: `titolo`, `data`, `descrizione`, `note`. Annullamento: `motivo`. `telefoni` può comparire più volte, un errore per ogni numero non valido o ripetuto, con il numero nel motivo. `tipo` e `ora` non generano errori.

## Deviazioni dal diagramma

- Due classi package-private non disegnate: `ControlliTesto` (controlli di obbligatorietà, lunghezza e formato condivisi dai due validatori) e `Testi` (normalizzazione condivisa dai servizi). Evitano codice duplicato; approvate dal team.
- `RegoleStatoAttivita` e `OrdinamentoAttivita` hanno un costruttore privato e sono `final`.
- Nessuna firma pubblica cambia rispetto allo scheletro, salvo `<T>` su `inTransazione`, `Validatore<...>` nei `implements` e il record `VoceRegistro`.

## Cosa è stato verificato

`mvn -pl mplan-core -am compile` termina senza errori. Una prova temporanea, poi cancellata, con repository in memoria e un `Clock` fisso ha controllato: doppione di telefono, normalizzazione e codice fiscale in maiuscolo, data predefinita, nessuna voce alla creazione, voci di registro per modifica, cambi di stato e annullamento, rifiuto dell'annullamento di una completata, motivo vuoto, modifica di un'attività annullata e ordine di storico e agenda (anche l'ordine di rubrica con nomi accentati, il codice fiscale con lingua predefinita turca e la stabilità dell'ordine con attività a pari data e ora). Nessun test è stato aggiunto al repository.
