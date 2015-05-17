/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package equa.desktop;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import equa.meta.Message;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author frankpeeters
 */
public class MessageTab extends javax.swing.JPanel implements Dockable {

    private static final long serialVersionUID = 1L;
    static final char ARROW = '\u2192';
    private DockKey key;
    private JTextArea taMessages;

    public MessageTab() {
        key = new DockKey("Messages");
        this.key.setCloseEnabled(false);
        initComponents();
    }

    @Override
    public DockKey getDockKey() {
        return key;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel pnNorth = new JPanel();
        pnNorth.setLayout(new BorderLayout());
        JButton btClear = new JButton("Clear");
        btClear.setSize(40, 10);
        pnNorth.add(btClear, BorderLayout.WEST);
        btClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                taMessages.setText("");
            }
        });
        add(pnNorth, BorderLayout.NORTH);
        taMessages = new JTextArea();
        JScrollPane spMessages = new JScrollPane(taMessages);
        add(spMessages, BorderLayout.CENTER);
    }

    public void addMessage(String message, String trigger) {

        taMessages.append(System.lineSeparator());
        GregorianCalendar gc = new GregorianCalendar();
        taMessages.append("[" + trigger + "]");
        taMessages.append(" on " + gc.getTime().toString() + " :" + System.lineSeparator());
        taMessages.append(ARROW + " " + message + System.lineSeparator());
    }

    public void addMessages(List<Message> messages, String trigger) {
        taMessages.append(System.lineSeparator());
        GregorianCalendar gc = new GregorianCalendar();
        taMessages.append("[" + trigger + "]");
        taMessages.append(" on " + gc.getTime().toString() + " :" + System.lineSeparator());
        for (Message message : messages) {
            taMessages.append(ARROW + " " + message.getText() + System.lineSeparator());
        }
    }
}
