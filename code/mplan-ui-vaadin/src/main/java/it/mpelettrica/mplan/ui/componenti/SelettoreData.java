package it.mpelettrica.mplan.ui.componenti;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;
import java.util.function.Supplier;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.component.popover.PopoverPosition;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Selettore di data con campo di testo libero nel formato {@code gg/mm/aaaa} e calendario
 * a comparsa con bordi arrotondati.
 * <p>
 * A differenza del calendario nativo, selezionare un giorno aggiorna subito la data senza
 * chiudere il riquadro: il calendario si chiude solo cliccando fuori, premendo {@code Esc}
 * o usando il pulsante {@code Chiudi}.
 */
public class SelettoreData extends CustomField<LocalDate> {

    private static final long serialVersionUID = 1L;

    private static final int COLONNE_SETTIMANA = 7;

    private static final int CELLE_GRIGLIA = 42;

    private final TextField campoTesto = new TextField();

    private final Button pulsanteCalendario = new Button(VaadinIcon.CALENDAR.create());

    private final Popover popover = new Popover();

    private final Span titoloMese = new Span();

    private final Div grigliaGiorni = new Div();

    private final Supplier<LocalDate> fornitoreOggi;

    private YearMonth meseVisualizzato;

    private LocalDate dataSelezionata;

    private boolean testoNonValido;

    private boolean aggiornandoDaModello;

    /**
     * Crea un selettore di data usando la data di sistema come riferimento per il pulsante
     * {@code Oggi}; per l'agenda e il form conviene passare {@code agendaService::oggi}.
     */
    public SelettoreData() {
        this(LocalDate::now);
    }

    /**
     * Crea un selettore di data con un fornitore esplicito della data odierna.
     *
     * @param fornitoreOggi funzione che restituisce la data corrente; non {@code null}
     */
    public SelettoreData(Supplier<LocalDate> fornitoreOggi) {
        this.fornitoreOggi = fornitoreOggi;
        this.meseVisualizzato = YearMonth.from(fornitoreOggi.get());
        addClassName("mplan-selettore-data");
        configuraCampoTesto();
        configuraPulsanteCalendario();
        configuraPopover();
        add(campoTesto, popover);
        aggiornaCalendario();
    }

    /**
     * Aggiunge un componente prefisso dentro il campo di testo (ad esempio {@code "Vai a…"}).
     *
     * @param prefisso il componente da mostrare all'inizio del campo
     */
    public void impostaPrefisso(Component prefisso) {
        campoTesto.setPrefixComponent(prefisso);
    }

    /**
     * Imposta l'etichetta accessibile del campo di testo interno.
     *
     * @param ariaLabel etichetta per gli screen reader
     */
    public void impostaAriaLabel(String ariaLabel) {
        campoTesto.setAriaLabel(ariaLabel);
    }

    /**
     * Indica se l'utente ha digitato del testo che non rappresenta una data valida.
     *
     * @return {@code true} se il campo contiene testo non vuoto e non interpretabile
     */
    public boolean haTestoNonValido() {
        return testoNonValido;
    }

    @Override
    public void focus() {
        campoTesto.focus();
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
        campoTesto.setInvalid(invalid);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        campoTesto.setReadOnly(readOnly);
        pulsanteCalendario.setVisible(!readOnly);
        if (readOnly) {
            popover.close();
        }
    }

    @Override
    protected LocalDate generateModelValue() {
        String testo = campoTesto.getValue();
        if (testo == null || testo.isBlank()) {
            testoNonValido = false;
            return null;
        }
        Optional<LocalDate> letta = LocalizzazioneDate.parseData(testo);
        testoNonValido = letta.isEmpty();
        return letta.orElse(null);
    }

    @Override
    protected void setPresentationValue(LocalDate nuovaData) {
        aggiornandoDaModello = true;
        try {
            dataSelezionata = nuovaData;
            testoNonValido = false;
            campoTesto.setValue(nuovaData == null ? "" : LocalizzazioneDate.formattaData(nuovaData));
            campoTesto.setInvalid(false);
            if (nuovaData != null) {
                meseVisualizzato = YearMonth.from(nuovaData);
            }
            aggiornaCalendario();
        } finally {
            aggiornandoDaModello = false;
        }
    }

    private void configuraCampoTesto() {
        campoTesto.addClassName("mplan-selettore-data-input");
        campoTesto.setPlaceholder("gg/mm/aaaa");
        campoTesto.setWidthFull();
        campoTesto.setSuffixComponent(pulsanteCalendario);
        campoTesto.addValueChangeListener(evento -> {
            if (aggiornandoDaModello || !evento.isFromClient()) {
                return;
            }
            elaboraTestoDigitato(evento.getValue());
        });
    }

    private void elaboraTestoDigitato(String testo) {
        if (testo == null || testo.isBlank()) {
            testoNonValido = false;
            dataSelezionata = null;
            setInvalid(false);
            aggiornaCalendario();
            setModelValue(null, true);
            return;
        }
        Optional<LocalDate> letta = LocalizzazioneDate.parseData(testo);
        if (letta.isEmpty()) {
            testoNonValido = true;
            dataSelezionata = null;
            setErrorMessage(LocalizzazioneDate.DATA_NON_VALIDA);
            setInvalid(true);
            aggiornaCalendario();
            setModelValue(null, true);
            return;
        }
        LocalDate data = letta.get();
        testoNonValido = false;
        dataSelezionata = data;
        meseVisualizzato = YearMonth.from(data);
        setInvalid(false);
        aggiornandoDaModello = true;
        try {
            campoTesto.setValue(LocalizzazioneDate.formattaData(data));
        } finally {
            aggiornandoDaModello = false;
        }
        aggiornaCalendario();
        setModelValue(data, true);
    }

    private void configuraPulsanteCalendario() {
        pulsanteCalendario.addThemeVariants(ButtonVariant.TERTIARY_INLINE);
        pulsanteCalendario.addClassName("mplan-selettore-data-btn");
        pulsanteCalendario.setAriaLabel("Apri calendario");
        pulsanteCalendario.setTooltipText("Apri calendario");
    }

    private void configuraPopover() {
        popover.addClassName("mplan-calendario-popover");
        popover.setTarget(pulsanteCalendario);
        popover.setPosition(PopoverPosition.BOTTOM_END);
        popover.setOpenOnClick(true);
        popover.setOpenOnFocus(false);
        popover.setCloseOnOutsideClick(true);
        popover.setCloseOnEsc(true);
        popover.add(creaContenutoCalendario());
        popover.addOpenedChangeListener(evento -> {
            if (evento.isOpened()) {
                LocalDate riferimento = dataSelezionata != null ? dataSelezionata : fornitoreOggi.get();
                meseVisualizzato = YearMonth.from(riferimento);
                aggiornaCalendario();
            }
        });
    }

    private Div creaContenutoCalendario() {
        Div contenitore = new Div();
        contenitore.addClassName("mplan-calendario");
        contenitore.add(creaIntestazioneMese(), creaIntestazioneGiorni(), grigliaGiorni, creaFooterCalendario());
        return contenitore;
    }

    private HorizontalLayout creaIntestazioneMese() {
        Button precedente = new Button(VaadinIcon.ANGLE_LEFT.create(), evento -> spostaMese(-1));
        precedente.addThemeVariants(ButtonVariant.TERTIARY);
        precedente.addClassName("mplan-cal-nav");
        precedente.setAriaLabel("Mese precedente");

        titoloMese.addClassName("mplan-cal-mese");

        Button successivo = new Button(VaadinIcon.ANGLE_RIGHT.create(), evento -> spostaMese(1));
        successivo.addThemeVariants(ButtonVariant.TERTIARY);
        successivo.addClassName("mplan-cal-nav");
        successivo.setAriaLabel("Mese successivo");

        HorizontalLayout intestazione = new HorizontalLayout(precedente, titoloMese, successivo);
        intestazione.addClassName("mplan-cal-header");
        intestazione.setPadding(false);
        intestazione.setSpacing(false);
        intestazione.setWidthFull();
        intestazione.setAlignItems(FlexComponent.Alignment.CENTER);
        return intestazione;
    }

    private static Div creaIntestazioneGiorni() {
        Div riga = new Div();
        riga.addClassName("mplan-cal-weekdays");
        var nomi = LocalizzazioneDate.giorniBrevi();
        // L'ordine parte da lunedi' (indice 1 in LocalizzazioneDate.giorniBrevi()) fino a domenica (indice 0)
        for (int i = 1; i <= COLONNE_SETTIMANA; i++) {
            Span etichetta = new Span(nomi.get(i % COLONNE_SETTIMANA));
            etichetta.addClassName("mplan-cal-weekday");
            riga.add(etichetta);
        }
        return riga;
    }

    private HorizontalLayout creaFooterCalendario() {
        Button oggi = new Button("Oggi", evento -> scegliDaCalendario(fornitoreOggi.get()));
        oggi.addThemeVariants(ButtonVariant.TERTIARY);
        oggi.addClassName("mplan-cal-footer-btn");

        Button chiudi = new Button("Chiudi", evento -> popover.close());
        chiudi.addThemeVariants(ButtonVariant.TERTIARY);
        chiudi.addClassName("mplan-cal-footer-btn");

        HorizontalLayout footer = new HorizontalLayout(oggi, chiudi);
        footer.addClassName("mplan-cal-footer");
        footer.setPadding(false);
        footer.setSpacing(false);
        footer.setWidthFull();
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        return footer;
    }

    private void spostaMese(int delta) {
        meseVisualizzato = meseVisualizzato.plusMonths(delta);
        aggiornaCalendario();
    }

    private void scegliDaCalendario(LocalDate data) {
        testoNonValido = false;
        dataSelezionata = data;
        meseVisualizzato = YearMonth.from(data);
        setInvalid(false);
        aggiornandoDaModello = true;
        try {
            campoTesto.setValue(LocalizzazioneDate.formattaData(data));
        } finally {
            aggiornandoDaModello = false;
        }
        aggiornaCalendario();
        // Aggiorna subito il valore senza chiudere il popover, cosi' l'utente puo' correggere al volo
        setModelValue(data, true);
    }

    private void aggiornaCalendario() {
        String nomeMese = LocalizzazioneDate.mesi().get(meseVisualizzato.getMonthValue() - 1);
        titoloMese.setText(Character.toUpperCase(nomeMese.charAt(0))
                + nomeMese.substring(1) + " " + meseVisualizzato.getYear());

        grigliaGiorni.removeAll();
        grigliaGiorni.addClassName("mplan-cal-grid");

        LocalDate primoDelMese = meseVisualizzato.atDay(1);
        int offset = primoDelMese.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        LocalDate inizio = primoDelMese.minusDays(offset);
        LocalDate oggi = fornitoreOggi.get();

        for (int i = 0; i < CELLE_GRIGLIA; i++) {
            LocalDate giorno = inizio.plusDays(i);
            Button cella = new Button(String.valueOf(giorno.getDayOfMonth()),
                    evento -> scegliDaCalendario(giorno));
            cella.addThemeVariants(ButtonVariant.TERTIARY);
            cella.addClassName("mplan-cal-day");
            if (giorno.getMonthValue() != meseVisualizzato.getMonthValue()) {
                cella.addClassName("mplan-cal-day-outside");
            }
            if (giorno.equals(oggi)) {
                cella.addClassName("mplan-cal-day-today");
            }
            if (giorno.equals(dataSelezionata)) {
                cella.addClassName("mplan-cal-day-selected");
            }
            cella.setAriaLabel(LocalizzazioneDate.formattaDataEstesa(giorno));
            grigliaGiorni.add(cella);
        }
    }
}
