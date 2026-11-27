package it.mpelettrica.mplan.ui.infrastruttura;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLayout;

import it.mpelettrica.mplan.api.ChiusuraApplicazione;
import it.mpelettrica.mplan.ui.navigazione.Navigatore;

/**
 * Cornice comune a tutte le schermate di MPlan: barra superiore persistente con logo MP elettrica,
 * navigazione rapida (Rubrica e Agenda) e pulsante discreto di chiusura dell'applicazione.
 */
public class LayoutPrincipale extends VerticalLayout implements RouterLayout, AfterNavigationObserver {

    private static final long serialVersionUID = 1L;

    private static final int DURATA_NOTIFICA_CHIUSURA_MS = 5000;

    private static final String PERCORSO_LOGO = "mplan-tema/icona.png";

    private final Navigatore navigatore;

    private final ChiusuraApplicazione chiusuraApplicazione;

    private final Button navRubrica;

    private final Button navAgenda;

    private final Main areaContenuto;

    private String percorsoCorrente = "";

    /**
     * Crea il layout principale condiviso tra tutte le viste.
     *
     * @param navigatore           navigatore tra le schermate
     * @param chiusuraApplicazione servizio che arresta l'applicazione locale
     */
    public LayoutPrincipale(Navigatore navigatore, ChiusuraApplicazione chiusuraApplicazione) {
        this.navigatore = navigatore;
        this.chiusuraApplicazione = chiusuraApplicazione;
        this.navRubrica = creaVoceNav("Rubrica", VaadinIcon.USERS, navigatore::vaiAHome);
        this.navAgenda = creaVoceNav("Agenda", VaadinIcon.CALENDAR, this::apriAgenda);
        this.areaContenuto = creaAreaContenuto();

        addClassName("mplan-shell");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        add(creaBarraSuperiore(), areaContenuto);
        expand(areaContenuto);
    }

    /**
     * Inserisce la schermata attiva nell'area centrale sotto la barra superiore.
     *
     * @param contenuto elemento radice della vista attiva
     */
    @Override
    public void showRouterLayoutContent(HasElement contenuto) {
        areaContenuto.getElement().removeAllChildren();
        if (contenuto != null) {
            areaContenuto.getElement().appendChild(contenuto.getElement());
        }
    }

    /**
     * Aggiorna lo stato attivo delle voci nella barra superiore dopo ogni cambio di schermata.
     *
     * @param evento evento di fine navigazione
     */
    @Override
    public void afterNavigation(AfterNavigationEvent evento) {
        percorsoCorrente = evento.getLocation().getPath();
        boolean suAgenda = Navigatore.ROTTA_AGENDA.equals(percorsoCorrente);
        boolean suRubrica = percorsoCorrente.isEmpty() || percorsoCorrente.startsWith("contatti");
        navRubrica.setClassName("mplan-nav-attivo", suRubrica);
        navAgenda.setClassName("mplan-nav-attivo", suAgenda);
    }

    private Header creaBarraSuperiore() {
        HorizontalLayout sinistra = new HorizontalLayout(creaBrand(), creaSeparatoreNav(), creaNavigazione());
        sinistra.addClassName("mplan-topbar-sinistra");
        sinistra.setPadding(false);
        sinistra.setSpacing(false);
        sinistra.setAlignItems(FlexComponent.Alignment.CENTER);

        Div interno = new Div(sinistra, creaPulsanteChiudi());
        interno.addClassName("mplan-topbar-interno");

        Header barra = new Header(interno);
        barra.addClassName("mplan-topbar");
        return barra;
    }

    private Button creaBrand() {
        Image logo = new Image(PERCORSO_LOGO, "Logo MP elettrica");
        logo.addClassName("mplan-topbar-logo");

        Span nomeApp = new Span("MPlan");
        nomeApp.addClassName("mplan-topbar-nome");

        HorizontalLayout contenutoBrand = new HorizontalLayout(logo, nomeApp);
        contenutoBrand.addClassName("mplan-brand-contenuto");
        contenutoBrand.setPadding(false);
        contenutoBrand.setSpacing(false);
        contenutoBrand.setAlignItems(FlexComponent.Alignment.CENTER);

        Button brand = new Button(contenutoBrand, evento -> navigatore.vaiAHome());
        brand.addThemeVariants(ButtonVariant.TERTIARY);
        brand.addClassName("mplan-brand");
        brand.setAriaLabel("MPlan – Torna alla Rubrica");
        return brand;
    }

    private static Span creaSeparatoreNav() {
        Span separatore = new Span();
        separatore.addClassName("mplan-topbar-separatore");
        separatore.getElement().setAttribute("aria-hidden", "true");
        return separatore;
    }

    private HorizontalLayout creaNavigazione() {
        HorizontalLayout nav = new HorizontalLayout(navRubrica, navAgenda);
        nav.addClassName("mplan-topbar-nav");
        nav.setPadding(false);
        nav.setSpacing(false);
        nav.setAlignItems(FlexComponent.Alignment.CENTER);
        return nav;
    }

    private Button creaPulsanteChiudi() {
        Button chiudi = new Button(VaadinIcon.POWER_OFF.create(), evento -> chiudiApplicazione());
        chiudi.addThemeVariants(ButtonVariant.TERTIARY);
        chiudi.addClassName("mplan-topbar-chiudi");
        chiudi.setAriaLabel("Chiudi applicazione");
        chiudi.setTooltipText("Chiudi applicazione");
        return chiudi;
    }

    private static Button creaVoceNav(String testo, VaadinIcon icona, Runnable azione) {
        Button voce = new Button(testo, icona.create(), evento -> azione.run());
        voce.addThemeVariants(ButtonVariant.TERTIARY);
        voce.addClassName("mplan-nav-voce");
        return voce;
    }

    private static Main creaAreaContenuto() {
        Main area = new Main();
        area.addClassName("mplan-contenuto");
        return area;
    }

    private void apriAgenda() {
        if (!Navigatore.ROTTA_AGENDA.equals(percorsoCorrente)) {
            navigatore.vaiAAgenda();
        }
    }

    private void chiudiApplicazione() {
        Notification.show("MPlan si sta chiudendo: puoi chiudere questa scheda del browser.",
                DURATA_NOTIFICA_CHIUSURA_MS, Notification.Position.TOP_CENTER);
        chiusuraApplicazione.chiudi();
    }
}
