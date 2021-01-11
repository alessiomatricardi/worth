package worth.client.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by alessiomatricardi on 11/01/21
 *
 * Interfaccia che definisce il comportamento di tutti i JPanel che
 * ospitano un container con layout CardLayout
 *
 * Nello specifico:
 * Qualsiasi componente che implementa questa interfaccia permette di
 * mostrare a schermo (uno per volta, in esclusiva) 2 o più componenti "interni"
 * chiamati cards. Queste cards sono tutte contenute in un unico container
 * che implementa il layout manager CardLayout
 */
public interface HostsCardsContainer {

    /**
     * Restituisce il card layout che gestisce il container delle cards
     */
    CardLayout getCardLayout();

    /**
     * Restituisce il container che ospita le cards
     */
    JPanel getContainerPanel();

}
