# MPlan – Intervista di elicitazione dei requisiti: domande e risposte

**Progetto:** MPlan
**Registrazione dell'intervista:** 08/10/2026 (audio di circa 45 minuti)
**Partecipanti:** il cliente (C) e due sviluppatori (S1, S2)
**Stato del documento:** validato con il cliente e con il team (12/11/2026)

---

## Come leggere questo documento

Per non introdurre requisiti inventati ogni risposta ha uno stato:

| Stato | Significato |
|---|---|
| **EMERSO** | Detto chiaramente dal cliente |
| **PARZIALE** | Emerso in modo incompleto o poco leggibile: da confermare |
| **PROPOSTA** | Idea degli sviluppatori, non confermata dal cliente |
| **NON EMERSO** | Non affrontato nell'intervista |


---

## 1. Contesto e flusso di lavoro attuale (AS-IS)

**D1.1 – Come gestisce oggi appuntamenti, clienti e interventi?**
- **Risposta:** il cliente usa un calendario in cui segna gli appuntamenti già fissati con i clienti. 
- **Stato:** EMERSO 

**D1.2 – Quali difficoltà vuole risolvere con l'applicazione?**
- **Risposta:** vuole sapere cosa è stato fatto e cosa resta da fare per ogni cliente, riassumere il lavoro della giornata, preparare i materiali per il giorno dopo e ritrovare a fine mese tutti gli interventi per la fatturazione.
- **Stato:** EMERSO 

---

## 2. Anagrafica dei clienti

**D2.1 – Quali dati vuole registrare per ogni cliente?**
- **Risposta:** nome, cognome, indirizzo, telefono (anche più contatti), codice fiscale e/o partita IVA ("se possibile, tanto meglio"). I dati servono anche per la fatturazione.
- **Stato:** EMERSO 

**D2.2 – Come devono essere inseriti i clienti?**
- **Risposta:** a mano, tramite una funzione di creazione cliente. Il cliente non ha un archivio da importare.
- **Stato:** EMERSO 

**D2.3 – Serve distinguere i privati dalle aziende?**
- **Risposta:** magari utilizzare un flag ma non è fondamentale.
- **Stato:** EMERSO 

**D2.4 – Come gestire i lavori non legati a un singolo cliente (condomini, "generale")?**
- **Risposta:** il cliente propone una voce generica ("Generale") dove segnare gli interventi non riferibili a un cliente preciso, ad esempio lavori ricorrenti in uno stesso luogo. Gli sviluppatori osservano che è equivalente a inserire il nome del luogo o del cantiere come se fosse un cliente.
- **Stato:** EMERSO

**D2.5 – Come distinguere un cantiere dal cliente?**
- **Risposta:** la soluzione adottata dal team è trattare contatti e cantieri come voci della stessa lista salvata nell'applicazione.
- **Stato:** EMERSO

---

## 3. Attività / interventi

**D3.1 – Quali informazioni compongono un'attività?**
- **Risposta:** un titolo breve (cosa c'è da fare), la data, una descrizione di ciò che è stato fatto e delle note, ad esempio materiali mancanti.
- **Stato:** EMERSO

**D3.2 – Quanta precisione serve su data e orario?**
- **Risposta:** serve una data e un orario indicativo. Non è richiesta la precisione ora per ora: basta sapere se l'attività è a mezza giornata o a giornata intera. Nella versione prevista il modulo di creazione contiene descrizione, note, orario e data.
- **Stato:** EMERSO 

**D3.3 – Cosa si scrive prima e cosa dopo l'intervento?**
- **Risposta:** prima si scrive solo il titolo breve (poche parole). Dopo l'intervento si completano la descrizione di ciò che è stato fatto e le note su ciò che manca o serve per la volta successiva.
- **Stato:** EMERSO

**D3.4 – Un'attività si può modificare dopo la creazione?**
- **Risposta:** sì. Il cliente può segnare un lavoro come da fare, poi aggiornare la descrizione quando lo ha svolto, anche solo in parte.
- **Stato:** EMERSO

**D3.5 – Come si indica lo stato di avanzamento?**
- **Risposta:** sono emersi i casi "fatto" e "fatto in parte", oltre ad "annullato" (vedi D7.1). Il cliente ha citato completata, parziale e annullata. Il cliente però lo reputa come opzionale.
- **Stato:** EMERSO

**D3.6 – Come si gestiscono i lavori lunghi su più giorni?**
- **Risposta:** i cantieri lunghi restano come attività, con una voce per ciascun giorno in cui si lavora. Per ognuno si annotano cosa è stato fatto e cosa manca. Il cliente non ha chiesto una funzione specifica di pianificazione di cantiere.
- **Stato:** EMERSO

---

## 4. Materiali

**D4.1 – Come annotare materiali usati, mancanti o necessari?**
- **Risposta:** come testo libero nelle note dell'attività (esempio: "mancano 30 metri di tubo", "cavo di tipo X"). Non è emersa la richiesta di un catalogo o di codici articolo.
- **Stato:** EMERSO

**D4.2 – Serve una lista dei materiali da preparare per il giorno dopo?**
- **Risposta:** il cliente vuole poter scrivere a fine giornata cosa serve per la visita successiva. Per ora basta una nota, non una lista generata automaticamente. Il cliente desidera che la nota vada nell'attività del giorno stesso e non in quella del giorno successivo.
- **Stato:** EMERSO

**D4.3 – Servono costi dei materiali o la gestione di giacenze/magazzino?**
- **Risposta:** nell'intervista non se ne è parlato. Il perimetro attuale non comprende magazzino, giacenze, soglie minime o costi.
- **Stato:** NON EMERSO

---

## 5. Storico e schermate principali

**D5.1 – Cosa deve vedere aprendo la scheda di un cliente?**
- **Risposta:** i dati principali del cliente in alto e, subito sotto, lo storico di tutti gli interventi, mostrato in forma di calendario. Da lì si può creare una nuova attività.
- **Stato:** EMERSO

**D5.2 – Come userà lo storico?**
- **Risposta:** per vedere cosa è stato fatto per un cliente nella settimana e, a fine mese, per elencare tutti gli interventi in vista della fatturazione.
- **Stato:** EMERSO

**D5.3 – Cosa mostra la schermata iniziale?**
- **Risposta:** l'elenco di tutti i contatti e cantieri salvati.
- **Stato:** EMERSO

---

## 6. Calendario

**D6.1 – Serve un calendario? Con quali viste?**
- **Risposta:** all'inizio il cliente lo chiede, con slot di giornata intera o mezza giornata. A un certo punto dice che non è indispensabile e che gli basta una lista per data. Nella versione finale emersa negli ultimi minuti il calendario è invece richiesto, nella scheda del cliente. Il cliente ha citato le viste giornaliera e mensile (magari tramite frecce).
- **Stato:** EMERSO (la richiesta è cambiata durante l'intervista)

**D6.2 – Serve un calendario generale con le attività di tutti i clienti?**
- **Risposta:** sì, accessibile dalla schermata iniziale tramite un menu a tendina, per vedere le attività svolte di tutti i clienti.
- **Stato:** EMERSO

---

## 7. Annullamento e tracciabilità

**D7.1 – Eliminare o annullare un'attività?**
- **Risposta:** si preferisce non eliminare le attività ma annullarle indicando il motivo, così lo storico resta completo.
- **Stato:** EMERSO

**D7.2 – Serve tracciare chi ha modificato o annullato e perché?**
- **Risposta:** gli sviluppatori propongono un registro delle modifiche con il motivo e, quando ci saranno più utenti, anche l'autore. Nella prima versione, mono-utente, resterebbe il motivo dell'annullamento.
- **Stato:** EMERSO

---

## 8. Utenti e accessi

**D8.1 – Chi userà l'applicazione?**
- **Risposta:** durante l'intervista si è parlato di più utenti e di un ruolo "master". Per decisione del team la prima versione sarà mono-utente e l'utente master è fuori scope.
- **Stato:** EMERSO (poi ridimensionato dal team)

**D8.2 – Quali dati condividere con altre persone e quali no?**
- **Risposta:** Il cliente ha specificato che solo il suo team di lavoro può accedere al portale tramite pc.
- **Stato:** EMERSO

**D8.3 – Serve un sistema di accesso?**
- **Risposta:** nella prima versione nessun accesso. In futuro, con più dispositivi, una password unica generale da non condividere con estranei al sistema.
- **Stato:** EMERSO

---

## 9. Piattaforma e vincoli

**D9.1 – Su quali dispositivi deve funzionare?**
- **Risposta:** prima solo su computer (Windows). Il cellulare è atteso in una fase successiva, e in futuro l'accesso da più dispositivi.
- **Stato:** EMERSO

**D9.2 – Dove e come conservare i dati?**
- **Risposta:** per la prima versione, un database embedded locale sul PC. È una scelta tecnica del team, in linea con il regolamento del corso. Il cliente vuole poter consultare la storia per anni.
- **Stato:** EMERSO (scelta del team)

**D9.3 – Servono notifiche?**
- **Risposta:** gli sviluppatori propongono una notifica a fine giornata per ricordare di compilare le note. Per il cliente è un aspetto opzionale.
- **Stato:** EMERSO 

**D9.4 – Quali sono i requisiti di usabilità?**
- **Risposta:** non è stata fatta una domanda esplicita. Dall'insieme emerge una forte preferenza per la semplicità: poche informazioni da scrivere (titolo breve, poi descrizione e note) e niente complicazioni ("non facciamo confusione").
- **Stato:** EMERSO
