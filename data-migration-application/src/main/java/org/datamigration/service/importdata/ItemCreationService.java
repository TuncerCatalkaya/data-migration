package org.datamigration.service.importdata;

import lombok.RequiredArgsConstructor;
import org.datamigration.jpa.entity.ItemEntity;
import org.datamigration.jpa.entity.ScopeEntity;
import org.datamigration.model.ItemPropertiesModel;
import org.datamigration.utils.DataMigrationUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
class ItemCreationService {

    public String[] getHeaders(String firstLine, char delimiter) {
        final int arraySize = (int) firstLine.chars()
                .filter(c -> c == delimiter)
                .count() + 1;
        return DataMigrationUtils.fastSplit(firstLine, delimiter, arraySize);
    }

    public ItemEntity createItemEntity(String line, ScopeEntity scopeEntity, String[] headers, long lineNumber, char delimiter) {
        final ItemEntity itemEntity = new ItemEntity();
        itemEntity.setScope(scopeEntity);
        itemEntity.setLineNumber(lineNumber);
        itemEntity.setProperties(getProperties(line, headers, delimiter));
        return itemEntity;
    }

    private Map<String, ItemPropertiesModel> getProperties(String line, String[] headers, char delimiter) {
        final Map<String, ItemPropertiesModel> properties = new HashMap<>(headers.length);
        final String[] fields = DataMigrationUtils.fastSplit(line, delimiter, headers.length);
        for (int i = 0; i < fields.length; i++) {
            properties.put(headers[i], ItemPropertiesModel.builder()
                    .value(fields[i])
                    .build());
        }
        return properties;
    }
}
