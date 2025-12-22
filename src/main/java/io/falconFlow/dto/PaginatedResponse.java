package io.falconFlow.dto;

import java.util.List;

public record PaginatedResponse(
        List<WorkflowItem> items,
        String nextPageToken
) {}