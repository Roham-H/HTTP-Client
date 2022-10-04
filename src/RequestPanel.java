import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * The main panel which show a tab which allows the user to create the intended request and to send it
 */
public class RequestPanel extends JSplitPane {

    private final JPanel centralLeftTopPanel;
    private final JPanel centralLeftBottomPanel;

    private JTextArea bodyMessageField;
    private final JFileChooser fileChooser = new JFileChooser();
    private JTextField queryURL;
    private final JPanel headerPanel = new JPanel(new GridLayout(0, 1));
    private final JPanel formDataPanel = new JPanel(new GridLayout(0, 1));
    private final JPanel queryPanel = new JPanel(new GridLayout(0, 1));
    private final JPanel authPanelBearerToken = new JPanel();
    private final JComboBox<String> requestBodyType;
    private final JButton headerButton = new JButton("Header");
    private final JButton authButton = new JButton("Auth - BearerToken");
    private final JButton queryButton = new JButton("Query");
    private final JPanel fillingTab = new JPanel(new BorderLayout());
    private final JFrame frame = ClientGUI.getFrame();

    private JTextField leftFormInputBar;
    private TextPrompt leftFormInputBarTP;
    private JTextField leftHeaderInputBar;
    private TextPrompt leftHeaderInputBarTP;

    private JTextField rightFormInputBar;
    private TextPrompt rightFormInputBarTP;
    private JTextField rightHeaderInputBar;
    private TextPrompt rightHeaderInputBarTp;

    private JTextField leftQueryBar;
    private JTextField rightQueryBar;
    private TextPrompt leftQueryBarTP;
    private TextPrompt rightQueryBarTP;

    private final JComboBox<String> methodList;
    private final JTextField urlBar;
    private final JPanel requestDetails;

    public RequestPanel(){
        super(JSplitPane.HORIZONTAL_SPLIT);
        JPanel centralLeftPanel = new JPanel(new BorderLayout());
        centralLeftTopPanel = new JPanel(new FlowLayout());
        centralLeftBottomPanel = new JPanel(new BorderLayout());
        setLeftComponent(centralLeftPanel);
        ButtonHandler buttonHandler = new ButtonHandler();


        requestDetails = new JPanel();
        String[] requestBodyTypeList = {"JSON", "Form Data", "Binary File"};
        requestBodyType = new JComboBox<>(requestBodyTypeList);
        requestBodyType.addActionListener(buttonHandler);

        requestDetails.add(requestBodyType);
        requestDetails.add(authButton);
        requestDetails.add(queryButton);
        requestDetails.add(headerButton);
        headerButton.addActionListener(buttonHandler);
        queryButton.addActionListener(buttonHandler);
        authButton.addActionListener(buttonHandler);
        centralLeftBottomPanel.add(requestDetails, BorderLayout.NORTH);

        String[] methodListName = {"GET", "DELETE", "POST", "PUT", "PATCH"};
        methodList = new JComboBox<>(methodListName);
        methodList.addActionListener(buttonHandler);
        centralLeftTopPanel.add(methodList);


        {
            urlBar = new JTextField(30);
            new TextPrompt("https://api.myproduct.com/v1/users", urlBar);
            urlBar.addActionListener(buttonHandler);
            urlBar.setActionCommand("SEND");
            centralLeftTopPanel.add(urlBar);
        }
        JButton send = new JButton("Send");
        {
            send.addActionListener(buttonHandler);
            send.setActionCommand("SEND");
            centralLeftTopPanel.add(send);
        }

        JScrollPane topPanel = new JScrollPane(centralLeftTopPanel);
        methodList.setMaximumSize(methodList.getPreferredSize());

        {
            GroupLayout gp = new GroupLayout(centralLeftTopPanel);
            centralLeftTopPanel.setLayout(gp);
            gp.setVerticalGroup(gp.createSequentialGroup().addGap(8).addGroup(gp.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(methodList)
                    .addComponent(urlBar)
                    .addComponent(send)));
            gp.linkSize(SwingConstants.VERTICAL, methodList, urlBar, send);
            gp.setHorizontalGroup(gp.createSequentialGroup().addGap(10).addComponent(methodList).addComponent(urlBar).addComponent(send).addGap(10));
            gp.setAutoCreateGaps(true);
        }

        topPanel.setPreferredSize(new Dimension(centralLeftTopPanel.getPreferredSize().width + 3, centralLeftTopPanel.getPreferredSize().height + 10));
        topPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        topPanel.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 9));
        topPanel.setBorder(BorderFactory.createEmptyBorder());


        centralLeftPanel.add(topPanel, BorderLayout.NORTH);
        centralLeftPanel.add(centralLeftBottomPanel, BorderLayout.CENTER);
        firstLook();
        setDividerSize(5);
    }
//    initializes the tab
    private void firstLook(){
        centralLeftBottomPanel.add(fillingTab, BorderLayout.CENTER);
        bodyMessageField = new JTextArea();
        JScrollPane scrollPane = new JScrollPane( bodyMessageField );
        bodyMessageField.setColumns(centralLeftTopPanel.getWidth());
        bodyMessageField.setLineWrap(true);
        fillingTab.add(scrollPane, BorderLayout.CENTER);

        queryPanel.add(new JLabel("URL Preview"));
        queryURL = new JTextField("https://api.myproduct.com/v1/users");
        queryURL.setEnabled(false);
        queryPanel.add(queryURL);

        addHeaderInputBars();
        authPanelMaker();
        addFormDataInputBars();
        addQueryInputBars();
    }
//    creates the authentication panel
    private void authPanelMaker(){

        GroupLayout layout = new GroupLayout(authPanelBearerToken);
        authPanelBearerToken.setLayout(layout);

        JLabel token = new JLabel("Token");
        JLabel prefix = new JLabel("Prefix");
        JTextField tokenBar = new JTextField();
        JTextField prefixBar = new JTextField();
        JCheckBox enabled = new JCheckBox("Enabled");

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup()
        .addComponent(token)
        .addComponent(prefix)
        .addComponent(enabled))
        .addGroup(layout.createParallelGroup()
        .addComponent(tokenBar)
        .addComponent(prefixBar)));

        layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup()
        .addComponent(token)
        .addComponent(tokenBar))
        .addGroup(layout.createParallelGroup()
        .addComponent(prefix)
        .addComponent(prefixBar))
        .addComponent(enabled));
    }
//    Creates the headers panel
    private void addHeaderInputBars() {

        leftHeaderInputBar = new JTextField();
        rightHeaderInputBar = new JTextField();
        leftHeaderInputBarTP = new TextPrompt("New header", leftHeaderInputBar);
        rightHeaderInputBarTp= new TextPrompt("New value", rightHeaderInputBar);
        headerPanel.add(rowMaker(leftHeaderInputBar, rightHeaderInputBar));
    }
//    Creates the form Data panel identically to the headers panel
    private void addFormDataInputBars() {

        leftFormInputBar = new JTextField();
        rightFormInputBar = new JTextField();
        rightFormInputBarTP = new TextPrompt("New value", rightFormInputBar);
        leftFormInputBarTP = new TextPrompt("New name", leftFormInputBar);
        JPanel row = rowMaker(leftFormInputBar, rightFormInputBar);
        formDataPanel.add(row);
    }

    private void addQueryInputBars(){
        queryLivePreview queryLivePreview = new queryLivePreview();
        leftQueryBar = new JTextField();
        rightQueryBar = new JTextField();
        leftQueryBar.getDocument().addDocumentListener(queryLivePreview);
        rightQueryBar.getDocument().addDocumentListener(queryLivePreview);
        rightQueryBarTP = new TextPrompt("New value", rightQueryBar);
        leftQueryBarTP = new TextPrompt("New name", leftQueryBar);
        queryPanel.add(rowMaker(leftQueryBar, rightQueryBar));
    }
//    Creates identical rows for the panels that have input bars
    private JPanel rowMaker(JTextField leftBar, JTextField rightBar) {

        InputBarsHandler inputBarsHandler = new InputBarsHandler();
        JPanel row = new JPanel();
        GroupLayout layout = new GroupLayout(row);
        row.setLayout(layout);

        JCheckBox rowCheckBox = new JCheckBox();
        Icon icon = new ImageIcon((new ImageIcon("open-trash-can.png")
                .getImage().getScaledInstance(10, 10, java.awt.Image.SCALE_SMOOTH)));
        JButton deleteButton = new JButton(icon);
        deleteButton.setPreferredSize(new Dimension(10, 10));

        rowCheckBox.setEnabled(false);
        rowCheckBox.setSelected(true);
        deleteButton.setEnabled(false);

        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 10, 10)
                .addComponent(leftBar, GroupLayout.DEFAULT_SIZE, 0, GroupLayout.DEFAULT_SIZE)
                .addComponent(rightBar, GroupLayout.DEFAULT_SIZE, 0, GroupLayout.DEFAULT_SIZE)
                .addComponent(rowCheckBox)
                .addComponent(deleteButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 10, 10));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 3, 3)
                .addGroup(layout.createParallelGroup()
                        .addComponent(leftBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                        .addComponent(rightBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                        .addComponent(rowCheckBox)
                        .addComponent(deleteButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 3, 3));

        rowCheckBox.addActionListener(e -> {
            row.setEnabled(rowCheckBox.isSelected());
            update();
        });

        deleteButton.addActionListener(e -> {
            row.getParent().remove(row);
            update();
        });

        leftBar.addFocusListener(inputBarsHandler);
        rightBar.addFocusListener(inputBarsHandler);
        return row;
    }

//    Finds the intended row among the rows available on panels like headers panel
    private JPanel rowFinder(JPanel panel, FocusEvent e){
        JTextField selected = (JTextField) e.getSource();
        for (int i = 0; i < panel.getComponentCount(); i++){
            if (panel.getComponent(i) instanceof JPanel) {
                JPanel row = (JPanel) panel.getComponent(i);
                if (row.getComponent(0).equals(selected) || row.getComponent(1).equals(selected)) {
                    return (JPanel) panel.getComponent(i);
                }
            }
        }
        return null;
    }

//    updates the GUI
    private void update(){

        if (bodyMessageField.getBackground().equals(Color.DARK_GRAY))
            changeTheme(1);
        else
            changeTheme(0);

        frame.revalidate();
        frame.repaint();
    }
//    Dynamic update for the query bar's url preview
    private class queryLivePreview implements DocumentListener{

        @Override
        public void insertUpdate(DocumentEvent e) {
            queryUrlUpdate();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            queryUrlUpdate();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            queryUrlUpdate();
        }

        private void queryUrlUpdate() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("?");

            Component[] components = queryPanel.getComponents();
            for (int i = 2; i < components.length; i++) {
                Component component = components[i];
                JPanel eachRow = (JPanel) component;
                if (component.isEnabled()) {
                    String leftBar = ((JTextField) eachRow.getComponent(0)).getText();
                    String rightBar = ((JTextField) eachRow.getComponent(1)).getText();
                    stringBuilder.append(leftBar)
                            .append( !leftBar.isEmpty() ? "=" : "")
                            .append(rightBar);
                    if (i < components.length - 2 && !leftBar.isEmpty() && !rightBar.isEmpty())
                        stringBuilder.append("&");

                }
            }
            if (urlBar.getText().isEmpty())
                queryURL.setText("https://api.myproduct.com/v1/users" + stringBuilder);
            else
                queryURL.setText(urlBar.getText() + stringBuilder);
            update();
        }
    }
//    Allows creating new rows for new inputs
    private class InputBarsHandler implements FocusListener{

        @Override
        public void focusGained(FocusEvent e) {
            if (e.getSource().equals(leftHeaderInputBar) || e.getSource().equals(rightHeaderInputBar)) {
                leftHeaderInputBarTP.setText("header");
                rightHeaderInputBarTp.setText("value");
                addHeaderInputBars();
                JPanel row = rowFinder(headerPanel, e);
                if (row != null) {
                    row.getComponent(2).setEnabled(true);
                    row.getComponent(3).setEnabled(true);
                }
            }

            else if (e.getSource().equals(leftFormInputBar) || e.getSource().equals(rightFormInputBar)) {
                leftFormInputBarTP.setText("name");
                rightFormInputBarTP.setText("value");
                addFormDataInputBars();
                JPanel row = rowFinder(formDataPanel, e);
                if (row != null) {
                    row.getComponent(2).setEnabled(true);
                    row.getComponent(3).setEnabled(true);
                }
            }
            else if (e.getSource().equals(leftQueryBar) || e.getSource().equals(rightQueryBar)){
                leftQueryBarTP.setText("name");
                rightQueryBarTP.setText("value");
                addQueryInputBars();
                JPanel row = rowFinder(queryPanel, e);
                if (row != null) {
                    row.getComponent(2).setEnabled(true);
                    row.getComponent(3).setEnabled(true);
                    update();
                }
            }
            update();
        }

        @Override
        public void focusLost(FocusEvent e) {

        }
    }
//    Allows the buttons to get the intended work done
    private class ButtonHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            if (e.getSource().equals(requestBodyType)){
                String selectedItem = (String) requestBodyType.getSelectedItem();
                switch (selectedItem) {
                    case "JSON":
                        fillingTab.removeAll();
                        fillingTab.add(new JScrollPane(bodyMessageField), BorderLayout.CENTER);
                        break;
                    case "Form Data":
                        fillingTab.removeAll();
                        fillingTab.add(formDataPanel, BorderLayout.NORTH);
                        break;
                    case "Binary File":
                        fillingTab.removeAll();
                        fillingTab.add(fileChooser, BorderLayout.CENTER);
                        break;
                }
            }

            if (e.getActionCommand().equals("SEND")) {
                Runnable runnable = () -> {
                    try {
                        Handler.requestPanels.get(ClientGUI.getRequestPanel()).sendRequest();
                        ResponsePanel responsePanel = new ResponsePanel();
                        setRightComponent(responsePanel);
                        update();
                    } catch (IOException | ParseException | InterruptedException ioException) {
                        ioException.printStackTrace();
                    }
                };
                new Thread(runnable).start();
            }

            else if (e.getSource().equals(headerButton)){
                fillingTab.removeAll();
                fillingTab.add(headerPanel, BorderLayout.NORTH);
            }
            else if (e.getSource().equals(queryButton)){
                fillingTab.removeAll();
                fillingTab.add(queryPanel, BorderLayout.NORTH);
            }
            else if (e.getSource().equals(authButton)){
                fillingTab.removeAll();
                fillingTab.add(authPanelBearerToken, BorderLayout.NORTH);
            }
            update();
        }
    }

    /**
     * @return current url with or without query inputs
     */
    public String getURL() {
        if (queryURL.getText().contains("https://api.myproduct.com/v1/users"))
            return urlBar.getText();
        else return queryURL.getText();
    }

    /**
     * @return the chosen method
     */
    public String getMethod() {
        return (String) methodList.getSelectedItem();
    }

    /**
     * @return the headers panel
     */
    public JPanel getHeaderPanel() {
        return headerPanel;
    }

    /**
     * @return the formData panel
     */
    public JPanel getFormDataPanel() {
        return formDataPanel;
    }

    /**
     * @return body as text
     */
    public String getBodyMessage() {
        return bodyMessageField.getText();
    }

    /**
     * @return the type of body
     */
    public String getBodyType(){
        return (String) requestBodyType.getSelectedItem();
    }

    /**
     * @return the file chooser
     */
    public JFileChooser getFileChooser() {
        return fileChooser;
    }

    /**
     * changes the theme of GUI
     * @param type 1 for dark and 0 for light theme
     */
    public void changeTheme(int type) {
        if (this.getRightComponent() != null)
            ((ResponsePanel) this.getRightComponent()).changeTheme(type);
        JPanel panel = (JPanel) ((BorderLayout) centralLeftBottomPanel.getLayout()).getLayoutComponent(BorderLayout.NORTH);
        if (type == 1) {
            headerButton.setBackground(Color.DARK_GRAY);
            headerButton.setForeground(Color.WHITE);
            authButton.setBackground(Color.DARK_GRAY);
            authButton.setForeground(Color.WHITE);
            queryButton.setBackground(Color.DARK_GRAY);
            queryButton.setForeground(Color.WHITE);
            requestBodyType.setBackground(Color.DARK_GRAY);
            requestBodyType.setForeground(Color.WHITE);
            centralLeftTopPanel.setBackground(Color.DARK_GRAY);
            centralLeftTopPanel.setForeground(Color.WHITE);
//            methodList.setBackground(Color.DARK_GRAY);
//            methodList.setBackground(Color.WHITE);
//            urlBar.setBackground(Color.DARK_GRAY);
//            urlBar.setBackground(Color.WHITE);
//            centralLeftTopPanel.getComponent(2).setBackground(Color.DARK_GRAY);
//            centralLeftTopPanel.getComponent(2).setBackground(Color.WHITE);
            requestDetails.setBackground(Color.DARK_GRAY);
            requestDetails.setForeground(Color.WHITE);
            bodyMessageField.setBackground(Color.DARK_GRAY);
            bodyMessageField.setForeground(new Color(0xFF6B00));
            bodyMessageField.setCaretColor(new Color(0xFF6B00));
            fillingTab.setBackground(Color.DARK_GRAY);
            fillingTab.setForeground(Color.WHITE);
            barPanelColorChanger(headerPanel, type);
            barPanelColorChanger(formDataPanel, type);
            this.setBackground(Color.BLACK);
        }
        else {
            headerButton.setBackground(Color.WHITE);
            headerButton.setForeground(Color.BLACK);
            authButton.setBackground(Color.WHITE);
            authButton.setForeground(Color.BLACK);
            queryButton.setBackground(Color.WHITE);
            queryButton.setForeground(Color.BLACK);
            requestBodyType.setBackground(Color.WHITE);
            requestBodyType.setForeground(Color.BLACK);
            centralLeftTopPanel.setBackground(Color.WHITE);
            centralLeftTopPanel.setForeground(Color.BLACK);
//            methodList.setBackground(Color.WHITE);
//            methodList.setBackground(Color.BLACK);
//            urlBar.setBackground(Color.WHITE);
//            urlBar.setBackground(Color.BLACK);
//            centralLeftTopPanel.getComponent(2).setBackground(Color.WHITE);
//            centralLeftTopPanel.getComponent(2).setBackground(Color.BLACK);
            requestDetails.setBackground(Color.WHITE);
            requestDetails.setForeground(Color.BLACK);
            bodyMessageField.setBackground(Color.WHITE);
            bodyMessageField.setForeground(Color.BLACK);
            bodyMessageField.setCaretColor(Color.BLACK);
            fillingTab.setBackground(Color.WHITE);
            fillingTab.setForeground(Color.BLACK);
            barPanelColorChanger(headerPanel, type);
            barPanelColorChanger(formDataPanel, type);
            this.setBackground(Color.WHITE);
        }
    }

    private void barPanelColorChanger(JPanel panel, int type) {
        if (type == 1) {
            for (int i = 0; i < panel.getComponentCount(); i++) {
                JPanel row = (JPanel) panel.getComponent(i);
                row.setBackground(Color.DARK_GRAY);
                row.setForeground(Color.WHITE);

                row.getComponent(0).setBackground(Color.DARK_GRAY);
                row.getComponent(0).setForeground(new Color(0xFF6B00));
                row.getComponent(1).setBackground(Color.DARK_GRAY);
                row.getComponent(1).setForeground(new Color(0xFF6B00));
                row.getComponent(2).setBackground(Color.DARK_GRAY);
                row.getComponent(3).setBackground(Color.DARK_GRAY);
            }
            panel.setBackground(Color.DARK_GRAY);
            panel.setForeground(Color.WHITE);
        }
        else {
            for (int i = 0; i < panel.getComponentCount(); i++) {
                JPanel row = (JPanel) panel.getComponent(i);
                row.setBackground(Color.WHITE);
                row.setForeground(Color.BLACK);

                row.getComponent(0).setBackground(Color.WHITE);
                row.getComponent(0).setForeground(Color.BLACK);
                row.getComponent(1).setBackground(Color.WHITE);
                row.getComponent(1).setForeground(Color.BLACK);
                row.getComponent(2).setBackground(Color.WHITE);
                row.getComponent(3).setBackground(Color.WHITE);
            }
            panel.setBackground(Color.WHITE);
            panel.setForeground(Color.BLACK);
        }
    }
}
