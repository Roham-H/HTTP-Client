import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class jurl {

    private static final String saveDir = "/resources/requests";
    private static final String outputDir = "/resources/responses";
    private static HttpResponse<byte[]> response;
    private static long responseDelay;

    private static void formDataMaker(HashMap<String, String> body, StringBuilder stringBuilder, String boundary, ArrayList<byte[]> dataBytes) {

        for (Map.Entry<String, String> entry : body.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            if (!k.startsWith("data") && !k.startsWith("file")) {
                stringBuilder.append("--").append(boundary).append("\r\n")
                        .append("Content-Disposition: form-data; name=\"")
                        .append(k).append("\"\r\n\r\n").append(v).append("\r\n");
            } else {
                try {
                    Path path = Paths.get(v);
                    String mimeType = Files.probeContentType(path);
                    stringBuilder.append("--").append(boundary).append("\r\n").append("Content-Disposition: form-data; filename=\"")
                            .append(path.getFileName()).append("\"\r\nContent-Type: ")
                            .append(mimeType).append("\r\n\r\n").append(new String(Files.readAllBytes(path)))
                            .append(new String("\r\n".getBytes()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        stringBuilder.append("--").append(boundary).append("--\r\n");
        dataBytes.add(0, stringBuilder.toString().getBytes());
    }

    private static void saveRequest(Request request) {

        File saveDir = new File(new File("").getAbsolutePath() + jurl.saveDir + request.getSaveDirectory());

        if (new File(new File("").getAbsolutePath() + jurl.saveDir).mkdirs())
            System.out.println("resources directory created!");
        if (saveDir.exists()) {
            try {
                ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(new File(
                        saveDir.getAbsolutePath() + "/" + request.getSaveName())));
                outputStream.writeObject(request);
                outputStream.close();
            } catch (IOException e) {

            }
        } else
            System.out.println("Directory " + request.getSaveDirectory() + " not found, request was not saved");
    }

    private static HttpRequest httpRequestMaker(Request request, String boundary, ArrayList<byte[]> dataBytes) {
        HttpRequest httpRequest;
        URI uri = URI.create(request.getUrl().toString());
        String method = request.getMethod();
        ArrayList<String> headersList = new ArrayList<>();
        if (request.getHeaders() != null)
            request.getHeaders().forEach((k, v) -> {
                headersList.add(k);
                headersList.add(v);
            });
        String[] headers = new String[headersList.size()];
        headersList.toArray(headers);
        switch (method) {
            case "POST":
                if (headers.length != 0)
                    httpRequest = HttpRequest.newBuilder()
                            .uri(uri)
                            .header("Content-Type", request.getBodyType().equals("json")
                                    ? "application/json" : request.getBodyType().equals("data")
                                    ? "multipart/form-data; boundary=" + boundary
                                    : "application/octet-stream")
                            .headers(headers)
                            .POST(HttpRequest.BodyPublishers.ofByteArrays(dataBytes))
                            .build();

                else
                    httpRequest = HttpRequest.newBuilder()
                            .uri(uri)
                            .setHeader("Content-Type", request.getBodyType().equals("json")
                                    ? "application/json" : request.getBodyType().equals("data")
                                    ? "multipart/form-data; boundary=" + boundary
                                    : "application/octet-stream")
                            .POST(HttpRequest.BodyPublishers.ofByteArrays(dataBytes))
                            .build();
                break;
            case "DELETE":
                httpRequest = HttpRequest.newBuilder()
                        .uri(uri)
                        .DELETE()
                        .build();
                break;
            case "PUT":
                if (headers.length != 0)
                    httpRequest = HttpRequest.newBuilder()
                            .uri(uri)
                            .header("Content-Type", request.getBodyType().equals("json")
                                    ? "application/json" : request.getBodyType().equals("data")
                                    ? "multipart/form-data; boundary=" + boundary
                                    : "application/octet-stream")
                            .headers(headers)
                            .PUT(HttpRequest.BodyPublishers.ofByteArrays(dataBytes))
                            .build();

                else
                    httpRequest = HttpRequest.newBuilder()
                            .uri(uri)
                            .setHeader("Content-Type", request.getBodyType().equals("json")
                                    ? "application/json" : request.getBodyType().equals("data")
                                    ? "multipart/form-data; boundary=" + boundary
                                    : "application/octet-stream")
                            .PUT(HttpRequest.BodyPublishers.ofByteArrays(dataBytes))
                            .build();
                break;
            case "PATCH":
                if (headers.length != 0)
                    httpRequest = HttpRequest.newBuilder()
                            .uri(uri)
                            .header("Content-Type", request.getBodyType().equals("json")
                                    ? "application/json" : request.getBodyType().equals("data")
                                    ? "application/form-data; boundary=" + boundary
                                    : "application/octet-stream")
                            .headers(headers)
                            .method("PATCH", HttpRequest.BodyPublishers.ofByteArrays(dataBytes))
                            .build();
                else
                    httpRequest = HttpRequest.newBuilder()
                            .uri(uri)
                            .setHeader("Content-Type", request.getBodyType().equals("json")
                                    ? "application/json" : request.getBodyType().equals("data")
                                    ? "application/form-data; boundary=" + boundary
                                    : "application/octet-stream")
                            .method("PATCH", HttpRequest.BodyPublishers.ofByteArrays(dataBytes))
                            .build();
                break;
            default:
                httpRequest = HttpRequest.newBuilder()
                        .uri(uri)
                        .build();
        }
        return httpRequest;
    }

    private static void writeOutput(HttpResponse<byte[]> response, Request request) {

        String content = (response.headers().firstValue("content-type").toString());
        File outputDir = new File(new File("").getAbsolutePath() + jurl.outputDir + "/" + "Output_" + request.getSaveName());
        outputDir.mkdirs();

        try {
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(new File(
                    outputDir.getAbsolutePath() + "/" + request.getOutputName()
                            + (!request.getOutputName().matches(".*\\.\\w+")
                            ? (content.contains("text") || content.contains("json")
                            ? ".txt" : content.contains("image") ? ".png" : "") : ""))));

            output.write(response.body());
            output.close();
        } catch (IOException e) {

        }
    }

    /**
     * Sends the given request
     * @param request
     * @throws IOException
     * @throws InterruptedException
     */
    public static void send(Request request) throws IOException, InterruptedException {

        if (request.isToBeSaved()) {
            saveRequest(request);
        }

        HttpClient client;
        String requestBody = null;

        if (request.getBody() != null)
            requestBody = request.getBody().toString();

        StringBuilder stringBuilder = new StringBuilder();
        String boundary = "---------------" + System.currentTimeMillis();

        ArrayList<byte[]> dataBytes = new ArrayList<>();

        if (requestBody != null)
            dataBytes.add(requestBody.getBytes());
        else
            dataBytes.add(new byte[]{});

        if (request.getBody() instanceof HashMap)
            formDataMaker((HashMap<String, String>) request.getBody(), stringBuilder, boundary, dataBytes);

        HttpRequest httpRequest = httpRequestMaker(request, boundary, dataBytes);

        if (request.getFollowRedirect())
            client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
        else
            client = HttpClient.newBuilder().build();

        long startTime = System.currentTimeMillis();
        response = client.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        responseDelay = System.currentTimeMillis() - startTime;

        if (request.hasOutput())
            writeOutput(response, request);

        System.out.println("[Status Code = " + response.statusCode() + "]");

        if (request.getShowHeader()) {
            Map<String, List<String>> headers = response.headers().map();
            headers.forEach((k, v) -> System.out.println("[" + k + ": " + v.toString().replaceAll("\\[", "")));
        }

//        if (!response.headers().firstValue("content-type").toString().contains("image"))
        System.out.println(new String(response.body()));
    }

    public static void printHelp() {
        System.out.println("arguments intro are to be printed here");
    }

    /**
     * @return response as byte array
     */
    public static HttpResponse<byte[]> getResponse() {
        return response;
    }

    public static long getResponseDelay() {
        return responseDelay;
    }
//    Reads the requests from the given directory
    private static ArrayList<Object> readRequests(String... folders) throws IOException, ClassNotFoundException, InterruptedException {
        ArrayList<Integer> integers = new ArrayList<>();
        StringBuilder nextDirs = new StringBuilder();
        if (folders != null) {
            for (String folder : folders) {
                if (!Files.isDirectory(Paths.get(folder)))
                    if (!folder.matches("\\d+"))
                        nextDirs.append("/").append(folder);
                    else
                        integers.add(Integer.parseInt(folder));
            }
        }
        File fileDir = new File("." + saveDir + nextDirs);
        File[] files = fileDir.listFiles();
        ArrayList<Object> chosenPaths = new ArrayList<>();
        ArrayList<Object> allPaths = new ArrayList<>();
        int prefix = 1;
        if (Files.exists(Paths.get("." + saveDir))) {
            if (files != null)
                for (File file : files)
                    if (!file.isDirectory()) {
                        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
                        while (true)
                            try {
                                Request request = (Request) inputStream.readObject();
                                allPaths.add(request);
                                if (integers.contains(allPaths.size())) {
                                    chosenPaths.add(request);
                                }
                            } catch (Exception e) {
                                break;
                            }
                        inputStream.close();
                    } else {
                        allPaths.add(file);
                        if (integers.contains(allPaths.size()))
                            chosenPaths.add(file);
                    }
            if (integers.size() == 0)
                for (int i = 0; i < allPaths.size(); i++, prefix++) {
                    Object object = allPaths.get(i);
                    if (object instanceof File && ((File) object).isDirectory()) {
                        System.out.println("Folder: " + prefix + ". " + ((File) object).getName());
                    } else {
                        if (object instanceof Request) {
                            System.out.println("Request: " + prefix + ". " + (object));
                        } else prefix--;
                    }
                }
        } else
            System.out.println("directory non-existent");
        if (integers.size() == 0)
            return allPaths;
        return chosenPaths;
    }
//    Creates new Directories
    private static void makeNewDir(String... folders) {
        StringBuilder nextDirs = new StringBuilder();
        for (String folder : folders) {
            nextDirs.append("/").append(folder);
        }
        File file = new File(new File("").getAbsolutePath() + saveDir + nextDirs);
        if (file.mkdirs())
            System.out.println("Directory successfully created");
        else System.out.println("Couldn't create directory");
    }
//    returns a request list after a deep search in the given directory
    private static ArrayList<Request> extractAllReqs(ArrayList<Object> objects) {
        ArrayList<String> dirs = new ArrayList<>();
        ArrayList<Request> requests = new ArrayList<>();
        for (Object object : objects) {
            if (object instanceof Request)
                requests.add((Request) object);
            else if (object instanceof File && ((File) object).isDirectory()) {
                if (!((new File(((File) object).getParent()).getName().equals("requests"))))
                    dirs.add(new File(((File) object).getParent()).getName());
                dirs.add(((File) object).getName());
                File[] files = ((File) object).listFiles();
                if (files == null) {
                    System.out.println(dirs + " is Empty");
                    continue;
                }
                try {
                    String[] strings;
                    for (int i = 0; i < files.length; i++) {
                        strings = new String[dirs.size()];
                        ArrayList<String> stringArrayList = new ArrayList<>(dirs);
                        stringArrayList.add(String.valueOf(i + 1));
                        strings = stringArrayList.toArray(strings);
                        ArrayList<Object> objectArrayList = readRequests(strings);
                        requests.addAll(extractAllReqs(objectArrayList));
                    }

                } catch (ClassNotFoundException | InterruptedException | IOException e) {

                }
            }
        }
        return requests;
    }
//    fires the given requests, or directories
    private static void fire(ArrayList<Object> objects) {
        ArrayList<Request> requests = extractAllReqs(objects);
        requests.forEach(k -> {
            try {
                send(k);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Sends the request to the given server
     * @param ip of the server
     * @param port of the server
     * @param requests to be sent
     */
    public static void toServer(String ip, int port, Request... requests) {
        try (Socket client = new Socket(ip, port)) {
            System.out.println("Connection established");
            ObjectOutputStream outputStream = new ObjectOutputStream(client.getOutputStream());
            InputStream inputStream = client.getInputStream();
            byte[] buffer = new byte[4096];
            for (Request req : requests)
                outputStream.writeObject(req);
            int read;
            while ((read = inputStream.read(buffer)) != -1)
                System.out.println(new String(buffer, 0, read) + "\n");
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Connection closed");
    }

    public static void main(String[] args) {
        try {
            List<String> argsList = Arrays.asList(args);
            ArrayList<String> argsArrayList = new ArrayList<>(argsList);
            if (args.length > 0 && !args[0].equals("create") && !args[0].equals("list") && !args[0].equals("fire") && !args[0].equals("--send") && !argsArrayList.contains("--proxy")) {
                Request request = new Request(args);
                send(request);
            } else {
                if (argsArrayList.contains("--proxy")) {
                    int proxyIndex = argsArrayList.indexOf("--proxy");
                    ArrayList<String> toBeRemoved = new ArrayList<>();
                    toBeRemoved.add(argsArrayList.get(proxyIndex));
                    String ip = null;
                    int port = 0;
                    for (int i = proxyIndex; i < argsArrayList.size(); i++) {
                        String arg = argsArrayList.get(i);
                        if (arg.equals("--ip") && i != argsArrayList.size() - 1) {
                            ip = argsArrayList.get(i + 1);
                            toBeRemoved.add(ip);
                            toBeRemoved.add(arg);
                        } else if (arg.equals("--port") && i != argsArrayList.size() - 1) {
                            String portString = argsArrayList.get(i + 1);
                            port = Integer.parseInt(portString);
                            toBeRemoved.add(portString);
                            toBeRemoved.add(arg);
                        }
                    }
                    argsArrayList.removeAll(toBeRemoved);
                    if (!argsArrayList.contains("--send")) {
                        String[] newArgs = new String[argsArrayList.size()];
                        newArgs = argsArrayList.toArray(newArgs);
                        toServer(ip, port, new Request(newArgs));
                    } else {
                        argsArrayList.remove("--send");
                        ArrayList<String> foldersList = new ArrayList<>(argsArrayList);
                        String[] folders = new String[foldersList.size()];
                        folders = foldersList.toArray(folders);
                        ArrayList<Request> requestsList = extractAllReqs(readRequests(folders));
                        Request[] requests = new Request[requestsList.size()];
                        requests = requestsList.toArray(requests);
                        toServer(ip, port, requests);
                    }
                } else {
                    switch (args[0]) {
                        case "list":
                            readRequests(args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : null);
                            break;
                        case "create":
                            makeNewDir(Arrays.copyOfRange(args, 1, args.length));
                            break;
                        case "fire":
                            fire(readRequests(Arrays.copyOfRange(args, 1, args.length)));
                            break;
                    }
                }
            }
        } catch (IOException | ParseException | InterruptedException | ClassNotFoundException e) {
            if (e instanceof FileNotFoundException) {
                System.out.println("Named file is non-existent");
            }
//            else
            e.printStackTrace();
        }
    }
}
