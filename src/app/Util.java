package app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Util {

    public static final int FRONT_END_PORT = 4001;
    public static final int SEQUENCER_PORT = 5001;
    public static final int REPLICA_MANAGER_PORT = 6001;
    public static final int REPLICA_PORT = 7000;
    public static final int KKL_PORT = 7001;
    public static final int DVL_PORT = 7002;
    public static final int WST_PORT = 7003;

    public static final String FRONT_END_HOST = "localhost";
    public static final String SEQUENCER_HOST = "localhost";
    public static final String[] REPLICA_MANAGER_HOSTS = new String[]{"localhost"};//, "localhost", "localhost", "localhost"};

    public static int getCampusPort(String campus) {
        int port;
        if (campus.equals("KKL")) port = Util.KKL_PORT;
        else if (campus.equals("DVL")) port = Util.DVL_PORT;
        else port = Util.WST_PORT;

        return port;
    }

    public static void writeLog(String fileName, String message) {
        try {
            File folder = new File("logs");
            folder.mkdir();
            File file = new File("logs/" + fileName);
            file.createNewFile();

            FileWriter fileWriter = new FileWriter(file, true);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.append(message);
            printWriter.append("\n\n");
            printWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
