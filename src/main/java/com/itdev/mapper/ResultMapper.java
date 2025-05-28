package com.itdev.mapper;

import com.itdev.dao.entity.Result;
import org.springframework.stereotype.Component;

@Component
public class ResultMapper implements Mapper<Result, Result>{

    @Override
    public Result map(Result fromObject, Result toObject) {
        copy(fromObject, toObject);
        return toObject;
    }

    @Override
    public Result map(Result fromObject) {
        Result toObject = new Result();
        copy(fromObject, toObject);
        return toObject;
    }

    private void copy(Result fromObject, Result toObject) {
        toObject.setValid(fromObject.isValid());
        toObject.setSubjectDomain(fromObject.getSubjectDomain());
        toObject.setEnvironment(fromObject.getEnvironment());
        toObject.setGenPrompt(fromObject.getGenPrompt());
        toObject.setGenAnswer(fromObject.getGenAnswer());
        toObject.setSearchAnswer(fromObject.getSearchAnswer());
        if (fromObject.getStatcheckResults() != null) {
            toObject.addStatcheckResults(fromObject.getStatcheckResults());
        }
        if (fromObject.getTrueTests() != null) {
            toObject.addTrueTests(fromObject.getTrueTests());
        }
    }
}
