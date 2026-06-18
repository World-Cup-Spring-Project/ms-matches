package br.com.infnet.msmatches.dto.response;

public record MatchSyncResponse(
        int created,
        int updated,
        int total
) {
}
