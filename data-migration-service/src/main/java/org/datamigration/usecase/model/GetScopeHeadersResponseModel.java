package org.datamigration.usecase.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetScopeHeadersResponseModel {

    private String[] headers;
    private LinkedList<String> extraHeaders;

}
