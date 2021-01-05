package worth.client.controller;

import worth.client.model.ClientModel;
import worth.client.ui.LoggedUI;
import worth.client.ui.WorthFrame;

import javax.swing.*;

/**
 * Created by alessiomatricardi on 05/01/21
 */
public class LoggedController {
    private final ClientModel model;
    private final LoggedUI view;

    public LoggedController(ClientModel model, LoggedUI view) {
        this.model = model;
        this.view = view;
        this.initController();
    }

    private void initController() {

    }

    private void changeContext() {
        WorthFrame frame = (WorthFrame) SwingUtilities.getWindowAncestor(this.view);
        frame.getCardLayout().next(frame.getCardHolder());
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

}
