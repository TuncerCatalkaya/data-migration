package org.datamigration.jpa.proxy;

import org.datamigration.domain.repository.ItemRepository;
import org.datamigration.jpa.repository.JpaItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ItemRepositoryProxy implements ItemRepository {

    private final JpaItemRepository jpaItemRepository;

    @Transactional
    @Override
    public void deleteAllById(List<UUID> itemIds) {
        jpaItemRepository.deleteAllById(itemIds);
    }
}
