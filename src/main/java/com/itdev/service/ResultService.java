package com.itdev.service;

import com.itdev.dao.entity.Result;
import com.itdev.dao.entity.StatcheckResult;
import com.itdev.dao.entity.TrueTest;
import com.itdev.dao.repository.ResultRepository;
import com.itdev.dao.repository.StatcheckResultRepository;
import com.itdev.dao.repository.TrueTestRepository;
import com.itdev.enums.Environment;
import com.itdev.mapper.ResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultService {

    private final ResultRepository resultRepository;
    private final ResultMapper resultMapper;
    private final StatcheckResultRepository statcheckResultRepository;
    private final TrueTestRepository trueTestRepository;

    public List<Result> findAllByEnvironment(Environment environment) {
        return resultRepository.findAllByEnvironment(environment);
    }

    public List<Result> findAllByEnvironmentWithLists(Environment environment) {
        List<Result> allByEnvironment = resultRepository.findAllByEnvironment(environment);
        final Integer n = -1;
        System.out.println("begin");
        allByEnvironment.forEach(result -> {
            n.compareTo(result.getTrueTests().size());
        });
        System.out.println("end");
        return allByEnvironment;
    }

    public List<Result> findAllByValid(Boolean valid) {
        return resultRepository.findAllByValid(valid);
    }

    public List<Result> findAll() {
        return resultRepository.findAll();
    }

    public List<Result> findAllWithLists() {
        List<Result> all = resultRepository.findAll();
        final Integer n = -1;
        System.out.println("begin");
        all.forEach(result -> {
            n.compareTo(result.getTrueTests().size());
        });
        System.out.println("end");
        return all;
    }

    public Optional<Result> findById(Integer id) {
        return resultRepository.findById(id);
    }

    @Transactional
    public Result create(Result result) {
        return Optional.of(result)
                .map(resultRepository::save)
                .orElseThrow();
    }

    @Transactional
    public Optional<Result> update(Integer id, Result fromObj) {
        return resultRepository.findById(id)
                .map(toObj -> resultMapper.map(fromObj, toObj))
                .map(resultRepository::saveAndFlush);
    }

    @Transactional
    public boolean delete(Integer id) {
        return resultRepository.findById(id)
                .map(entity -> {
                    resultRepository.delete(entity);
                    resultRepository.flush();
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public Optional<Result> addStatcheckResult(StatcheckResult res) {
        return Optional.of(statcheckResultRepository.save(res))
                .flatMap(statcheckResult ->
                        Optional.of(statcheckResult.getResult())
                                .map(result -> {
                                    result.addStatcheckResult(statcheckResult);
                                    resultRepository.saveAndFlush(result);
                                    return result;
                                })
                );
    }

    @Transactional
    public Optional<Result> addTrueTest(TrueTest test) {
        return Optional.of(trueTestRepository.save(test))
                .flatMap(trueTest ->
                        Optional.of(trueTest.getResult())
                                .map(result -> {
                                    result.addTrueTest(trueTest);
                                    resultRepository.saveAndFlush(result);
                                    return result;
                                })
                );
    }
}
