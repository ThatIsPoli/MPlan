package it.mpelettrica.mplan.ui.componenti;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.timepicker.TimePicker.TimePickerI18n;

/**
 * Raccoglie la localizzazione italiana dei selettori di data e ora e la formattazione
 * delle date e degli orari mostrati nelle schermate.
 * <p>
 * Evita di ripetere in ogni schermata i nomi dei mesi, dei giorni, il formato
 * {@code gg/mm/aaaa} e i messaggi per l'input non interpretabile (DAT.5, FUN.11).
 * La classe non e' istanziabile: espone solo metodi statici.
 */
public final class LocalizzazioneDate {

    /** Formato delle date mostrato e accettato dai selettori. */
    public static final String FORMATO_DATA = "dd/MM/yyyy";

    /** Locale italiano usato in tutta l'interfaccia. */
    public static final Locale ITALIANO = Locale.ITALY;

    private static final DateTimeFormatter FORMATO_DATA_BREVE =
            DateTimeFormatter.ofPattern(FORMATO_DATA, ITALIANO);

    private static final DateTimeFormatter FORMATO_DATA_ESTESA =
            DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", ITALIANO);

    private static final DateTimeFormatter FORMATO_ORA =
            DateTimeFormatter.ofPattern("HH:mm", ITALIANO);

    private static final Duration PASSO_ORARIO = Duration.ofMinutes(15);

    private static final List<String> MESI = List.of(
            "gennaio", "febbraio", "marzo", "aprile", "maggio", "giugno",
            "luglio", "agosto", "settembre", "ottobre", "novembre", "dicembre");

    private static final List<String> GIORNI = List.of(
            "domenica", "lunedì", "martedì", "mercoledì", "giovedì", "venerdì", "sabato");

    private static final List<String> GIORNI_BREVI = List.of(
            "dom", "lun", "mar", "mer", "gio", "ven", "sab");

    private static final int LUNEDI = 1;

    private LocalizzazioneDate() {
    }

    /**
     * Applica la localizzazione italiana a un selettore di data: nomi dei mesi e dei giorni,
     * settimana che inizia il lunedi', formato {@code gg/mm/aaaa} e messaggio per una data
     * scritta male.
     *
     * @param selettore il selettore di data da configurare; non {@code null}
     */
    public static void applica(DatePicker selettore) {
        selettore.setLocale(ITALIANO);
        selettore.setI18n(creaI18nData());
        selettore.setPlaceholder("gg/mm/aaaa");
    }

    /**
     * Applica la localizzazione italiana a un selettore di orario: formato a 24 ore,
     * passo di 15 minuti nell'elenco proposto e messaggio per un orario scritto male.
     *
     * @param selettore il selettore di orario da configurare; non {@code null}
     */
    public static void applica(TimePicker selettore) {
        selettore.setLocale(ITALIANO);
        selettore.setStep(PASSO_ORARIO);
        selettore.setI18n(creaI18nOra());
        selettore.setPlaceholder("hh:mm");
    }

    /**
     * Formatta una data nel formato breve {@code gg/mm/aaaa}.
     *
     * @param data la data da formattare; non {@code null}
     * @return la data formattata, ad esempio {@code 26/11/2026}
     */
    public static String formattaData(LocalDate data) {
        return FORMATO_DATA_BREVE.format(data);
    }

    /**
     * Formatta una data con il nome del giorno della settimana.
     *
     * @param data la data da formattare; non {@code null}
     * @return la data formattata, ad esempio {@code giovedì 26/11/2026}
     */
    public static String formattaDataEstesa(LocalDate data) {
        return FORMATO_DATA_ESTESA.format(data);
    }

    /**
     * Formatta un orario nel formato {@code hh:mm}.
     *
     * @param ora l'orario da formattare; non {@code null}
     * @return l'orario formattato, ad esempio {@code 09:30}
     */
    public static String formattaOra(LocalTime ora) {
        return FORMATO_ORA.format(ora);
    }

    private static DatePickerI18n creaI18nData() {
        return new DatePickerI18n()
                .setDateFormat(FORMATO_DATA)
                .setMonthNames(MESI)
                .setWeekdays(GIORNI)
                .setWeekdaysShort(GIORNI_BREVI)
                .setFirstDayOfWeek(LUNEDI)
                .setToday("Oggi")
                .setCancel("Annulla")
                .setDialogAccessibleName("Calendario")
                .setBadInputErrorMessage("Data non valida: usa il formato gg/mm/aaaa.")
                .setRequiredErrorMessage("Inserisci una data.");
    }

    private static TimePickerI18n creaI18nOra() {
        return new TimePickerI18n()
                .setBadInputErrorMessage("Ora non valida: usa il formato hh:mm.");
    }
}
