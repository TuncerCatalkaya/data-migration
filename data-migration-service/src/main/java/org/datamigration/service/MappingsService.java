package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.MappingEntity;
import org.datamigration.jpa.repository.JpaMappingRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MappingsService {

    private final JpaMappingRepository jpaMappingRepository;

    public MappingEntity createNewMapping(MappingEntity mappingEntity) {
        return jpaMappingRepository.save(mappingEntity);
    }

}
