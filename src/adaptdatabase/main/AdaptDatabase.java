package adaptdatabase.main;

import adaptdatabase.entities.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;

/**
 * @author AlbertSanchez
 */
public class AdaptDatabase {

    // Input and output dataset folder (The path must be finished with a slash '/')
    private String[] inPath =
            {"C:\\Users\\Ahmet\\Desktop\\Albert\\Step1\\"/*"/Users/AlbertSanchez/Desktop/Post/DatasetRides500/iOS0/",
     "/Users/AlbertSanchez/Desktop/Post/DatasetRides500/iOS1/",
     "/Users/AlbertSanchez/Desktop/Post/DatasetRides500/android0/",
     "/Users/AlbertSanchez/Desktop/Post/DatasetRides500/android1/",
     "/Users/AlbertSanchez/Desktop/Post/DatasetRides500/android2/"*/};

    private String outPath = "C:\\Users\\Ahmet\\Desktop\\Albert\\Step2\\";//"/Users/AlbertSanchez/Desktop/Post/DatasetStatistics500/3/";

    // Incidents not used
    private static int[] DISCARDEDINCIDENTS = {0, 1, 5, 8}; // {} -> none incidents discarted / {1,2} -> incidents 1 and 2 discarted

    // Incidents not used
    private static final boolean BINARYCLASSIFICATION = true; // Change all type of incients to 1 -> Try to identify if there is an incident or not
    private static final boolean TERNARYCLASSIFICATION = false; // Change all type of incients to 2 (except the one in ELEMENTINTERNARYCLASSIFICATIO)
    private static final int ELEMENTINTERNARYCLASSIFICATION = 2; // Incident type that will consider in the Ternary classification (p.e. if =1, we will consider NoIncident, Incident type 1 and incidents from types 2 to 8 will be considered incidents of the same type

    //To include/exclude incidents type 0 when extraction
    private static final boolean INCLUDE_NOINCIDENTS = true;

    //Filter categories
    private static final boolean filterCategories = true;

    // Extraction
    private static final boolean EXTRACTION = true;

    // Include Incidents with user TAG
    private static final boolean USERTAG = false;

    // Minimum number of readings in 3 seconds (2 GPS Coordinates)
    private static final int MINNUMBEROFREADINGS = 1;

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
    private static final int TRAININGDATASET = 70; //%

    // Logging
    static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AdaptDatabase.class);

    public AdaptDatabase(String[] inPath, String outPath) {
        this.inPath = inPath;
        this.outPath = outPath;
    }

    public static void main(String[] args) {
        String[] regions = {/*"Augsburg", */"Bern", "Berlin", "Bielefeld", "Düsseldorf", "Eichwalde", "Hannover", "Leipzig", "München", "other", "Pforzheim", "Ruhrgebiet", "Stuttgart", "UNKNOWN", "Wuppertal"};
        String parent = "C:\\Users\\Ahmet\\tubCloud\\Shared\\SimRa\\Machine Learning 2\\CleanDataset\\";
        ArrayList<String> rideInPaths = new ArrayList<>();
        for (int i = 0; i < regions.length; i++) {
            rideInPaths.add(parent + regions[i] + "\\");
            System.out.println(parent + regions[i] + "\\");
        }
        String[] inPath = rideInPaths.toArray(new String[rideInPaths.size()]);
        AdaptDatabase ad = new AdaptDatabase(inPath, "C:\\Users\\Ahmet\\tubCloud\\Shared\\SimRa\\Machine Learning 2\\PrepareDatabase\\");
        ad.adaptDatabase();
    }

    public void adaptDatabase() {

        String INPUTDATASETPATH = "";
        Utils u, u1;
        List<Incident> incidents;
        List<Ride> rides;
        List<WindowedRide> windowedRides;
        List<NNDataset> nndataset;
        long d = 0l, t0 = 0l, t1 = 0l;
        String[] files = new String[inPath.length];

        t0 = System.currentTimeMillis();

        logger.info("Start");

        for (int i = 0; i < inPath.length; i++) {
            if (!inPath[i].endsWith(File.separator)) inPath[i] += File.separator;
            INPUTDATASETPATH = inPath[i];
            System.out.println("INPUTDATASETPATH: " + INPUTDATASETPATH);
            u = new Utils(INPUTDATASETPATH, outPath, EXTRACTION, USERTAG, MINNUMBEROFREADINGS, WINDOWFRAME,
                    WINDOWSPLIT, DISCARDEDINCIDENTS, BINARYCLASSIFICATION,
                    TERNARYCLASSIFICATION, ELEMENTINTERNARYCLASSIFICATION);

            System.out.println("Beginning the Data Extraction");
            logger.info("Starting data extraction");
            System.out.println("Searching files in " + INPUTDATASETPATH + " ...");
            logger.info("Dataset path: " + INPUTDATASETPATH);

            // Get all the incidents
            incidents = new ArrayList<>();
            incidents = u.getSimraIncidents();

            System.out.println("Files read. " + incidents.size() + " incidents found");

            // Get rides
            rides = new ArrayList<>();
            try {
                rides = u.getRides(incidents);
                incidents = null;
            } catch (Exception e) {
                System.out.println("Error while getting rides of " + INPUTDATASETPATH + ": " + e.getMessage());
            }

            System.out.println("Rides readed: " + rides.size());

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

            if (!INCLUDE_NOINCIDENTS)
                nndataset = nndataset.stream().filter(x -> x.getIncident_type() != 0).collect((Collectors.toList()));

            // Generate excel
            if (EXTRACTION) {
                String filepath = "";
                try {
                    if (filterCategories)
                        filepath = u.writeCSVFile(outPath, u.filterCategories(nndataset));
                    else
                        filepath = u.writeCSVFile(outPath, nndataset);

                    files[i] = filepath;
                    System.out.println("CSV Generated! - NNDataset csv path: " + filepath);
                    nndataset = null;
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }

        }

        u1 = new Utils(INPUTDATASETPATH, outPath, EXTRACTION, USERTAG, MINNUMBEROFREADINGS, WINDOWFRAME,
                WINDOWSPLIT, DISCARDEDINCIDENTS, BINARYCLASSIFICATION,
                TERNARYCLASSIFICATION, ELEMENTINTERNARYCLASSIFICATION);

        t1 = System.currentTimeMillis();

        d = t1 - t0;

        try {
            String datasetfile = u1.mergeFiles(outPath, files);
            u1.splitDataset(datasetfile, TRAININGDATASET);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        System.out.println("Time Elapsed: " + d + "ms");
        logger.info("End");
    }

}
