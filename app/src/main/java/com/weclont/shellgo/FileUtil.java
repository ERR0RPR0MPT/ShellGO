package com.weclont.shellgo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by Weclont on 2023/1/2.
 */

public class FileUtil {

    static String config_path = MainApplication.getServiceContext().getExternalFilesDir("config").getAbsolutePath();

    public static void inputLineLog(String str) {
        inputLineLog(str, "log.txt");
    }

    public static void inputLineLog(String str, String logName) {
        try {
            String a = getFile(logName)+"\n"+str;
            saveFile(a, logName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveFile(String str, String fileName) {
        try {
            File file = new File(config_path, fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(str.getBytes());
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deletefile(String fileName) {
        try {
            File file = new File(config_path, fileName);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFile(String fileName) {
        try {
            File file = new File(config_path, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileInputStream fis = new FileInputStream(file);
            byte[] b = new byte[1024];
            int len = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((len = fis.read(b)) != -1) {
                baos.write(b, 0, len);
            }
            baos.close();
            fis.close();
            return baos.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
