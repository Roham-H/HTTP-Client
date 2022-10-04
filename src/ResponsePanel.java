import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * the panel which pops after sending or when selecting an already sent request
 */
public class ResponsePanel extends JPanel {
//    The response recieved after sending the request
    private final HttpResponse<byte[]> response = Handler.requestPanels.get(ClientGUI.getRequestPanel()).getResponse();
    private String responseBody;
    private final JPanel centerPanel = new JPanel(new BorderLayout());
    private JPanel headerPanel;
    private final JLabel nameCol = new JLabel("NAME");
    private final JLabel valueCol = new JLabel("VALUE");
    private final ButtonHandler buttonHandler = new ButtonHandler();


    public ResponsePanel() {
        super();
        setLayout(new BorderLayout());
        northPanelMaker();
        centerPanelMaker();
    }
//    Creates the northern panel of response tab which consists of the status message, size and response time of the request
    private void northPanelMaker() {

        JPanel northPanel = new JPanel();
        String statusCode = String.valueOf(response.statusCode());
        JLabel statusMessage = new JLabel(statusCode + (statusCode.startsWith("2") ? "OK" : ""));
        JLabel ping = new JLabel(jurl.getResponseDelay() + "ms");
        JLabel responseSize = new JLabel(response.body().length / 1000F + "KB");
        statusMessage.setLocation(5, 5);

        {
            GroupLayout gp = new GroupLayout(northPanel);
            gp.setVerticalGroup(gp.createSequentialGroup()
                    .addGap(4)
                    .addGroup(gp.createParallelGroup()
                            .addComponent(statusMessage)
                            .addComponent(ping)
                            .addComponent(responseSize))
                    .addGap(4));
            gp.setHorizontalGroup(gp.createSequentialGroup()
                    .addGap(6)
                    .addComponent(statusMessage)
                    .addComponent(ping)
                    .addComponent(responseSize)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED));
            gp.setAutoCreateGaps(true);
            northPanel.setLayout(gp);
        }

        northPanel.setBorder(BorderFactory.createBevelBorder(0));

        statusMessage.setBorder(BorderFactory.createBevelBorder(0));
        ping.setBorder(BorderFactory.createBevelBorder(0));
        responseSize.setBorder(BorderFactory.createBevelBorder(0));

        northPanel.add(statusMessage);
        northPanel.add(ping);
        northPanel.add(responseSize);

        add(northPanel, BorderLayout.NORTH);
    }
//    Creates the central panel of response tab, which has a setting for body view and showing header tab,
//    and has an area to show response body
    private void centerPanelMaker() {

        JScrollPane tabsScrollPane = new JScrollPane();
        JPanel tabs = new JPanel();
        tabsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        tabsScrollPane.getViewport().setView(tabs);

        JButton headerButton = new JButton("Header");
        headerButton.addActionListener(buttonHandler);

        String[] messageTypes = {"Preview", "Source Code", "Raw Data"};
        JComboBox<String> messageType = new JComboBox<>(messageTypes);
        messageType.setMaximumSize(messageType.getPreferredSize());
        messageType.addActionListener(buttonHandler);

//        tabs.add(messageType);
//        tabs.add(headerButton);
        {
            GroupLayout gp = new GroupLayout(tabs);
            gp.setVerticalGroup(gp.createSequentialGroup().addGap(6)
                    .addGroup(gp.createParallelGroup().addComponent(messageType).addComponent(headerButton))
                    .addGap(6));
            gp.setHorizontalGroup(gp.createSequentialGroup().addGap(6).addComponent(messageType).addComponent(headerButton)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED));
            gp.setAutoCreateGaps(true);
            tabs.setLayout(gp);
        }
        tabsScrollPane.setPreferredSize(new Dimension(tabs.getPreferredSize().width + 3, tabs.getPreferredSize().height));
        tabsScrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 9));
        tabsScrollPane.setBorder(BorderFactory.createEmptyBorder());

        centerPanel.add(tabsScrollPane, BorderLayout.NORTH);

        responseBody = new String(response.body());
        JTextArea responseBodyField = new JTextArea(responseBody);
        responseBodyField.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(responseBodyField);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        headerPanelMaker();
    }

//    Creates the header panel with the response received
    private void headerPanelMaker() {

        JPanel headerTab = new JPanel();
        JPanel headerPanel;
        JButton copy = new JButton("Copy to Clipboard");
        copy.setActionCommand("COPY");
        copy.addActionListener(buttonHandler);
        headerPanel = new JPanel(new GridBagLayout());
        {
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);
            c.gridx = 0;
            c.gridy = 0;
            headerPanel.add(nameCol, c);
        }
        {
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);
            c.gridx = 1;
            c.gridy = 0;
            headerPanel.add(valueCol, c);
        }

        int i = 1;
        for (Map.Entry<String, List<String>> entry : response.headers().map().entrySet()) {
            String k = entry.getKey();
            List<String> v = entry.getValue();
            JTextField left = new JTextField(k);
            JTextField right = new JTextField(v.toString().replaceAll("\\[", "").replaceAll("]", ""));
            left.setEditable(false);
//            left.setLineWrap(true);
            right.setEditable(false);
//            right.setLineWrap(true);
            {
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.BOTH;
                c.insets = new Insets(4, 4, 4, 4);
                c.gridy = i;
                c.gridx = 0;
                c.weightx = 1.0;
                headerPanel.add(left, c);
            }
            {
                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.BOTH;
                c.insets = new Insets(4, 4, 4, 4);
                c.gridy = i;
                c.gridx = 1;
                c.weightx = 1.0;
                headerPanel.add(right, c);
            }
            i++;
        }

        {
            GroupLayout groupLayout = new GroupLayout(headerTab);
            headerTab.setLayout(groupLayout);

            groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
                    .addComponent(headerPanel)
                    .addComponent(copy));

            groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                    .addComponent(headerPanel)
                    .addGroup(groupLayout.createSequentialGroup()
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(copy)));
            groupLayout.setAutoCreateGaps(true);
            groupLayout.setAutoCreateContainerGaps(true);
        }
        {
            JPanel copyToClipboard = new JPanel(new FlowLayout(FlowLayout.LEFT));
            copyToClipboard.add(copy);
            headerPanel.add(copyToClipboard);
        }
        {
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.getViewport().setView(headerTab);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 9));
            scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(9, 0));
            this.headerPanel = new JPanel(new BorderLayout());
            this.headerPanel.add(scrollPane, BorderLayout.CENTER);
        }
    }
//    Handles different buttons of the panel, so that the intended action will be done when they're selected
    private class ButtonHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            if (e.getActionCommand().equals("COPY")) {
                new Thread(() -> {
                    StringBuilder headers = new StringBuilder();
                    response.headers().map().forEach((k, v) -> headers
                            .append(k)
                            .append(":")
                            .append(v.toString().replaceAll("\\[", "").replaceAll("]", ""))
                            .append("\n"));
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    StringSelection stringSelection = new StringSelection(headers.toString());
                    clipboard.setContents(stringSelection, null);
                }).start();
            } else {
                BorderLayout centerPanelLayout = (BorderLayout) centerPanel.getLayout();
                JPanel centralTabs = (JPanel) ((JScrollPane) centerPanelLayout.getLayoutComponent(BorderLayout.NORTH)).getViewport().getView();
                JComboBox<Component> messageType = (JComboBox<Component>) centralTabs.getComponent(0);
                JButton headerButton = (JButton) centralTabs.getComponent(1);

                centerPanel.remove(centerPanelLayout.getLayoutComponent(BorderLayout.CENTER));
                if (e.getSource().equals(messageType)) {
                    String selected = (String) messageType.getSelectedItem();
                    JTextArea responseBodyField = new JTextArea(responseBody);
                    responseBodyField.setEditable(false);
                    JScrollPane scrollPane = new JScrollPane(responseBodyField);
                    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                    JTextPane text;
                    switch (Objects.requireNonNull(selected)) {
                        case "Preview":
                            List<String> headers = response.headers().allValues("content-type");
                            text = new JTextPane();
                            for (String header : headers) {
                                if (header.contains("image")) {
                                    text.setEditable(false);
                                    Icon icon = new ImageIcon(response.body());
                                    text.insertIcon(icon);
                                }
                                if (header.contains("json")) {
                                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                    JsonParser jsonParser = new JsonParser();
                                    JsonElement jsonElement = jsonParser.parse(responseBody);
                                    text.setContentType("application/json");
                                    text.setText(gson.toJson(jsonElement));
                                    text.setEditable(false);
                                }
                                if (header.contains("html")) {
                                    text.setEditable(false);
                                    text.setContentType("text/html");
                                    HTMLEditorKit kit = new HTMLEditorKit();
                                    text.setEditorKit(kit);
                                    Document document = kit.createDefaultDocument();
                                    text.setDocument(document);
                                    text.setText("<html><body><p>" + responseBody + "</p></body></html>");
                                }
                            }
                            scrollPane.getViewport().setView(text);
                            break;
                        case "Source Code":
                            List<String> headersList = response.headers().allValues("content-type");
                            text = new JTextPane();
                            for (String header : headersList) {
                                if (header.contains("json")) {
                                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                                    JsonParser jsonParser = new JsonParser();
                                    JsonElement jsonElement = jsonParser.parse(responseBody);
                                    text.setText(gson.toJson(jsonElement));
                                    text.setEditable(false);
                                }
                                if (header.contains("html")) {
                                    text.setEditable(false);
                                    text.setContentType("text/html");
                                    HTMLEditorKit kit = new HTMLEditorKit();
                                    text.setEditorKit(kit);
                                    Document document = kit.createDefaultDocument();
                                    text.setDocument(document);
                                    text.setText(responseBody);
                                }
                            }
                            responseBodyField.setText(text.getText());
                            responseBodyField.setLineWrap(true);
                            scrollPane.getViewport().setView(responseBodyField);
                            break;
                        case "Raw Data":
                            responseBodyField.setText(responseBody);
                            responseBodyField.setLineWrap(true);
                            scrollPane.getViewport().setView(responseBodyField);
                            break;
                    }
                    centerPanel.add(scrollPane, BorderLayout.CENTER);
                } else if (e.getSource().equals(headerButton)) {
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(headerPanel, BorderLayout.NORTH);
                    centerPanel.add(panel, BorderLayout.CENTER);
                }
            }
            revalidate();
            repaint();
        }
    }

    /**
     * changes the theme of GUI
     * @param type 1 for dark and 0 for light theme
     */
    public void changeTheme(int type) {
        BorderLayout layout = (BorderLayout) getLayout();
        JPanel northPanel = (JPanel) layout.getLayoutComponent(BorderLayout.NORTH);
        JPanel centerPanel = (JPanel) layout.getLayoutComponent(BorderLayout.CENTER);
        BorderLayout centerLayout = (BorderLayout) centerPanel.getLayout();

        if (type == 1) {

            northPanel.setBackground(Color.DARK_GRAY);
            northPanel.getComponent(0).setBackground(Color.WHITE);
            northPanel.getComponent(0).setForeground(Color.WHITE);
            northPanel.getComponent(1).setBackground(Color.WHITE);
            northPanel.getComponent(1).setForeground(Color.WHITE);
            northPanel.getComponent(2).setBackground(Color.WHITE);
            northPanel.getComponent(2).setForeground(Color.WHITE);
            ((JScrollPane) centerLayout.getLayoutComponent(BorderLayout.NORTH)).getViewport().getView().setBackground(Color.DARK_GRAY);
            JScrollPane scrollPane = (JScrollPane) centerLayout.getLayoutComponent(BorderLayout.NORTH);
            scrollPane.getViewport().getView().setForeground(Color.WHITE);
            JScrollPane body = (JScrollPane) centerLayout.getLayoutComponent(BorderLayout.CENTER);
            body.getViewport().getView().setBackground(Color.DARK_GRAY);
            body.getViewport().getView().setForeground(Color.WHITE);
        } else {
            northPanel.setBackground(Color.WHITE);
            northPanel.getComponent(0).setForeground(Color.BLACK);
            northPanel.getComponent(1).setForeground(Color.black);
            northPanel.getComponent(2).setForeground(Color.black);
            ((JScrollPane) centerLayout.getLayoutComponent(BorderLayout.NORTH)).getViewport().getView().setBackground(Color.white);
            JScrollPane scrollPane = (JScrollPane) centerLayout.getLayoutComponent(BorderLayout.NORTH);
            scrollPane.getViewport().getView().setForeground(Color.black);
//            JScrollPane body = (JScrollPane) centerLayout.getLayoutComponent(BorderLayout.CENTER);
//            body.getViewport().getView().setBackground(Color.white);
//            body.getViewport().getView().setForeground(Color.black);
        }
        this.setBackground(Color.BLACK);
    }
}
