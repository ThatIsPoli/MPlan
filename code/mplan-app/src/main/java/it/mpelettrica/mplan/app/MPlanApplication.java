package it.mpelettrica.mplan.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MPlanApplication {

	private static final Logger LOGGER = LogManager.getLogger(MPlanApplication.class);

    public static void main(String[] args) {
        LOGGER.info("Avvio dell'applicazione MPlan...");
        LOGGER.debug("Messaggio di debug (visibile solo se configurato a livello DEBUG)");
        LOGGER.info("Inizializzazione completata con successo!");
	}

}
