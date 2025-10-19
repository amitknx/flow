package com.flow.flowboard.common.dto.workflow;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowUpdateRequest {

    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private Object data;

    private Boolean active;
}
