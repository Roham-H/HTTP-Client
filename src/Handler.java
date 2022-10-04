import java.io.*;
import java.util.HashMap;

/**
 * The class responsible for connecting the GUI and its request panels to their connector
 * each panel has its own connector so send the given arguments to jURL
 */
public class Handler implements Serializable {

    public static HashMap<RequestPanel, Connector> requestPanels  = new HashMap<>();
    private final ClientGUI clientGUI;

    public Handler(){
        clientGUI = new ClientGUI();
    }

    /**
     * show GUI
     */
    public void show(){
        clientGUI.show();
    }
}
