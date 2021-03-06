/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.lazy.KStar;
import weka.classifiers.trees.LMT;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

/**
 *
 * @author Alejandra
 */
public class AppController {

    private final String CONFIGURATIONFILENAME = "/configurations/config.cfg";
    private final String Q1MODEL = "q1model";
    private final String WEKAFILENAME="wekaFile.arff"; 
    Properties prop;
    InputStream input;
    private String[] attributes;
    private String separator = ",";
    private BufferedReader br;
    private FileProcessor processor;
    private final double PERCENT = 66;

    public AppController() {
        loadInitialConfiguration();
        System.out.println(getProperty("Q1Attributes"));
        processor = new FileProcessor();
    }
    
    /**
     * This function initializate the application. 
     */
    private void loadInitialConfiguration() {
        prop = new Properties();
        input = null;
        try {
            input = getClass().getResourceAsStream(CONFIGURATIONFILENAME);
            prop.load(input);
        } catch (Exception eta) {
            eta.printStackTrace();
        }
    }

    public String getProperty(String key) {
        String value = prop.getProperty(key);
        return value;
    }
    
    /**
     * This function translates de CSV file to an ARFF file. 
     * @param model Question to answer 
     * @param fileName CSV file with the events. 
     * @throws FileNotFoundException
     * @throws ClassNotFoundException 
     */
    public void translateDocument(String model, String fileName) throws FileNotFoundException, ClassNotFoundException {
        if (model.equals("Q1")) {
            try {
                String[] headers = getProperty(model + "Attributes").split(separator);
                FileProcessor.headers = headers;
                processor.wekaFile(fileName);
                processor.openFile();
                //processor.translateFile();
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(AppController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(AppController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (model.equals("Q2")) {

        }
    }
    /**
     * This function uses WEKA Library to analize the translated events from the WEKA File. 
     * @return A String with the result of the analysis.  
     * @throws Exception
     */

    public String analize() throws Exception{
        DecimalFormat df = new DecimalFormat("0.0000"); 
        StringBuilder builder = new StringBuilder();
        StringBuilder suspiciousEvents =  new StringBuilder();
        KStar kstar = (KStar)SerializationHelper.read(Q1MODEL+".model"); 
        ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(WEKAFILENAME);
        Instances instances = dataSource.getDataSet(); 
        instances.setClassIndex(instances.numAttributes()-1);
        double actualValue; Instance instance; Double prediction; 
        double[] probabilities;
        long countTrue = 0;
        long countFalse = 0;
        suspiciousEvents.append("===========Evento Sospechoso=================="+"\n"); 
        suspiciousEvents.append(String.join("\t", processor.originalHeaders)+"\n");
        builder.append("Event ID:\tPredicción:\tProbabilidad:+\n");
        for(int i =0; i <instances.numInstances(); i++){
            actualValue= instances.instance(i).classValue(); 
            instance= instances.instance(i); 
            prediction = kstar.classifyInstance(instance); 
            probabilities = kstar.distributionForInstance(instances.instance(i));
            builder.append((i+1)+"\t");
            builder.append((Double.compare(prediction, 0) ==0 ? "Sospechoso" : "No sospechoso")+"\t");
            builder.append(probabilities[0]+"\t");
            builder.append("\n"); 
            if (Double.compare(prediction, 0)==0) {
                countTrue++;
                String eventLine =  processor.getEventByFileNumber(i+1);
                String[] line = eventLine.split(FileProcessor.DELIMITER);
                String event = String.join("\t", line); 
                suspiciousEvents.append(event+"\n"); 
            }else if(Double.compare(prediction, 1)==0){
                countFalse++;
            }
        }
        builder.append("===========================================================================================\n"); 
        builder.append("Resumen\n");
        builder.append("Número de instancias sospechosas: ");
        builder.append(countTrue);
        builder.append("\n");
        
        builder.append("Número de instancias no sospechosas: ");
        builder.append(countFalse);
        builder.append("\n");
        builder.append(suspiciousEvents.toString()); 
        
        return builder.toString();
    }
    
    /**
     * This function uses WEKA library to train a new model, saving the model generated.  
     * @param fileName
     * @param model 
     */
    public void translateTraining(String fileName, String model) {
        processor = new FileProcessor();
        try {
            String[] headers = getProperty(model + "AttributesTrain").split(separator);
            FileProcessor.headers = headers;
            processor.wekaFileTraining(fileName);
            processor.saveMap();
        } catch (IOException ex) {
            Logger.getLogger(AppController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
    /**
     * This function uses the WEKA Library to retrain the model
     * @return The result of the retraining. 
     * @throws Exception 
     */
    public String retrain() throws Exception {
        StringBuilder results = new StringBuilder();
        try {
            copyModel();
            BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(FileProcessor.WEKATRAININGFILE));
            Instances instances = new Instances(reader);
            instances.setClassIndex(instances.numAttributes() - 1);
            results.append("Evaluando el " + PERCENT + "% de los datos suministrados\n");
            int trainSize = (int) Math.round(instances.numInstances() * PERCENT / 100);
            int testSize = instances.numInstances() - trainSize;
            Instances test = new Instances(instances, trainSize, testSize);
            Instances train = new Instances(instances, 0, trainSize);
            KStar kStar = new KStar();
            kStar.buildClassifier(train);
            Evaluation eval = new Evaluation(train);
            eval.evaluateModel(kStar, test);
            reader.close();
            results.append("Resultado del entrenamiento para: " + kStar.getClass().getSimpleName()
                    + "\n---------------------------------\n");
            results.append(eval.toSummaryString("Resultados", true));
            results.append("Fmeasure: " + eval.fMeasure(1) + "\nPrecision: " + eval.precision(1) + "\nRecall: " + eval.recall(1));
            results.append(eval.toMatrixString());
            results.append(eval.toClassDetailsString());
            results.append("AUC = " + eval.areaUnderROC(1));
            weka.core.SerializationHelper.write(Q1MODEL+".model", kStar);
        } catch (IOException ex) {
            Logger.getLogger(AppController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return results.toString();
    }
    
    /**
     * Gets the number of known instances. 
     * @return A number that represents the known instances.
     */
    public int getKnownInstances() {
        return processor.getKnown();
    }
    
    /**
     * Gets the number of unknown instances. 
     * @return A number that represents the unknown instances.
     */
    public int getUnknownInstances() {
        return processor.getUnknown();
    }
    
    /**
     * Formats the string
     * @param instanceNumber File number
     * @param prediction number with the prediction 
     * @param probabilities Probabillity 
     * @return 
     */
    private String prepareString(int instanceNumber, double prediction, double[] probabilities) {
        StringBuilder builder = new StringBuilder();
        builder.append("------------------------------------------------------------------------------------\n");
        MessageFormat format = new MessageFormat("\"%10{0} %30{1} %20{2} %5{3} %5s{4}\""); 
        
        return builder.toString();
    }
    
    /**
     * Changes the event column delimiter 
     * @param delimiter String with the delimiter symbol
     */
    public void changeDelimiter(String delimiter){
            FileProcessor.DELIMITER = delimiter.trim(); 
    }

    private void copyModel() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            KStar kstar = (KStar)SerializationHelper.read(Q1MODEL+".model");
            weka.core.SerializationHelper.write(Q1MODEL+format.format(new Date())+".model", kstar);
        } catch (FileNotFoundException ex) {
            System.out.println("Not existent model");
        } catch (Exception ex) {
            Logger.getLogger(AppController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
}
