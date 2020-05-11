import javax.swing.*;
import java.awt.*;

public class LeftPanel extends JPanel {

    public LeftPanel(){
        setLayout(new BorderLayout());
        JTree requests = new JTree();
        add(requests, BorderLayout.CENTER);
    }
}
