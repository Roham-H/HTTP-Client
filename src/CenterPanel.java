import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.Flow;

public class CenterPanel extends JSplitPane {

    private JPanel centralRightPanel;
    private JPanel centralLeftPanel;
    private JPanel centralLeftTopPanel;
    private JPanel centralLeftBottomPanel;

    private JTextField URLBar;
    private JTextArea bodyMessageField;
    private JPanel headerPanel = new JPanel(new GridLayout( 0, 2));
    private JComboBox methodList;
    private JComboBox requestBodyType;
    private JButton headerButton = new JButton("Header");
    private JButton authButton = new JButton("Auth - BearerToken");
    private JButton queryButton = new JButton("Query");
    private JPanel fillingTab = new JPanel(new BorderLayout());
    private JFrame frame = ClientGUI.getFrame();

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
        String[] requestBodyTypeList = {"JSON", "From Data", "Binary File"};
        requestBodyType = new JComboBox(requestBodyTypeList);
        requestBodyType.addActionListener(buttonHandler);

        requestDetails.add(requestBodyType);
        requestDetails.add(authButton);
        requestDetails.add(queryButton);
        requestDetails.add(headerButton);
        headerButton.addActionListener(buttonHandler);
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
        scrollPane.setPreferredSize(new Dimension(centralLeftPanel.getPreferredSize()));
        addInputBars();
    }

    private void addInputBars(){
        JTextField headerField = new JTextField("New header");
        JTextField valueField = new JTextField("New value");
        new TextPrompt("New header", headerField);
        new TextPrompt("New value", valueField);
        headerPanel.add(headerField);
        headerField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (headerField.getText().equals("New header")) {
                    addInputBars();
                    headerField.setText("Header");
                    valueField.setText("Value");
                    update();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });
        headerPanel.add(valueField);
        valueField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (valueField.getText().equals("New value")){
                    addInputBars();
                    headerField.setText("Header");
                    valueField.setText("Value");
                    update();
                }
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
    }

    private void update(){
        frame.revalidate();
        frame.repaint();
    }

    private class ButtonHandler implements ActionListener {

        private BorderLayout layout = (BorderLayout) fillingTab.getLayout();
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(requestBodyType)){
                String selectedItem = (String) requestBodyType.getSelectedItem();
                switch (selectedItem) {
                    case "JSON":
                        fillingTab.removeAll();
                        fillingTab.setLayout(new BorderLayout());
                        fillingTab.add(new JScrollPane( bodyMessageField), BorderLayout.CENTER);
                        update();
                        break;
                }
            }
            else if (e.getSource().equals(headerButton)){
                fillingTab.removeAll();
                fillingTab.add(headerPanel, BorderLayout.NORTH);
                headerPanel.setVisible(true);
                update();
            }
        }
    }
}
