package com.connectsphere.auth.dto;

import java.util.List;

public record AdminSystemOverviewResponse(
        List<ServiceHealthResponse> services
) {
}
