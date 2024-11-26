package org.dataintegration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.dataintegration.jpa.entity.ItemEntity;
import org.dataintegration.jpa.entity.ScopeEntity;
import org.dataintegration.model.ItemPropertiesModel;
import org.dataintegration.utils.DataIntegrationUtils;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@Service
@RequiredArgsConstructor
class ItemCreationService {

    String[] getHeaders(String firstLine, char delimiter) {
        final int arraySize = (int) firstLine.chars()
                .filter(c -> c == delimiter)
                .count() + 1;
        return DataIntegrationUtils.fastSplit(firstLine, delimiter, arraySize);
    }

    boolean isHeaderValid(String[] headers) {
        if (headers == null || headers.length == 0) {
            return false;
        }

        final Set<String> uniqueHeaders = new HashSet<>(headers.length);

        for (String header : headers) {
            final String trimmedHeader = header.trim();
            if (trimmedHeader.isEmpty() || !uniqueHeaders.add(trimmedHeader)) {
                return false;
            }
        }
        return true;
    }

    ItemEntity createItemEntity(String line, ScopeEntity scopeEntity, String[] headers, long lineNumber, char delimiter) {
        final ItemEntity itemEntity = new ItemEntity();
        itemEntity.setScope(scopeEntity);
        itemEntity.setLineNumber(lineNumber);
        itemEntity.setProperties(getProperties(line, headers, delimiter));
        return itemEntity;
    }

    private Map<String, ItemPropertiesModel> getProperties(String line, String[] headers, char delimiter) {
        final Map<String, ItemPropertiesModel> properties = new WeakHashMap<>(headers.length);
        final String[] fields = DataIntegrationUtils.fastSplit(line, delimiter, headers.length);
        for (int i = 0; i < fields.length; i++) {
            properties.put(headers[i], ItemPropertiesModel.builder()
                    .value(fields[i])
                    .build());
        }
        return properties;
    }
}
