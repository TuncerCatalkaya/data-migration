package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.exception.DuplicateHostException;
import org.datamigration.jpa.entity.DatabaseEntity;
import org.datamigration.jpa.entity.HostEntity;
import org.datamigration.jpa.repository.JpaDatabaseRepository;
import org.datamigration.jpa.repository.JpaHostRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HostsService {

    private final JpaHostRepository jpaHostRepository;
    private final JpaDatabaseRepository jpaDatabaseRepository;

    public HostEntity createOrUpdate(HostEntity hostEntity) {
        if (jpaHostRepository.existsByUrlWithCount(hostEntity.getUrl(), hostEntity.getId())) {
            throw new DuplicateHostException("Host url " + hostEntity.getUrl() + " already exists.");
        }
        return jpaHostRepository.save(hostEntity);
    }

    public List<HostEntity> getAll() {
        return jpaHostRepository.findAll().stream()
                .map(hostEntity -> {
                    final Set<DatabaseEntity> databasesEntity = jpaDatabaseRepository.findAllByHost_Id(hostEntity.getId());
                    hostEntity.setDatabases(databasesEntity);
                    return hostEntity;
                })
                .toList();
    }

    public void delete(UUID hostId) {
        jpaHostRepository.deleteById(hostId);
    }
}
