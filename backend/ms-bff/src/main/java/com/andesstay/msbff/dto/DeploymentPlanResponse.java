package com.andesstay.msbff.dto;

import java.util.List;

public record DeploymentPlanResponse(
        DeploymentTarget target,
        String projectName,
        List<String> prerequisites,
        List<String> commands,
        List<String> notes
) {
}
