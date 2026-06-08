package br.com.infnet.msmatches.dto;

import br.com.infnet.msmatches.domain.enums.MatchStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeMatchStatusRequest(@NotNull MatchStatus status) {
}
