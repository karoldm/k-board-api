package com.karoldm.k_board_api.dto;

import java.util.Set;
import java.util.UUID;

public record AddMemberDTO(
        Set<UUID> membersId,
        UUID projectId
) {
}
