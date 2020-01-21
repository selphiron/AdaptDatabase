/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptdatabase.entities;

import java.util.List;

/**
 *
 * @author AlbertSanchez
 */
public class SimraDatasetRide {
    
    private SimraDatasetIncident incidentDetail;
    private List<Double> latitude;
    private List<Double> longitude;
    private List<Float> acc_x;
    private List<Float> acc_y;
    private List<Float> acc_z;
    private List<Long> timestamp;
    private List<Float> acc_68;
    private List<Float> gyr_a;
    private List<Float> gyr_b;
    private List<Float> gyr_c;

    public SimraDatasetRide()
    {
        
    }
    
    public SimraDatasetRide(SimraDatasetIncident incidentDetail, List<Double> latitude, List<Double> longitude, List<Float> acc_x, List<Float> acc_y, List<Float> acc_z, List<Long> timestamp, List<Float> acc_68, List<Float> gyr_a, List<Float> gyr_b, List<Float> gyr_c) {
        this.incidentDetail = incidentDetail;
        this.latitude = latitude;
        this.longitude = longitude;
        this.acc_x = acc_x;
        this.acc_y = acc_y;
        this.acc_z = acc_z;
        this.timestamp = timestamp;
        this.acc_68 = acc_68;
        this.gyr_a = gyr_a;
        this.gyr_b = gyr_b;
        this.gyr_c = gyr_c;
    }

    public SimraDatasetIncident getSimraDatasetIncident() {
        return incidentDetail;
    }

    public void setSimraDatasetIncident(SimraDatasetIncident incidentDetail) {
        this.incidentDetail = incidentDetail;
    }

    public List<Double> getLatitude() {
        return latitude;
    }

    public void setLatitude(List<Double> latitude) {
        this.latitude = latitude;
    }

    public List<Double> getLongitude() {
        return longitude;
    }

    public void setLongitude(List<Double> longitude) {
        this.longitude = longitude;
    }

    public List<Float> getAcc_x() {
        return acc_x;
    }

    public void setAcc_x(List<Float> acc_x) {
        this.acc_x = acc_x;
    }

    public List<Float> getAcc_y() {
        return acc_y;
    }

    public void setAcc_y(List<Float> acc_y) {
        this.acc_y = acc_y;
    }

    public List<Float> getAcc_z() {
        return acc_z;
    }

    public void setAcc_z(List<Float> acc_z) {
        this.acc_z = acc_z;
    }

    public List<Long> getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(List<Long> timestamp) {
        this.timestamp = timestamp;
    }

    public List<Float> getAcc_68() {
        return acc_68;
    }

    public void setAcc_68(List<Float> acc_68) {
        this.acc_68 = acc_68;
    }

    public List<Float> getGyr_a() {
        return gyr_a;
    }

    public void setGyr_a(List<Float> gyr_a) {
        this.gyr_a = gyr_a;
    }

    public List<Float> getGyr_b() {
        return gyr_b;
    }

    public void setGyr_b(List<Float> gyr_b) {
        this.gyr_b = gyr_b;
    }

    public List<Float> getGyr_c() {
        return gyr_c;
    }

    public void setGyr_c(List<Float> gyr_c) {
        this.gyr_c = gyr_c;
    }    
    
}
