package it.mpelettrica.mplan.ui.componenti;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.converter.Converter;

/**
 * Raccoglie la localizzazione italiana del selettore di data, la lettura degli orari scritti
 * dall'utente e la formattazione delle date e degli orari mostrati nelle schermate.
 * <p>
 * Evita di ripetere in ogni schermata i nomi dei mesi, dei giorni, i formati
 * {@code gg/mm/aaaa} e {@code hh:mm} e i messaggi per l'input non interpretabile (DAT.5).
 * La classe non e' istanziabile: espone solo metodi statici.
 */
public final class LocalizzazioneDate {

    /** Formato delle date mostrato e accettato dai selettori. */
    private static final String FORMATO_DATA = "dd/MM/yyyy";

    /** Locale italiano usato in tutta l'interfaccia. */
    public static final Locale ITALIANO = Locale.ITALY;

    private static final DateTimeFormatter FORMATO_DATA_BREVE =
            DateTimeFormatter.ofPattern(FORMATO_DATA, ITALIANO);

    private static final DateTimeFormatter FORMATO_DATA_ESTESA =
            DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", ITALIANO);

    private static final DateTimeFormatter FORMATO_ORA =
            DateTimeFormatter.ofPattern("HH:mm", ITALIANO);

    /** Messaggio mostrato per un orario scritto male o fuori scala (DAT.2.3, DAT.5). */
    private static final String ORA_NON_VALIDA = "Ora non valida: usa il formato hh:mm, ad esempio 08:30.";

    /** Ore da 0 a 23 e minuti da 00 a 59, separati da due punti o da un punto. */
    private static final Pattern FORMATO_ORA_SCRITTA = Pattern.compile("([01]?[0-9]|2[0-3])[:.]([0-5][0-9])");

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
     * Prepara un campo di testo per l'orario: suggerimento {@code hh:mm} e solo cifre e separatori
     * digitabili. Il valore va letto con {@link #convertitoreOra()}.
     *
     * @param campo il campo dell'orario da configurare; non {@code null}
     */
    public static void applicaOra(TextField campo) {
        campo.setPlaceholder("hh:mm");
        campo.setAllowedCharPattern("[0-9:.]");
        campo.setClearButtonVisible(true);
    }

    /**
     * Restituisce il convertitore tra il testo dell'orario e l'orario: un testo vuoto vale
     * "nessun orario"; un testo fuori formato o fuori scala (ad esempio {@code 25:99}) e' un errore
     * con il messaggio "Ora non valida: usa il formato hh:mm", invece di essere corretto in silenzio.
     *
     * @return il convertitore da usare nel {@code Binder}
     */
    public static Converter<String, LocalTime> convertitoreOra() {
        return Converter.from(LocalizzazioneDate::leggiOra,
                ora -> ora == null ? "" : formattaOra(ora));
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

    private static Result<LocalTime> leggiOra(String testo) {
        String ripulito = testo == null ? "" : testo.strip();
        if (ripulito.isEmpty()) {
            return Result.ok(null);
        }
        Matcher parti = FORMATO_ORA_SCRITTA.matcher(ripulito);
        if (!parti.matches()) {
            return Result.error(ORA_NON_VALIDA);
        }
        return Result.ok(LocalTime.of(Integer.parseInt(parti.group(1)), Integer.parseInt(parti.group(2))));
    }
}
