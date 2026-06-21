package br.com.infnet.msmatches.domain.enums;

public enum MatchStatus {
    SCHEDULED,
    LIVE,
    HALF_TIME,
    FINISHED,
    POST_MATCH_CLOSED,
    POSTPONED,
    CANCELLED;

    public boolean marksAsFinished() {
        return this == FINISHED || this == POST_MATCH_CLOSED;
    }
}
