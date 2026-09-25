package com.groupa.digitalbackendapplication.domain.dto.response;

public record TotalAdminOverview (
        long totalSystemAdmin,
        long totalAdmin,
        long totalActiveAdmin,
        long totalSuspendAdmin)
{
}
