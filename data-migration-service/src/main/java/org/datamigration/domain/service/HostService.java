package org.datamigration.domain.service;

import org.datamigration.domain.model.HostModel;
import org.datamigration.domain.repository.HostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HostService {

    private final HostRepository hostRepository;

    public HostModel createOrUpdateHost(HostModel host) {
        return hostRepository.save(host);
    }

    public void deleteHost(UUID hostId) {
        hostRepository.deleteById(hostId);
    }

}
