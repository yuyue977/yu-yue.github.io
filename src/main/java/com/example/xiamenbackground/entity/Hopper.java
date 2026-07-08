package com.example.xiamenbackground.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "DouManagement")
public class Hopper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "DouID", nullable = false)
    private Float douId;

    @Column(name = "MaterialInfo", nullable = false, length = 255)
    private String materialInfo;

    @Column(name = "Remain")
    private Float remain;

    @Column(name = "DouCapacity")
    private Float douCapacity;

    @Column(name = "DouStatus", nullable = false)
    private Boolean douStatus = false;

    @Column(name = "IsLocked")
    private String isLocked;

    @Column(name = "Status", nullable = false)
    private String status = "启用";

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Float getDouId() { return douId; }
    public void setDouId(Float douId) { this.douId = douId; }
    public String getMaterialInfo() { return materialInfo; }
    public void setMaterialInfo(String materialInfo) { this.materialInfo = materialInfo; }
    public Float getRemain() { return remain; }
    public void setRemain(Float remain) { this.remain = remain; }

    public Float getDouCapacity() { return douCapacity; }
    public void setDouCapacity(Float douCapacity) { this.douCapacity = douCapacity; }
    public Boolean getDouStatus() { return douStatus; }
    public void setDouStatus(Boolean douStatus) { this.douStatus = douStatus; }

    public String getIsLocked() { return isLocked; }
    public void setIsLocked(String isLocked) { this.isLocked = isLocked; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

}
