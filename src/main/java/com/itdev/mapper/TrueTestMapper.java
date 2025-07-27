package com.itdev.mapper;

import com.itdev.dao.entity.TrueTest;
import com.itdev.dao.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrueTestMapper implements Mapper<TrueTest, TrueTest>{

    private final ResultRepository resultRepository;

    @Override
    public TrueTest map(TrueTest fromObject, TrueTest toObject) {
        copy(fromObject, toObject);
        return toObject;
    }

    @Override
    public TrueTest map(TrueTest fromObject) {
        TrueTest toObject = new TrueTest();
        copy(fromObject, toObject);
        return toObject;
    }

    private void copy(TrueTest fromObject, TrueTest toObject) {
        toObject.setType(fromObject.getType());
        toObject.setOneTailed(fromObject.getOneTailed());
        toObject.setTestValue(fromObject.getTestValue());
        toObject.setDf1(fromObject.getDf1());
        toObject.setDf2(fromObject.getDf2());
        toObject.setPValue(fromObject.getPValue());
        toObject.setConsistent(fromObject.getConsistent());
        toObject.setEquality(fromObject.getEquality());
        if (fromObject.getResult() != null) {
            toObject.setResult(resultRepository.findById(fromObject.getResult().getId()).orElseThrow());
        }
    }
}
