package com.itdev.dao.entity;

import com.itdev.enums.TestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;


@Entity
@Table(name = "statcheck_result")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString(exclude = {"result"})
@Builder
public class StatcheckResult implements BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
//    @JoinColumn(name = "result_id")
    private Result result;

    private boolean afterProc; // after processing LLM
    private String source;
    @Enumerated(EnumType.STRING)
    private TestType type;
    private Integer df1;
    private Integer df2;
    private BigDecimal testValue;
    private String pComparison;
    private BigDecimal reportedP;
    private BigDecimal computedP;
    private boolean error;
    private boolean decision_error;
    private boolean oneTailed;
    private int apaFactor;
}
