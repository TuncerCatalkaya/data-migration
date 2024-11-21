package org.datamigration.service;

import lombok.RequiredArgsConstructor;
import org.datamigration.exception.MappingNotFoundException;
import org.datamigration.exception.MappingValidationException;
import org.datamigration.jpa.entity.MappingEntity;
import org.datamigration.jpa.repository.JpaMappingRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MappingsService {

    private final JpaMappingRepository jpaMappingRepository;

    public MappingEntity createOrUpdateMapping(MappingEntity mappingEntity) {
        return jpaMappingRepository.save(mappingEntity);
    }

    public List<MappingEntity> getAll() {
        return jpaMappingRepository.findAllByDeleteFalse(Sort.by(Sort.Direction.ASC, "createdDate"));
    }

    public List<MappingEntity> getAll(UUID scopeId) {
        return jpaMappingRepository.findAllByScope_IdAndDeleteFalse(scopeId, Sort.by(Sort.Direction.ASC, "createdDate"));
    }

    public MappingEntity get(UUID mappingId) {
        return jpaMappingRepository.findById(mappingId)
                .orElseThrow(() -> new MappingNotFoundException("Mapping with id " + mappingId + " not found."));
    }

    public void markForDeletion(UUID mappingId) {
        jpaMappingRepository.markForDeletion(mappingId);
    }

    public void markForDeletionByScope(UUID scopeId) {
        final List<MappingEntity> mappingEntities =
                jpaMappingRepository.findAllByScope_IdAndDeleteFalse(scopeId, Sort.unsorted());
        mappingEntities.forEach(mappingEntity -> mappingEntity.setDelete(true));
        jpaMappingRepository.saveAll(mappingEntities);
    }

    public void validateMapping(UUID mappingId, Map<String, String[]> mapping, String[] headers) {
        final String errorPrefix = "Mapping with id " + mappingId + " ";
        validateSources(errorPrefix, mapping, headers);
        validateTargets(errorPrefix, mapping);
    }

    private void validateSources(String errorPrefix, Map<String, String[]> mapping, String[] headers) {
        final Set<String> sources = mapping.keySet();
        if (sources.size() != headers.length) {
            throw new MappingValidationException(errorPrefix + "has a different size of source mappings than available headers.");
        }
        for (String header : headers) {
            if (!sources.contains(header)) {
                throw new MappingValidationException(errorPrefix + "has source mappings that are different to the original headers.");
            }
        }
    }

    private void validateTargets(String errorPrefix, Map<String, String[]> mapping) {
        final Map<String, String> valueCache = new HashMap<>();
        final Map<String, String> duplicatedValues = new HashMap<>();
        final Set<String> emptyValues = new HashSet<>();
        for (Map.Entry<String, String[]> valueEntry : mapping.entrySet()) {
            for (String value : valueEntry.getValue()) {
                final String key = valueEntry.getKey();
                if (!StringUtils.hasText(value)) {
                    emptyValues.add(key);
                } else if (valueCache.containsKey(value)) {
                    final String rootDuplicateKey = valueCache.get(value);
                    duplicatedValues.put(key, value);
                    duplicatedValues.put(rootDuplicateKey, value);
                }
                valueCache.putIfAbsent(value, key);
            }
        }
        String targetErrorMsg = errorPrefix + "has one or more target errors.";
        if (!duplicatedValues.isEmpty()) {
            targetErrorMsg += " Duplicated values are present in: " + duplicatedValues + ".";
        }
        if (!emptyValues.isEmpty()) {
            targetErrorMsg += " Empty values are present in following targets: " + emptyValues + ".";
        }
        if (!duplicatedValues.isEmpty() || !emptyValues.isEmpty()) {
            throw new MappingValidationException(targetErrorMsg);
        }
    }
}
