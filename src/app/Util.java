package app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Util {

    public static final int FRONT_END_PORT = 4001;
    public static final int SEQUENCER_PORT = 5001;
    public static final int[] REPLICA_MANAGER_PORT = new int[]{6001, 6002, 6003, 6004};
    public static final int[] REPLICA_PORT = new int[]{7000, 8000, 9000, 10000};
    public static final int[] KKL_PORT = new int[]{7001, 8001, 9001, 10001};
    public static final int[] DVL_PORT = new int[]{7002, 8002, 9002, 10002};
    public static final int[] WST_PORT = new int[]{7003, 8003, 9003, 10003};

    public static final String FRONT_END_HOST = "192.168.2.19";
    public static final String SEQUENCER_HOST = "192.168.2.19";
    public static final String[] REPLICA_HOSTS = new String[]{"192.168.2.19", "192.168.2.19", "192.168.2.19", "192.168.2.19"};

    public static int getCampusPort(String campus, int i) {
        int port;
        if (campus.equals("KKL")) port = Util.KKL_PORT[i];
        else if (campus.equals("DVL")) port = Util.DVL_PORT[i];
        else port = Util.WST_PORT[i];

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