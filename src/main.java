import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class main {

//    The Path used to save client data
    private static final String relativePath = "/resources/app/client data";
    private static Handler handler;
    public static void main(String[] args) {
        if (!retrieveClient())
            handler = new Handler();
        handler.show();
    }

    /**
     * retrieves the client settings from file
     * @return true if retrieve successful
     */
    private static boolean retrieveClient() {
        File file = new File(new File("").getAbsolutePath() + relativePath);
        if (Files.exists(Paths.get(file.getPath()))) {
            try {
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
                main.handler = (Handler) inputStream.readObject();
//                System.out.println("true");
                return true;
            }catch (IOException | ClassNotFoundException e){
//                e.printStackTrace();
                return false;
            }
        } return false;
    }

    /**
     * used to save the current gui with all the settings
     */
    public static void save() {
        File file = new File(new File("").getAbsolutePath() + "/resources/app");
        file.mkdirs();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(new File(file.getAbsolutePath() + "/client data")));
            outputStream.writeObject(handler);
            outputStream.close();
//            System.out.println("saved");
        }catch (IOException e){
//            e.printStackTrace();
        }
    }
}
