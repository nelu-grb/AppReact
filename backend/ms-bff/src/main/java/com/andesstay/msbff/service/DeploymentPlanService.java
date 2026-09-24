package com.andesstay.msbff.service;

import com.andesstay.msbff.dto.DeploymentPlanRequest;
import com.andesstay.msbff.dto.DeploymentPlanResponse;
import com.andesstay.msbff.dto.DeploymentTarget;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeploymentPlanService {

    public DeploymentPlanResponse createPlan(DeploymentPlanRequest request) {
        return switch (request.target()) {
            case LOCAL -> localPlan(request);
            case AWS -> awsPlan(request);
            case AZURE -> azurePlan(request);
        };
    }

    private DeploymentPlanResponse localPlan(DeploymentPlanRequest request) {
        return new DeploymentPlanResponse(
                DeploymentTarget.LOCAL,
                request.projectName(),
                List.of("Docker Desktop with Docker Compose v2"),
                List.of(
                        "docker compose -f docker-compose.messaging.yml up -d",
                        "docker compose up -d --build"
                ),
                List.of("The local deployment uses the repository .env file and Docker volumes.")
        );
    }

    private DeploymentPlanResponse awsPlan(DeploymentPlanRequest request) {
        String region = valueOrDefault(request.awsRegion(), "us-east-1");
        return new DeploymentPlanResponse(
                DeploymentTarget.AWS,
                request.projectName(),
                List.of("AWS CLI", "Docker", "An AWS account with permissions for ECR and ECS"),
                List.of(
                        "aws sts get-caller-identity",
                        "powershell -ExecutionPolicy Bypass -File scripts/deploy-backend.ps1 -Target AWS -ProjectName "
                                + request.projectName() + " -AwsRegion " + region
                ),
                List.of(
                        "The AWS wizard validates the CLI and shows the prerequisites for ECR/ECS; review networking, secrets and task sizing before applying infrastructure changes.",
                        "Do not place AWS access keys in the frontend or in the repository."
                )
        );
    }

    private DeploymentPlanResponse azurePlan(DeploymentPlanRequest request) {
        String resourceGroup = valueOrDefault(request.azureResourceGroup(), request.projectName() + "-rg");
        String location = valueOrDefault(request.azureLocation(), "eastus");
        return new DeploymentPlanResponse(
                DeploymentTarget.AZURE,
                request.projectName(),
                List.of("Azure CLI", "Docker", "An Azure subscription with permissions for ACR and Container Apps"),
                List.of(
                        "az account show",
                        "powershell -ExecutionPolicy Bypass -File scripts/deploy-backend.ps1 -Target Azure -ProjectName "
                                + request.projectName() + " -AzureResourceGroup " + resourceGroup
                                + " -AzureLocation " + location
                ),
                List.of(
                        "The Azure wizard validates the CLI and shows the prerequisites for ACR/Container Apps; review networking, secrets and sizing before applying infrastructure changes.",
                        "Use managed identities or a secret manager instead of storing Azure credentials in the frontend."
                )
        );
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
