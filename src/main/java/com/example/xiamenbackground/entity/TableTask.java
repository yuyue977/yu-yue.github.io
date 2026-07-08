package com.example.xiamenbackground.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 任务表实体类 - 对应数据库 table_task 表
 */
@Entity
@Table(name = "table_task")
public class TableTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 料仓ID
     */
    @Column(name = "cang", nullable = false)
    private Integer cang;

    /**
     * 料斗ID
     */
    @Column(name = "dou", nullable = false)
    private Integer dou;

    /**
     * 作业体积（立方米）
     */
    @Column(name = "volume")
    private Float volume;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCang() {
        return cang;
    }

    public void setCang(Integer cang) {
        this.cang = cang;
    }

    public Integer getDou() {
        return dou;
    }

    public void setDou(Integer dou) {
        this.dou = dou;
    }

    public Float getVolume() {
        return volume;
    }

    public void setVolume(Float volume) {
        this.volume = volume;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
