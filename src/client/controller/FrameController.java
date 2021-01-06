package worth.client.controller;

import worth.client.model.ClientModel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by alessiomatricardi on 06/01/21
 *
 * Controller che gestisce gli eventi da dover chiamare alla chiusura del frame
 */
public class FrameController {
    private final JFrame frame;
    private final ClientModel model;

    public FrameController(ClientModel model, JFrame frame) {
        this.model = model;
        this.frame = frame;
        this.initController();
    }

    private void initController() {
        // alla chiusura dell'applicazione viene effettuato il logout
        frame.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                model.closeConnection();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            }
        });
    }
}
