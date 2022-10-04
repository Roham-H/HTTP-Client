import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple server written to respond to a received request
 */
public class SampleServer {

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            serverSocket.setSoTimeout(10000);
            System.out.println("Waiting for connection...");
            ExecutorService pool = Executors.newCachedThreadPool();
            while (!serverSocket.isClosed()) {
                Socket connection = serverSocket.accept();
                pool.execute(new ClientHandler(connection));
            }
        } catch (IOException ignored) {

        }
        System.out.println("Server socket closed");
    }

    private static class ClientHandler implements Runnable{

        private final Socket connection;

        public ClientHandler(Socket connection){
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                OutputStream outputStream = connection.getOutputStream();
                System.out.println("Connection established");
                Request request;
                while (true) {
                    try {
                        request = (Request) in.readObject();
                        outputStream.write(("Request received...").getBytes());
                        outputStream.write(request.toString().getBytes());
                        jurl.send(request);
                        System.out.println();
                        HttpResponse<byte[]> response = jurl.getResponse();
                        outputStream.write(("Response received...\n" + response.statusCode()).getBytes());
                        outputStream.write(response.body());
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (IOException ioException) {
//                ioException.printStackTrace();
            }
            finally {
                try {
                    connection.close();
                } catch (IOException e) {
//                    e.printStackTrace();
                }
            }
        }
    }
}
