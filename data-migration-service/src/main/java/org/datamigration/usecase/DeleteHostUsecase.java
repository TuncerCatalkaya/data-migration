package org.datamigration.usecase;

import org.datamigration.domain.service.HostService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Transactional
@Component
@RequiredArgsConstructor
public class DeleteHostUsecase {

    private final HostService hostService;

    public void delete(UUID hostId) {
        hostService.deleteHost(hostId);
    }

}
