package com.itdev.dao.repository;

import com.itdev.dao.entity.Result;
import com.itdev.enums.Environment;
import com.itdev.enums.SubjectDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Integer> {

    List<Result> findAllByEnvironment(Environment environment);
    List<Result> findAllBySubjectDomain(SubjectDomain subjectDomain);
}
