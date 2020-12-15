
package sr_splitter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {
    
    public static ArrayList<String[]> csvList;
    public static boolean done;
    public static ArrayList<BidRecord> ready;
    
    public static int addedSets = 0;
    public static int analyzedSets = 0;
    
    
    public static void start(boolean separated){
        try {
            SR_Splitter.mainFrame.setProgress("Postęp: wczytywanie");
            int oneSetLength = 100000;
            int maxSets = 100;
            
            csvList = new ArrayList<>();
            done = false;
            ready = new ArrayList<>();
            
            addedSets = 0;
            analyzedSets = 0;
            
            Thread loader = new Thread(new Runnable(){
                @Override
                public void run() {
                    BufferedReader br = null;
                    try {
                        br = new BufferedReader(new FileReader(SR_Splitter.path));
                        String line;
                        int index = 0;
                        String[] single = new String[oneSetLength];
                        
                        while ((line = br.readLine()) != null) {
                            String bidr = line;
                            single[index] = bidr;
                            index++;
                            if(index >= oneSetLength){
                                csvList.add(single);
                                index = 0;
                                single = new String[oneSetLength];
                                addedSets++;
                            }
                            
                            while(csvList.size() > maxSets){
                                // Oczekiwanie na przetworzenie przez ANALYZERA
                                System.out.print("");
                            }
                        }
                        
                        csvList.add(single);
                        addedSets++;
                        
                        done = true;
                        SR_Splitter.mainFrame.setProgress("Postęp: przetwarzanie");
                        
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            br.close();
                        } catch (IOException ex) {
                            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            Thread analyzer = new Thread(new Runnable(){
                @Override
                public void run() {
                    //boolean first = true;
                    String clientId = SR_Splitter.mainFrame.getClientId().trim();
                    
                    while(true){
                        while(csvList.size() == 0){
                            if(done) break;
                            // Oczekiwanie na dodanie przez LOADERA
                            System.out.print("");
                        }
                        if(done && csvList.size() == 0) break;
                        String[] single = csvList.remove(0);
                        for(String bid : single){
                            // Tylko w ostatnim secie
                            if(bid == null) break;
                            
                            String[] csvSplitted = bid.split("	");
                            BidRecord readyBid = new BidRecord(bid, new Date(Long.parseLong(csvSplitted[3])*1000),csvSplitted[21]);
                            //if(first)System.out.println(/*readyBid.getUserId() + " " + */csvSplitted[21]);
                            if(readyBid.getUserId().equals(clientId)) ready.add(readyBid);
                        }
                        analyzedSets++;
                        /*if(first) {
                            System.out.println("After 100k: " + ready.size());
                            first = false;
                        }*/
                        System.gc();
                    }
                }
            });
            
            loader.start();
            analyzer.start();
            
            loader.join();
            System.out.println("Zakonczono dodawanie.");
            analyzer.join();
            
            SR_Splitter.mainFrame.setProgress("Postęp: zakończono");
            System.out.println("Znaleziono: " + ready.size());
            
            sort();
            System.out.println("Posortowano");
            //show();
            saveCsv();
            System.out.println("Zapisano");
            
            if(separated){
                saveSeparatedDates();
                System.out.println("Zapisano wyodrębnione daty");
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private static void sort(){
        ready.sort(new Comparator<BidRecord>(){
            @Override
            public int compare(BidRecord o1, BidRecord o2) {
                return o1.getClickDate().after(o2.getClickDate())? 1:-1;
            }
        });
    }
    
    private static void show(){
        for(BidRecord bd : ready) System.out.println(bd.clickDate + " " + bd.clickDate.getTime());
    }
    
    private static void saveCsv() throws IOException{
        FileWriter writer = new FileWriter(SR_Splitter.exportPath + SR_Splitter.mainFrame.getClientId().trim() + ".csv");
        for(BidRecord bd : ready) writer.append(bd.fullText + "\n");
        writer.flush();
        writer.close();
    }
    
    private static void saveSeparatedDates() throws IOException{
        int nrOfDay = 0;
        FileWriter writer = new FileWriter(SR_Splitter.exportPath + SR_Splitter.mainFrame.getClientId().trim() + "_" + nrOfDay + ".csv");
        int day = -1;
        for(BidRecord bd : ready) {
            System.out.println(day + " " + bd.clickDate.getDate());
            if(day == -1) day = bd.clickDate.getDate();
            if(day != bd.clickDate.getDate()){
                nrOfDay++;
                writer.flush();
                writer.close();
                writer = new FileWriter(SR_Splitter.exportPath + SR_Splitter.mainFrame.getClientId().trim() + "_" + nrOfDay + ".csv");
                day = bd.clickDate.getDate();
            }
            writer.append(bd.fullText + "\n");
        }
        writer.flush();
        writer.close();
    }
}
