package it.mpelettrica.mplan.ui.infrastruttura;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.PageTitle;

import it.mpelettrica.mplan.ui.navigazione.Navigatore;

/**
 * Schermata mostrata quando l'indirizzo richiesto non corrisponde a nessuna schermata di MPlan.
 * <p>
 * Sostituisce la pagina predefinita di Vaadin, in inglese. Vaadin sceglie il gestore
 * dell'eccezione piu' specifica, quindi {@link VistaErrore}, che gestisce {@link Exception},
 * non basterebbe per {@link NotFoundException}.
 */
@PageTitle("MPlan – Pagina non trovata")
public class VistaPaginaNonTrovata extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LogManager.getLogger(VistaPaginaNonTrovata.class);

    private static final int NON_TROVATA = 404;

    private final Navigatore navigatore;

    /**
     * Crea la schermata della pagina non trovata.
     *
     * @param navigatore il navigatore usato per tornare alla schermata principale
     */
    public VistaPaginaNonTrovata(Navigatore navigatore) {
        this.navigatore = navigatore;
        addClassName("mplan-pagina");
        setPadding(false);
        setSpacing(false);
        add(creaRiquadro(creaIcona(), creaTitolo(), creaTesto(), creaPulsanteHome()));
    }

    /**
     * Registra nel log l'indirizzo non trovato.
     *
     * @param evento    l'evento di navigazione verso l'indirizzo inesistente
     * @param parametro l'eccezione che segnala la rotta mancante
     * @return il codice HTTP 404 (non trovato)
     */
    @Override
    public int setErrorParameter(BeforeEnterEvent evento, ErrorParameter<NotFoundException> parametro) {
        LOG.warn("Pagina non trovata: {}", evento.getLocation().getPath());
        return NON_TROVATA;
    }

    private static Card creaRiquadro(Component... contenuto) {
        Card riquadro = new Card();
        riquadro.addClassName("mplan-errore-card");
        riquadro.add(contenuto);
        return riquadro;
    }

    private static Icon creaIcona() {
        Icon icona = VaadinIcon.SEARCH.create();
        icona.addClassName("mplan-errore-icona");
        return icona;
    }

    private static H1 creaTitolo() {
        H1 titolo = new H1("Pagina non trovata");
        titolo.addClassName("mplan-titolo-pagina");
        return titolo;
    }

    private static Paragraph creaTesto() {
        Paragraph testo = new Paragraph("L'indirizzo richiesto non corrisponde a nessuna schermata di MPlan.");
        testo.addClassName("mplan-errore-testo");
        return testo;
    }

    private Button creaPulsanteHome() {
        Button home = new Button("Torna alla Home", evento -> navigatore.vaiAHome());
        home.addThemeVariants(ButtonVariant.PRIMARY);
        return home;
    }
}
