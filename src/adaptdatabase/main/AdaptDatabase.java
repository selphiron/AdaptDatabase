/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptdatabase.main;

import adaptdatabase.entities.*;
import java.io.File;
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
    
    // Incidents not used
    private static int[] DISCARTEDINCIDENTS = {0,1,5,8}; // {} -> none incidents discarted / {1,2} -> incidents 1 and 2 discarted
            
    // Incidents not used
    private static final boolean BINARYCLASSIFICATION = false; // Change all type of incients to 1 -> Try to identify if there is an incident or not
    private static final boolean TERNARYCLASSIFICATION = false; // Change all type of incients to 2 (except the one in ELEMENTINTERNARYCLASSIFICATIO)
    private static final int ELEMENTINTERNARYCLASSIFICATION = 8; // Incident type that will consider in the Ternary classification (p.e. if =1, we will consider NoIncident, Incident type 1 and incidents from types 2 to 8 will be considered incidents of the same type
    
    // Excel Extraction
    private static final boolean EXTRACTION = true;
    
    // Include Incidents with user TAG
    private static final boolean USERTAG = false;
    
    // Minimum number of readings in 3 seconds (2 GPS Coordinates)
    private static final int MINNUMBEROFREADINGS = 10;

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
        
    // Logging
    static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AdaptDatabase.class);
    
    public static void main(String[] args) {
        
        String INPUTDATASETPATH = "";
        Utils u;
        List<Incident> incidents;
        List<Ride> rides;
        List<WindowedRide> windowedRides;
        List<NNDataset> nndataset;
        
        if(BINARYCLASSIFICATION)
        {
            OUTPUTDATASETPATH += "binaryDS/";
            DISCARTEDINCIDENTS = new int[]{0};
        }
        else if(TERNARYCLASSIFICATION)
        {
            OUTPUTDATASETPATH += "ternaryDS/" + String.valueOf(ELEMENTINTERNARYCLASSIFICATION) + "/";
            if(!new File(OUTPUTDATASETPATH).exists()) new File(OUTPUTDATASETPATH).mkdir();
            DISCARTEDINCIDENTS = new int[]{0};
        }
        else
        {
            OUTPUTDATASETPATH += "DS/";
        }
        
        for(int i=0; i<datasetpaths.length; i++)
        {
            
            INPUTDATASETPATH = datasetpaths[i];

            u = new Utils(INPUTDATASETPATH,OUTPUTDATASETPATH, EXTRACTION, USERTAG, MINNUMBEROFREADINGS, WINDOWFRAME,
                    WINDOWSPLIT, TRAININGDATASET, USEDDATASET, DISCARTEDINCIDENTS, BINARYCLASSIFICATION,
                    TERNARYCLASSIFICATION, ELEMENTINTERNARYCLASSIFICATION);

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
            System.out.println("Files readed. " + incidents.size() + " incidents found");
            logger.info("Type 0 incidents: " + incidents.stream().filter(x -> x.getIncident()==0).count());
            logger.info("Type 1 incidents: " + incidents.stream().filter(x -> x.getIncident()==1).count());
            logger.info("Type 2 incidents: " + incidents.stream().filter(x -> x.getIncident()==2).count());
            logger.info("Type 3 incidents: " + incidents.stream().filter(x -> x.getIncident()==3).count());
            logger.info("Type 4 incidents: " + incidents.stream().filter(x -> x.getIncident()==4).count());
            logger.info("Type 5 incidents: " + incidents.stream().filter(x -> x.getIncident()==5).count());
            logger.info("Type 6 incidents: " + incidents.stream().filter(x -> x.getIncident()==6).count());
            logger.info("Type 7 incidents: " + incidents.stream().filter(x -> x.getIncident()==7).count());
            logger.info("Type 8 incidents: " + incidents.stream().filter(x -> x.getIncident()==8).count());
            
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
