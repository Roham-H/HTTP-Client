import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.io.Serializable;
import java.util.HashMap;


/**
 * The base of GUI, consists of a List of requests,
 * and a split pane which also consists of the request input panel and its response window
 */
public class ClientGUI implements Serializable {

//    The main frame
    private static JFrame frame;
//    The frame used to change options, such as theme
    private final JFrame optionsFrame;
    private static JSplitPane mainPanel = new JSplitPane();
    private final RequestListPanel requestListPanel;
    private static JCheckBox followRedirect;
    private JCheckBox hideOnSystemTray;

    public ClientGUI() {

        frame = new JFrame("HTTP Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setBounds(250, 150, 1000, 550);
        requestListPanel = new RequestListPanel();
        mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        mainPanel.add(requestListPanel);
        frame.setContentPane(mainPanel);
        frame.addWindowListener(new SaveBeforeClose());
        frame.setMinimumSize(new Dimension(500, 400));
        JMenuBar menuBar = createMenuBar();
        frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
        mainPanel.setDividerSize(5);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setExtendedState(Frame.ICONIFIED);
            }
        });
        optionsFrame = makeOptionsFrame();
    }

    /**
     * replaces the response window with the selected request's response window
     * @param requestPanel of the response window which is intended to be shown
     */
    public static void switchRequest(RequestPanel requestPanel){
        if (mainPanel.getRightComponent() != null)
            mainPanel.remove(mainPanel.getRightComponent());
        mainPanel.setRightComponent(requestPanel);
        frame.pack();
    }

    /**
     * Makes the frame visible
     */
    public void show(){
        frame.setVisible(true);
    }

    /**
     * @return GUI's main frame
     */
    public static JFrame getFrame() {
        return frame;
    }
//    The menubar which allows the user to change the settings of Application such as toggling its view mode
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
//    Handles the menubar's components
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
                    if (requestListPanel.isVisible()) {
                        requestListPanel.setVisible(false);
                        mainPanel.setEnabled(false);
                    }else{
                        requestListPanel.setVisible(true);
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
                    changeTheme(0);
                    break;
                case "Dark theme":
                    changeTheme(1);
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
//    Saves the current frame with all its settings
    private class SaveBeforeClose implements WindowListener{

        @Override
        public void windowOpened(WindowEvent e) {

        }

        @Override
        public void windowClosing(WindowEvent e) {
            if (e.getSource().equals(frame)){
                main.save();
            }
        }

        @Override
        public void windowClosed(WindowEvent e) {

        }

        @Override
        public void windowIconified(WindowEvent e) {

        }

        @Override
        public void windowDeiconified(WindowEvent e) {

        }

        @Override
        public void windowActivated(WindowEvent e) {

        }

        @Override
        public void windowDeactivated(WindowEvent e) {

        }
    }
//    Changes the main frame and its main panels theme
    private void changeTheme(int type){
        frame.setBackground(Color.DARK_GRAY);
        mainPanel.setBackground(Color.DARK_GRAY);
        ((RequestPanel) mainPanel.getRightComponent()).changeTheme(type);
        ((RequestListPanel) mainPanel.getLeftComponent()).changeTheme(type);
    }
//    Shows the app's and its developer's info
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
        aboutFrame.setContentPane(aboutMainPanel);
        aboutMainPanel.add(applicationName, BorderLayout.NORTH);
        aboutMainPanel.add(aboutPanel, BorderLayout.CENTER);
        aboutPanel.add(developerName);
        aboutPanel.add(developerAUTID);
        aboutPanel.add(developerEmail);
        aboutFrame.pack();
        return aboutFrame;
    }
//    Creates the options frame, which pops up to allow the user to change settings, such as setting the theme or follow redirect checkbox
    private JFrame makeOptionsFrame(){
        MenuBarHandler menuBarHandler = new MenuBarHandler();
        JFrame optionsFrame = new JFrame("Options");
        optionsFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        optionsFrame.setResizable(false);
        optionsFrame.setLocationRelativeTo(frame);
        followRedirect = new JCheckBox("follow redirect");
        followRedirect.setSelected(true);
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
        optionsFrame.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        optionsFrame.add(hideOnSystemTray, c);
        optionsFrame.add(followRedirect, c);
        c.gridy = 1;
        optionsFrame.add(light, c);
        c.gridy = 2;
        optionsFrame.add(dark, c);
        optionsFrame.pack();
        return optionsFrame;
    }

    /**
     * @return the request panel
     */
    public static RequestPanel getRequestPanel() {
        return (RequestPanel) mainPanel.getRightComponent();
    }

    /**
     * @return {@code true} if follow redirect is selected
     */
    public static boolean getFollowRedirect() {
        return followRedirect.isSelected();
    }
}
