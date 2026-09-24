package com.andesstay.msbff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DeploymentPlanRequest(
        @NotNull DeploymentTarget target,
        @NotBlank
        @Pattern(regexp = "[a-zA-Z0-9][a-zA-Z0-9-]{1,30}", message = "projectName must be 2-31 characters and contain only letters, numbers or hyphens")
        String projectName,
        String awsRegion,
        String azureResourceGroup,
        String azureLocation
) {
}
