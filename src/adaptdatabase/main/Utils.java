package adaptdatabase.main;

import adaptdatabase.entities.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager; //Logging
import org.jtransforms.fft.*; //FFT
import org.apache.poi.ss.usermodel.*; //Excel Export
import org.apache.poi.ss.util.*; //Excel Export
import org.apache.poi.hssf.usermodel.*; //Excel Export

/**
 * @author AlbertSanchez
 */
public class Utils {

    static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Utils.class);

    private final String INPUTDATASETPATH;
    private final String OUTPUTDATASETPATH;
    private final boolean EXTRACTION;
    private final boolean USERTAG;
    private final int MINNUMBEROFREADINGS;
    private final int WINDOWFRAME;
    private final int WINDOWSPLIT;
    private final int[] DISCARTEDINCIDENTS;
    private final boolean BINARYCLASSIFICATION;
    private final boolean TERNARYCLASSIFICATION;
    private final int ELEMENTINTERNARYCLASSIFICATION;

    DoubleFFT_1D fft;
    double[] xFFT;
    double[] yFFT;
    double[] zFFT;
    double[] fftDataX;
    double[] fftDataY;
    double[] fftDataZ;


    public Utils(String INPUTDATASETPATH, String OUTPUTDATASETPATH, boolean EXTRACTION, boolean USERTAG,
                 int MINNUMBEROFREADINGS, int WINDOWFRAME, int WINDOWSHIFT, int[] DISCARTEDINCIDENTS,
                 boolean BINARYCLASSIFICATION, boolean TERNARYCLASSIFICATION, int ELEMENTINTERNARYCLASSIFICATION) {
        this.INPUTDATASETPATH = INPUTDATASETPATH;
        this.OUTPUTDATASETPATH = OUTPUTDATASETPATH;
        this.EXTRACTION = EXTRACTION;
        this.USERTAG = USERTAG;
        this.MINNUMBEROFREADINGS = MINNUMBEROFREADINGS;
        this.WINDOWFRAME = WINDOWFRAME;
        this.WINDOWSPLIT = WINDOWSHIFT;
        this.DISCARTEDINCIDENTS = DISCARTEDINCIDENTS;
        this.BINARYCLASSIFICATION = BINARYCLASSIFICATION;
        this.TERNARYCLASSIFICATION = TERNARYCLASSIFICATION;
        this.ELEMENTINTERNARYCLASSIFICATION = ELEMENTINTERNARYCLASSIFICATION;
    }

    public List<Incident> getSimraIncidents() {
        System.out.println("Getting SimRa incidents");
        List<Incident> incidents = new ArrayList<>();
        List<String> fileNames = new ArrayList<String>();
        fileNames = getFileNames();

        if (fileNames.isEmpty()) {
            System.out.println("0 files found");
            logger.warn("0 files found in the dataset path");
        } else {
            System.out.println(fileNames.size() + " files found");
            logger.info(fileNames.size() + " files found in the dataset");
        }

        for (String file : fileNames) {
            System.out.println(file);
            try {
                List<Incident> i = readIncidents(file);
                incidents.addAll(i);
            } catch (Exception e) {
                System.out.println("Exception while reading " + file);
                e.printStackTrace();
            }
        }

        System.out.println("Incidents: ");
        for (Incident i : incidents) {
            System.out.println(" - " + i.getDs_name() + " - Type: " + i.getIncident());
        }

        return incidents;
    }

    public List<String> getFileNames() {
        List<String> results = new ArrayList<>();
        File[] files = new File(INPUTDATASETPATH).listFiles();

        for (File file : files) {
            if (file.isFile())
                if (!file.getName().startsWith(".") && file.getName().startsWith("VM"))
                    results.add(INPUTDATASETPATH + file.getName());
        }
        return results;
    }

    public List<Incident> readIncidents(String file)
            throws IOException {
        List<Incident> incidents = new ArrayList<>();
        FileReader reader = new FileReader(file);
        BufferedReader br = new BufferedReader(reader);
        String line;

        long ts = 0l;

        String splitPattern = Pattern.quote(System.getProperty("file.separator"));
        String[] s = file.split(splitPattern);
        String fileName = s[s.length - 1];

        br.readLine(); //Read the 1st line which is <app version>#<file version>
        br.readLine(); //Read the 2nd line which are the headers
        line = br.readLine();
        while (!line.equals("")) {
            Incident incident = new Incident();
            incident.setDs_name(fileName);
            String[] incidentFields = line.split(",", -1);
            incident.setKey(Integer.parseInt(incidentFields[0]));
            incident.setLatitude(Double.parseDouble(incidentFields[1]));
            incident.setLongitude(Double.parseDouble(incidentFields[2]));

            if (Long.parseLong(incidentFields[3]) == 1337 && USERTAG) {
                ts = takeTSforUserTagIncidents(incidentFields, file);
                incident.setTimestamp(ts);
            } else
                incident.setTimestamp(Long.parseLong(incidentFields[3]));


            incident.setBike(Integer.parseInt(incidentFields[4]));
            if (incidentFields[5].equals("1"))
                incident.setChildCheckBox(true);
            else
                incident.setChildCheckBox(false);
            if (incidentFields[6].equals("1"))
                incident.setTrailerCheckBox(true);
            else
                incident.setTrailerCheckBox(false);
            incident.setpLoc(Integer.parseInt(incidentFields[7]));
            if (incidentFields[8].equals(""))
                incident.setIncident(0);
            else
                incident.setIncident(Integer.parseInt(incidentFields[8]));
            if (incidentFields[9].equals("1"))
                incident.setI1(true);
            else
                incident.setI1(false);
            if (incidentFields[10].equals("1"))
                incident.setI2(true);
            else
                incident.setI2(false);
            if (incidentFields[11].equals("1"))
                incident.setI3(true);
            else
                incident.setI3(false);
            if (incidentFields[12].equals("1"))
                incident.setI4(true);
            else
                incident.setI4(false);
            if (incidentFields[13].equals("1"))
                incident.setI5(true);
            else
                incident.setI5(false);
            if (incidentFields[14].equals("1"))
                incident.setI6(true);
            else
                incident.setI6(false);
            if (incidentFields[15].equals("1"))
                incident.setI7(true);
            else
                incident.setI7(false);
            if (incidentFields[16].equals("1"))
                incident.setI8(true);
            else
                incident.setI8(false);
            if (incidentFields[17].equals("1"))
                incident.setI9(true);
            else
                incident.setI9(false);
            if (incidentFields[18].equals("1"))
                incident.setScary(true);
            else
                incident.setScary(false);

            String description = incidentFields[19];
            if (incidentFields.length > 20)
                for (int i = 20; i < incidentFields.length; i++)
                    description = description + incidentFields[i];

            incident.setDesc(description);

            if (incidentFields.length > 20 && incidentFields[20].equals("1"))
                incident.setI10(true);
            else
                incident.setI10(false);


            // Include USERTAG incidents
            if (USERTAG) {
                if (!excludedIncident(incident.getIncident()))
                    incidents.add(incident);
            } else {
                if (!excludedIncident(incident.getIncident()) && incident.getTimestamp() != 1337)
                    incidents.add(incident);
            }

            line = br.readLine();
        }
        return incidents;
    }

    public List<Ride> getRides(List<Incident> incidents)
            throws IOException {
        boolean fileWithWrongFormat = false;
        List<Ride> rides = new ArrayList<>();
        Ride ride;
        int numberOfRides = 0;

        List<Double> latitude, longitude,
                tmp_latitude, tmp_longitude;
        List<Float> acc_x, acc_y, acc_z, acc_68, gyr_a, gyr_b, gyr_c,
                tmp_acc_x, tmp_acc_y, tmp_acc_z, tmp_acc_68, tmp_gyr_a, tmp_gyr_b, tmp_gyr_c;
        List<Long> timestamp, tmp_timestamp;

        double prevLat = 0, prevLon = 0;
        float prevAcc_68 = 0, prevGyr_a = 0, prevGyr_b = 0, prevGyr_c = 0;
        int readings = 0;

        // Take only the ds_name to avoid reading N times a file that has
        List<String> filenames = incidents.stream().map(Incident::getDs_name)
                .collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());


        for (String f : filenames) {
            fileWithWrongFormat = false;
            ride = new Ride();
            ride.setDs_name(f);
            // Take all the incidents of the file
            ride.setIncidents(incidents.stream().filter(i -> i.getDs_name().equals(f)).collect(Collectors.toList()));

            latitude = new ArrayList<>();
            longitude = new ArrayList<>();
            acc_x = new ArrayList<>();
            acc_y = new ArrayList<>();
            acc_z = new ArrayList<>();
            timestamp = new ArrayList<>();
            acc_68 = new ArrayList<>();
            gyr_a = new ArrayList<>();
            gyr_b = new ArrayList<>();
            gyr_c = new ArrayList<>();

            tmp_latitude = new ArrayList<>();
            tmp_longitude = new ArrayList<>();
            tmp_acc_x = new ArrayList<>();
            tmp_acc_y = new ArrayList<>();
            tmp_acc_z = new ArrayList<>();
            tmp_timestamp = new ArrayList<>();
            tmp_acc_68 = new ArrayList<>();
            tmp_gyr_a = new ArrayList<>();
            tmp_gyr_b = new ArrayList<>();
            tmp_gyr_c = new ArrayList<>();

            FileReader r = new FileReader(INPUTDATASETPATH + f);
            BufferedReader br = new BufferedReader(r);

            // Read until header of the ride data
            String line = br.readLine();
            while (!line.startsWith("lat,lon,X,Y,Z,timeStamp")) {
                line = br.readLine();
                if (line.equals("lat,lon,X,Y,Z,timeStamp")) {
                    fileWithWrongFormat = true;
                    break;
                }
            }

            if (!fileWithWrongFormat) {
                line = br.readLine();
                if (line != null) {
                    String[] rideDataFields = line.split(",", -1);

                    // Save values that are not in constant updating
                    prevLat = Double.parseDouble(rideDataFields[0]);
                    prevLon = Double.parseDouble(rideDataFields[1]);
                    if (rideDataFields.length > 5) {
                        prevAcc_68 = Float.parseFloat(rideDataFields[6]);
                        prevGyr_a = Float.parseFloat(rideDataFields[7]);
                        prevGyr_b = Float.parseFloat(rideDataFields[8]);
                        prevGyr_c = Float.parseFloat(rideDataFields[9]);
                    }

                    // Read all the file
                    while (line != null) {
                        rideDataFields = line.split(",", -1);
                        // Counter for readings
                        // If there is no data in Latitude field (rideDataFields[0]) we
                        // add the counter
                        if (rideDataFields[0].equals("")) {
                            readings++;
                        }
                        // If there is data we save the tmp data if the # of readings is enough
                        else {
                            // If the number of readings is sufficient we save it
                            if (readings >= MINNUMBEROFREADINGS) {
                                latitude.addAll(tmp_latitude);
                                longitude.addAll(tmp_longitude);
                                acc_x.addAll(tmp_acc_x);
                                acc_y.addAll(tmp_acc_y);
                                acc_z.addAll(tmp_acc_z);
                                timestamp.addAll(tmp_timestamp);
                                acc_68.addAll(tmp_acc_68);
                                gyr_a.addAll(tmp_gyr_a);
                                gyr_b.addAll(tmp_gyr_b);
                                gyr_c.addAll(tmp_gyr_c);
                                tmp_latitude.clear();
                                tmp_longitude.clear();
                                tmp_acc_x.clear();
                                tmp_acc_y.clear();
                                tmp_acc_z.clear();
                                tmp_timestamp.clear();
                                tmp_acc_68.clear();
                                tmp_gyr_a.clear();
                                tmp_gyr_b.clear();
                                tmp_gyr_c.clear();
                            }
                            readings = 1;

                        }

                        if (rideDataFields[0].equals(""))
                            tmp_latitude.add(prevLat);
                        else {
                            tmp_latitude.add(Double.parseDouble(rideDataFields[0]));
                            prevLat = tmp_latitude.get(tmp_latitude.size() - 1);
                        }

                        if (rideDataFields[1].equals(""))
                            tmp_longitude.add(prevLon);
                        else {
                            tmp_longitude.add(Double.parseDouble(rideDataFields[1]));
                            prevLon = tmp_longitude.get(tmp_longitude.size() - 1);
                        }

                        tmp_acc_x.add(Float.parseFloat(rideDataFields[2]));
                        tmp_acc_y.add(Float.parseFloat(rideDataFields[3]));
                        tmp_acc_z.add(Float.parseFloat(rideDataFields[4]));
                        tmp_timestamp.add(Long.parseLong(rideDataFields[5]));
                        if (rideDataFields.length > 5) {
                            if (rideDataFields[6].equals(""))
                                tmp_acc_68.add(prevAcc_68);
                            else {
                                tmp_acc_68.add(Float.parseFloat(rideDataFields[6]));
                                prevAcc_68 = tmp_acc_68.get(tmp_acc_68.size() - 1);
                            }
                            if (rideDataFields[7].equals(""))
                                tmp_gyr_a.add(prevGyr_a);
                            else {
                                tmp_gyr_a.add(Float.parseFloat(rideDataFields[7]));
                                prevGyr_a = tmp_gyr_a.get(tmp_gyr_a.size() - 1);
                            }
                            if (rideDataFields[8].equals(""))
                                tmp_gyr_b.add(prevGyr_b);
                            else {
                                tmp_gyr_b.add(Float.parseFloat(rideDataFields[8]));
                                prevGyr_b = tmp_gyr_b.get(tmp_gyr_b.size() - 1);
                            }
                            if (rideDataFields[9].equals(""))
                                tmp_gyr_c.add(prevGyr_c);
                            else {
                                tmp_gyr_c.add(Float.parseFloat(rideDataFields[9]));
                                prevGyr_c = tmp_gyr_c.get(tmp_gyr_c.size() - 1);
                            }
                        }

                        // Read next line
                        line = br.readLine();

                        if (line == null && readings >= MINNUMBEROFREADINGS) {
                            latitude.addAll(tmp_latitude);
                            longitude.addAll(tmp_longitude);
                            acc_x.addAll(tmp_acc_x);
                            acc_y.addAll(tmp_acc_y);
                            acc_z.addAll(tmp_acc_z);
                            timestamp.addAll(tmp_timestamp);
                            acc_68.addAll(tmp_acc_68);
                            gyr_a.addAll(tmp_gyr_a);
                            gyr_b.addAll(tmp_gyr_b);
                            gyr_c.addAll(tmp_gyr_c);
                            tmp_latitude.clear();
                            tmp_longitude.clear();
                            tmp_acc_x.clear();
                            tmp_acc_y.clear();
                            tmp_acc_z.clear();
                            tmp_timestamp.clear();
                            tmp_acc_68.clear();
                            tmp_gyr_a.clear();
                            tmp_gyr_b.clear();
                            tmp_gyr_c.clear();
                            readings = 1;
                        }
                    }

                    // Saving detailed data to the ride
                    if (!latitude.isEmpty())
                        ride.setLatitude(latitude);
                    if (!longitude.isEmpty())
                        ride.setLongitude(longitude);
                    if (!acc_x.isEmpty())
                        ride.setAcc_x(acc_x);
                    if (!acc_y.isEmpty())
                        ride.setAcc_y(acc_y);
                    if (!acc_z.isEmpty())
                        ride.setAcc_z(acc_z);
                    if (!timestamp.isEmpty())
                        ride.setTimestamp(timestamp);
                    if (!acc_68.isEmpty())
                        ride.setAcc_68(acc_68);
                    if (!gyr_a.isEmpty())
                        ride.setGyr_a(gyr_a);
                    if (!gyr_b.isEmpty())
                        ride.setGyr_b(gyr_b);
                    if (!gyr_c.isEmpty())
                        ride.setGyr_c(gyr_c);
                    if (ride.getLatitude() != null || ride.getLongitude() != null || ride.getAcc_x() != null ||
                            ride.getAcc_y() != null || ride.getAcc_z() != null || ride.getTimestamp() != null ||
                            ride.getAcc_68() != null || ride.getGyr_a() != null || ride.getGyr_b() != null ||
                            ride.getGyr_c() != null) {
                        rides.add(ride);
                        //System.out.println("Ride added from file: " + f);
                        numberOfRides++;
                        if (numberOfRides % 100 == 0) System.out.println(numberOfRides + " rides readed");

                    }
                } else System.out.println("Filename: " + f + " is empty");
            }
        }

        return rides;

    }

    public List<WindowedRide> chopRides(List<Ride> rides) {
        List<WindowedRide> windowedRides = new ArrayList<>();
        WindowedRide ride;
        int i = 0;
        long[] timestamps;
        long timeslot = 0l;
        List<Integer> initIndexes, endIndexes;
        Long dt = 0l, iTs = 0l;
        int bikeType = 0, phoneLocation = 0, incidentType = 0;
        int numberOfWindowedRides = 0;
        int incidentSet = 0;

        for (Ride r : rides) {
            System.out.println(r.getDs_name());
            initIndexes = new ArrayList<>();
            endIndexes = new ArrayList<>();

            // Phone Location and Bike Type should not change between incidents
            bikeType = r.getIncidents().get(0).getBike();
            phoneLocation = r.getIncidents().get(0).getpLoc();

            i = 0;

            // We check that the ride is longer than the actual Windowframe span
            if (r.getTimestamp().get(r.getTimestamp().size() - 1) - r.getTimestamp().get(0) >= WINDOWFRAME) {
                timestamps = r.getTimestamp().stream().mapToLong(x -> x).toArray();
                initIndexes.add(i);
                for (int j = 1; j < timestamps.length; j++) {
                    System.out.println("r.getDs_name(): " + r.getDs_name());
                    dt = timestamps[j] - timestamps[i];
                    System.out.println(dt + "=" + timestamps[j] +" - " + timestamps[i]);
                    if (dt >= WINDOWFRAME) {
                        endIndexes.add(j);
                        j = i;
                        while ((timestamps[j] - timestamps[i]) < WINDOWFRAME / WINDOWSPLIT) {
                            j++;
                            //System.out.println("while");
                            //System.out.println("j: " + j + " | i:" + i);
                            //System.out.println((timestamps[j]-timestamps[i]));
                            if (j == timestamps.length - 1)
                                break;
                        }

                        j--;
                        i = j;
                        initIndexes.add(i);
                    } else if (j == timestamps.length - 1)
                        endIndexes.add(j);
                    //System.out.println(j);

                }
                int ii = 0, ei = 0;
                for (int k = 0; k < initIndexes.size(); k++) {
                    ii = initIndexes.get(k);
                    ei = endIndexes.get(k);

                    ride = new WindowedRide();

                    // Look if between the middle interval there is an incident
                    for (Incident incident : r.getIncidents()) {
                        iTs = incident.getTimestamp();

                        timeslot = (long) Math.ceil(timestamps[ei] - timestamps[ii]) / WINDOWSPLIT;

                        if (timestamps[ii] + timeslot <= iTs && timestamps[ei] - timeslot >= iTs) {
                            if (BINARYCLASSIFICATION) {
                                incidentType = 1;
                                System.out.println("Incident in window: " + (k + 1));
                            } else if (TERNARYCLASSIFICATION) {
                                if (incident.getIncident() == ELEMENTINTERNARYCLASSIFICATION)
                                    incidentType = 2;
                                else
                                    incidentType = 1;
                            } else
                                incidentType = incident.getIncident();
                            incidentSet++;
                            break;
                        } else
                            incidentType = 0;
                    }

                    ride.setDs_name(r.getDs_name());
                    ride.setPhoneLocation(phoneLocation);
                    ride.setBikeType(bikeType);
                    ride.setIncident(incidentType);
                    ride.setLatitude(r.getLatitude().subList(ii, ei));
                    ride.setLongitude(r.getLongitude().subList(ii, ei));
                    ride.setAcc_x(r.getAcc_x().subList(ii, ei));
                    ride.setAcc_y(r.getAcc_y().subList(ii, ei));
                    ride.setAcc_z(r.getAcc_z().subList(ii, ei));
                    ride.setTimestamp(r.getTimestamp().subList(ii, ei));
                    ride.setAcc_68(r.getAcc_68().subList(ii, ei));
                    ride.setGyr_a(r.getGyr_a().subList(ii, ei));
                    ride.setGyr_b(r.getGyr_b().subList(ii, ei));
                    ride.setGyr_c(r.getGyr_c().subList(ii, ei));

                    windowedRides.add(ride);
                    numberOfWindowedRides++;
                    if (numberOfWindowedRides % 1000 == 0)
                        System.out.println(numberOfWindowedRides + " windowed rides");
                }
            }
        }
        System.out.println(windowedRides.stream().map(x -> x).filter(x -> x.getIncident() != 0).count());
        System.out.println("Incidents set: " + incidentSet);

        return windowedRides;

    }

    public List<NNDataset> calculateStatistics(List<WindowedRide> rides) {
        List<NNDataset> nnDatasetList = new ArrayList<>();
        NNDataset nnDataset;
        long t0 = 0l, t1 = 0l, d = 0l;
        int numberOfNNDataset = 0;

        for (WindowedRide ride : rides) {
            nnDataset = new NNDataset();

            nnDataset.setSpeed(getSpeed(ride));
            nnDataset.setMean_acc_x(getMeanF(ride.getAcc_x()));
            nnDataset.setMean_acc_y(getMeanF(ride.getAcc_y()));
            nnDataset.setMean_acc_z(getMeanF(ride.getAcc_z()));
            nnDataset.setStd_acc_x(getStdDeviation(ride.getAcc_x()));
            nnDataset.setStd_acc_y(getStdDeviation(ride.getAcc_y()));
            nnDataset.setStd_acc_z(getStdDeviation(ride.getAcc_z()));
            nnDataset.setSma(getSma(ride.getAcc_x(), ride.getAcc_y(), ride.getAcc_z()));
            nnDataset.setMean_svm(getMeanD(getSvm(ride.getAcc_x(), ride.getAcc_y(), ride.getAcc_z())));

            t0 = System.nanoTime();

            calculateFFT(ride);
            nnDataset.setEntropyX(getEntropy(xFFT));
            nnDataset.setEntropyY(getEntropy(yFFT));
            nnDataset.setEntropyZ(getEntropy(zFFT));

            t1 = System.nanoTime();

            d += (t1 - t0);  //divide by 1000000 to get milliseconds.

            nnDataset.setBike_type(ride.getBikeType());
            nnDataset.setPhone_location(ride.getPhoneLocation());
            nnDataset.setIncident_type(ride.getIncident());

            nnDatasetList.add(nnDataset);
            numberOfNNDataset++;
            if (numberOfNNDataset % 1000 == 0) System.out.println(numberOfNNDataset + " NNDataset records added");

        }

        System.out.println("FFT elapsed time: " + d / 100000 + "ms.");
        return nnDatasetList;
    }

    public float getSpeed(WindowedRide ride) // m/s
    {
        double iniLat = ride.getLatitude().get(0);
        double iniLon = ride.getLongitude().get(0);
        double endLat = ride.getLatitude().get(ride.getLatitude().size() - 1);
        double endLon = ride.getLongitude().get(ride.getLongitude().size() - 1);
        long dt = (ride.getTimestamp().get(ride.getTimestamp().size() - 1) - ride.getTimestamp().get(0)) / 1000; // s

        double earthRadius = 6371000; //m (3958.75 miles)
        double dLat = Math.toRadians(endLat - iniLat);
        double dLon = Math.toRadians(endLon - iniLon);
        double sindLat = Math.sin(dLat / 2);
        double sindLon = Math.sin(dLon / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLon, 2)
                * Math.cos(Math.toRadians(iniLat)) * Math.cos(Math.toRadians(endLat));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (float) ((earthRadius * c) / dt);
    }

    public float getMeanF(List<Float> x) {
        return (float) x.stream().mapToDouble(i -> i).average().orElse(0);
    }

    public float getMeanD(List<Double> x) {
        return (float) x.stream().mapToDouble(i -> i).average().orElse(0);
    }

    public double getSma(List<Float> x, List<Float> y, List<Float> z) {
        double sma = 0;

        double xA[] = x.stream().mapToDouble(i -> i).toArray();
        double yA[] = y.stream().mapToDouble(i -> i).toArray();
        double zA[] = z.stream().mapToDouble(i -> i).toArray();

        if (xA.length == yA.length && yA.length == zA.length) {
            for (int i = 0; i < xA.length; i++) {
                sma += Math.abs(xA[i]) + Math.abs(yA[i]) + Math.abs(zA[i]);
            }
        }

        return sma / xA.length;
    }

    public List<Double> getSvm(List<Float> x, List<Float> y, List<Float> z) {
        int i = 0;
        List<Double> svm = new ArrayList<>();

        for (Float f : x) {
            double d = Math.sqrt(Math.pow(f, 2)) + Math.sqrt(Math.pow(y.get(i), 2)) + Math.sqrt(Math.pow(z.get(i), 2));
            svm.add(d);
            i++;
        }

        return svm;

    }

    public float getMean_svm(List<Float> x, List<Float> y, List<Float> z) {

        double xA[] = x.stream().mapToDouble(i -> i).toArray();
        double yA[] = y.stream().mapToDouble(i -> i).toArray();
        double zA[] = z.stream().mapToDouble(i -> i).toArray();

        double svm[] = new double[x.size()];

        for (int i = 0; i < xA.length; i++) {
            svm[i] = Math.sqrt(Math.pow(xA[i], 2) + Math.pow(yA[i], 2) + Math.pow(zA[i], 2));
        }

        return (float) Arrays.stream(svm).average().orElse(0);
    }

    public double getVariance(List<Float> x) {
        double variance = 0;

        double xA[] = x.stream().mapToDouble(i -> i).toArray();

        if (xA.length > 0) {
            float mean = getMeanF(x);

            for (int i = 0; i < xA.length; i++) {
                variance += Math.pow(xA[i] - mean, 2);
            }

            variance = variance / (float) xA.length;
        }

        return variance;
    }

    public float getStdDeviation(List<Float> x) {
        float stdDeviation = 0;

        if (x.size() > 0) {
            stdDeviation = (float) Math.sqrt(getVariance(x));
        }

        return stdDeviation;
    }

    public double getEnergy(double[] fftValues) {
        float energy = 0;

        // Calculate energy
        for (int i = 0; i < fftValues.length; i++) {
            energy += Math.pow(fftValues[i], 2);
        }

        if (fftValues.length > 0) {
            energy = energy / (float) fftValues.length;
        }

        return energy;
    }

    private double getEntropy(double[] fftValues) {
        double entropy = 0;
        double[] psd = new double[fftValues.length];

        if (fftValues.length > 0) {
            // Calculate Power Spectral Density
            for (int i = 0; i < fftValues.length; i++) {
                psd[i] = (Math.pow(fftValues[i], 2) / fftValues.length);
            }

            double psdSum = getSum(psd);

            if (psdSum > 0) {
                // Normalize calculated PSD so that it can be viewed as a Probability Density Function
                for (int i = 0; i < fftValues.length; i++) {
                    psd[i] = psd[i] / psdSum;
                }

                // Calculate the Frequency Domain Entropy
                for (int i = 0; i < fftValues.length; i++) {
                    if (psd[i] != 0) {
                        entropy += psd[i] * Math.log(psd[i]);
                    }
                }

                entropy *= -1;
            }
        }

        return entropy;

    }

    public double getSum(double[] values) {
        double sum = 0;

        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }

        return sum;
    }

    public void calculateFFT(WindowedRide r) {
        xFFT = r.getAcc_x().stream().mapToDouble(i -> i).toArray();
        yFFT = r.getAcc_y().stream().mapToDouble(i -> i).toArray();
        zFFT = r.getAcc_z().stream().mapToDouble(i -> i).toArray();

        fft = new DoubleFFT_1D(xFFT.length);

        fftDataX = new double[2 * xFFT.length];
        fftDataY = new double[2 * yFFT.length];
        fftDataZ = new double[2 * zFFT.length];

        for (int i = 0; i < xFFT.length; i++) {
            // copying data to the fft data buffer, imaginary part is 0
            fftDataX[2 * i] = xFFT[i];
            fftDataX[2 * i + 1] = 0;
            fftDataY[2 * i] = yFFT[i];
            fftDataY[2 * i + 1] = 0;
            fftDataZ[2 * i] = zFFT[i];
            fftDataZ[2 * i + 1] = 0;
        }

        fft.complexForward(fftDataX);
        fft.complexForward(fftDataY);
        fft.complexForward(fftDataZ);

    }

    public String writeCSVFile(String path, List<NNDataset> nnDataset) throws IOException {
        String filename;
        String line = "";
        int csvRecords = 0;

        filename = obtainFilename(path, "tmp");

        FileWriter writer = new FileWriter(filename);

        //line = "speed,mean_acc_x,mean_acc_y,mean_acc_z,std_acc_x,std_acc_y,std_acc_z,sma,mean_svm,entropyX,entropyY,entropyZ,bike_type,phone_location,incident_type\n";
        //writer.append(line);

        for (NNDataset l : nnDataset) {
            line = l.getSpeed() + ",";
            line += l.getMean_acc_x() + ",";
            line += l.getMean_acc_y() + ",";
            line += l.getMean_acc_z() + ",";
            line += l.getStd_acc_x() + ",";
            line += l.getStd_acc_y() + ",";
            line += l.getStd_acc_z() + ",";
            line += l.getSma() + ",";
            line += l.getMean_svm() + ",";
            line += l.getEntropyX() + ",";
            line += l.getEntropyY() + ",";
            line += l.getEntropyZ() + ",";
            line += l.getBike_type() + ",";
            line += l.getPhone_location() + ",";
            line += l.getIncident_type() + "\n";

            writer.append(line);

            csvRecords++;

            if (csvRecords % 1000 == 0) System.out.println(csvRecords + " csv records added");

        }

        writer.flush();
        writer.close();
        System.out.println(String.format("Csv records: %d", csvRecords));
        return filename;
    }

    public List<NNDataset> filterCategories(List<NNDataset> nndataset) {
        long[] countType = new long[9];
        long max = 0;
        List<NNDataset> outDataset = new ArrayList<>();

        countType[0] = nndataset.stream().map(x -> x).filter(x -> x.getIncident_type() == 0).count();
        countType[1] = nndataset.stream().map(x -> x).filter(x -> x.getIncident_type() == 1).count();
        countType[2] = nndataset.stream().map(x -> x).filter(x -> x.getIncident_type() == 2).count();
        countType[3] = nndataset.stream().map(x -> x).filter(x -> x.getIncident_type() == 3).count();
        countType[4] = nndataset.stream().map(x -> x).filter(x -> x.getIncident_type() == 4).count();
        countType[5] = nndataset.stream().map(x -> x).filter(x -> x.getIncident_type() == 5).count();
        countType[6] = nndataset.stream().map(x -> x).filter(x -> x.getIncident_type() == 6).count();
        countType[7] = nndataset.stream().map(x -> x).filter(x -> x.getIncident_type() == 7).count();
        countType[8] = nndataset.stream().map(x -> x).filter(x -> x.getIncident_type() == 8).count();

        for (int i = 1; i <= 8; i++) {
            if (max < countType[i])
                max = countType[i];
        }

        outDataset.addAll(nndataset.stream().filter(x -> x.getIncident_type() == 0).limit(max).collect(Collectors.toList()));
        outDataset.addAll(nndataset.stream().filter(x -> x.getIncident_type() == 1).collect(Collectors.toList()));
        outDataset.addAll(nndataset.stream().filter(x -> x.getIncident_type() == 2).collect(Collectors.toList()));
        outDataset.addAll(nndataset.stream().filter(x -> x.getIncident_type() == 3).collect(Collectors.toList()));
        outDataset.addAll(nndataset.stream().filter(x -> x.getIncident_type() == 4).collect(Collectors.toList()));
        outDataset.addAll(nndataset.stream().filter(x -> x.getIncident_type() == 5).collect(Collectors.toList()));
        outDataset.addAll(nndataset.stream().filter(x -> x.getIncident_type() == 6).collect(Collectors.toList()));
        outDataset.addAll(nndataset.stream().filter(x -> x.getIncident_type() == 7).collect(Collectors.toList()));
        outDataset.addAll(nndataset.stream().filter(x -> x.getIncident_type() == 8).collect(Collectors.toList()));

        return outDataset;
    }

    public boolean excludedIncident(int incident_type) {
        for (int i = 0; i < DISCARTEDINCIDENTS.length; i++)
            if (incident_type == DISCARTEDINCIDENTS[i]) return true;

        return false;

    }

    public long takeTSforUserTagIncidents(String[] incidentFields, String file) throws IOException {
        long ts = 1337l;
        double lat = Double.parseDouble(incidentFields[1]);
        double lon = Double.parseDouble(incidentFields[2]);
        boolean fileWithWrongFormat = false;
        boolean coorFound = false;

        FileReader r = new FileReader(file);
        BufferedReader br = new BufferedReader(r);

        // Read until header of the ride data
        String line = br.readLine();
        while (!line.equals("lat,lon,X,Y,Z,timeStamp,acc,a,b,c")) {
            line = br.readLine();
            if (line.equals("lat,lon,X,Y,Z,timeStamp")) {
                fileWithWrongFormat = true;
                break;
            }
        }

        if (!fileWithWrongFormat) {
            line = br.readLine();
            if (line != null) {
                String[] fileFields;

                // Read all the file
                while (line != null && !coorFound) {
                    fileFields = line.split(",", -1);
                    if (!fileFields[0].equals("")) {
                        if (Double.parseDouble(fileFields[0]) == lat &&
                                Double.parseDouble(fileFields[1]) == lon) {
                            ts = Long.parseLong(fileFields[5]);
                            coorFound = true;
                        }
                    }
                    line = br.readLine();
                }
            } else System.out.println("Filename: " + file + " is empty");
        }
        br.close();
        r.close();

        return ts;
    }

    public String obtainFilename(String path, String name) {
        String[] s, s1;
        String filename = "";
        boolean dsFile = false;
        int maxFile = 0;

        // To ensure any file is overwrited
        File f = new File(path);
        String[] files = f.list();
        for (int i = 0; i < files.length; i++) {
            if (new File(path + files[i]).isFile()) {
                s = files[i].split(name);
                if (s.length == 2) {
                    if (s[1].equals(".csv"))
                        dsFile = true;
                    else {
                        s1 = s[1].split(".csv");
                        if (s1[0].matches("^[0-9]*$"))
                            if (Integer.valueOf(s1[0]) > maxFile)
                                maxFile = Integer.valueOf(s1[0]);
                    }
                }
            }
        }

        maxFile++;

        filename = path + name + maxFile + ".csv";

        return filename;
    }

    public String mergeFiles(String path, String[] files) throws IOException {

        if (files.length == 0)
            return "";
        else {
            String filename = obtainFilename(path, "dataset");
            String line = "";
            FileWriter writer = new FileWriter(filename);
            BufferedReader br;
            File f;

            for (int i = 0; i < files.length; i++) {
                br = new BufferedReader(new FileReader(files[i]));
                line = br.readLine();
                while (line != null) {
                    writer.append(line + "\n");
                    line = br.readLine();
                }
                br.close();
                f = new File(files[i]);
                f.delete();
            }
            writer.flush();
            writer.close();


            return filename;
        }
    }

    public String[] splitDataset(String file, int trainPercentage) throws IOException {
        String[] filenames = {file.replace(".csv", "_train.csv"), file.replace(".csv", "_test.csv")};
        String line = "";
        int lines = 0;
        int lineNumber = 0, trainLines = 0;

        BufferedReader br = new BufferedReader(new FileReader(file));
        line = br.readLine();
        while (line != null) {
            lines++;
            line = br.readLine();
        }
        br.close();

        trainLines = lines * trainPercentage / 100;

        FileWriter writerTrain = new FileWriter(filenames[0]);
        FileWriter writerTest = new FileWriter(filenames[1]);

        br = new BufferedReader(new FileReader(file));
        line = br.readLine();

        while (line != null) {
            if (lineNumber < trainLines) writerTrain.append(line + "\n");
            else writerTest.append(line + "\n");

            line = br.readLine();
            lineNumber++;
        }
        br.close();
        writerTrain.flush();
        writerTrain.close();
        writerTest.flush();
        writerTest.close();


        return filenames;

    }
}
