/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Administrator
 */
public class FileUtil {

    public static List readList(String filename) throws FileNotFoundException, IOException {
        String lines = "";
        List list = new ArrayList();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
            while ((lines = bufferedReader.readLine()) != null) {
                list.add(lines);
            }
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                    bufferedReader = null;
                } catch (IOException ioe) {
                    // nothing
                }
            }
        }
        return list;
    }

    public static boolean writeContent(String filename, String content, boolean append) throws FileNotFoundException, IOException {
        calculateFileSize(filename, 5000);
        FileOutputStream fileOutputStream = null;
        boolean done = false;
        try {
            fileOutputStream = new FileOutputStream(new File(filename), append);
            fileOutputStream.write(content.getBytes());
            done = true;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException ioe) {
                }
            }

        }
        return done;
    }

    public static void writeLog(String filename, String content) {
        try {
            calculateFileSize(filename, 5000);
            writeContent(filename, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + " : " + content + " \n", true);
        } catch (FileNotFoundException nfe) {
            // File not found. Do nothing
        } catch (IOException ioe) {
            System.out.println("<--- Error while writing Log --> ");
            ioe.printStackTrace();
        }
    }

    public static void writeObject(String filename, Object obj) throws IOException {
        ObjectOutputStream objectOutputStream = null;
        if (obj != null) {
            try {
                objectOutputStream = new ObjectOutputStream(new FileOutputStream(new File(filename)));
                objectOutputStream.writeObject(obj);
            } finally {
                if (objectOutputStream != null) {
                    try {
                        objectOutputStream.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }
    }

    public static Object readObject(String filename, boolean delete) throws FileNotFoundException, IOException {
        ObjectInputStream ois = null;
        Object obj = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(filename));
            obj = ois.readObject();

        } catch (ClassNotFoundException ex) {
            // Ignore
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                    if (delete) {
                        File file = new File(filename);
                        if (file.exists()) {
                            if (file.delete()) {
                                System.out.println("Backup file Deleted -> " + file);
                            } else {
                                System.out.println("Unable to Delete Backup file -> " + file);
                            }
                        }
                    }
                } catch (IOException ioe) {
                }
            }
        }
        return obj;
    }

    public static Properties readProperties(String filename) throws FileNotFoundException, IOException {
        Properties props = new Properties();
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filename);
            props.load(fileInputStream);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                    fileInputStream = null;
                } catch (IOException ioe) {
                }
            }
        }
        return props;
    }

    private static void calculateFileSize(String filename, long limit) throws IOException {
        long fileSize = 0;
        File file = null;
        boolean renamed = false, created = false;
        file = new File(filename);
        if (!file.isFile() || !file.exists()) {
            created = file.createNewFile();
            if (created) {
                System.out.println("File Created :: " + filename);
            } else {
                System.out.println("Error in File Creation :: " + filename);
            }
        } else {
            fileSize = file.length();
            fileSize = fileSize / 1024;
            if (fileSize >= limit) {
                System.out.println("FILE SIZE(" + filename + ")::" + fileSize + "KB");
                String extension = "", newFileName = "";
                String currentDate = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss").format(new Date());
                extension = filename.substring(filename.indexOf(".") + 1, filename.length());
                newFileName = filename.substring(0, filename.indexOf("."));
                newFileName = newFileName + "_" + currentDate + "." + extension;
                File newFile = new File(newFileName);
                renamed = file.renameTo(newFile);
                if (renamed) {
                    file = new File(filename);
                    file.createNewFile();
                }
            }
        }
    }
}
