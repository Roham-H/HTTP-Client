import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CenterPanel extends JSplitPane {

    private JPanel centralRightPanel;
    private JPanel centralLeftPanel;
    private JPanel centralLeftTopPanel;
    private JPanel centralLeftBottomPanel;

    private JTextField URLBar;
    private JTextArea bodyMessageField;
    private JPanel headerPanel = new JPanel(new GridLayout(0, 1));
    private JPanel formDataPanel = new JPanel(new GridLayout(0, 1));
    private JPanel queryPanel = new JPanel(new GridLayout(0, 1));
    private JPanel authPanelBearerToken = new JPanel();
    private JComboBox methodList;
    private JComboBox requestBodyType;
    private JButton headerButton = new JButton("Header");
    private JButton authButton = new JButton("Auth - BearerToken");
    private JButton queryButton = new JButton("Query");
    private JPanel fillingTab = new JPanel(new BorderLayout());
    private JFrame frame = ClientGUI.getFrame();

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

    public CenterPanel(){
        super(JSplitPane.HORIZONTAL_SPLIT);
        centralLeftPanel = new JPanel(new BorderLayout());
        centralRightPanel = new JPanel(new BorderLayout());
        centralLeftTopPanel = new JPanel(new FlowLayout());
        centralLeftBottomPanel = new JPanel(new BorderLayout());
        add(centralLeftPanel);
        add(centralRightPanel);
        ButtonHandler buttonHandler = new ButtonHandler();


        JPanel requestDetails = new JPanel();
        String[] requestBodyTypeList = {"JSON", "Form Data", "Binary File"};
        requestBodyType = new JComboBox(requestBodyTypeList);
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
        methodList = new JComboBox(methodListName);
        methodList.addActionListener(buttonHandler);
        JScrollPane topPanel = new JScrollPane(centralLeftTopPanel);
        centralLeftTopPanel.add(methodList);


        URLBar = new JTextField(30);
        new TextPrompt("https://api.myproduct.com/v1/users", URLBar);
        centralLeftTopPanel.add(URLBar);
        centralLeftTopPanel.add(new JButton("Send"));

        centralLeftPanel.add(topPanel, BorderLayout.NORTH);
        centralLeftPanel.add(centralLeftBottomPanel, BorderLayout.CENTER);
        firstLook();
        topPanel.setPreferredSize(new Dimension(centralLeftTopPanel.getPreferredSize().width + 10, topPanel.getPreferredSize().height));
    }

    private void firstLook(){
        centralLeftBottomPanel.add(fillingTab, BorderLayout.CENTER);
        bodyMessageField = new JTextArea();
        JScrollPane scrollPane = new JScrollPane( bodyMessageField );
        bodyMessageField.setColumns(centralLeftTopPanel.getWidth());
        bodyMessageField.setLineWrap(true);
        fillingTab.add(scrollPane, BorderLayout.CENTER);
//        scrollPane.setPreferredSize(new Dimension(centralLeftPanel.getPreferredSize()));

        queryPanel.add(new JLabel("URL Preview"));
        JTextField queryURL = new JTextField("...");
        queryURL.setEnabled(false);
        queryPanel.add(queryURL);
        addHeaderInputBars();
        authPanelMaker();
        addFormDataInputBars();
        addQueryInputBars();
    }

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

    private void addHeaderInputBars() {

        leftHeaderInputBar = new JTextField();
        rightHeaderInputBar = new JTextField();
        leftHeaderInputBarTP = new TextPrompt("New header", leftHeaderInputBar);
        rightHeaderInputBarTp= new TextPrompt("New value", rightHeaderInputBar);
        headerPanel.add(rowMaker(leftHeaderInputBar, rightHeaderInputBar));
    }

    private void addFormDataInputBars() {

        leftFormInputBar = new JTextField();
        rightFormInputBar = new JTextField();
        rightFormInputBarTP = new TextPrompt("New value", rightFormInputBar);
        leftFormInputBarTP = new TextPrompt("New name", leftFormInputBar);
        formDataPanel.add(rowMaker(leftFormInputBar, rightFormInputBar));
    }

    private void addQueryInputBars(){

        leftQueryBar = new JTextField();
        rightQueryBar = new JTextField();
        rightQueryBarTP = new TextPrompt("New value", rightQueryBar);
        leftQueryBarTP = new TextPrompt("New name", leftQueryBar);
        queryPanel.add(rowMaker(leftQueryBar, rightQueryBar));
    }

    private JPanel rowMaker(JTextField leftBar, JTextField rightBar){
        InputBarsHandler inputBarsHandler = new InputBarsHandler();
        JPanel row = new JPanel();
        GroupLayout layout = new GroupLayout(row);
        row.setLayout(layout);

        JCheckBox rowCheckBox = new JCheckBox();
        Icon icon = new ImageIcon((new ImageIcon("open-trash-can.png").getImage().
                getScaledInstance(10, 10,  java.awt.Image.SCALE_SMOOTH)));
        JButton deleteButton = new JButton(icon);
        deleteButton.setPreferredSize(new Dimension(10, 10));
        rowCheckBox.setEnabled(false);
        deleteButton.setEnabled(false);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(leftBar, GroupLayout.DEFAULT_SIZE, 0, GroupLayout.DEFAULT_SIZE)
                .addComponent(rightBar, GroupLayout.DEFAULT_SIZE, 0, GroupLayout.DEFAULT_SIZE)
                .addComponent(rowCheckBox)
                .addComponent(deleteButton));

        layout.setVerticalGroup(layout.createParallelGroup()
                .addComponent(leftBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(rightBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addComponent(rowCheckBox)
                .addComponent(deleteButton));

        rowCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                row.setEnabled(rowCheckBox.isSelected());
                update();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (headerPanel.isAncestorOf(row)) {
                    headerPanel.remove(row);
                }else if (formDataPanel.isAncestorOf(row)) {
                    formDataPanel.remove(row);
                }else if (queryPanel.isAncestorOf(row)){
                    queryPanel.remove(row);
                }
                update();
            }
        });

        leftBar.addFocusListener(inputBarsHandler);
        rightBar.addFocusListener(inputBarsHandler);
        return row;
    }


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


    private void update(){
        frame.revalidate();
        frame.repaint();
    }

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
                }
            }
            update();
        }

        @Override
        public void focusLost(FocusEvent e) {

        }
    }

    private class ButtonHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(requestBodyType)){
                String selectedItem = (String) requestBodyType.getSelectedItem();
                switch (selectedItem) {
                    case "JSON":
                        fillingTab.removeAll();
                        fillingTab.add(new JScrollPane( bodyMessageField), BorderLayout.CENTER);
                        break;
                    case "Form Data":
                        fillingTab.removeAll();
                        fillingTab.add(formDataPanel, BorderLayout.NORTH);
                        break;
                    case "Binary File":
                        fillingTab.removeAll();
                        fillingTab.add(new JFileChooser(), BorderLayout.CENTER);
                        break;
                }
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
}
