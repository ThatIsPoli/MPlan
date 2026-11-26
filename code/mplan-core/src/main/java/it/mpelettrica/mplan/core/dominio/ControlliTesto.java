package it.mpelettrica.mplan.core.dominio;

import it.mpelettrica.mplan.api.ErroreValidazione;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Controlli sui testi condivisi dai validatori: obbligatorietà, lunghezza massima e formato.
 * I testi si valutano dopo {@code strip()}; un testo di soli spazi vale come assente.
 */
final class ControlliTesto {

    private ControlliTesto() {
    }

    /**
     * Toglie gli spazi iniziali e finali.
     *
     * @param valore testo da pulire, anche {@code null}
     * @return il testo pulito, o {@code null} se il valore era {@code null}
     */
    static String pulisci(String valore) {
        return valore == null ? null : valore.strip();
    }

    /**
     * Controlla un testo obbligatorio con lunghezza massima.
     *
     * @param errori lista a cui aggiungere l'eventuale errore
     * @param campo nome del campo
     * @param etichetta nome del campo, per il messaggio (per esempio "Nome")
     * @param valore testo da controllare
     * @param massimo numero massimo di caratteri
     */
    static void obbligatorio(List<ErroreValidazione> errori, String campo, String etichetta,
            String valore, int massimo) {
        String pulito = pulisci(valore);
        if (pulito == null || pulito.isEmpty()) {
            errori.add(new ErroreValidazione(campo, etichetta + ": campo obbligatorio"));
        } else {
            controllaLunghezza(errori, campo, etichetta, pulito, massimo);
        }
    }

    /**
     * Controlla un testo facoltativo con lunghezza massima.
     *
     * @param errori lista a cui aggiungere l'eventuale errore
     * @param campo nome del campo
     * @param etichetta nome del campo, per il messaggio
     * @param valore testo da controllare
     * @param massimo numero massimo di caratteri
     */
    static void facoltativo(List<ErroreValidazione> errori, String campo, String etichetta,
            String valore, int massimo) {
        String pulito = pulisci(valore);
        if (pulito != null && !pulito.isEmpty()) {
            controllaLunghezza(errori, campo, etichetta, pulito, massimo);
        }
    }

    /**
     * Controlla che un testo facoltativo, se presente, rispetti un formato.
     *
     * @param errori lista a cui aggiungere l'eventuale errore
     * @param campo nome del campo
     * @param valore testo da controllare
     * @param formato formato che il testo pulito deve rispettare per intero
     * @param messaggio motivo da mostrare se il formato non è rispettato
     */
    static void formato(List<ErroreValidazione> errori, String campo, String valore,
            Pattern formato, String messaggio) {
        String pulito = pulisci(valore);
        if (pulito != null && !pulito.isEmpty() && !formato.matcher(pulito).matches()) {
            errori.add(new ErroreValidazione(campo, messaggio));
        }
    }

    private static void controllaLunghezza(List<ErroreValidazione> errori, String campo,
            String etichetta, String pulito, int massimo) {
        if (pulito.length() > massimo) {
            errori.add(new ErroreValidazione(campo,
                    etichetta + ": al massimo " + massimo + " caratteri"));
        }
    }
}
