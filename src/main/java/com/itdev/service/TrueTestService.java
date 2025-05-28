package com.itdev.service;

import com.itdev.dao.entity.TrueTest;
import com.itdev.dao.repository.TrueTestRepository;
import com.itdev.mapper.TrueTestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrueTestService {
    
    private final TrueTestRepository trueTestRepository;
    private final TrueTestMapper trueTestMapper;

    public List<TrueTest> findAll() {
        return trueTestRepository.findAll();
    }

    public Optional<TrueTest> findById(Integer id) {
        return trueTestRepository.findById(id);
    }

    @Transactional
    public Optional<TrueTest> update(Integer id, TrueTest fromObj) {
        return trueTestRepository.findById(id)
                .map(toObj -> trueTestMapper.map(fromObj, toObj))
                .map(trueTestRepository::saveAndFlush);
    }

    @Transactional
    public boolean delete(Integer id) {
        return trueTestRepository.findById(id)
                .map(entity -> {
                    trueTestRepository.delete(entity);
                    trueTestRepository.flush();
                    return true;
                })
                .orElse(false);
    }
}
