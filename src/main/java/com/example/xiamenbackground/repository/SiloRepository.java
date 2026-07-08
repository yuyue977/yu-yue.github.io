package com.example.xiamenbackground.repository;

import com.example.xiamenbackground.entity.Silo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SiloRepository extends JpaRepository<Silo, Integer>, JpaSpecificationExecutor<Silo> {
}
