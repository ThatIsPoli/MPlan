# MPlan – Riassunto delle esigenze del cliente

**Progetto:** MPlan  
**Fonte:** intervista del 08/10/2026 (`intervista-01-domande-risposte.md`)  
**Stato del documento:** bozza da validare con il cliente e con il team

---

## 1. Chi è il cliente e cosa gli serve

Il cliente è un professionista che esegue interventi presso clienti privati e cantieri. Vuole tenere traccia degli appuntamenti e, per ogni cliente, sapere cosa è stato fatto e cosa resta da fare.

Vuole un'applicazione semplice che gli permetta di riassumere il lavoro della giornata, preparare le informazioni necessarie per il giorno successivo e ritrovare a fine mese tutti gli interventi per la fatturazione.

---

## 2. Cosa vuole che il software faccia

1. **Archiviare clienti e cantieri**, inserendoli a mano, con nome, cognome, indirizzo, telefono (anche più contatti), codice fiscale e/o partita IVA. La distinzione tra privato e azienda può essere gestita con un semplice flag, ma non è fondamentale.
2. **Gestire lavori non legati a un singolo cliente**, tramite una voce generica ("Generale") per interventi non riferibili a un cliente preciso. Il team osserva anche che il nome del luogo o del cantiere può essere trattato come una voce della stessa lista.
3. **Registrare le attività** (interventi), con titolo breve, data, descrizione di ciò che è stato fatto e note, ad esempio materiali mancanti.
4. **Indicare data e orario in modo indicativo**: non è richiesta la precisione ora per ora; basta sapere se l'attività è a mezza giornata o a giornata intera.
5. **Compilare l'attività in due momenti**: prima solo il titolo breve, dopo l'intervento la descrizione di ciò che è stato fatto e le note su ciò che manca o serve per la volta successiva.
6. **Modificare le attività dopo la creazione**, anche quando il lavoro è stato svolto solo in parte.
7. **Indicare lo stato di un'attività**: completata, parziale o annullata. Il cliente lo considera opzionale.
8. **Gestire lavori lunghi su più giorni** creando una voce per ciascun giorno di lavoro e annotando cosa è stato fatto e cosa manca.
9. **Annotare i materiali** usati, mancanti o necessari come testo libero nelle note. La nota su ciò che serve per la visita successiva va scritta nell'attività del giorno stesso, non in quella del giorno dopo.
10. **Consultare lo storico** di ogni cliente: nella scheda, sotto i dati principali, in forma di calendario, con la possibilità di creare una nuova attività.
11. **Vedere lo storico per la settimana e per la fatturazione di fine mese**, così da elencare tutti gli interventi effettuati.
12. **Vedere un elenco di tutti i contatti e cantieri** nella schermata iniziale.
13. **Vedere un calendario generale** con le attività di tutti i clienti, accessibile dalla schermata iniziale tramite un menu a tendina. Sono state citate le viste giornaliera e mensile.
14. **Annullare le attività senza eliminarle**, indicando il motivo, così da mantenere completo lo storico.
15. **Tracciare le modifiche/annullamenti**: nella prima versione mono-utente resta il motivo dell'annullamento; quando ci saranno più utenti, potrà essere tracciato anche l'autore.
16. **Limitare l'accesso al team di lavoro del cliente**. Nella prima versione, però, l'applicazione sarà mono-utente e senza sistema di accesso; in futuro, con più dispositivi, è prevista una password unica generale.

---

## 3. Cosa ha detto di non volere o non avere bisogno

- Non vuole complicazioni: preferisce poche informazioni da compilare.
- Non ha chiesto precisione ora per ora: bastano data e orario indicativo, con l'indicazione di mezza giornata o giornata intera.
- Non è emersa la richiesta di un catalogo o di codici articolo per i materiali.
- Non ha chiesto una funzione di gestione del magazzino, di giacenze, di soglie minime o di costi dei materiali.
- Non ha chiesto una lista dei materiali generata automaticamente: per ora basta una nota nell'attività.
- A un certo punto ha detto che il calendario non era indispensabile e che poteva bastare una lista per data. Nella versione finale emersa negli ultimi minuti, però, il calendario è richiesto nella scheda del cliente ed è previsto anche un calendario generale.

---

## 4. Decisioni del team per la prima versione

- Applicazione **desktop per PC (Windows)**, **mono-utente**.
- **Nessun sistema di accesso** nella prima versione.
- **Database embedded locale**, in linea con il regolamento del corso. Il cliente vuole poter consultare la storia per anni.
- **Fuori scope**: utente master e ruoli, mobile e accesso da più dispositivi nella prima versione.
- **Evoluzioni future**: mobile e accesso da più dispositivi, con una password unica generale da non condividere con estranei al sistema.

---

## 5. Punti confermati o opzionali emersi dalle ripsoste iindicate con PARZIALE/PROPOSTO

1. La gestione definitiva dei lavori "generali" (condomini, luoghi ricorrenti): il cliente propone la voce "Generale", mentre il team osserva che il luogo/cantiere può essere trattato come una voce della stessa lista.
2. La gestione dello stato dell'attività (completata, parziale, annullata) è stata indicata dal cliente come **opzionale**.
3. La notifica a fine giornata per ricordare di compilare le note è una proposta degli sviluppatori ed è considerata **opzionale** dal cliente.
4. Nella prima versione l'applicazione sarà mono-utente; il tema degli accessi del team e della password unica riguarda una futura evoluzione.
