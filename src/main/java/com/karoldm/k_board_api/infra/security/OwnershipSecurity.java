package com.karoldm.k_board_api.infra.security;

import com.karoldm.k_board_api.services.ProjectService;
import com.karoldm.k_board_api.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@AllArgsConstructor
@Component("ownershipSecurity")
public class OwnershipSecurity {

    private final UserService userService;
    private final ProjectService projectService;

    public boolean isOwner(UUID projectId) {
        var user = userService.getSessionUser();
        var project = projectService.findProjectById(projectId);
        return project.isPresent() && project.get().getOwner().getId().equals(user.getId());
    }
}
