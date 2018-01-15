/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alejandra Blanco, Nicolás Gómez and Edwin Cerón
 */
public class FileProcessor {

    private static BufferedReader br;
    private static Writer writer;
    private static String line;
    static String[] originalHeaders;
    static String[] headers;
    static Integer[] index;
    static Integer[] originalIndex;
    static String WEKAFILENAME = "wekaFile.arff";
    static String UNKNOWNFILE = "unknownInstances.csv";
    static String WEKATRAININGFILE = "wekaFileTraining.arff";
    private static Writer writerUnknown;
    private BufferedWriter bw;
    private int known = 0;
    private int unknown = 0;

    static HashMap<String, HashMap<String, Integer>> generalInformation;

    static ArrayList<String> numerics = new ArrayList<>();

    static ArrayList<String> requestUrl = new ArrayList<>();

    private StringBuilder data = new StringBuilder();

    public static String DELIMITER = ";";

    private static final String WEKA_DELIMITER = ",";

    public void wekaFileTraining(String filePath) throws IOException {
        try {
            loadData();
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("outTraduction.bin"));
            generalInformation = (HashMap<String, HashMap<String, Integer>>) in.readObject();
            in.close();
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(WEKATRAININGFILE), "utf-8"));
            br = new BufferedReader(new FileReader(filePath));
            prepareWekaFile();
            writeFile(translateNewFile());
        } catch (FileNotFoundException e) {
            System.out.println("MAP NOT FOUND");
            generalInformation = new HashMap<>();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FileProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void prepareWekaFile() throws IOException {
        writer.write("% 1. Title: Events\n");
        writer.write("% \n");
        writer.write("% 2. Sources\n");
        writer.write("%     + (a) Creators: Maria Blanco, Nicolas Gomez, Edwin Ceron\n");
        writer.write("@RELATION events\n");
        writer.write("\n");
        line = br.readLine();
        if (line.contains("=")) {
            line = br.readLine();
        }
        originalHeaders = line.split(DELIMITER);
        System.out.println("NumAttr: " + headers.length);
        index = new Integer[originalHeaders.length];
        originalIndex = new Integer[headers.length];
        for (int i = 0; i < originalHeaders.length; i++) {
            for (int j = 0; j < headers.length; j++) {
                if (originalHeaders[i].trim().equals(headers[j].trim())) {
                    if (generalInformation.get(concat(headers[j].trim())) == null) {
                        generalInformation.put(concat(headers[j].trim()), new HashMap<>());
                    }
                    index[i] = j;
                    originalIndex[j] = i;
                }else{
                    
                }
            }
        }
        ;
    }

    private void loadData() throws FileNotFoundException, IOException {
        br = new BufferedReader(new FileReader(WEKATRAININGFILE));
        String line = br.readLine();
        System.out.println(line);
        if (line != null) {
            while (!line.contains("@DATA")) {
                line = br.readLine();
            }
            line = br.readLine();
            while (!(line == null)) {
                data.append(line + "\n");
                line = br.readLine();
            }
        }
        br.close();
    }

    public void wekaFile(String filePath) throws FileNotFoundException, UnsupportedEncodingException, IOException, ClassNotFoundException {
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("outTraduction.bin"));
            generalInformation = (HashMap<String, HashMap<String, Integer>>) in.readObject();
            in.close();
            System.out.println("Cargado");
        } catch (FileNotFoundException e) {
            generalInformation = new HashMap<>();
            System.out.println("Creado");
        }
        br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "ISO-8859-1"));
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(WEKAFILENAME), "utf-8"));
        writerUnknown = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(UNKNOWNFILE), "utf-8"));
        prepareWekaFile();
    }

    public void openFile() throws IOException {
        addHeaders();
        String[] linea;
        StringBuffer newLine;
        int count = 0;
        int counterHeaders = 0;
        StringBuilder lines = new StringBuilder();
        boolean knownline;
        while (!(line == null)) {
            if (count++ == 0) {
                line = br.readLine();
                continue;
            }
            knownline = true;
            linea = line.split(DELIMITER);
            //System.out.println("ARRAY " + Arrays.toString(linea) + " " + linea.length);
            newLine = new StringBuffer();
            //System.out.println("LINE: " + count);
           // System.out.println("LINElog: " + line);
            for (int i = 0; i < headers.length && knownline; i++) {
                try{
                    int originalHeaderIndex = originalIndex[i];
                    String linea_i = linea[originalHeaderIndex];
                    linea_i = new String(linea_i.getBytes("ISO-8859-1"));
                    HashMap<String, Integer> map = generalInformation.get(concat(headers[i]));
                if (map != null) {

                    String name = concat(headers[i]);
                    if (numerics.contains(name)) {
                        map.put(linea_i, Integer.parseInt(linea_i));
                    } else if (requestUrl.contains(name)) {
                        if (!linea_i.isEmpty()) {
                            URL url = new URL(linea[i]);
                        }
                        map.put(linea_i, Integer.parseInt(linea_i));
                    } else {
                        if (!map.containsKey(linea_i)) {
                            //map.put(linea_i, map.size());
                            knownline = false;
                        }
                    }
                    counterHeaders++;
                    newLine.append(map.get(linea_i));
                    newLine.append(counterHeaders != headers.length ? WEKA_DELIMITER : ",?\n");
                }
                }catch(Exception e){
                    System.out.println(line);
                    e.printStackTrace();
                    System.out.println("");
                }
                
            }
            if (knownline) {
                setKnown(getKnown() + 1);
                lines.append(newLine.toString());
            } else {
                setUnknown(getUnknown() + 1);
                writerUnknown.write(line + "\n");
                //System.out.println(line);
            }
            counterHeaders = 0;
            line = br.readLine();
        }
        for (int i = 0; i < headers.length; i++) {
            String s = headers[i];
            String name = concat(s);
            if (numerics.contains(name)) {
                writer.write("@ATTRIBUTE " + concat(s) + " NUMERIC\n");
            } else {
                HashMap<String, Integer> map = generalInformation.get(name);
                //writer.write("@ATTRIBUTE " + concat(s) + " NUMERIC\n");
                writer.write("@ATTRIBUTE " + concat(s) + " " + toList(map) + "\n");
            }
        }
        writer.write("@ATTRIBUTE IllegitimateRequest {0, 1}\n");
        writer.write("\n");
        writer.write("@DATA\n");
        br.close();
        writer.write(lines.toString());
        writer.close();
        writerUnknown.close();
    }

    private StringBuilder translateNewFile() throws IOException {
        String[] linea;
        StringBuffer newLine;
        int count = 0;
        int counterHeaders = 0;
        StringBuilder lines = new StringBuilder();
        //System.out.println(line);
        while (!(line == null)) {
            if (count++ == 0) {
                line = br.readLine();
                continue;
            }
            linea = line.split(DELIMITER);
            //System.out.println(line);
            newLine = new StringBuffer();
            for (int i = 0; i < headers.length; i++) {
                int originalHeaderIndex = originalIndex[i];
                String linea_i = linea[originalHeaderIndex];
                //System.out.println(linea_i + " " + count);
                HashMap<String, Integer> map = generalInformation.get(concat(headers[i]));
                if (map != null) {
                    String name = concat(headers[i]);
                    if (numerics.contains(name)) {
                        map.put(linea_i, Integer.parseInt(linea_i));
                    } else if (requestUrl.contains(name)) {
                        if (!linea_i.isEmpty()) {
                            URL url = new URL(linea[i]);
                        }
                        map.put(linea_i, Integer.parseInt(linea_i));
                    } else {
                        if (!map.containsKey(linea_i)) {
                            map.put(linea_i, map.size());
                        }
                    }
                    counterHeaders++;
                    newLine.append(map.get(linea_i));
                    newLine.append(counterHeaders != headers.length ? WEKA_DELIMITER : "\n");
                }
            }
            lines.append(newLine.toString());
            counterHeaders = 0;
            line = br.readLine();
        }
        return lines;
    }

    private void writeFile(StringBuilder lines) throws IOException {
        for (int i = 0; i < headers.length; i++) {
            String s = headers[i];
            String name = concat(s);
            if (numerics.contains(name)) {
                writer.write("@ATTRIBUTE " + concat(s) + " NUMERIC\n");
            } else {
                HashMap<String, Integer> map = generalInformation.get(name);
                writer.write("@ATTRIBUTE " + concat(s) + " " + toList(map) + "\n");
            }
        }
        writer.write("\n");
        writer.write("@DATA\n");
        br.close();
        writer.write(data.toString());
        writer.write(lines.toString());
        writer.close();
    }

    private static String concat(String s) {
        String[] line = s.split(" ");
        StringBuffer res = new StringBuffer();
        for (String l : line) {
            res.append(l);
        }
        return res.toString();
    }

    private static String toList(HashMap<String, Integer> map) {
        ArrayList<Integer> arr = new ArrayList<>(map.values());
        char[] res = arr.toString().toCharArray();
        res[0] = '{';
        res[res.length - 1] = '}';
        return new String(res);
    }

    public void saveMap() throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("outTraduction.bin"));
        out.writeObject(generalInformation);
        out.close();
        System.out.println("Traductor");
    }

    /**
     * @return the known
     */
    public int getKnown() {
        return known;
    }

    /**
     * @param known the known to set
     */
    public void setKnown(int known) {
        this.known = known;
    }

    /**
     * @return the unknown
     */
    public int getUnknown() {
        return unknown;
    }

    /**
     * @param unknown the unknown to set
     */
    public void setUnknown(int unknown) {
        this.unknown = unknown;
    }

    private void addHeaders() throws IOException {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < originalHeaders.length; i++) {
            builder.append(originalHeaders[i]);
            builder.append(i != originalHeaders.length-1 ? DELIMITER : "\n");
        }
        writerUnknown.write("sep=,\n");
        writerUnknown.write(builder.toString());
    }
    
    public static String transformLine(String line){
        StringBuilder builder = new StringBuilder();
        String[] linearray = line.split(","); 
        for(int i =0; i < headers.length; i++){
            HashMap<String, Integer> dictionary = generalInformation.get(headers[i]); 
            for(String s : dictionary.keySet()){
                if(linearray[i].equals(dictionary.get(s))){
                    builder.append(s); 
                }
            }
        }
        System.out.println(builder.toString());
        return builder.toString();
    }
}
