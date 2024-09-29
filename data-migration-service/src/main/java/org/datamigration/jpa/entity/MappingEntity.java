package org.datamigration.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MappingEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private Date createdDate;

    private boolean finished;

    private boolean locked;

    private boolean delete;

    private long lastProcessedBatch;

    @NotNull
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> mapping;

    @OneToMany(mappedBy = "mapping", fetch = FetchType.LAZY)
    private Set<MappingItemStatusEntity> items = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private HostEntity host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scope_id", nullable = false)
    private ScopeEntity scope;

}
