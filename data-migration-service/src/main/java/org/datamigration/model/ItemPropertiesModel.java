package org.datamigration.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPropertiesModel {

    private String value;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private boolean edited = false;

}
