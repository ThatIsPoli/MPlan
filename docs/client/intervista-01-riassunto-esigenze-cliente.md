# MPlan – Riassunto delle esigenze del cliente

**Progetto:** MPlan
**Fonte:** intervista del 08/10/2026 (`intervista-02-domande-risposte.md`) e appunti del team
**Stato del documento:** bozza da validare con il cliente e con il team

---

## 1. Chi è il cliente e cosa gli serve

Il cliente è un professionista che esegue interventi presso clienti privati e cantieri (esempi: sistemare un cancello, posare cavi e tubi). Lavora su più clienti nella stessa settimana e spesso ritorna sullo stesso lavoro più giorni.

Vuole un'applicazione semplice per **tenere traccia dei clienti e di ciò che fa per ciascuno**: cosa c'è da fare, cosa è stato fatto, cosa manca per la volta successiva. Lo storico gli serve anche per la fatturazione a fine mese.

---

## 2. Cosa vuole che il software faccia

1. **Archiviare i clienti e i cantieri**, inserendoli a mano, con nome, cognome, indirizzo, telefono, codice fiscale e/o partita IVA. La distinzione tra privato e azienda può essere gestita con un semplice flag, ma non è fondamentale.
2. **Registrare le attività** (interventi), con titolo breve, data, orario indicativo (mezza o intera giornata), descrizione di ciò che è stato fatto e note su ciò che manca.
3. **Compilare l'attività in due momenti**: prima solo il titolo, dopo l'intervento descrizione e note. Le attività si possono aggiornare.
4. **Indicare lo stato di un'attività**: completata, parziale o annullata.
5. **Annotare i materiali** usati, mancanti o necessari come testo libero nelle note. La nota su ciò che serve per la visita successiva va scritta nell'attività del giorno stesso, non in quella del giorno dopo.
6. **Consultare lo storico** di ogni cliente: nella scheda, sotto i dati principali, in forma di calendario, con la possibilità di creare una nuova attività.
7. **Vedere un elenco di tutti i contatti e cantieri** nella schermata iniziale.
8. **Vedere un calendario generale** con le attività di tutti i clienti, aprendolo da un menu a tendina nella schermata iniziale. Il cliente ha citato le viste giornaliera e mensile.
9. **Annullare le attività senza eliminarle**, indicando il motivo, per non perdere lo storico.
10. **Limitare l'accesso al solo team di lavoro del cliente.** Come e quando non è stato specificato; per la prima versione mono-utente non è rilevante.

---

## 3. Cosa ha detto di non volere o non avere bisogno

- Non vuole complicazioni: preferisce poche informazioni da compilare.
- Non ha chiesto precisione oraria: bastano mezza giornata o giornata intera.
- Non ha chiesto una funzione di gestione del magazzino, di giacenze, di soglie minime o di costi dei materiali.
- Non ha chiesto una lista dei materiali generata automaticamente: per ora basta una nota.
- A un certo punto ha detto di non volere necessariamente un calendario, preferendo una lista per data. Il team ha poi reintrodotto il calendario (scheda cliente e calendario generale).

---

## 4. Decisioni del team per la prima versione

- Applicazione **desktop per PC (Windows)**, **mono-utente**, senza accesso.
- **Database embedded locale**, in linea con il regolamento del corso.
- **Fuori scope**: utente master e ruoli, mobile, accesso da più dispositivi.
- **Evoluzioni future**: mobile e più dispositivi, con una password unica generale non condivisibile con estranei.

---

## 5. Punti aperti da chiarire con il cliente

1. Come trattare in modo definitivo i lavori "generali" (condomini, luoghi ricorrenti): voce "Generale" o cantiere come contatto?
2. Confermare che le attività si annullano con un motivo e non si eliminano.
3. In futuro, come e quando deve accedere il team di lavoro (password unica, più dispositivi)?
4. Serve una notifica a fine giornata per ricordare di compilare le note?
5. Il perimetro include o esclude la gestione di materiali, giacenze e costi? (Il prototipo iniziale del progetto e il README li prevedevano, ma nell'intervista non sono emersi.)

---

## 6. Limiti di questo documento

I punti segnati PARZIALE o PROPOSTA in `intervista-02-domande-risposte.md` vanno confermati prima di trasformarli in requisiti.
