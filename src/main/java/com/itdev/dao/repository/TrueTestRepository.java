package com.itdev.dao.repository;

import com.itdev.dao.entity.Result;
import com.itdev.dao.entity.TrueTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrueTestRepository extends JpaRepository<TrueTest, Integer> {

    List<Result> findAllByResultId(Integer resultId);
}
