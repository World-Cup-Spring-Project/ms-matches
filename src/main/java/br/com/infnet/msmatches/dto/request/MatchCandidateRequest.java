package br.com.infnet.msmatches.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MatchCandidateRequest(
        @NotBlank String playerName,
        @NotNull BigDecimal matchRating
) {}
