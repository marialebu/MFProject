package controller;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
//import org.apache.commons.io.FilenameUtils;


public class Main {

    private static BufferedReader br;
    private static Writer writer;
    private static String line;
    private static String directory;
    static String[] headers;


    static HashMap<Integer, HashMap<String, Integer>> generalInformation = new HashMap<>();

    static ArrayList<String> numerics = new ArrayList<>();

    static ArrayList<String> requestUrl = new ArrayList<>();


    private static final String DELIMITER = ";";

    private static final String WEKA_DELIMITER = ",";


    

    private BufferedReader wekaFile() throws IOException {
        System.out.print("Write the directory folder path where the program will work: ");
        Scanner sc = new Scanner(System.in);
        directory = sc.nextLine();
        System.out.print("Write the CSV file name: ");
        String file = sc.nextLine();
        System.out.print("Do you want to set certain parameters as NUMERIC (Y-N): ");
        String option = sc.nextLine().toLowerCase();
        while(!(option.equals("y") || option.equals("n"))){
            System.out.print("Do you want to leave certain parameters as NUMERIC (Y-N): ");
            option = sc.nextLine().toLowerCase();
        }
        if (option.equals("y")){
            while (!option.isEmpty()){
                System.out.print("Which parameter do you want to leave as NUMERIC (blank to exit): ");
                option = sc.nextLine();
                numerics.add(concat(option));
            }
        }

        System.out.print("Do you want to set certain parameters as REQUEST URL (Y-N): ");
        option = sc.nextLine().toLowerCase();
        while(!(option.equals("y") || option.equals("n"))){
            System.out.print("Do you want to leave certain parameters as REQUEST URL (Y-N): ");
            option = sc.nextLine().toLowerCase();
        }
        if (option.equals("y")){
            while (!option.isEmpty()){
                System.out.print("Which parameter do you want to leave as REQUEST URL (blank to exit): ");
                option = sc.nextLine();
                requestUrl.add(concat(option));
            }
        }


        sc.close();
        br = new BufferedReader(new FileReader(directory+"//"+file));
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(directory+"\\wekaFile.arff"), "utf-8"));
        writer.write("% 1. Title: Events\n");
        writer.write("% \n");
        writer.write("% 2. Sources\n");
        writer.write("%      (a) Creators: Maria Blanco, Nicolas Gomez, Edwin Ceron\n");
        writer.write("@RELATION events\n");
        writer.write("\n");
        line = br.readLine();
        headers = line.split(DELIMITER);
        for(int i = 0 ; i < headers.length; i++){
            generalInformation.put(i, new HashMap<>());
        }
        
        return br; 
    }

    private static void traduceArchivo() throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(directory+"\\outTraduction.txt"), "utf-8"));
        writer.write("----------FILE--------------");
        writer.write("\n");
        writer.write("\n");
        for (int i = 0 ; i < headers.length; i++){
            writer.write("----------"+concat(headers[i])+"----------"+"\n");
            HashMap<String, Integer> map = generalInformation.get(i);
            for(String s : map.keySet()){
                writer.write(map.get(s)+" -> "+s+"\n");
            }
            writer.write("\n");
        }


        writer.close();
    }

    private static void openFile(String fileName) throws IOException {
        br = new BufferedReader(new FileReader(fileName));
        String[] linea;
        StringBuffer newLine;
        int count = 0;
        StringBuilder lines = new StringBuilder();
        while(!(line==null)){
            if(count++==0){
                line = br.readLine();
                continue;
            }
            linea = line.split(DELIMITER);
            newLine = new StringBuffer();
                for(int i=0; i<linea.length; i++){
                    String name = concat(headers[i]);
                    HashMap<String, Integer> map = generalInformation.get(i);
                    if (numerics.contains(name)){
                        map.put(linea[i], Integer.parseInt(linea[i]));
                    }
                    else if (requestUrl.contains(name)){
                        if(!linea[i].isEmpty()){
                            URL url = new URL(linea[i]);
                        }
                        map.put(linea[i], Integer.parseInt(linea[i]));
                    }
                    else{
                        if(!map.containsKey(linea[i])){
                            map.put(linea[i], map.size());
                        }
                    }
                newLine.append(map.get(linea[i]));
                newLine.append(i!=linea.length-1?WEKA_DELIMITER:"\n");
            }
            lines.append(newLine.toString());
            line = br.readLine();
        }
        for(int i = 0 ; i < headers.length; i++){
            String s = headers[i];
            String name = concat(s);
            if (numerics.contains(name)){
                writer.write("@ATTRIBUTE "+concat(s)+" NUMERIC\n");
            }else{
                writer.write("@ATTRIBUTE "+concat(s)+" "+toList(generalInformation.get(i))+"\n");
            }
        }
        writer.write("\n");
        writer.write("@DATA\n");
        br.close();
        writer.write(lines.toString());
        writer.close();
    }

    private static String concat(String s) {
        String[] line = s.split(" ");
        StringBuffer res = new StringBuffer();
        for(String l : line){
            res.append(l);
        }
        return res.toString();
    }

    private static String toList(HashMap<String, Integer> map) {
        ArrayList<Integer> arr = new ArrayList<>(map.values());
        char[] res = arr.toString().toCharArray();
        res[0] = '{';
        res[res.length-1] = '}';
        return new String(res);
    }
}
