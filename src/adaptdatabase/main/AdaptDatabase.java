/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptdatabase.main;

import adaptdatabase.entities.*;
import com.sun.media.jfxmedia.logging.Logger;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author AlbertSanchez
 */
public class AdaptDatabase {

    // Input and output dataset folder (The path must be finished with a slash '/')
    private static final String inputDatasetPath = Paths.get("").toAbsolutePath().toString() + "/test/TestDS/";
    //private static final String inputDatasetPath = "/Users/AlbertSanchez/Dropbox/TFM/Dataset/";
    private static final String outputDatasetPath = "";
    
    // Excel Extraction
    private static final boolean EXTRACTION = true;
    
    // Include Incidents with user TAG
    private static final boolean userTAG = false;
    
    // Minimum number of readings in 3 seconds (2 GPS Coordinates)
    private static final int minNumberOfReadings = 15;
    
    // Samples Target between 2 GPS Coordinates (Mean between 988 rides = 22.80)
    private static final int sampleTarget = 24;

    // Windowing
    private static final int windowFrame = 6000; //ms
    private static final int windowShift = 3000; //ms
            
    public static void main(String[] args) {
         
        Utils u = new Utils(inputDatasetPath,outputDatasetPath, EXTRACTION, userTAG, minNumberOfReadings, sampleTarget, windowFrame, windowShift);
                
        System.out.println(inputDatasetPath);
        System.out.println("Begining the Data Extraction");
        System.out.println("...");
        System.out.println("Searching files in " + inputDatasetPath + " ...");
        
        // Get all the incidents
        List<Incident> incidents = new ArrayList<>();
        incidents = u.getSimraIncidents();
        
        System.out.println("Files readed. " + incidents.size() + " incidents found");
        
        // Get rides
        List<Ride> rides = new ArrayList<>(); 
        try
        {
            rides = u.getRides(incidents);
        }
        catch (Exception e)
        {
            Logger.logMsg(Logger.WARNING, e.getMessage());
        }
        
        System.out.println("Rides readed: " + rides.size());
        //for (Ride r : rides)
        //    r.getTimestamp().forEach(System.out::println);
                   
        // Chop rides in little windows
        List<WindowedRide> windowedRides = new ArrayList<>();
        
        System.out.println("Windowing files");

        windowedRides = u.chopRides(rides);
        
        System.out.println("Windowed Rides! - Number of windowed rides: " + windowedRides.size());


        // Calculate statistics
        List<NNDataset> nndataset = new ArrayList<>();
        
        nndataset = u.calculateStatistics(windowedRides);
        
        System.out.println("NN Dataset Generated! - NNDataset Size: " + nndataset.size());

        // Find ride part in incidents
        
    }
    
    
}
