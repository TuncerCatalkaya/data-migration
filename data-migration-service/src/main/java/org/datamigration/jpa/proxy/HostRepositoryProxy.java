package org.datamigration.jpa.proxy;

import org.datamigration.domain.model.HostModel;
import org.datamigration.domain.repository.HostRepository;
import org.datamigration.jpa.repository.JpaHostRepository;
import org.datamigration.mapper.HostMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HostRepositoryProxy implements HostRepository {

    private final HostMapper hostMapper = Mappers.getMapper(HostMapper.class);
    private final JpaHostRepository jpaHostRepository;

    @Transactional
    @Override
    public HostModel save(HostModel host) {
        return Optional.of(host)
                .map(hostMapper::hostToHostEntity)
                .map(jpaHostRepository::save)
                .map(hostMapper::hostEntityToHost)
                .orElse(null);
    }

    @Transactional
    @Override
    public void deleteById(UUID id) {
        jpaHostRepository.deleteById(id);
    }

}
