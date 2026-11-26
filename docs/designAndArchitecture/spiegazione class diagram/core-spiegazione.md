# Il modulo core di MPlan, spiegato semplice

Questo documento spiega il class diagram `mplan-core` del file StarUML (`docs/uml/mplan.mdj`): cosa c'è dentro il core, come si legge il diagramma, perché ogni classe è fatta così e come il core si collega agli altri moduli. Le scelte sono quelle concordate nella PR #58 e nelle issue #54 e #55.

Le firme di `mplan-api` sono nel suo class diagram e sono spiegate in `api-spiegazione.md`.

## 1. Dove sta il core

Il programma è diviso in cinque moduli Maven. Questa è l'unica architettura del progetto (design, sezione 2, D1 e D2).

```
   mplan-ui-vaadin       schermate
         │ usa solo
         ▼
     mplan-api           contratto: interfacce dei servizi, dati, errori
         ▲
         │ implementa
     mplan-core          logica: servizi, regole, interfacce dei repository
         ▲
         │ implementa le interfacce dei repository
   mplan-persistence     database SQLite con jOOQ

   mplan-app             crea tutti gli oggetti e li collega
```

| Modulo | Cosa fa |
|---|---|
| `mplan-api` | Dice cosa si può chiedere al programma: `ContattoService`, `AttivitaService`, `AgendaService`, i dati (`Contatto`, `Attivita`, `DatiAttivita`, ...) e gli errori |
| `mplan-core` | Fa il lavoro: controlla i dati, applica le regole, decide cosa salvare |
| `mplan-persistence` | Salva e legge dal database, implementando le interfacce del package `repository` del core |
| `mplan-ui-vaadin` | Mostra le schermate nel browser e parla solo con le interfacce di api |
| `mplan-app` | Avvia il programma con Spring Boot: ottiene repository e transazioni da `PersistenzaFactory` di `mplan-persistence`, crea i servizi, li collega e apre la Home nel browser |

Il core non conosce né Vaadin né il database. Questo permette di provarlo con JUnit usando repository finti in memoria (D8).

## 2. Nomi dei package

Tutto il codice sta sotto `it.mpelettrica.mplan`. In Java i package iniziano con il dominio internet al contrario di chi possiede il software, per non scontrarsi con le librerie di altri. Il dominio è `mpelettrica.it`, il sito del committente. Lo stesso nome è già usato come `groupId` nei `pom.xml` e nel package di `MPlanApplication`, ed è stato confermato nella PR #58.

Nel file StarUML questo si vede come una catena di package `it` > `mpelettrica` > `mplan`, con dentro `api` e `core`. Rebel genera le cartelle Java seguendo la stessa catena.

## 3. Tre package, non tre livelli

Dentro `mplan-core` le classi sono divise in tre package:

| Package | Contiene | Può usare |
|---|---|---|
| `core.servizi` | le classi che la GUI chiama attraverso api | `dominio`, `repository`, api |
| `core.dominio` | regole e controlli, senza database e senza GUI | solo api |
| `core.repository` | le interfacce di cui il core ha bisogno per salvare e leggere | api e i tipi del registro |

Nessuno usa `servizi`, quindi non ci sono giri di dipendenze.

All'inizio questi pacchetti erano chiamati "livello 1, 2 e 3" e avevano un diagramma ciascuno. Nella review della PR #58 Michele ha fatto notare che così sembravano una seconda architettura, mentre sono solo un modo di ordinare le classi dentro un modulo: il compilatore controlla i confini tra moduli, non quelli tra package dello stesso modulo. Per questo adesso:

- c'è un solo diagramma per modulo Maven (`mplan-core`, e poi `mplan-persistence`, ecc.);
- i tre package sono disegnati come blocchi dentro lo stesso diagramma;
- la descrizione dei package va nel design alla sezione 3 (Software Design), non tra le decisioni architetturali.

## 4. Come leggere il diagramma

Il diagramma è a fasce, dall'alto verso il basso:

```
api (grigio)       ContattoService  AttivitaService  AgendaService  VoceAgenda
servizi            ContattoServiceImpl  AttivitaServiceImpl  AgendaServiceImpl     Clock (java.time)
                   OsservabileAttivita  OsservatoreAttivita  RegistroModifiche
repository         GestoreTransazioni  ContattoRepository  AttivitaRepository  RegistroRepository
dominio            ValidatoreContatto  Validatore  ValidatoreAttivita  RegoleStatoAttivita
                   OrdinamentoAttivita  VoceRegistro  TipoModifica
api (grigio)       Contatto  DatiContatto  ErroreValidazione  DatiAttivita  StatoAttivita  Attivita
```

Gli elementi grigi non fanno parte del core: sono riferimenti ad altri moduli (api) o a Java (`Clock`). Mostrano solo il nome perché la loro descrizione completa sta nel diagramma del loro modulo.

Il package `api` è disegnato due volte. Sopra ci sono le interfacce dei servizi, ognuna sopra la classe che la implementa, e `VoceAgenda` accanto ad `AgendaService`, che la restituisce. Sotto ci sono i tipi usati da repository e dominio, ognuno sotto la classe che lo usa. In questo modo le frecce restano corte e non attraversano tutto il diagramma. UML permette di mostrare lo stesso package più volte nello stesso diagramma. Per lo stesso motivo `ContattoRepository` sta al centro della sua fascia: lo usano tutti e tre i servizi.

## 5. Le frecce

| Disegno | Nome | Significato | Esempio | In Java |
|---|---|---|---|---|
| tratteggiata con triangolo vuoto | realizzazione | implementa un'interfaccia | `AttivitaServiceImpl` verso `AttivitaService` | `implements` |
| tratteggiata con punta aperta | dipendenza | usa quel tipo in un metodo (parametro, risultato o variabile locale), ma non lo tiene come campo | `AttivitaRepository` verso `Attivita` | un `import` e l'uso dentro i metodi |
| continua con punta aperta da un lato e X dall'altro | associazione navigabile | ha un campo di quel tipo | `ContattoServiceImpl` verso `ContattoRepository` | `private final ContattoRepository contatti;` |
| continua con triangolo vuoto | generalizzazione | estende una classe | `AttivitaServiceImpl` verso `OsservabileAttivita` | `extends` |

Come si legge un'associazione, per esempio `ContattoServiceImpl  X──── -contatti 1 ───>  ContattoRepository`:

- la punta della freccia vuol dire che `ContattoServiceImpl` può usare `ContattoRepository`;
- la X dal lato di `ContattoServiceImpl` vuol dire che il repository non sa chi lo usa;
- `-contatti` è il nome del campo (il meno vuol dire privato). Il nome del ruolo si scrive vicino alla classe che quel ruolo indica, cioè dal lato della punta, come nelle slide del corso (esempio `Student` e `Professor` con il ruolo `+lecturer`): il campo però appartiene alla classe dal lato della X;
- `1` dal lato del repository: il servizio ne ha esattamente uno;
- `*` dal lato del servizio: lo stesso repository può essere usato da più servizi;
- `osservatori *` dal lato di `OsservatoreAttivita`: `OsservabileAttivita` ha una lista di osservatori, che `AttivitaServiceImpl` eredita.

Per questo molte classi non hanno attributi nel riquadro. Quando il tipo di un campo è una classe disegnata nel diagramma, il campo si mostra come associazione e non si ripete nel riquadro: è la forma che le slide indicano come preferibile. Rebel genera i campi proprio dalle associazioni. Le interfacce invece non possono avere campi, e i validatori e `RegoleStatoAttivita` non ne hanno bisogno.

## 6. Notazioni particolari

**Sottolineato.** Un attributo o un'operazione sottolineati sono statici (slide "Variabile di classe e operazione di classe"): appartengono alla classe e si usano senza creare un oggetto, per esempio `RegoleStatoAttivita.puoPassareA(da, a)`. Nel core sono statici solo `puoPassareA`, `modificabile`, `PER_STORICO` e `PER_AGENDA`, perché sono regole fisse senza stato.

**Corsivo e `#`.** Un nome in corsivo indica una classe astratta: non si possono creare oggetti di quella classe, serve solo da superclasse. Nel core è astratta solo `OsservabileAttivita`. Il `#` davanti a un'operazione vuol dire protetta: la possono usare solo la classe stessa e le sue sottoclassi, come i tre metodi `notifica...` che `AttivitaServiceImpl` eredita.

**{readOnly}.** Il valore non si può cambiare. `PER_STORICO` e `PER_AGENDA` diventano costanti `public static final`, mentre i quattro campi di `VoceRegistro` diventano i componenti del record (punto 8).

**Il rettangolino tratteggiato con la T.** È il parametro di tipo di una classe generica, come in `List<T>`. `Validatore<T>` vuol dire "valida dati di un tipo che sceglie chi lo implementa":

```java
interface Validatore<T> {
    List<ErroreValidazione> valida(T dati);
}
class ValidatoreContatto implements Validatore<DatiContatto> { ... }   // T = DatiContatto
class ValidatoreAttivita implements Validatore<DatiAttivita> { ... }   // T = DatiAttivita
```

Lo stesso vale per `inTransazione(operazione: Supplier<T>): T`, dove `T` è il tipo del risultato. In StarUML `T` è dichiarato come parametro template dell'operazione, altrimenti Rebel genererebbe una classe `T.java` inesistente.

**Optional.** `trova(id)` restituisce `Optional<...>` perché l'id potrebbe non esistere: chi lo chiama deve gestire il caso "non trovato".

## 7. Package `repository`

Contiene solo interfacce. Dicono cosa serve al core per salvare e leggere, senza dire come. Le implementa `mplan-persistence` con SQLite e jOOQ (D6). Nel diagramma del core non si vedono le implementazioni perché stanno in un altro modulo.

**ContattoRepository**: `trova(id)`, `tutti()`, `aggiungi(contatto)`, `aggiorna(contatto)`. `aggiungi` serve per i contatti nuovi e `aggiorna` per quelli già salvati. Lo usano tutti e tre i servizi: `ContattoServiceImpl` per salvare i contatti, `AttivitaServiceImpl` per controllare che il contatto di una nuova attività esista, `AgendaServiceImpl` per leggere i nomi da mostrare nell'agenda.

**AttivitaRepository**: `trova(id)`, `perContatto(idContatto)` per lo storico, `perGiorno(giorno)` per l'agenda, `aggiungi(attivita)`, `aggiorna(attivita)`. Non c'è un metodo per cancellare: è così che il progetto garantisce che le attività non si eliminano mai (FUN.12.2). `perContatto` riceve solo l'id del contatto, perché con la specifica 0.4.0 lo storico non ha più il filtro per periodo. Un overload `trova(idContatto)` accanto a `trova(id)` non è possibile: tutti e due riceverebbero un solo `UUID` e avrebbero la stessa firma.

**RegistroRepository**: `aggiungi(voce)` aggiunge una voce al registro delle modifiche (FUN.13), che si scrive solo in coda. `trovaPerAttivita(idAttivita)` rilegge le voci di un'attività: nessuna schermata la usa, ma serve ai test per controllare che il registro venga davvero scritto.

**GestoreTransazioni**: esegue più operazioni come un blocco unico. Se una fallisce, si annullano tutte (D5). La versione con `Runnable` serve quando non c'è niente da restituire, quella con `Supplier<T>` quando serve un risultato:

```java
Attivita a = transazioni.inTransazione(() -> {
    attivita.aggiorna(nuova);
    return nuova;
});
```

## 8. Package `dominio`

Qui stanno le regole che valgono sempre, senza database e senza GUI.

**Validatore<T>** è l'interfaccia comune dei due validatori. È stata aggiunta perché i due controlli hanno la stessa forma e perché il regolamento chiede di usare interfacce e generics (VIN.1). I servizi la usano come tipo del loro campo, quindi le frecce `-validatore` arrivano a `Validatore` e non alle classi concrete:

```java
private final Validatore<DatiContatto> validatore;   // in ContattoServiceImpl
private final Validatore<DatiAttivita> validatore;   // in AttivitaServiceImpl
```

`mplan-app` passa `new ValidatoreContatto()` e `new ValidatoreAttivita()`, ma i servizi dipendono solo dall'interfaccia, come già succede con i repository.

**ValidatoreContatto** e **ValidatoreAttivita** controllano obbligatorietà, lunghezze e formati (DAT.1, DAT.2, DAT.5). Ricevono i dati scritti dall'utente e restituiscono la lista degli errori, uno per campo. Se la lista è vuota i dati vanno bene.

**RegoleStatoAttivita** contiene la macchina a stati delle attività (FUN.6.2, FUN.6.3, Figura 8.1 della specifica). `puoPassareA(COMPLETATA, ANNULLATA)` restituisce `false`, `modificabile(ANNULLATA)` restituisce `false`. Sta nel core e non in `StatoAttivita` di api: così api resta un contratto senza logica.

**OrdinamentoAttivita** contiene i due modi di ordinare le attività:

- `PER_STORICO`: dalla più vecchia alla più recente come in una chat, e nello stesso giorno prima quelle senza orario (FUN.9, FUN.9.2);
- `PER_AGENDA`: prima quelle senza orario, poi per orario (FUN.10.2, FUN.10.3).

Si usano così: `lista.sort(OrdinamentoAttivita.PER_AGENDA)`. Il repository restituisce i dati senza ordinarli, così la regola sta in un posto solo.

**VoceRegistro** è una riga del registro delle modifiche: quando, quale attività, che tipo di modifica, motivo. Una volta scritta non cambia più, quindi è un **«record»** di Java come i record di api (punto 4 di `api-spiegazione.md`). **TipoModifica** dice il tipo: `MODIFICA`, `CAMBIO_STATO`, `ANNULLAMENTO` (DAT.4). Stanno nel core perché la GUI non li usa.

## 9. Package `servizi`

Le tre classi `...ServiceImpl` implementano le interfacce di api. Non contengono regole: le chiedono a `dominio` e salvano tramite `repository`.

Hanno un costruttore esplicito che riceve tutti i pezzi di cui hanno bisogno, per esempio `AttivitaServiceImpl(attivita, contatti, transazioni, validatore, clock)`. Il servizio non crea da solo i suoi collaboratori: glieli passa `mplan-app`. Nei test invece si passano un repository finto e un `Clock` fermo su una data, così il risultato è sempre lo stesso (D8, D9). Il costruttore esplicito serve anche a Rebel per generare i campi `private final`.

**ContattoServiceImpl**: `elenca()`, `trova(id)`, `crea(dati)`, `modifica(id, dati)`, `valida(dati)`. Usa `ContattoRepository`, `GestoreTransazioni` e un `Validatore<DatiContatto>`.

**AttivitaServiceImpl**:

| Metodo | Parametri e scopo |
|---|---|
| `trova(id)` | Carica un'attività dal suo id, per il Form attività aperto dallo storico o dall'agenda (GUI.4.2, GUI.7.2) |
| `crea(idContatto, dati)` | `idContatto` è il contatto della scheda aperta, `dati` è ciò che l'utente ha scritto. Prima controlla con `contatti.trova(idContatto)` che il contatto esista (DAT.2.6). Restituisce l'attività creata, con stato *da fare* |
| `modifica(id, dati)` | Aggiorna i testi, per esempio la descrizione scritta dopo il lavoro |
| `segnaParziale(id)`, `segnaCompletata(id)` | Basta l'id: lo stato di arrivo è già nel nome |
| `annulla(id, motivo)` | Il motivo è obbligatorio (DAT.3.1): se è vuoto o supera i 1000 caratteri il servizio lancia `ValidazioneException`, che la GUI mostra. L'attività resta nello storico |
| `storico(idContatto)` | Tutte le attività del contatto, già ordinate con `PER_STORICO` |
| `valida(dati)` | La GUI la chiama prima di salvare per mostrare gli errori campo per campo (D4) |
| `aggiungiOsservatore(osservatore)` | Ereditato da `OsservabileAttivita`: registra chi deve essere avvisato dei cambiamenti (vedi il punto 10) |

Usa `AttivitaRepository`, `ContattoRepository`, `GestoreTransazioni`, un `Validatore<DatiAttivita>`, `Clock` (per la data proposta, che è oggi), e chiede a `RegoleStatoAttivita` e `OrdinamentoAttivita`.

Il controllo del contatto sta nel core e non nel database perché DAT.2.6 si verifica con un test di unità sui repository in memoria (D8), dove la chiave esterna del database non c'è.

**AgendaServiceImpl**: `attivitaDelGiorno(giorno)` restituisce una `VoceAgenda` per ogni attività di quel giorno, ordinate con `PER_AGENDA`. `VoceAgenda` è un record di api con l'attività e il nome del contatto, che il servizio legge con `ContattoRepository`. Così ogni riga dell'agenda mostra orario, contatto e titolo (GUI.7.1) e la GUI non deve cercare il contatto di ogni attività. La GUI chiama `attivitaDelGiorno` con le frecce e con "Vai a...". `oggi()` dice qual è il giorno corrente, perché l'agenda si apre su oggi (FUN.10.4). Così la GUI non legge l'orologio del PC e tutto resta provabile con un `Clock` fermo.

**Gli id.** `ContattoServiceImpl` e `AttivitaServiceImpl` creano l'id del nuovo oggetto con `UUID.randomUUID()` dentro `crea`. Non serve un generatore passato nel costruttore come il `Clock`: nessuna regola dipende dal valore dell'id e nei test l'id si legge dall'oggetto restituito. Non c'è nemmeno un controllo dei doppioni, perché la chiave primaria della tabella rifiuta già un id ripetuto (design, D3).

**Gli errori.** Gli errori di digitazione si vedono con `valida()`, campo per campo (D4). Le operazioni che la GUI non offre, come un'attività su un contatto inesistente o un cambio di stato non ammesso, sono errori di programmazione: il servizio lancia `IllegalArgumentException` o `IllegalStateException`. Se `crea` o `modifica` ricevono dati che il validatore rifiuta, o `annulla` un motivo vuoto o troppo lungo, il servizio lancia `ValidazioneException` con la lista degli errori, come quella di `valida()`; i guasti del database arrivano dalla persistenza come `PersistenzaException`. Queste due eccezioni stanno in api ed estendono `RuntimeException`, quindi non vanno dichiarate nelle firme (D4). I costruttori di `Contatto` e `Attivita` non applicano regole: controllano solo che i campi obbligatori non siano `null` e, se ne manca uno, lanciano `NullPointerException`, perché è un errore di programmazione (D3).

## 10. Il pattern Observer

Il registro delle modifiche (FUN.13) è fatto con il pattern Observer:

| Ruolo | Classe |
|---|---|
| soggetto (classe astratta) | `OsservabileAttivita`: tiene la lista `osservatori`, li registra con `aggiungiOsservatore(osservatore)` e li avvisa con i metodi protetti `notificaModifica(attivita)`, `notificaCambioStato(attivita, precedente)`, `notificaAnnullamento(attivita, motivo)` |
| soggetto concreto | `AttivitaServiceImpl`: estende `OsservabileAttivita` e chiama il metodo `notifica...` giusto a ogni modifica, cambio di stato o annullamento |
| osservatore (interfaccia) | `OsservatoreAttivita`: `attivitaModificata(attivita)`, `statoCambiato(attivita, precedente)`, `attivitaAnnullata(attivita, motivo)` |
| osservatore concreto | `RegistroModifiche`: crea la `VoceRegistro` con l'ora presa dal `Clock` e la salva con `RegistroRepository.aggiungi` |

`mplan-app` crea `RegistroModifiche` e lo registra con `aggiungiOsservatore`. La notifica avviene dentro la stessa transazione del salvataggio, quindi l'attività e la voce del registro si salvano insieme o non si salvano affatto.

Il soggetto è una superclasse astratta, come nella forma del pattern vista a lezione. Non usiamo `java.util.Observable`, che ha la stessa forma, perché è deprecata da Java 9 e passa i dati come `Object`, senza controllo dei tipi: `OsservabileAttivita` fa la stessa cosa con i tipi del progetto.

Perché questa scelta: il servizio non deve sapere come si scrive il registro, deve solo avvisare. È anche la risposta alla domanda di Michele sulla singola responsabilità: `AttivitaServiceImpl` coordina i passi di un caso d'uso, le regole stanno in `dominio`, il salvataggio nei repository, la gestione degli osservatori in `OsservabileAttivita` e il registro in `RegistroModifiche`. Se in futuro servisse un altro effetto, per esempio la notifica serale di FUN.14, basterebbe aggiungere un altro osservatore senza toccare il servizio.

Degli altri pattern visti a lezione:

- **Singleton**: non serve nel core, perché i servizi ricevono i collaboratori dal costruttore per poterli sostituire nei test. Non serve neanche nella GUI: ogni schermata ha la sua rotta di Vaadin (design, D7) e il `Navigatore`, che ricorda la schermata di provenienza per tornare indietro (GUI.1.1), lo crea Spring una volta per ogni scheda del browser (`@UIScope`). Con un Singleton tutte le schede userebbero la stessa pila di schermate;
- **Abstraction-Occurrence**: servirebbe un'entità "lavoro" che raggruppa le attività di più giorni, ma la specifica sceglie apposta un'attività per giorno (FUN.7);
- **General Hierarchy**: nel progetto non c'è nessuna struttura ad albero;
- **Player-Role**: servirebbero ruoli separati per privato, azienda e cantiere, mentre la specifica vuole un solo contatto con un tipo facoltativo (FUN.1.3, DAT.1.7).

## 11. Esempio completo: l'utente annulla un'attività

1. La GUI chiama `attivitaService.annulla(id, "cliente assente")`. Vede solo l'interfaccia di api.
2. `AttivitaServiceImpl` carica l'attività con `attivita.trova(id)`.
3. Controlla `RegoleStatoAttivita.puoPassareA(statoAttuale, ANNULLATA)`. Se la risposta è no, lancia `IllegalStateException`, perché la GUI non offre l'annullamento di un'attività completata o già annullata.
4. Dentro `transazioni.inTransazione(...)` salva l'attività con `attivita.aggiorna(...)` e avvisa gli osservatori con `notificaAnnullamento(attivita, motivo)`, che chiama `attivitaAnnullata(attivita, motivo)` su ognuno.
5. `RegistroModifiche` crea una `VoceRegistro` di tipo `ANNULLAMENTO` e la salva con `registro.aggiungi(voce)`.
6. Il repository vero, in `mplan-persistence`, scrive nel database. Il core non sa come.

## 12. Cosa resta da fare

- Nel core non resta niente da disegnare: il diagramma di `mplan-api` ora c'è ed è spiegato in `api-spiegazione.md`. Il codice del core si scrive dopo la generazione con Rebel.
- Dopo la generazione `VoceRegistro` va trasformata a mano in record, come i quattro record di api, perché Rebel genera una classe normale.
