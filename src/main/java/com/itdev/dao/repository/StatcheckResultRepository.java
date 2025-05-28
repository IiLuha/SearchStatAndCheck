package com.itdev.dao.repository;

import com.itdev.dao.entity.Result;
import com.itdev.dao.entity.StatcheckResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatcheckResultRepository extends JpaRepository<StatcheckResult, Integer> {

    List<Result> findAllByResultId(Integer resultId);
}
