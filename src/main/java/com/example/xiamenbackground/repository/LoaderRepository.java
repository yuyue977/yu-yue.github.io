package com.example.xiamenbackground.repository;

import com.example.xiamenbackground.entity.Loader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoaderRepository extends JpaRepository<Loader, Integer>, JpaSpecificationExecutor<Loader> {
}
