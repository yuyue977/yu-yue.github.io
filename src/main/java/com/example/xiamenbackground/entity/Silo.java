package com.example.xiamenbackground.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CangManagement")
public class Silo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "CangID", nullable = false, length = 20)
    private String cangId;

    @Column(name = "MaterialInfo", nullable = false, length = 255)
    private String materialInfo;

    @Column(name = "Remain")
    private Float remain;

    @Column(name = "IsLocked", nullable = false)
    private Boolean isLocked = false;

    @Column(name = "CangCapacity")
    private Float cangCapacity;

    @Column(name = "CangStatus", nullable = false)
    private Boolean cangStatus = true;

    @Column(name = "Status", nullable = false)
    private String status = "启用";

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCangId() { return cangId; }
    public void setCangId(String cangId) { this.cangId = cangId; }
    public String getMaterialInfo() { return materialInfo; }
    public void setMaterialInfo(String materialInfo) { this.materialInfo = materialInfo; }
    public Float getRemain() { return remain; }
    public void setRemain(Float remain) { this.remain = remain; }
    public Boolean getIsLocked() { return isLocked; }
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }
    public Float getCangCapacity() { return cangCapacity; }
    public void setCangCapacity(Float cangCapacity) { this.cangCapacity = cangCapacity; }
    public Boolean getCangStatus() { return cangStatus; }
    public void setCangStatus(Boolean cangStatus) { this.cangStatus = cangStatus; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
