package com.itdev.dao.repository;

import com.itdev.dao.entity.Result;
import com.itdev.enums.Environment;
import com.itdev.enums.SubjectDomain;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface ResultRepository extends JpaRepository<Result, Integer> {

    @EntityGraph(attributePaths = {"statcheckResults"})
    List<Result> findAllByEnvironment(Environment environment);
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"statcheckResults"})
    List<Result> findAll();

    List<Result> findAllBySubjectDomain(SubjectDomain subjectDomain);
    List<Result> findAllByValid(Boolean valid);

}
