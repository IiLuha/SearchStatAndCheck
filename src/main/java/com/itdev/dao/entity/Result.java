package com.itdev.dao.entity;

import com.itdev.enums.Environment;
import com.itdev.enums.SubjectDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.lang.NonNull;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@EqualsAndHashCode(exclude = {"trueTests", "statcheckResults"})
@ToString(exclude = {"trueTests", "statcheckResults"})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result implements BaseEntity<Integer>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Boolean valid;
    @Enumerated(EnumType.STRING)
    private SubjectDomain subjectDomain;
    @Enumerated(EnumType.STRING)
    private Environment environment;
    private String genPrompt;
    private String genAnswer;
    private String searchAnswer;
    private String resultTable;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TrueTest> trueTests = new ArrayList<>();

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StatcheckResult> statcheckResults = new ArrayList<>();

    public void addTrueTest(@NonNull TrueTest trueTest) {
        trueTests.add(trueTest);
        trueTest.setResult(this);
    }

    public void addStatcheckResult(@NonNull StatcheckResult statcheckResult) {
        statcheckResults.add(statcheckResult);
        statcheckResult.setResult(this);
    }

    public void addTrueTests(List<TrueTest> trueTests) {
        this.trueTests.addAll(trueTests);
        trueTests.forEach(it -> it.setResult(this));
    }

    public void addStatcheckResults(List<StatcheckResult> statcheckResults) {
        this.statcheckResults.addAll(statcheckResults);
        statcheckResults.forEach(it -> it.setResult(this));
    }
}
