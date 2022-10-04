import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple savable request with methods: GET-DELETE-POST-PUT-PATCH
 */
public class Request implements Serializable {

    private URL url;
    //    a map of request's headers
    private final HashMap<String, String> headers = new HashMap<>();
    //    the method, with GET as default value
    private String method = "GET";
    //    request's body which is made of object, so that it can later be cast to a Map(Form Data) or a String(JSON)
    private Object body;
    private boolean showHeader;
    private boolean followRedirect;
    //    request's default name, used when its output name is not specified
    private String outputName = String.valueOf(System.currentTimeMillis());
    private boolean hasOutput;
    //    used as a tag later in jurl to set headers
    private String bodyType;
    private transient boolean toBeSaved;
    private boolean outputWasSet = false;
    private String saveDirectory;
    private String saveName = "request_" + System.currentTimeMillis();

    /**
     * @param args the command-line arguments used to make a request
     * @throws MalformedURLException to indicate that a malformed URL has occurred
     * @throws ParseException        to indicate that JSON body couldn't be parsed
     */
    public Request(String[] args)
            throws InputMismatchException, MalformedURLException, ParseException {

        for (int i = 0; i < args.length; i++) {

//            if --help is found in the arguments, the jurl's guide will be printed
            if (args[i].equals("--help")) {
                jurl.printHelp();
            }
//            if an argument matches the regex given below, it might be a url and if so, it'll be set as the requests url
            if (args[i].matches("(\\S+\\.\\S+)"))
                if (i == 0) {
                    if (args[i].startsWith("http://") || args[i].startsWith("https://"))
                        url = new URL(args[i]);
                    else
                        url = new URL("http://" + args[i]);
                } else if (!args[i - 1].equals("-M") && !args[i - 1].equals("-H") && !args[i - 1].equals("-d")
                        && !args[i - 1].equals("-O") && !args[i - 1].equals("--json") && !args[i - 1].equals("--upload")) {
                    if (args[i].startsWith("http://") || args[i].startsWith("https://"))
                        url = new URL(args[i]);
                    else url = new URL("http://" + args[i]);
                }

//                if -M is found, the next argument will be checked to see if the method can be found,
//                if not, an exception will be thrown
            if (args[i].equals("-M"))
                if (i != args.length - 1)
                    switch (args[i + 1]) {
                        case "GET":
                            method = "GET";
                            break;
                        case "DELETE":
                            method = "DELETE";
                            break;
                        case "POST":
                            method = "POST";
                            break;
                        case "PUT":
                            method = "PUT";
                            break;
                        case "PATCH":
                            method = "PATCH";
                            break;
                        default:
                            throw new InputMismatchException("false method");
                    }
                else throw new InputMismatchException("false method");

//                for -H the next argument must have the regex given below, if not an exception will be thrown
            if (args[i].equals("-H")) {
                if (i != args.length - 1)
                    if (!args[i + 1].matches("\\s*(\\S+:\\S+;?)+"))
                        throw new InputMismatchException("incorrect format for header");
                    else
                        makeHeadersMap(args[i + 1]);
                else
                    throw new InputMismatchException("false header");
            }

//                checks to make sure the body is either JSON or Form Data, if both are available, an exception will be thrown
            if (args[i].equals("-d") || args[i].equals("--json"))
                if (body != null && !bodyType.equals("octet-stream"))
                    throw new InputMismatchException("message body should either be as Form Data format or JSON");

//                    after -d the argument must be as Form Data format, if not an exception will be thrown
            if (args[i].equals("-d") && body == null) {
                if (i != args.length - 1) {
                    if (!args[i + 1].matches("(\\s*(\\S+=\\S+)+\\s*)"))
                        throw new InputMismatchException("incorrect format for form data");
                    body = args[i + 1];
                    stringToFormDataMap((String) body);
                    bodyType = "data";
                } else throw new InputMismatchException("false Form Data body");
            }

//                after --json the argument must be as JSON format (excluding double quotations ""), if not a parsing exception will be thrown
            if (args[i].equals("--json") && body == null) {
                if (i != args.length - 1) {
                    JSONParser jsonParser = new JSONParser();
                    String bodyJ = args[i + 1].replaceAll("\\{", "{\"").
                            replaceAll("}", "\"}").replaceAll(":(?!\\{)", "\":\"")
                            .replaceAll(",", "\",\"");

                    body = jsonParser.parse(bodyJ);
                    body = bodyJ;
                    bodyType = "json";
                } else throw new InputMismatchException("false --json body");
            }

//                next argument must be a file with either an absolute path to it, or a relative one
            if (args[i].equals("--upload")) {
                if (i != args.length - 1) {
                    if (!args[i + 1].matches("-\\w")) {
                        stringToFormDataMap("data=" + args[i + 1]);
                        bodyType = "octet-stream";
                    }
                } else throw new InputMismatchException("false --upload argument");
            }

//                -O as output can be followed by a name, if not the current time as milliseconds will be appended to it to be used as output name
            if (args[i].equals("-O")) {
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    outputName = args[i + 1];
                    outputWasSet = true;
                } else {
                    outputName = "output_[" + System.currentTimeMillis() + "]";
                }
                hasOutput = true;
            }

//            can be followed with folders existing in the directory, default: ./resources/requests
            if (args[i].equals("--save")) {
                toBeSaved = true;
                StringBuilder dir = new StringBuilder();
                if (i + 1 < args.length) {
                    for (; i < args.length - 1 && !args[i + 1].startsWith("-"); i++) {
                        dir.append("/").append(args[i + 1]);
                    }
                }
                saveDirectory = dir.toString();
            }

            if (args[i].equals("--name")){
                if (i < args.length - 1){
                    setSaveName(args[i + 1]);
                }
            }

            if (args[i].equals("-i"))
                showHeader = true;

            if (args[i].equals("-f"))
                followRedirect = true;
        }
    }

    //    converts the given key-values in body to a Map
    private void stringToFormDataMap(String body) {

        Pattern pattern = Pattern.compile("(\\S+)=(\\S+)");
        this.body = new HashMap<String, String>();
        String[] strings = body.split("&");
        for (String kv : strings) {
            Matcher matcher = pattern.matcher(kv);
            if (matcher.find()) {
                ((HashMap) this.body).put(matcher.group(1), matcher.group(2));
            }
        }
    }

    //    converts the given headers to a Map
    private void makeHeadersMap(String headerString) {
        Pattern pattern = Pattern.compile("(\\S+):(\\S+)");
        String[] strings = headerString.split(";");
        for (String kv : strings) {
            Matcher matcher = pattern.matcher(kv);
            if (matcher.find())
                headers.put(matcher.group(1), matcher.group(2));
        }
    }

    /**
     * @return request's headers
     */
    public HashMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * @return request's body
     */
    public Object getBody() {
        return body;
    }

    /**
     * @return request's method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return request's output name
     */
    public String getOutputName() {
        return outputName;
    }

    /**
     * @return request's url
     */
    public URL getUrl() {
        return url;
    }

    /**
     * @return {@code true} if request has output
     * {@code false} otherwise
     */
    public boolean hasOutput() {
        return hasOutput;
    }

    /**
     * @return {@code true} if response must show headers
     * {@code false} otherwise
     */
    public boolean getShowHeader() {
        return showHeader;
    }

    /**
     * @return {@code true} if request should be redirected
     * {@code false} otherwise
     */
    public boolean getFollowRedirect() {
        return followRedirect;
    }

    /**
     * @return bodyType,
     * data for Form Data,
     * --json for JSON,
     * octet-stream if --upload has been called
     */
    public String getBodyType() {
        return bodyType;
    }

    /**
     * @return {@code true} if request is to be saved
     * {@code false} otherwise
     */
    public boolean isToBeSaved() {
        return toBeSaved;
    }

    /**
     * @return the request's save directory
     */
    public String getSaveDirectory() {
        return saveDirectory;
    }

    /**
     * @param saveName the name the request will be saved by
     */
    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    /**
     * @return the name that the request is saved by
     */
    public String getSaveName() {
        return saveName;
    }

    public boolean outputWasSet(){
        return outputWasSet;
    }

    public void setOutputName(String outputName) {
        this.outputName = "output_[" + outputName + "]";
    }

    @Override
    public String toString() {
        return "url: " + url +
                (headers.size() != 0 ?
                        " | method: '" + method + '\'' +
                                " | headers: " + headers : "") +
                (bodyType != null ?
                        " | body: " + body : "") +
                " | followRedirect: " + followRedirect +
                (hasOutput ? " | outputName: '" + outputName + '\''
                        : "");
    }
}
