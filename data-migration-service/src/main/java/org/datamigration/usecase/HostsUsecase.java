package org.datamigration.usecase;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.HostEntity;
import org.datamigration.mapper.HostMapper;
import org.datamigration.model.HostModel;
import org.datamigration.service.HostsService;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HostsUsecase {

    private final HostMapper hostMapper = Mappers.getMapper(HostMapper.class);
    private final HostsService hostsService;

    public HostModel createOrUpdateHost(HostModel host) {
        final HostEntity hostEntity = hostMapper.hostToHostEntity(host);
        hostEntity.getDatabases().forEach(databaseEntity -> databaseEntity.setHost(hostEntity));
        return Optional.of(hostEntity)
                .map(hostsService::createOrUpdate)
                .map(hostMapper::hostEntityToHost)
                .orElse(null);
    }

    public Set<HostModel> getAllHosts() {
        return hostsService.getAll().stream()
                .map(hostMapper::hostEntityToHost)
                .collect(Collectors.toSet());
    }

    public void deleteHost(UUID hostId) {
        hostsService.delete(hostId);
    }

}
