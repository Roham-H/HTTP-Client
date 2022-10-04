import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

/**
 * The panel to demonstrate a view for the requests
 * by using a JTree, and allows the user to add new Requests under root, or folders
 */
public class RequestListPanel extends JPanel {
//    The root name of the tree;
    private final DefaultMutableTreeNode root = new DefaultMutableTreeNode(
            new File(new File("").getAbsolutePath() + "/resources/requests").getName());
    private final DefaultTreeModel treeModel;
    private final JTree requests;
//    Connects each tree node, to a Request panel, which is used to change the shown panel on screen
    private final HashMap<DefaultMutableTreeNode, RequestPanel> requestPanelMap;
    private final JMenuBar addBar = new JMenuBar();

    public RequestListPanel(){
        setLayout(new BorderLayout());
        AddNode addNode = new AddNode();
        TreeListener treeListener = new TreeListener();
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JMenu createMenu = new JMenu("Add +");
        JMenuItem createRequest = new JMenuItem("New request");
        JMenuItem createFolder = new JMenuItem("New folder");
        requestPanelMap = new HashMap<>();

        treeModel = new DefaultTreeModel(root);
        requests = new JTree(treeModel);
        populateTree(new File(new File("").getAbsolutePath() + "/resources/requests"));
        requests.setEditable(true);
        requests.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);
        requests.setShowsRootHandles(true);
        requests.setRootVisible(false);
        requests.addTreeSelectionListener(treeListener);
        createRequest.addActionListener(addNode);
        createRequest.setActionCommand("NEW_REQUEST");
        createFolder.addActionListener(addNode);
        createFolder.setActionCommand("NEW_FOLDER");
        createMenu.add(createRequest);
        createMenu.add(createFolder);
        addBar.add(createMenu);
        addPanel.add(addBar);
        add(addPanel, BorderLayout.NORTH);
        add(new JScrollPane(requests), BorderLayout.CENTER);
    }
//    Used to add a child node to the given parent
    private void addNode(DefaultMutableTreeNode parent ,DefaultMutableTreeNode child){

        treeModel.insertNodeInto(child, parent, parent.getChildCount() == 0 || parent.equals(root) ? parent.getChildCount() : parent.getChildCount() - 1);
        if (child.getAllowsChildren()) {
            DefaultMutableTreeNode addRequestToFolder = new DefaultMutableTreeNode("Add request");
            treeModel.insertNodeInto(addRequestToFolder, child, child.getChildCount());
        }
        requests.scrollPathToVisible(new TreePath(child.getPath()));
    }
//    was intended to populate the tree with saved requests
    private void populateTree(File rootDir){
        File[] dirs = rootDir.listFiles();
        try {
            for (File file : dirs) {
                if (!file.isDirectory() && file.getName().startsWith("request")) {
                    DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode(file.getParentFile().getName());
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(file.getName());
                    childNode.setAllowsChildren(false);
                    addNode(parentNode, childNode);
                } else
                    populateTree(file);
            }
        }catch (Exception e){
            
        }
    }
//    The listener used to add new nodes to the tree
    private class AddNode implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("NEW_REQUEST")){
                String nodeName;
                do {
                    nodeName = JOptionPane.showInputDialog("Enter request name");
                    if (nodeName == null)
                        break;
                } while (nodeName.isEmpty());

                if (nodeName != null) {
                    DefaultMutableTreeNode newRequest = new DefaultMutableTreeNode(nodeName);
                    newRequest.setAllowsChildren(false);
                    addNode(root, newRequest);

                    RequestPanel requestPanel = new RequestPanel();
                    requestPanelMap.put(newRequest, requestPanel);
                    Connector connector = new Connector();
//                    connector.args.add("--name");
//                    connector.args.add(nodeName);
                    Handler.requestPanels.put(requestPanel, connector);

                    ClientGUI.switchRequest(requestPanelMap.get(newRequest));
                    ClientGUI.getFrame().setTitle("HTTP Client" + " - " + newRequest.getUserObject());
                }
            }else if (e.getActionCommand().equals("NEW_FOLDER")){
                DefaultMutableTreeNode parentNode;
                TreePath selectedPath = requests.getSelectionPath();

                if (selectedPath == null)
                    parentNode = root;

                else {
                    parentNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                }

                String folderName = JOptionPane.showInputDialog("Enter folder name");
                DefaultMutableTreeNode newFolder = new DefaultMutableTreeNode(folderName);
                if (!parentNode.getAllowsChildren())
                    parentNode = root;
                addNode(parentNode, newFolder);
            }
            treeModel.reload();
            revalidate();
            repaint();
        }
    }
//    Listener which allows the app to add a request under a folder
    private class TreeListener implements TreeSelectionListener {

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            if (e.getNewLeadSelectionPath() != null) {
                DefaultMutableTreeNode selected = (DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
                if (selected.toString().equals("Add request")) {
                    String nodeName;
                    do {
                        nodeName = JOptionPane.showInputDialog("Enter request name");
                        if (nodeName == null)
                            break;
                    } while (nodeName.isEmpty());

                    if (nodeName != null) {

                        DefaultMutableTreeNode newRequest = new DefaultMutableTreeNode(nodeName);
                        newRequest.setAllowsChildren(false);
                        addNode((DefaultMutableTreeNode) selected.getParent(), newRequest);
                        RequestPanel requestPanel = new RequestPanel();
                        requestPanelMap.put(newRequest, requestPanel);

                        Connector connector = new Connector();
                        Handler.requestPanels.put(requestPanel, connector);
//                        connector.args.add("--name");
//                        connector.args.add(nodeName);

                        ClientGUI.switchRequest(requestPanelMap.get(newRequest));
                        ClientGUI.getFrame().setTitle("HTTP Client" + " - " + newRequest.getUserObject());
                        treeModel.reload();
                        revalidate();
                        repaint();
                    }
                }else if (!selected.getAllowsChildren()){
                    ClientGUI.switchRequest(requestPanelMap.get(selected));
                    ClientGUI.getFrame().setTitle("HTTP Client" + " - " + selected.getUserObject());
                }
            }
        }
    }

    /**
     * Changes the theme of GUI
     * @param type 1 for dark theme and 0 for light
     */
    public void changeTheme(int type) {
        BorderLayout layoutManager = (BorderLayout) this.getLayout();
        if (type == 1) {
            requests.setBackground(Color.GRAY);
            addBar.setBackground(Color.DARK_GRAY);
            ((JScrollPane) layoutManager.getLayoutComponent(BorderLayout.CENTER)).getViewport().getView().setBackground(Color.DARK_GRAY);
            (layoutManager.getLayoutComponent(BorderLayout.NORTH)).setBackground(Color.DARK_GRAY);
            addBar.getComponent().setBackground(Color.DARK_GRAY);
            JMenu menu = addBar.getMenu(0);
            menu.setBackground(Color.DARK_GRAY);
            menu.setForeground(Color.WHITE);
            menu.getMenuComponent(0).setBackground(Color.DARK_GRAY);
            menu.getMenuComponent(0).setForeground(Color.WHITE);
            menu.getMenuComponent(1).setBackground(Color.DARK_GRAY);
            menu.getMenuComponent(1).setForeground(Color.WHITE);
        }
        else {
            requests.setBackground(Color.WHITE);
            addBar.setBackground(Color.WHITE);
            ((JScrollPane) layoutManager.getLayoutComponent(BorderLayout.CENTER)).getViewport().getView().setBackground(Color.WHITE);
            (layoutManager.getLayoutComponent(BorderLayout.NORTH)).setBackground(Color.WHITE);
            addBar.getComponent().setBackground(Color.WHITE);
            JMenu menu = addBar.getMenu(0);
            menu.setBackground(Color.WHITE);
            menu.setForeground(Color.BLACK);
            menu.getMenuComponent(0).setBackground(Color.WHITE);
            menu.getMenuComponent(0).setForeground(Color.BLACK);
            menu.getMenuComponent(1).setBackground(Color.WHITE);
            menu.getMenuComponent(1).setForeground(Color.BLACK);
        }
    }
}
