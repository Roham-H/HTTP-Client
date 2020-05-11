import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.security.Key;

public class ClientGUI {

    private static JFrame frame;
    private final JFrame optionsFrame;
    private final JSplitPane mainPanel;
    private final CenterPanel centerPanel;
    private final LeftPanel leftPanel;
    private JCheckBox followRedirect;
    private JCheckBox hideOnSystemTray;

    public ClientGUI(){

        frame = new JFrame("HTTP Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setBounds(250, 150, 1000, 550);
        leftPanel = new LeftPanel();
        mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerPanel = new CenterPanel();

        mainPanel.add(leftPanel);
        mainPanel.add(centerPanel);
        frame.setContentPane(mainPanel);
        frame.setMinimumSize(new Dimension(500, 400));
        JMenuBar menuBar = createMenuBar();
        frame.setJMenuBar(menuBar);
        frame.pack();
        mainPanel.setDividerSize(5);
        centerPanel.setDividerSize(5);
//        centerPanel.setDividerLocation(centerPanel.getLeftComponent().getPreferredSize().width);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setExtendedState(Frame.ICONIFIED);
            }
        });
        optionsFrame = makeOptionsFrame();
    }

    public void show(){
        frame.setVisible(true);
    }

    public static JFrame getFrame() {
        return frame;
    }

    private JMenuBar createMenuBar(){

        MenuBarHandler menuBarHandler = new MenuBarHandler();
        JMenuBar menuBar = new JMenuBar();
        JMenu application = new JMenu("Application");
        application.setMnemonic(KeyEvent.VK_A);
        JMenu view = new JMenu("View");
        view.setMnemonic(KeyEvent.VK_V);
        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
        menuBar.add(application);
        menuBar.add(view);
        menuBar.add(help);
        JMenuItem options = new JMenuItem("Options", KeyEvent.VK_O);
        JMenuItem exit = new JMenuItem("Exit", KeyEvent.VK_E);
        application.add(options);
        options.addActionListener(menuBarHandler);
        application.add(exit);
        exit.addActionListener(menuBarHandler);
        JMenuItem toggleFullScreen = new JMenuItem("Toggle Full Screen", KeyEvent.VK_T);
        JMenuItem toggleSideBar = new JMenuItem("Toggle Sidebar", KeyEvent.VK_O);
        view.add(toggleFullScreen);
        toggleFullScreen.addActionListener(menuBarHandler);
        view.add(toggleSideBar);
        toggleSideBar.addActionListener(menuBarHandler);
        JMenuItem about = new JMenuItem("About", KeyEvent.VK_A);
        JMenuItem helpItem = new JMenuItem("Help", KeyEvent.VK_H);
        KeyStroke ctrlS = KeyStroke.getKeyStroke("control S");
        toggleSideBar.setAccelerator(ctrlS);
        help.add(about);
        about.addActionListener(menuBarHandler);
        help.add(helpItem);
        helpItem.addActionListener(menuBarHandler);
        return menuBar;
    }

    private class MenuBarHandler implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case "Exit":
                    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                    break;
                case "Options":
                    optionsFrame.setVisible(true);
                    break;
                case "Toggle Full Screen":
                    if (frame.getExtendedState() != JFrame.MAXIMIZED_BOTH){
                        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                    }else {
                        frame.setExtendedState(JFrame.NORMAL);
                    }
                    break;
                case "Toggle Sidebar":
                    if (leftPanel.isVisible()) {
                        leftPanel.setVisible(false);
                        mainPanel.setEnabled(false);
                    }else{
                        leftPanel.setVisible(true);
                        mainPanel.setEnabled(true);
                        mainPanel.setDividerLocation(mainPanel.getLeftComponent().getPreferredSize().width);
                    }
                    break;
                case "Hide to tray":
                    if (hideOnSystemTray.isSelected()) {
                        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    } else {
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    }
                    break;
                case "Light theme":
                    if (!mainPanel.getBackground().equals(Color.WHITE)) {
                        mainPanel.setBackground(Color.WHITE);
                        leftPanel.setBackground(Color.WHITE);
                    }
                    break;
                case "Dark theme":
                    if (mainPanel.getBackground().equals(Color.WHITE)) {
                        centerPanel.setBackground(new Color(0xE6000000, true));
                        leftPanel.setBackground(new Color(0xFF001E));
                    }
                    break;
                case "About":
                    showAbout().setVisible(true);
                    break;
                case "Help":
                    new JFrame().setVisible(true);
                    break;
            }
        }
    }

    private JFrame showAbout(){
        JFrame aboutFrame = new JFrame("About");
        aboutFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        aboutFrame.setResizable(false);
        aboutFrame.setLocationRelativeTo(frame);

        JLabel applicationName = new JLabel("Roham-H HTTP Client");
        JLabel developerName = new JLabel("Developer:  Roham Hayedi");
        JLabel developerAUTID = new JLabel("ID:  9831107");
        JLabel developerEmail = new JLabel("Email:  rohamhayedi@gmail.com");
        JPanel aboutPanel = new JPanel(new GridLayout(0, 2));
        JPanel aboutMainPanel = new JPanel(new BorderLayout());
        aboutFrame.add(aboutMainPanel);
        aboutMainPanel.add(applicationName, BorderLayout.NORTH);
        aboutMainPanel.add(aboutPanel, BorderLayout.CENTER);
        aboutPanel.add(developerName);
        aboutPanel.add(developerAUTID);
        aboutPanel.add(developerEmail);
        aboutFrame.pack();
        return aboutFrame;
    }

    private JFrame makeOptionsFrame(){

        MenuBarHandler menuBarHandler = new MenuBarHandler();
        JFrame optionsFrame = new JFrame("Options");
        optionsFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        optionsFrame.setResizable(false);
        optionsFrame.setLocationRelativeTo(frame);
        followRedirect = new JCheckBox("follow redirect");
        followRedirect.addActionListener(menuBarHandler);
        hideOnSystemTray = new JCheckBox("Hide to tray");
        hideOnSystemTray.addActionListener(menuBarHandler);
        ButtonGroup themes = new ButtonGroup();
        JRadioButton light = new JRadioButton("Light theme");
        light.setSelected(true);
        JRadioButton dark = new JRadioButton("Dark theme");
        light.addActionListener(menuBarHandler);
        dark.addActionListener(menuBarHandler);
        themes.add(light);
        themes.add(dark);
        GroupLayout layout = new GroupLayout(optionsFrame.getContentPane());
        optionsFrame.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup()
        .addComponent(followRedirect)
        .addComponent(light)
        .addComponent(dark))
        .addComponent(hideOnSystemTray));
        layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup()
        .addComponent(followRedirect)
        .addComponent(hideOnSystemTray))
        .addComponent(light)
        .addComponent(dark));
        optionsFrame.pack();
        return optionsFrame;
    }
}
