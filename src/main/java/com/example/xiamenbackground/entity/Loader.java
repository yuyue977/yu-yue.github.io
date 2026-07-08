package com.example.xiamenbackground.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "LoaderManagement")
public class Loader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "LoaderNumber", nullable = false, length = 200)
    private String loaderNumber;

    @Column(name = "LoaderModel", nullable = false, length = 20)
    private String loaderModel;

    @Column(name = "LoaderBucketCapacity", nullable = false)
    private Float loaderBucketCapacity;

    @Column(name = "DeviceID", nullable = false, length = 20)
    private String deviceId;

    @Column(name = "Battery", nullable = false)
    private Integer battery;

    @Column(name = "Status", length = 20)
    private String status = "待机";

    @Column(name = "Remain")
    private Float remain;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLoaderNumber() { return loaderNumber; }
    public void setLoaderNumber(String loaderNumber) { this.loaderNumber = loaderNumber; }
    public String getLoaderModel() { return loaderModel; }
    public void setLoaderModel(String loaderModel) { this.loaderModel = loaderModel; }
    public Float getLoaderBucketCapacity() { return loaderBucketCapacity; }
    public void setLoaderBucketCapacity(Float loaderBucketCapacity) { this.loaderBucketCapacity = loaderBucketCapacity; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public Integer getBattery() { return battery; }
    public void setBattery(Integer battery) { this.battery = battery; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Float getRemain() { return remain; }
    public void setRemain(Float remain) { this.remain = remain; }
}
