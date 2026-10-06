# MPMat – Intervista di Elicitazione dei Requisiti

**Progetto:** MPStock  
**Data dell'intervista:** Da decidere

---

## 🎯 Obiettivo del Documento

Questo questionario serve a condurre una sessione strutturata di elicitazione dei requisiti (secondo i principi del Capitolo 9 e dello standard IEEE 830). L'obiettivo è comprendere il flusso operativo reale, identificare i vincoli di dominio, definire i casi d'uso primari e delimitare con precisione il perimetro del software (Scope Management).

---

## 1\. Scenario Operativo Attuale e Flusso di Lavoro (AS-IS)

* **D1.1:** Come viene gestito attualmente il magazzino e il controllo dei materiali prima di uscire per un cantiere/intervento?

  * *Note / Risposta:*

---

## 2\. Tipologie di Materiale e Dati dell'Articolo (Dominio)

* **D2.1:** Come vengono identificati i prodotti in magazzino? È sufficiente il nome del pezzo?  
    
  * *Note / Risposta:*


* **D2.2:** Come misuro la quantità di ciascun materiale?  
    
  * *Pezzi singoli / discreti* (interruttori, prese, quadri, differenziali)?  
  * *Materiali continui a misura* (metri di cavo, canaline, tubi corrugati, matasse)?  
  * *Note / Risposta:*


  
---

## 3\. Preparazione dell'Intervento / Kit Lavoro (Core Use Case)

* **D3.1:** Come si intende comporre la lista del materiale necessario per un determinato lavoro?  
    
  - [ ] Selezionando prodotti da un catalogo esistente  
  - [ ] Inserendo codice rapido e quantità  
  - [ ] Altro: \_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_  
  * *Note / Risposta:*


* **D3.2:** Cosa deve mostrare la schermata al momento della verifica della disponibilità?  
    
  - [ ] Un indicatore chiaro di esito (es. Verde \= Tutto presente, Rosso \= Mancanze)  
  - [ ] Il dettaglio riga per riga di ciò che è presente e di quanti pezzi mancano  
  * *Note / Risposta:*

* **D3.4:**  È necessario indicare i costi dei materiali di ciascun lavoro?

  * *Note / Risposta:*

---

## 4\. Gestione Mancanze e Lista della Spesa (Riordino)

* **D4.1:** Per gli articoli non disponibili a magazzino, quale formato deve avere la "Lista della Spesa / Riordino"?  
    
  - [ ] Un riepilogo a video con i soli pezzi mancanti e le rispettive quantità  
  - [ ] Una distinta esportabile o inviabile (es. testo/messaggio per il fornitore)  
  * *Note / Risposta:*


* **D4.2:** Quando il materiale ordinato viene finalmente acquistato e portato in magazzino, come preferisce caricarlo a sistema?  
    
  - [ ] Conferma rapida della lista della spesa precedentemente generata ("Merce arrivata")  
  - [ ] Incremento manuale delle giacenze articolo per articolo  
  * *Note / Risposta:*

---

## 5\. Soglie Minime e Monitoraggio Scorte (Avvisi Sottoscorta)

* **D5.1:** Esistono materiali di consumo comune (es. morsetti, nastro isolante, prese standard) per i quali è utile definire una "scorta minima di sicurezza"?  
    
  * *Note / Risposta:*


* **D5.2:** Cosa deve accadere quando la quantità di un materiale scende al di sotto della soglia minima?  
    
  - [ ] Evidenziazione visiva (es. riga gialla/rossa nel catalogo)  
  - [ ] Inserimento automatico nella lista dei riordini suggeriti  
  * *Note / Risposta:*

---

## 6\. Rientro dal Cantiere (Gestione Resi / Avanzi)

* **D6.1:** A fine giornata, cosa succede con i materiali prelevati ma non utilizzati? Preferisce indicare i materiali usati?  
  - [ ] Capita raramente, possono essere reinseriti manualmente come nuovo carico  
  - [ ] È utile una funzione "Rientro Merce" legata all'intervento svolto  
  * *Note / Risposta:*

---

## 7\. Requisiti Non Funzionali e Vincoli di Utilizzo (IEEE 830\)

* **D7.1:** Quali sono i requisiti di usabilità prioritari?  
    
  * *Note / Risposta:* semplicità di utilizzo
