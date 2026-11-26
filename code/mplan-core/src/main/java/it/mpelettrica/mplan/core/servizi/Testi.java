package it.mpelettrica.mplan.core.servizi;

/**
 * Normalizzazione dei testi scritti dall'utente prima di salvarli: spazi tolti e testi vuoti
 * trattati come assenti.
 */
final class Testi {

    private Testi() {
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
     * Pulisce il testo e lo sostituisce con {@code null} se resta vuoto.
     *
     * @param valore testo da normalizzare, anche {@code null}
     * @return il testo pulito, o {@code null} se assente o vuoto
     */
    static String nullSeVuoto(String valore) {
        String pulito = pulisci(valore);
        return pulito == null || pulito.isEmpty() ? null : pulito;
    }
}
