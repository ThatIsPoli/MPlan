# Il modulo api di MPlan, spiegato semplice

Questo documento spiega il class diagram `mplan-api` del file StarUML (`docs/uml/mplan.mdj`): cosa c'è in api, come si legge il diagramma e perché ogni elemento è fatto così. Completa le firme che nella spiegazione del core erano ancora provvisorie.

## 1. A cosa serve api

`mplan-api` è il contratto tra la GUI e il resto del programma: dice cosa si può chiedere (le interfacce dei servizi), con quali dati e quali errori possono tornare indietro. Non dipende da nessun altro modulo, solo da Java (design, D2).

| Modulo | Come usa api |
|---|---|
| `mplan-ui-vaadin` | Chiama i servizi e mostra i dati. Del progetto vede solo api |
| `mplan-core` | Implementa `ContattoService`, `AttivitaService` e `AgendaService` e crea `Contatto`, `Attivita` e `VoceAgenda` |
| `mplan-persistence` | Rilegge `Contatto` e `Attivita` dal database e lancia `PersistenzaException` |
| `mplan-app` | Implementa `ChiusuraApplicazione` con `ChiusuraApplicazioneImpl` e passa i servizi alla GUI (`app-spiegazione.md`) |

Tutto sta in un solo package, `it.mpelettrica.mplan.api`: gli elementi sono quattordici e dividerli in sotto-package non li renderebbe più facili da leggere.

## 2. Come si legge il diagramma

Il diagramma è diviso in fasce, dall'alto verso il basso:

```text
servizi             ContattoService  AttivitaService  AgendaService  ChiusuraApplicazione
dati restituiti     TipoContatto  Contatto  StatoAttivita  Attivita  VoceAgenda
dati di input       DatiContatto (sotto Contatto)  DatiAttivita (sotto Attivita)
errori              ErroreValidazione  ValidazioneException  PersistenzaException
java.lang (grigio)  RuntimeException
```

Le notazioni sono le stesse del core (punti 5 e 6 di `core-spiegazione.md`). In più qui compaiono:

- **«record»** sopra il nome: la classe diventa un record di Java (punto 4);
- **«enumeration»**: un tipo con un elenco fisso di valori;
- **`{readOnly}`** sui campi dei record: dopo la creazione non cambiano.

I tipi di Java usati nelle firme (`UUID`, `LocalDate`, `LocalTime`, `List`, `Optional`, `String`) sono scritti come testo e non sono disegnati. Non ci sono nemmeno frecce dai servizi ai dati che usano: i tipi si leggono già nelle firme, e con una freccia per ogni tipo il diagramma diventerebbe illeggibile.

## 3. I servizi

**ContattoService**: `elenca()` per la rubrica (FUN.3), `trova(id)`, `crea(dati)` (FUN.1), `modifica(id, dati)` (FUN.2) e `valida(dati)`.

**AttivitaService**:

| Metodo | Scopo |
|---|---|
| `trova(id)` | Carica un'attività dal suo id. Serve al Form attività: con Vaadin ogni schermata ha un indirizzo (rotta) e, quando il form si apre dallo storico o dall'agenda, riceve solo l'id dell'attività (GUI.4.2, GUI.7.2) |
| `crea(idContatto, dati)` | Nuova attività sul contatto della scheda aperta (FUN.4) |
| `modifica(id, dati)` | Aggiorna titolo, data, orario, descrizione e note (FUN.5) |
| `segnaParziale(id)`, `segnaCompletata(id)` | Cambio di stato (FUN.6.1) |
| `annulla(id, motivo)` | Annullamento con il motivo (FUN.12) |
| `storico(idContatto)` | Tutte le attività del contatto, già in ordine (FUN.9) |
| `valida(dati)` | Errori campo per campo, senza salvare (D4) |

**AgendaService**: `attivitaDelGiorno(giorno)` restituisce le righe dell'agenda (FUN.10) e `oggi()` il giorno su cui aprirla (FUN.10.4).

**ChiusuraApplicazione** ha un solo metodo, `chiudi()`. Dalla Home il comando `chiudi` chiude l'applicazione (Tabella 8.2 della specifica), ma con Vaadin chiudere la scheda del browser non basta: va fermato il server interno (design, D7). Può farlo solo `mplan-app`, che avvia Spring Boot, mentre la GUI vede solo api. Per questo l'interfaccia sta in api e la implementa `ChiusuraApplicazioneImpl` di `mplan-app`. Non si chiama `...Service` perché non lavora sui dati.

I servizi restituiscono dati che la GUI può solo leggere: ogni cambiamento passa da un metodo del servizio (D3).

## 4. I dati restituiti: record ed enumerazioni

`Contatto`, `Attivita`, `VoceAgenda` ed `ErroreValidazione` sono **record** di Java, uno dei costrutti moderni chiesti da VIN.1. Per un record Java crea da solo:

- un campo privato e non modificabile per ogni componente;
- il costruttore con tutti i campi;
- un metodo per leggere ogni campo, con lo stesso nome del campo: `contatto.nome()`, `attivita.stato()`;
- `equals`, `hashCode` e `toString`.

Non ci sono setter, quindi i dati non si possono cambiare dopo la creazione, come chiede D3. Nel costruttore del record si controlla con `Objects.requireNonNull` che i campi obbligatori non siano `null`. Sono i campi che in `schema.sql` sono `NOT NULL`, più la lista dei telefoni:

| Record | Non possono essere `null` | Possono essere `null` |
|---|---|---|
| `Contatto` | `id`, `nome`, `telefoni` | `cognome`, `indirizzo`, `codiceFiscale`, `partitaIva`, `tipo` |
| `Attivita` | `id`, `idContatto`, `titolo`, `data`, `stato` | `ora`, `descrizione`, `note`, `motivoAnnullamento` |
| `VoceAgenda` | `attivita`, `nomeContatto` | – |
| `ErroreValidazione` | `campo`, `motivo` | – |

Se un contatto non ha numeri, `telefoni` è una lista vuota. Il costruttore la copia con `List.copyOf`, così nessuno può aggiungere numeri da fuori.

Javazzar, l'estensione di StarUML che prima si chiamava Rebel, genera dal diagramma una classe normale con i campi e un metodo `getX()` per ognuno. Va trasformata a mano in record, togliendo i `getX()` perché il record ha già i suoi metodi `x()`, e va scritta nella relazione tra le classi modificate (Documento 2, 2.5).

**VoceAgenda** è una riga dell'agenda: l'attività e il nome del suo contatto (GUI.7.1). Il campo `attivita` è disegnato come associazione `-attivita 1` verso `Attivita`, con la regola del core: quando il tipo di un campo è una classe del diagramma si usa la freccia e il campo non si ripete nel riquadro. Il `*` dall'altro lato dice che la stessa attività può comparire in più voci, una per ogni volta che si carica l'agenda.

**Attivita** contiene `idContatto` e non il `Contatto` intero: nello storico il contatto è già noto, perché è quello della scheda, e nell'agenda il nome arriva con `VoceAgenda`. `ora` è `null` quando l'orario non è indicato (DAT.2.3) e `motivoAnnullamento` è `null` finché l'attività non viene annullata (DAT.3.1).

**StatoAttivita** ha i quattro stati di FUN.6: `DA_FARE`, `PARZIALE`, `COMPLETATA`, `ANNULLATA`. Le transizioni ammesse non stanno qui ma in `RegoleStatoAttivita` del core, perché sono una regola e api contiene solo il contratto.

**TipoContatto** vale `PRIVATO` o `AZIENDA` (DAT.1.7). Nel contatto può essere `null`, perché il tipo è facoltativo e non ha un valore predefinito.

Le due enumerazioni restano campi nel riquadro (`-stato`, `-tipo`), come `tipo` in `VoceRegistro` nel core.

## 5. I dati di input

`DatiContatto` e `DatiAttivita` contengono ciò che l'utente ha scritto nel form, anche se non è valido (D3). Sono classi normali e non record perché il form le riempie un campo alla volta: il `Binder` di Vaadin collega ogni campo del form a una coppia di getter e setter. Getter e setter non sono disegnati, per non raddoppiare le righe: Javazzar li genera da solo, uno per ogni campo.

`DatiAttivita` non ha lo stato né il contatto: lo stato cambia solo con `segnaParziale`, `segnaCompletata` e `annulla` (FUN.6.3), e il contatto si passa a parte in `crea`.

## 6. Gli errori

**ErroreValidazione** è un record con il campo sbagliato e il motivo, per esempio `titolo` e "obbligatorio". È ciò che la GUI mostra accanto al campo (DAT.5).

**ValidazioneException** si lancia quando `crea`, `modifica` o `annulla` ricevono dati non validi. Porta con sé la lista degli errori (`-errori *`, che si legge con `getErrori()`), così la GUI mostra gli stessi messaggi di `valida()` anche in questo caso. Nel modello l'estremo `errori` è segnato `{ordered}`, così Javazzar genera una `List` come quella di `valida()` e non un `Set`. Il `0..1` dall'altro lato dice che un errore sta al massimo in un'eccezione: quelli restituiti da `valida()` non stanno in nessuna.

**PersistenzaException** arriva dalla persistenza quando il database ha un problema. Il secondo costruttore riceve anche la causa, cioè l'errore originale di JDBC (`SQLException`), che così non si perde nei log (D4).

Tutte e due estendono `RuntimeException` (grigia, da `java.lang`): sono eccezioni non controllate e non compaiono nelle firme dei servizi (D4).

## 7. Cosa non c'è in api, e perché

- **Ricerca per nome** (FUN.3.1, priorità C): la GUI filtra l'elenco restituito da `elenca()`, senza un metodo in più.
- **Eliminazione delle attività**: non esiste, perché la vieta FUN.12.2.
- **Lettura del registro delle modifiche** (FUN.13): nessuna schermata lo mostra; lo leggono solo i test, dal repository del core.
- **Classi di Vaadin o di Spring**: api non usa librerie, così core e test non se le portano dietro.

## 8. Esempio completo: la GUI apre un'attività dall'agenda

1. L'agenda chiama `agendaService.oggi()` e poi `attivitaDelGiorno(giorno)`. Per ogni `VoceAgenda` mostra `attivita().ora()`, `nomeContatto()` e `attivita().titolo()` (GUI.7.1).
2. L'utente sceglie un'attività e la GUI apre la rotta del Form attività con l'id dell'attività.
3. Il form chiama `attivitaService.trova(id)` e riempie un `DatiAttivita` con i valori letti.
4. L'utente scrive le note e salva. Il form chiama `valida(dati)` e, se la lista è vuota, `modifica(id, dati)`.
5. Se `modifica` lancia comunque una `ValidazioneException`, il form legge `getErrori()` e mostra i messaggi accanto ai campi.

## 9. Cosa resta da fare

- Dopo la generazione con Javazzar:
  - trasformare i quattro record e scrivere i controlli dei `null`;
  - in `ValidazioneException` lasciare un solo `getErrori()`: Javazzar ne genera uno dal campo `errori`, oltre a quello disegnato;
  - rendere `final` il campo `errori` e assegnarlo nel costruttore.
- Esportare l'immagine del diagramma per il Documento 4.
