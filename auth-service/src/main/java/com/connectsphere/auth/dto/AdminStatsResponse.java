package com.connectsphere.auth.dto;

public record AdminStatsResponse(
        long totalUsers,
        long activeUsers,
        long inactiveUsers,
        long verifiedUsers,
        long admins
) {
}

