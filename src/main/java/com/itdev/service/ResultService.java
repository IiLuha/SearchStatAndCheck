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

    public List<Result> findAll() {
        return resultRepository.findAll();
    }

    public Optional<Result> findById(Integer id) {
        return resultRepository.findById(id);
    }

    @Transactional
    public Result create(Result userDto) {
        return Optional.of(userDto)
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
