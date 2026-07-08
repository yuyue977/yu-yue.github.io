package com.example.xiamenbackground.repository;

import com.example.xiamenbackground.entity.Hopper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface HopperRepository extends JpaRepository<Hopper, Integer>, JpaSpecificationExecutor<Hopper> {
}
