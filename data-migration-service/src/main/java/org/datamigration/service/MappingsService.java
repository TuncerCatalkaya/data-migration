package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.MappingEntity;
import org.datamigration.jpa.repository.JpaMappingRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MappingsService {

    private final JpaMappingRepository jpaMappingRepository;

    public MappingEntity createNewMapping(MappingEntity mappingEntity, String[] headers) {
        final Map<String, String> mapping = new HashMap<>();
        for (String header : headers) {
            mapping.put(header, header);
        }
        mappingEntity.setCreatedDate(new Date());
        mappingEntity.setFinished(false);
        mappingEntity.setLocked(false);
        mappingEntity.setDelete(false);
        mappingEntity.setLastProcessedBatch(-1);
        mappingEntity.setMapping(mapping);
        return jpaMappingRepository.save(mappingEntity);
    }

}
