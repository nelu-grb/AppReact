package com.andesstay.msbff.controller;

import com.andesstay.msbff.dto.DeploymentPlanRequest;
import com.andesstay.msbff.dto.DeploymentPlanResponse;
import com.andesstay.msbff.dto.DeploymentTarget;
import com.andesstay.msbff.service.DeploymentPlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deployments")
public class DeploymentController {

    private final DeploymentPlanService deploymentPlanService;

    public DeploymentController(DeploymentPlanService deploymentPlanService) {
        this.deploymentPlanService = deploymentPlanService;
    }

    @GetMapping("/options")
    public Mono<Map<String, Object>> options() {
        return Mono.just(Map.of(
                "targets", List.of(
                        Map.of("id", DeploymentTarget.LOCAL, "label", "Local", "requiresCloudCredentials", false),
                        Map.of("id", DeploymentTarget.AWS, "label", "AWS", "requiresCloudCredentials", true),
                        Map.of("id", DeploymentTarget.AZURE, "label", "Azure", "requiresCloudCredentials", true)
                )
        ));
    }

    @PostMapping("/plan")
    public Mono<DeploymentPlanResponse> plan(@Valid @RequestBody DeploymentPlanRequest request) {
        return Mono.just(deploymentPlanService.createPlan(request));
    }
}
