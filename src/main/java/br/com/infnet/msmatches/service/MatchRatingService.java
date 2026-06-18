package br.com.infnet.msmatches.service;

import br.com.infnet.msmatches.domain.model.Match;
import br.com.infnet.msmatches.domain.model.SquadPlayer;
import br.com.infnet.msmatches.infra.kafka.events.MatchCandidateEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchRatingService {

    private static final int TOP_CANDIDATES = 3;
    private static final double MIN_RATING = 6.0;
    private static final double MAX_RATING = 10.0;

    private final SquadService squadService;

    public List<MatchCandidateEvent> generateTopCandidates(Match match) {
        List<SquadPlayer> players = new ArrayList<>();
        players.addAll(squadService.getPlayersByTeamName(match.getHomeTeamLabel()));
        players.addAll(squadService.getPlayersByTeamName(match.getAwayTeamLabel()));

        return players.stream()
                .map(player -> new RatedPlayer(player, randomRating()))
                .sorted(Comparator.comparing(RatedPlayer::rating).reversed())
                .limit(TOP_CANDIDATES)
                .map(rated -> new MatchCandidateEvent(rated.player().name(), rated.rating()))
                .toList();
    }

    private BigDecimal randomRating() {
        double value = ThreadLocalRandom.current().nextDouble(MIN_RATING, MAX_RATING);
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP);
    }

    private record RatedPlayer(SquadPlayer player, BigDecimal rating) {
    }
}
