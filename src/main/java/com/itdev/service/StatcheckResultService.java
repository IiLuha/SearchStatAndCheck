package com.itdev.service;

import com.itdev.dao.entity.StatcheckResult;
import com.itdev.dao.repository.StatcheckResultRepository;
import com.itdev.mapper.StatcheckResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatcheckResultService {

    private final StatcheckResultRepository statcheckResultRepository;
    private final StatcheckResultMapper statcheckResultMapper;

    public List<StatcheckResult> findAll() {
        return statcheckResultRepository.findAll();
    }

    public Optional<StatcheckResult> findById(Integer id) {
        return statcheckResultRepository.findById(id);
    }

    @Transactional
    public Optional<StatcheckResult> update(Integer id, StatcheckResult fromObj) {
        return statcheckResultRepository.findById(id)
                .map(toObj -> statcheckResultMapper.map(fromObj, toObj))
                .map(statcheckResultRepository::saveAndFlush);
    }

    @Transactional
    public boolean delete(Integer id) {
        return statcheckResultRepository.findById(id)
                .map(entity -> {
                    statcheckResultRepository.delete(entity);
                    statcheckResultRepository.flush();
                    return true;
                })
                .orElse(false);
    }
}
