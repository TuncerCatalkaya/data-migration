package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.ItemEntity;
import org.datamigration.jpa.repository.JpaItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemsService {

    private final JpaItemRepository jpaItemRepository;

    public Page<ItemEntity> getAll(UUID scopeId, Pageable pageable) {
        return jpaItemRepository.findAllByScope_Id(scopeId, pageable);
    }

}
