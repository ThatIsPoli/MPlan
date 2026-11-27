# Registro delle modifiche ai requisiti UI (Restyling MPlan)

Questo documento tiene traccia delle scelte grafiche e strutturali apportate a `mplan-ui-vaadin` e `mplan-app` che modificano o integrano la specifica originale dei requisiti (Documento 3) e il design della GUI (Documento 4), spiegando **cosa** è cambiato, **come**, **dove** nel codice e **perché**.

---

## 1. Logo aziendale (`icona.png` al posto di `icona.svg`)

- **Cosa cambia rispetto a prima:** sostituita l'icona provvisoria quadrata blu (`M` bianca) con il logo ufficiale **MP** rosso di MP elettrica s.a.s., usato sia come favicon sia nella barra superiore dell'applicazione.
- **Come:** rimosso `icona.svg`, inserito `icona.png` (1254×1254), aggiornata la configurazione `AppShellConfigurator` (`addFavIcon`) e inserito il componente `Image` nella barra superiore.
- **Dove:**
  - `code/mplan-ui-vaadin/src/main/resources/META-INF/resources/mplan-tema/icona.png`
  - `code/mplan-app/src/main/java/it/mpelettrica/mplan/app/MPlanApplication.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/infrastruttura/LayoutPrincipale.java`
- **Perché:** dare all'applicazione un'identità visiva coerente con il marchio aziendale.

---

## 2. Barra superiore persistente su tutte le schermate (impatto su `GUI.2`, `GUI.2.2`, navigazione)

- **Cosa cambia rispetto ai requisiti (`GUI.2` / `GUI.2.2`):**
  - Nella specifica originale la `HomeView` esponeva nella propria intestazione i tre pulsanti *Nuovo contatto*, *Agenda giornaliera* e *Chiudi applicazione*, mentre le schermate interne non avevano una barra superiore condivisa.
  - Ora è presente una **barra superiore persistente** in tutte le schermate (incluse le pagine di errore `VistaErrore` e `VistaPaginaNonTrovata`) contenente:
    - logo **MP** + nome app **MPlan** (clic per tornare alla Rubrica);
    - voci di navigazione rapida **Rubrica** e **Agenda** (con evidenziazione della sezione attiva);
    - pulsante di chiusura a destra.
  - Di conseguenza, nella toolbar interna di `HomeView` resta solo il pulsante primario **Nuovo contatto** (affiancato al titolo *Rubrica* e al contatore dinamico dei contatti).
- **Come:** introdotta la classe `LayoutPrincipale` (`RouterLayout` + `AfterNavigationObserver`) e collegata tramite `@Route(..., layout = LayoutPrincipale.class)` e `@ParentLayout(LayoutPrincipale.class)`.
- **Dove:**
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/infrastruttura/LayoutPrincipale.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/schermate/HomeView.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/schermate/AgendaGiornalieraView.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/schermate/SchedaContattoView.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/schermate/FormContattoView.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/schermate/FormAttivitaView.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/infrastruttura/VistaErrore.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/infrastruttura/VistaPaginaNonTrovata.java`
- **Perché:** rendere costante l'orientamento dell'utente in tutta l'applicazione, permettere di aprire l'agenda o tornare in rubrica da qualsiasi schermata senza affollare la toolbar della Home, mantenendo comunque l'azione *Agenda giornaliera* sempre visibile (anzi estesa a tutte le viste).
- **Nota per l'aggiornamento del Documento 3:** aggiornare `GUI.2` / `GUI.2.2` precisando che la navigazione *Rubrica* / *Agenda* e il comando *Chiudi applicazione* risiedono nella barra superiore globale dell'app.

---

## 3. Pulsante "Chiudi applicazione" come sola icona discreta (impatto su `GUI.2`, Tabella 8.2)

- **Cosa cambia rispetto ai requisiti (`GUI.2`):** il pulsante di arresto del server locale (`chiudi`) non mostra più l'etichetta testuale `"Chiudi applicazione"` in rosso acceso dentro la toolbar della Home, ma è rappresentato da una **sola icona discreta** (`POWER_OFF`) all'estremità destra della barra superiore.
- **Come:** `Button` terziario con sola icona `VaadinIcon.POWER_OFF`, `aria-label="Chiudi applicazione"` (accessibilità), `tooltipText="Chiudi applicazione"` al passaggio del mouse e stile di hover rosso tenue; il comportamento (`Notification` + `ChiusuraApplicazione.chiudi()`) resta invariato.
- **Dove:**
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/infrastruttura/LayoutPrincipale.java`
  - `code/mplan-ui-vaadin/src/main/resources/META-INF/resources/mplan-tema/tema.css` (`.mplan-topbar-chiudi`)
- **Perché:** il testo lungo in rosso maiuscolo appesantiva visivamente la schermata principale dando un falso senso di allarme; l'icona dedicata nell'angolo in alto a destra segue le convenzioni desktop pur restando accessibile e chiara tramite tooltip.
- **Nota per l'aggiornamento del Documento 3:** specificare in `GUI.2` che il comando `chiudi` è un pulsante icona nella barra superiore con tooltip `"Chiudi applicazione"`.

---

## 4. Pulsante "Indietro" nell'intestazione dei form (impatto su `GUI.3`, `GUI.5`)

- **Cosa cambia rispetto ai requisiti:** oltre al pulsante *Indietro* in fondo alla card accanto a *Salva*, `FormContattoView` e `FormAttivitaView` mostrano ora un pulsante *Indietro* con freccia anche in alto a sinistra accanto al titolo della pagina, allineandosi a `SchedaContattoView` e `AgendaGiornalieraView`.
- **Come:** aggiunto `Button("Indietro", VaadinIcon.ARROW_LEFT.create(), ...)` in `creaIntestazione()`.
- **Dove:**
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/schermate/FormContattoView.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/schermate/FormAttivitaView.java`
- **Perché:** coerenza di navigazione su tutte le schermate secondarie ed evitamento dello scorrimento fino in fondo al form per uscire senza salvare.

---

## 5. Sistema grafico, palette, tipografia e ordine controlli (stile Cal.com + Vercel)

- **Cosa cambia rispetto al design precedente:**
  - **Palette e primario:** abbandonato il blu `#024ad8` a favore di una scala neutra chiara (`#f4f4f5` sfondo, `#ffffff` superfici, `#e4e4e7` bordi `1px`) con **nero antracite (`#18181b`)** per le azioni primarie; il rosso resta riservato al marchio MP, ad avvisi/errori e all'annullamento.
  - **Tipografia e pulsanti:** rimosso il riferimento al font inesistente `"Forma DJR Micro"` (resta solo `Inter` locale in `InterVariable.woff2`, quindi **rispettato `NFR.6`** senza richieste esterne); rimosso `text-transform: uppercase` dai pulsanti e dai badge di stato.
  - **Badge di stato (`GUI.8`):** pillole arrotondate con pallino semantico colorato (grigio neutro *Da fare*, ambra *Parziale*, verde *Completata*, rosso *Annullata*); mantenuto invariato il titolo barrato per le attività annullate (`GUI.8`).
  - **Barra giorno in Agenda (`GUI.7`):** raggruppati i controlli `<` `Oggi` `>` a sinistra, la data estesa al centro e il selettore *Vai a…* a destra.
  - **Barra stato nel Form Attività (`GUI.5.2`):** disposta su una singola riga orizzontale (badge stato a sinistra, pulsanti di transizione a destra) invece che impilata in verticale.
  - **Ricerca in Rubrica (`GUI.2`):** spostate le indicazioni di ricerca direttamente nel placeholder (`"Cerca per nome, telefono, codice fiscale o partita IVA…"`), eliminando il testo di aiuto ridondante sotto il campo, e aggiunto un contatore dinamico dei contatti sotto al titolo *Rubrica*.
- **Dove:**
  - `code/mplan-ui-vaadin/src/main/resources/META-INF/resources/mplan-tema/tema.css`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/schermate/HomeView.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/schermate/AgendaGiornalieraView.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/componenti/BarraStatoAttivita.java`
- **Perché:** migliorare leggibilità, gerarchia visiva, compattezza e coerenza geometrica su tutto il gestionale desktop senza alterare le regole di dominio.

---

## 6. Rimozione della scritta `"MP elettrica"` nella topbar

- **Cosa cambia:** nella barra superiore, accanto al logo **MP** e al nome **MPlan**, è stato rimosso il badge testuale `"MP elettrica"`.
- **Come e dove:** rimosso lo `Span` `.mplan-topbar-azienda` da `LayoutPrincipale.java` e le relative regole da `tema.css`.
- **Perché:** il logo aziendale rende già immediata l'identità di marca e la topbar risulta più pulita.

---

## 7. Contrasto hover e tag `Privato` / `Azienda` in Rubrica (`GUI.2`)

- **Cosa cambia:**
  - Aumentato il contrasto di hover su righe della tabella contatti ed elenchi attività passando da `#fafafa` a `#f1f1f4` (`--mplan-colore-hover`).
  - Il tag **Privato** usa ora sfondo bianco `#ffffff` con bordo sottile `#e4e4e7` e testo `#52525b` (non sembra più un pulsante premuto); il tag **Azienda** usa una tinta azzurra tenue (`#eff6ff` / `#dbeafe` / `#1e40af`).
- **Dove:** `code/mplan-ui-vaadin/src/main/resources/META-INF/resources/mplan-tema/tema.css`.
- **Perché:** rendere subito percepibile la riga sotto il cursore e distinguere con chiarezza i due tipi di contatto senza creare ambiguità con bottoni cliccabili.

---

## 8. Altezza adattiva di tabelle e liste (`HomeView`, `SchedaContattoView`, `AgendaGiornalieraView`, `NFR.3`)

- **Cosa cambia:** i riquadri di Rubrica, Storico attività e Agenda giornaliera non occupano più tutto lo spazio vuoto fino al fondo della finestra con aree bianche vuote, ma si adattano al numero effettivo di righe.
- **Come e dove:**
  - Rimosso `setSizeFull()` e `expand(...)` in `HomeView.java`, `SchedaContattoView.java` e `AgendaGiornalieraView.java`.
  - In `HomeView.java`, quando i contatti visibili sono fino a 15 la `Grid` usa `setAllRowsVisible(true)` per aderire alle righe; oltre 15 righe attiva `.mplan-tabella-scroll-limitata` (`height: min(68vh, 620px)`) preservando la virtualizzazione (`NFR.3`).
  - In `ElencoAttivita.java` e `tema.css`, `.mplan-elenco-lista` usa `height: auto; max-height: min(62vh, 560px)` e attiva altezza fissa scrollabile `.mplan-elenco-lungo` oltre 12 elementi.
- **Perché:** evitare l'effetto di righe mancanti o invisibili quando i contatti o le attività sono pochi, mantenendo buone prestazioni con molti record.

---

## 9. Form a tutta larghezza, contatore storico e motivo annullamento negli elenchi (`GUI.3`, `GUI.4.3`, `GUI.5`, `GUI.7.1`, `GUI.8`)

- **Cosa cambia:**
  - **Form a tutta larghezza:** rimosso il limite `max-width: 840px` da `.mplan-card-form` in `tema.css`; `FormContattoView` e `FormAttivitaView` occupano ora tutta la larghezza del contenitore (`1140px`) come le altre schermate.
  - **Contatore in Scheda contatto:** aggiunto un discreto contatore (`.mplan-sezione-contatore`) accanto al titolo `"Storico attività"` in `SchedaContattoView.java`.
  - **Motivo dell'annullamento negli elenchi:** quando un'attività è nello stato `ANNULLATA` e ha un motivo valorizzato, `ElencoAttivita.java` mostra sotto il titolo barrato un'anteprima troncata con ellissi (`Motivo: …`, massimo 90 caratteri, testo completo nel tooltip `title`), lasciando in `FormAttivitaView` il riquadro completo.
  - **Orario flessibile in Agenda:** alleggerito il peso visivo dell'etichetta `"In giornata"` (`.mplan-riga-ora-flessibile`) rispetto agli orari puntuali.
- **Perché:** allineare i form alla griglia dell'app, dare contesto immediato sulle attività annullate senza doverle aprire una per una e senza toccare i contratti di `mplan-api`.

---

## 10. Selettore data personalizzato `gg/mm/aaaa` e calendario non auto-chiudente (`GUI.5`, `GUI.7.3`, `DAT.5`)

- **Cosa cambia rispetto al `DatePicker` standard:**
  - Introdotto il componente personalizzato `SelettoreData` al posto del `DatePicker` di Vaadin sia in `AgendaGiornalieraView` (`Vai a…`) sia in `FormAttivitaView` (`Data`).
  - **Inserimento manuale:** l'utente può scrivere liberamente la data nel formato `gg/mm/aaaa` (con validazione rigorosa `LocalizzazioneDate.parseData` e messaggio `DAT.5`: `"Data non valida: usa il formato gg/mm/aaaa."`).
  - **Calendario a comparsa con bordi arrotondati (`12px`):** il pulsante calendario apre un `Popover` arrotondato (`--mplan-raggio-card`); cliccare su un giorno o su `Oggi` aggiorna subito la data **senza chiudere automaticamente il calendario**, così eventuali errori di clic si correggono al volo. Il calendario si chiude solo cliccando fuori, premendo `Esc` o cliccando `Chiudi`.
- **Dove:**
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/componenti/SelettoreData.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/componenti/LocalizzazioneDate.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/schermate/AgendaGiornalieraView.java`
  - `code/mplan-ui-vaadin/src/main/java/it/mpelettrica/mplan/ui/schermate/FormAttivitaView.java`
  - `code/mplan-ui-vaadin/src/main/resources/META-INF/resources/mplan-tema/tema.css`
- **Nota per Documento 3 e Documento 4:**
  - I requisiti `GUI.5`, `GUI.7.3`, `DAT.2.2` e `DAT.5` restano soddisfatti (formato `gg/mm/aaaa`, valore vuoto = oggi nel form attività, errore in Agenda se vuoto o malformato). Va aggiornata solo la nota descrittiva del selettore data precisando che il calendario resta aperto dopo la selezione fino a chiusura esplicita (`Chiudi`, `Esc` o clic esterno) e che `"Vai a…"` è integrato nella barra del giorno a destra.

---

## 11. Verifica su moduli Java e diagrammi UML

- **Moduli non UI:** `mplan-api`, `mplan-core` e `mplan-persistence` non sono stati modificati. In `mplan-app` l'unica modifica precedente riguarda la favicon (`icona.png`) in `MPlanApplication.java`.
- **Diagrammi UML e regole di dominio:** invariati il Use Case Diagram e la macchina a stati dell'attività (`DA_FARE`, `PARZIALE`, `COMPLETATA`, `ANNULLATA` e relative transizioni).
- **Funzionalità rinviate fuori perimetro UI:** l'eventuale vista settimanale o mensile dell'agenda richiede l'estensione di `AgendaService` (`mplan-api`), della logica applicativa (`mplan-core`), delle query (`mplan-persistence`) e dei requisiti `FUN.10` / `GUI.7`, per cui non è stata introdotta in questa fase dedicata alla sola grafica.

