package com.itdev.mapper;

import com.itdev.dao.entity.StatcheckResult;
import com.itdev.dao.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatcheckResultMapper implements Mapper<StatcheckResult, StatcheckResult> {

    private final ResultRepository resultRepository;

    @Override
    public StatcheckResult map(StatcheckResult fromObject, StatcheckResult toObject) {
        copy(fromObject, toObject);
        return toObject;
    }

    @Override
    public StatcheckResult map(StatcheckResult fromObject) {
        StatcheckResult toObject = new StatcheckResult();
        copy(fromObject, toObject);
        return toObject;
    }

    private void copy(StatcheckResult fromObject, StatcheckResult toObject) {
        toObject.setAfterProc(fromObject.getAfterProc());
        toObject.setSource(fromObject.getSource());
        toObject.setType(fromObject.getType());
        toObject.setDf1(fromObject.getDf1());
        toObject.setDf2(fromObject.getDf2());
        toObject.setTestValue(fromObject.getTestValue());
        toObject.setPComparison(fromObject.getPComparison());
        toObject.setReportedP(fromObject.getReportedP());
        toObject.setComputedP(fromObject.getComputedP());
        toObject.setError(fromObject.getError());
        toObject.setDecisionError(fromObject.getDecisionError());
        toObject.setOneTailed(fromObject.getOneTailed());
        toObject.setApaFactor(fromObject.getApaFactor());
        if (fromObject.getResult() != null) {
            toObject.setResult(resultRepository.findById(fromObject.getResult().getId()).orElseThrow());
        }
    }
}
