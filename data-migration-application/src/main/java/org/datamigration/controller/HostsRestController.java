package org.datamigration.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.datamigration.model.HostModel;
import org.datamigration.usecase.HostsUsecase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Tag(name = "/hosts")
@RestController
@RequestMapping("${server.root.path}/hosts")
@RequiredArgsConstructor
public class HostsRestController {

    private final HostsUsecase hostsUsecase;

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @PutMapping
    public HostModel createOrUpdateHost(@RequestBody @Valid HostModel hostModel) {
        return hostsUsecase.createOrUpdateHost(hostModel);
    }

    @PreAuthorize("containsAnyAuthority('ROLE_SUPER_USER')")
    @GetMapping
    public Set<HostModel> getHosts() {
        return hostsUsecase.getAllHosts();
    }

}