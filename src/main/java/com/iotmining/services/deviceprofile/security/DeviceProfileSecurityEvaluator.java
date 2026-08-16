package com.iotmining.services.deviceprofile.security;

import com.iotmining.services.deviceprofile.entity.DeviceProfile;
import com.iotmining.services.deviceprofile.repository.DeviceProfileRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("deviceProfileSecurity")
@RequiredArgsConstructor
@Slf4j
public class DeviceProfileSecurityEvaluator {

    private final DeviceProfileRepository repository;

    /**
     * Evaluates if the current user can access the given Device Profile.
     * Access is granted if:
     * 1. User is a Super Admin.
     * 2. The user's tenant owns the profile.
     * 3. The profile has been specifically assigned to the user's tenantId or userId.
     */
    public boolean canAccessProfile(Authentication authentication, UUID profileId) {
        // 1. Basic Auth & Null Checks (Prevents SpEL crashes)
        if (authentication == null || !authentication.isAuthenticated() || profileId == null) {
            return false;
        }

        // 2. Super Admins have universal access
        if (isSuperAdmin(authentication)) {
            return true;
        }

        // 3. Extract IDs directly from the JWT Claims (populated by LiteJwtAuthenticationFilter)
        String userTenantIdStr = extractTenantId(authentication);
        String userIdStr = extractUserId(authentication);

        if (userTenantIdStr == null || userIdStr == null) {
            log.warn("Security Alert: Missing tenantId or userId in token claims.");
            return false;
        }

        UUID userTenantId;
        try {
            userTenantId = UUID.fromString(userTenantIdStr);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for userTenantId in token: {}", userTenantIdStr);
            return false;
        }

        // 4. Fetch the target Device Profile
        DeviceProfile profile = repository.findById(profileId).orElse(null);
        if (profile == null) {
            log.warn("Device Profile {} not found during security evaluation.", profileId);
            return false;
        }

        // 5. Evaluate Permissions (The "OR" Logic for Cross-Tenant/User Support)

        // Rule A: Ownership (Does this profile belong to the user's home tenant?)
        boolean isOwner = userTenantId.equals(profile.getTenantId());

        // Rule B: Explicit Assignment (Has the profile been shared specifically with this tenant or user?)
        boolean isAssigned = profile.getAssignedCustomers() != null &&
                (profile.getAssignedCustomers().contains(userTenantIdStr) ||
                        profile.getAssignedCustomers().contains(userIdStr));

        // 6. Grant Access Decision
        if (isOwner || isAssigned) {
            return true;
        } else {
            log.warn("Security Alert: Access denied for user {} (tenant {}) to device profile {}",
                    userIdStr, userTenantIdStr, profileId);
            return false;
        }
    }

    // --- Clean, Null-Safe Helper Methods ---

    private String extractTenantId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Claims claims) {
            return claims.get("tenantId", String.class);
        }
        return null;
    }

    private String extractUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Claims claims) {
            return claims.get("userId", String.class);
        }
        return null;
    }

    private boolean isSuperAdmin(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Claims claims) {
            return Boolean.TRUE.equals(claims.get("superAdmin", Boolean.class));
        }
        return false;
    }
}