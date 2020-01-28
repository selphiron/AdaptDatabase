/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptdatabase.main;

import adaptdatabase.entities.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
/**
 *
 * @author AlbertSanchez
 */
public class AdaptDatabase {

    // Input and output dataset folder (The path must be finished with a slash '/')
    //private static final String INPUTDATASETPATH = Paths.get("").toAbsolutePath().toString() + "/test/";
    private static final String INPUTDATASETPATH = "/Users/AlbertSanchez/Desktop/TFM (noDropBox)/Dataset/";
    private static final String OUTPUTDATASETPATH = "/Users/AlbertSanchez/Desktop/";
    
    // Excel Extraction
    private static final boolean EXTRACTION = true;
    
    // Include Incidents with user TAG
    private static final boolean USERTAG = false;
    
    // Minimum number of readings in 3 seconds (2 GPS Coordinates)
    private static final int MINNUMBEROFREADINGS = 15;
    
    // Samples Target between 2 GPS Coordinates (Mean between 988 rides = 22.80)
    private static final int SAMPLETARGET = 24;

    // Windowing
    private static final int WINDOWFRAME = 6000; //ms
    private static final int WINDOWSHIFT = 3000; //ms
    
    // Dataset usage
    private static final int TRAININGDATASET = 100; //%
    private static final int USEDDATASET = 100; //%
            
    // Logging
    static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AdaptDatabase.class);
    
    public static void main(String[] args) {
         
        Utils u = new Utils(INPUTDATASETPATH,OUTPUTDATASETPATH, EXTRACTION, USERTAG, MINNUMBEROFREADINGS, SAMPLETARGET, WINDOWFRAME, WINDOWSHIFT, TRAININGDATASET, USEDDATASET);
                
        System.out.println(INPUTDATASETPATH);
        System.out.println("Begining the Data Extraction");
        logger.info("Starting data extraction");
        System.out.println("...");
        System.out.println("Searching files in " + INPUTDATASETPATH + " ...");
        logger.info("Dataset path: " + INPUTDATASETPATH);

        
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
            //Logger.logMsg(Logger.WARNING, e.getMessage());
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

        // Generate excel
        if (EXTRACTION)
        {
            String filepath = "";
            try
            {
                //filepath = u.writeXLSNNDataset(OUTPUTDATASETPATH, nndataset);
                //System.out.println("Excel Generated! - NNDataset excel path: " + filepath);
                filepath = u.writeCSVFile(OUTPUTDATASETPATH, u.filterCategories(nndataset));
                System.out.println("CSV Generated! - NNDataset csv path: " + filepath);

            }
            catch(Exception e)
            {
                logger.error(e.getMessage());            
            }
        }
            
    }
    
    
}
