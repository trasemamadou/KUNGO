package com.kungo.professional.domain.entity;

import com.kungo.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity
@Table(name = "languages")
public class Language extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 5)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;
}
