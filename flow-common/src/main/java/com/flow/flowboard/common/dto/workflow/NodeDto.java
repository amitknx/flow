package com.flow.flowboard.common.dto.workflow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeDto {
    private String id;
    private Map<String, Object> data;
    private String name;
    private String type;
    private PositionDto position;
    private String sourcePosition;
}
