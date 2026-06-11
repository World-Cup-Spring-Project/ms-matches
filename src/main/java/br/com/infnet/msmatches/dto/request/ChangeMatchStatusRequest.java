package br.com.infnet.msmatches.dto.request;

import br.com.infnet.msmatches.domain.enums.MatchStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ChangeMatchStatusRequest(
        @NotNull MatchStatus status,
        String correlationId,
        @Valid List<MatchCandidateRequest> candidates
) {}
