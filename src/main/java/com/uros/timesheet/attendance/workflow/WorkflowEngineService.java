package com.uros.timesheet.attendance.workflow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowEngineService {

    private final WorkflowDefinitionRepository workflowDefinitionRepository;

    private static final Set<String> PRIVILEGED_ROLES = Set.of("ADMIN", "SUPERADMIN", "SYSTEM");

    @Transactional(readOnly = true)
    public boolean canTransition(
            String entityType,
            String currentStatus,
            String targetStatus,
            Set<String> userRoles
    ) {
        if (userHasPrivilegedRole(userRoles)) {
            log.debug("[WORKFLOW] Privileged role detected ({}), override transition allowed for entityType='{}', {} -> {}",
                    extractPrivilegedRole(userRoles), entityType, currentStatus, targetStatus);
            return true;
        }

        WorkflowDefinition definition = workflowDefinitionRepository.findByEntityType(entityType)
                .orElseThrow(() ->
                        new IllegalArgumentException("Workflow definition not found for entityType: " + entityType));

        Optional<WorkflowStep> currentStepOpt = definition.getSteps().stream()
                .filter(step -> step.getStatus().equalsIgnoreCase(currentStatus))
                .findFirst();

        if (currentStepOpt.isEmpty()) {
            log.warn("[WORKFLOW] Workflow step not found for entityType='{}', status='{}'", entityType, currentStatus);
            throw new IllegalStateException("Workflow step not found for status: " + currentStatus);
        }

        WorkflowStep currentStep = currentStepOpt.get();

        if (!currentStep.getAllowedTransitions().contains(targetStatus)) {
            log.info("[WORKFLOW] Transition NOT allowed: entityType='{}', status='{}' -> '{}', reason=not declared in allowed transitions.",
                    entityType, currentStatus, targetStatus);
            return false;
        }

        boolean roleAllowed = userRoles.stream().anyMatch(role ->
                currentStep.getAllowedRoles().contains(role)
        );
        if (!roleAllowed) {
            log.info("[WORKFLOW] Transition NOT allowed: entityType='{}', status='{}' -> '{}', userRoles={}, allowedRoles={}",
                    entityType, currentStatus, targetStatus, userRoles, currentStep.getAllowedRoles());
        }
        return roleAllowed;
    }

    private boolean userHasPrivilegedRole(Set<String> userRoles) {
        return userRoles.stream().anyMatch(PRIVILEGED_ROLES::contains);
    }

    private String extractPrivilegedRole(Set<String> userRoles) {
        return userRoles.stream().filter(PRIVILEGED_ROLES::contains).findFirst().orElse(null);
    }
}