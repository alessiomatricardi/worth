package worth.client.ui.loggedPanels.projectPanels;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by alessiomatricardi on 08/01/21
 */
public class MembersPanel extends JPanel {
    private JScrollPane membersScrollPane;
    private JPanel addMemberPanel;
    private JTextField addMemberField;
    private JButton addMemberButton;

    public MembersPanel() {
        // questo componente è diviso in 2 parti
        // nella prima c'è la lista dei membri del progetto
        // nella seconda la possibilità di aggiungere un utente
        this.setLayout(new GridLayout(1,2));

        // istanza containers
        // layout scrollabile, se necessario
        membersScrollPane = new JScrollPane(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        // aumenta velocità dello scorrimento
        membersScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // niente bordi
        membersScrollPane.setBorder(BorderFactory.createEmptyBorder());
        addMemberPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // aggiungi buttons al container
        JLabel label = new JLabel("Add member");
        Font font = label.getFont();
        label.setFont(new Font(font.getName(), Font.BOLD, (int)(font.getSize() * 1.5)));
        addMemberField = new JTextField();
        addMemberButton = new JButton("Add member");
        JPanel memberContainer = new JPanel(new GridLayout(3, 1, 10, 10));
        memberContainer.add(label);
        memberContainer.add(addMemberField);
        memberContainer.add(addMemberButton);
        addMemberPanel.add(memberContainer);

        // aggiungo componenti
        this.add(membersScrollPane, BorderLayout.WEST);
        this.add(addMemberPanel, BorderLayout.EAST);
    }

    public void setUI(List<JLabel> members) {
        JPanel membersPanel = new JPanel();
        membersPanel.setLayout(new GridLayout(0, 1, 0, 0));

        // inserisco gli utenti nel pannello
        String text = members.size() + " member" + (members.size() == 1 ? "" : "s");
        JLabel numOfMembers = new JLabel(text);
        Font font = numOfMembers.getFont();
        numOfMembers.setFont(new Font(font.getName(), Font.BOLD, (int)(font.getSize() * 1.5)));
        membersPanel.add(numOfMembers);

        for (JLabel member : members) {
            member.setFont(new Font(font.getName(), Font.PLAIN, (int)(font.getSize() * 1.3)));
            member.setPreferredSize(new Dimension(300, 20));
            membersPanel.add(member);
        }

        // container contiene il pannello degli utenti
        JPanel container = new JPanel(new BorderLayout(0,0));
        container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        container.add(membersPanel, BorderLayout.NORTH);

        membersScrollPane.setViewportView(container);
    }

    public JTextField getAddMemberField() {
        return addMemberField;
    }

    public JButton getAddMemberButton() {
        return addMemberButton;
    }

}
