package org.datamigration.usecase;

import org.datamigration.domain.model.HostModel;
import org.datamigration.domain.service.HostService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Transactional
@Component
@RequiredArgsConstructor
public class CreateHostUsecase {

    private final HostService hostService;

    public HostModel createOrUpdate(HostModel host) {
        return hostService.createOrUpdateHost(host);
    }

}
