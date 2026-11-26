package it.mpelettrica.mplan.ui.infrastruttura;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.PageTitle;

import it.mpelettrica.mplan.ui.navigazione.Navigatore;

/**
 * Schermata mostrata quando l'apertura di una schermata fallisce, ad esempio per un guasto
 * del database durante la lettura dei dati.
 * <p>
 * Sostituisce la pagina di errore predefinita di Vaadin, che mostrerebbe il testo
 * dell'eccezione: qui l'utente vede un messaggio comprensibile, un codice da riferire e un
 * pulsante per tornare alla schermata principale. L'eccezione completa finisce nel log.
 * Gli errori durante l'uso dei pulsanti sono invece gestiti da {@link GestoreErroriUI}.
 */
@PageTitle("MPlan – Errore")
public class VistaErrore extends VerticalLayout implements HasErrorParameter<Exception> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger(VistaErrore.class);

    private static final int ERRORE_INTERNO = 500;

    private final Paragraph testo;

    private final Span codice;

    private final Navigatore navigatore;

    /**
     * Crea la schermata di errore.
     *
     * @param navigatore il navigatore usato per tornare alla schermata principale
     */
    public VistaErrore(Navigatore navigatore) {
        this.navigatore = navigatore;
        this.testo = creaTesto();
        this.codice = creaCodice();
        addClassName("mplan-pagina");
        setPadding(false);
        setSpacing(false);
        add(creaRiquadro(creaIcona(), creaTitolo(), testo, codice, creaPulsanteHome()));
    }

    /**
     * Registra l'errore nel log e prepara il messaggio da mostrare.
     *
     * @param evento    l'evento di navigazione che ha causato l'errore
     * @param parametro l'eccezione intercettata durante l'apertura della schermata
     * @return il codice HTTP 500 (errore interno)
     */
    @Override
    public int setErrorParameter(BeforeEnterEvent evento, ErrorParameter<Exception> parametro) {
        String codiceErrore = GestoreErroriUI.nuovoCodice();
        LOG.error("Errore nell'apertura di {}, codice {}", evento.getLocation().getPath(), codiceErrore,
                parametro.getCaughtException());
        testo.setText(GestoreErroriUI.messaggioPer(parametro.getCaughtException()));
        codice.setText("Codice errore: " + codiceErrore);
        return ERRORE_INTERNO;
    }

    private static Card creaRiquadro(Component... contenuto) {
        Card riquadro = new Card();
        riquadro.addClassName("mplan-errore-card");
        riquadro.add(contenuto);
        return riquadro;
    }

    private static Icon creaIcona() {
        Icon icona = VaadinIcon.WARNING.create();
        icona.addClassName("mplan-errore-icona");
        return icona;
    }

    private static H1 creaTitolo() {
        H1 titolo = new H1("Qualcosa non ha funzionato");
        titolo.addClassName("mplan-titolo-pagina");
        return titolo;
    }

    private static Paragraph creaTesto() {
        Paragraph paragrafo = new Paragraph();
        paragrafo.addClassName("mplan-errore-testo");
        return paragrafo;
    }

    private static Span creaCodice() {
        Span testoCodice = new Span();
        testoCodice.addClassName("mplan-errore-codice");
        return testoCodice;
    }

    private Button creaPulsanteHome() {
        Button home = new Button("Torna alla Home", evento -> navigatore.vaiAHome());
        home.addThemeVariants(ButtonVariant.PRIMARY);
        return home;
    }
}
