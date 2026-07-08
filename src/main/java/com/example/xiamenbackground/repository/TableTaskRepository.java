package com.example.xiamenbackground.repository;

import com.example.xiamenbackground.entity.TableTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务表 Repository
 */
@Repository
public interface TableTaskRepository extends JpaRepository<TableTask, Integer> {

    /**
     * 查询指定料仓在时间范围内的作业量总和
     * @param cangId 料仓ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 作业量总和
     */
    @Query("SELECT SUM(t.volume) FROM TableTask t WHERE t.cang = ?1 AND t.endTime >= ?2 AND t.endTime <= ?3")
    Float sumVolumeByCangAndTimeRange(Integer cangId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询指定料斗在时间范围内的作业量总和
     * @param douId 料斗ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 作业量总和
     */
    @Query("SELECT SUM(t.volume) FROM TableTask t WHERE t.dou = ?1 AND t.endTime >= ?2 AND t.endTime <= ?3")
    Float sumVolumeByDouAndTimeRange(Integer douId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询指定料仓在时间范围内的所有任务记录
     * @param cangId 料仓ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 任务列表
     */
    List<TableTask> findByCangAndEndTimeBetween(Integer cangId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询指定料斗在时间范围内的所有任务记录
     * @param douId 料斗ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 任务列表
     */
    List<TableTask> findByDouAndEndTimeBetween(Integer douId, LocalDateTime startTime, LocalDateTime endTime);
}
