package worth.utils;

import java.awt.*;

/**
 * Created by alessiomatricardi on 05/01/21
 *
 * Estensione di CardLayout che calcola la dimensione della finestra in base
 * al componente attualmente attivo
 * Di default, CardLayout calcola preferredLayoutSize
 * come il max valore di preferredSize di tutte le sue card
 */
public class MyCardLayout extends CardLayout {

    @Override
    public Dimension preferredLayoutSize(Container parent) {

        Component current = findCurrentComponent(parent);
        if (current != null) {
            Insets insets = parent.getInsets();
            Dimension pref = current.getPreferredSize();
            pref.width += insets.left + insets.right;
            pref.height += insets.top + insets.bottom;
            return pref;
        }
        return super.preferredLayoutSize(parent);
    }

    private Component findCurrentComponent(Container parent) {
        for (Component comp : parent.getComponents()) {
            if (comp.isVisible()) {
                return comp;
            }
        }
        return null;
    }
}
