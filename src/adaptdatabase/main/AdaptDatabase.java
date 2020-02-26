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
    private static final String[] datasetpaths = 
    {"/Users/AlbertSanchez/Desktop/TFM (noDropBox)/Dataset/dataset/SimRa_Sample_02_20_20/iOS0/",
    "/Users/AlbertSanchez/Desktop/TFM (noDropBox)/Dataset/dataset/SimRa_Sample_02_20_20/iOS1/",
    "/Users/AlbertSanchez/Desktop/TFM (noDropBox)/Dataset/dataset/SimRa_Sample_02_20_20/android0/",
    "/Users/AlbertSanchez/Desktop/TFM (noDropBox)/Dataset/dataset/SimRa_Sample_02_20_20/android1/",
    "/Users/AlbertSanchez/Desktop/TFM (noDropBox)/Dataset/dataset/SimRa_Sample_02_20_20/android2/"};
    private static String OUTPUTDATASETPATH = "/Users/AlbertSanchez/Desktop/TFM (noDropBox)/Dataset/";
    
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
    private static final int WINDOWSPLIT = 3; 
    
    /****************************************************
    <-----------------------------------> Window Frame
    <-----------> Window Shift = 1/3 of window frame
     -----------------------------------
    |     |     |     |     |     |     |
    |     |     |     |     |     |     |
     -----------------------------------
                <-----------> Interval where incident is considered
    ****************************************************/
    
    // Dataset usage
    private static final int TRAININGDATASET = 100; //%
    private static final int USEDDATASET = 100; //%
    
    // Incidents not used
    private static final int[] DISCARTEDINCIDENTS = {0,1,5,8}; // {} -> none incidents discarted / {1,2} -> incidents 1 and 2 discarted
            
    // Incidents not used
    private static final boolean BINARYCLASSIFICATION = false; // Change all type of incients to 1 -> Try to identify if there is an incident or not
    
    // Logging
    static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AdaptDatabase.class);
    
    public static void main(String[] args) {
        
        String INPUTDATASETPATH = "";
        Utils u;
        List<Incident> incidents;
        List<Ride> rides;
        List<WindowedRide> windowedRides;
        List<NNDataset> nndataset;
        
        if(BINARYCLASSIFICATION) OUTPUTDATASETPATH += "binaryDS/";
        else OUTPUTDATASETPATH += "DS/";
        
        for(int i=0; i<datasetpaths.length; i++)
        {
            
            INPUTDATASETPATH = datasetpaths[i];

            u = new Utils(INPUTDATASETPATH,OUTPUTDATASETPATH, EXTRACTION, USERTAG, MINNUMBEROFREADINGS, SAMPLETARGET, WINDOWFRAME, WINDOWSPLIT, TRAININGDATASET, USEDDATASET, DISCARTEDINCIDENTS, BINARYCLASSIFICATION);

            System.out.println(INPUTDATASETPATH);
            System.out.println("Begining the Data Extraction");
            logger.info("Starting data extraction");
            System.out.println("...");
            System.out.println("Searching files in " + INPUTDATASETPATH + " ...");
            logger.info("Dataset path: " + INPUTDATASETPATH);

            // Get all the incidents
            incidents = new ArrayList<>();
            incidents = u.getSimraIncidents();

            System.out.println("Files readed. " + incidents.size() + " incidents found");

            // Get rides
            rides = new ArrayList<>(); 
            try
            {
                rides = u.getRides(incidents);
                incidents = null;
            }
            catch (Exception e)
            {
                System.out.println("Error while getting rides: " + e.getMessage());
            }

            System.out.println("Rides readed: " + rides.size());
            //for (Ride r : rides)
            //    r.getTimestamp().forEach(System.out::println);

            // Chop rides in little windows
            windowedRides = new ArrayList<>();

            System.out.println("Windowing files");

            windowedRides = u.chopRides(rides);
            rides = null;
            System.out.println("Windowed Rides! - Number of windowed rides: " + windowedRides.size());

            // Calculate statistics
            nndataset = new ArrayList<>();
            nndataset = u.calculateStatistics(windowedRides);
            windowedRides = null;
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
                    nndataset = null;
                }
                catch(Exception e)
                {
                    logger.error(e.getMessage());            
                }
            }
        }
            
    }
    
    
}
