package br.com.infnet.msmatches.service;

import br.com.infnet.msmatches.domain.model.SquadPlayer;
import br.com.infnet.msmatches.dto.squad.SquadTeamJson;
import br.com.infnet.msmatches.exception.InvalidStatusChangeException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SquadService {

    private static final String SQUADS_FILE = "data/worldcup.squads.json";

    private final ObjectMapper objectMapper;
    private Map<String, List<SquadPlayer>> playersByTeamName;

    @PostConstruct
    void loadSquads() {
        try (InputStream input = new ClassPathResource(SQUADS_FILE).getInputStream()) {
            List<SquadTeamJson> teams = objectMapper.readValue(input, new TypeReference<>() {
            });

            playersByTeamName = teams.stream()
                    .collect(Collectors.toMap(
                            team -> normalizeTeamName(team.name()),
                            team -> team.players().stream()
                                    .map(player -> new SquadPlayer(
                                            team.fifa_code() + "-" + player.number(),
                                            player.name()
                                    ))
                                    .toList()
                    ));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load squads file: " + SQUADS_FILE, exception);
        }
    }

    public List<SquadPlayer> getPlayersByTeamName(String teamName) {
        if (!StringUtils.hasText(teamName)) {
            throw new InvalidStatusChangeException("Team name is required to rate players");
        }

        List<SquadPlayer> players = playersByTeamName.get(normalizeTeamName(teamName));
        if (players == null || players.isEmpty()) {
            throw new InvalidStatusChangeException("Squad not found for team: " + teamName);
        }

        return players;
    }

    private String normalizeTeamName(String teamName) {
        return teamName.trim().toLowerCase(Locale.ROOT);
    }
}
