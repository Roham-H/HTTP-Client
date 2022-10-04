import org.json.simple.parser.ParseException;
import javax.swing.*;
import java.io.IOException;
import java.io.Serializable;
import java.net.http.HttpResponse;
import java.util.ArrayList;

/**
 * the class used to connect a Request Panel to jURL
 * extracts the inputs and inserts them into an array of arguments
 */

public class Connector implements Serializable {

    public Connector(){
    }
//    Reads the input bars of identical panels
    private String inputPanelToString(JPanel inputPanel, String inputType){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < inputPanel.getComponentCount(); i++){
            JPanel row = (JPanel) inputPanel.getComponent(i);
            if (((JCheckBox) row.getComponent(2)).isSelected() && row.getComponent(2).isEnabled()){
                stringBuilder
                        .append(((JTextField)row.getComponent(0)).getText())
                        .append(inputType.equals("header") ? ":" : "=")
                        .append(((JTextField)row.getComponent(1)).getText())
                        .append(inputType.equals("header") ? ";" :  "&");
            }
        }
        return stringBuilder.toString();
    }
//    Fills the list with arguments extracted from the panel
    private void fillArgs(ArrayList<String> args){
        boolean hasBody = false;
        RequestPanel requestPanel = ClientGUI.getRequestPanel();
        args.add(requestPanel.getURL().stripIndent());
        args.add("-i");
        if (ClientGUI.getFollowRedirect())
            args.add("-f");
        args.add("-M");
        args.add(requestPanel.getMethod());
        if (args.get(args.size() - 1).equals("POST") || args.get(args.size() - 1).equals("PATCH") || args.get(args.size() - 1).equals("PUT"))
            hasBody = true;
        String headerString = inputPanelToString(requestPanel.getHeaderPanel(), "header");
        if ( !headerString.isEmpty() ) {
            args.add("-H");
            args.add(headerString);
        }
        if (hasBody){
            switch (requestPanel.getBodyType()) {
                case "Form Data":
                    String formData = inputPanelToString(requestPanel.getFormDataPanel(), "Form Data");
                    args.add("-d");
                    args.add(formData);
                    break;
                case "JSON":
                    args.add("--json");
                    args.add(requestPanel.getBodyMessage().replaceAll("\"" , ""));
                    break;
                case "Binary File":
                    args.add("--upload");
                    args.add(requestPanel.getFileChooser().getSelectedFile().getAbsolutePath());
                    break;
            }
        }
    }

    /**
     * Creates a new Requests with inputs extracted from the panels and sends it
     * @throws IOException
     * @throws ParseException
     * @throws InterruptedException
     */
    public void sendRequest() throws IOException, ParseException, InterruptedException {
        ArrayList<String> argsList = new ArrayList<>();
        fillArgs(argsList);
        String[] args = new String[argsList.size()];
        argsList.toArray(args);
        System.out.println(argsList);
        jurl.send(new Request(args));
    }

    /**
     * @return the response received as bytes array
     */
    public HttpResponse<byte[]> getResponse(){
        return jurl.getResponse();
    }
}
