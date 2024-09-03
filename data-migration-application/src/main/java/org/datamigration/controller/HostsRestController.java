package org.datamigration.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.datamigration.domain.model.HostModel;
import org.datamigration.usecase.HostsUsecase;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "/hosts")
@RestController
@RequestMapping("${server.root.path}/hosts")
@RequiredArgsConstructor
public class HostsRestController {

    private final HostsUsecase hosts;

    @PutMapping
    public HostModel createOrUpdateHost(@RequestBody HostModel host) {
        return hosts.createOrUpdate(host);
    }

    @DeleteMapping("/{hostId}")
    public void deleteHost(@PathVariable UUID hostId) {
        hosts.delete(hostId);
    }

}
