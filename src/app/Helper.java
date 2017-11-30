package app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;

public class Helper {
    public static int KKLPort = 1234;
    public static int WSTPort = 4123;
    public static int DVLPort = 3290;

    public static int returnPort(String campus_name) {

        if (campus_name.equals("KKL")) {
            return KKLPort;
        } else if (campus_name.equals("WST")) {
            return WSTPort;
        } else {
            return DVLPort;
        }
    }

    public static void writelog(String folder_name, String sub_folder_name, String file_name, String function_name, String parameters, boolean result_type, String response) {
        try {
            File folder;
            if (sub_folder_name != null) {
                folder = new File("logs/"+folder_name + "/" + sub_folder_name);
                folder.mkdirs();
            } else {
                folder = new File("logs/"+folder_name);
                folder.mkdirs();
            }
            File file = new File(folder + "/" + file_name);
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
            writer.append("\n");
            writer.append("\nDate: " + new Date().toString());
            writer.append("\nRequest Type: " + function_name);
            writer.append("\nParameters: " + "\n\t"+parameters);
            writer.append("\nSuccess: " + result_type);
            writer.append("\nServer Response: " + response);
            writer.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
