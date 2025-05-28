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
import javax.persistence.ManyToOne;
import java.math.BigDecimal;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString(exclude = {"result"})
@Builder
public class TrueTest implements BaseEntity<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    private Result result;

    @Enumerated(EnumType.STRING)
    private TestType type;
    private boolean oneTailed;
    private BigDecimal testValue;
    private Integer df1;
    private Integer df2;
    private BigDecimal pValue;
    private boolean consistent;
    private boolean isEquality;
}
