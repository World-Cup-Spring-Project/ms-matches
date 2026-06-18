package br.com.infnet.msmatches.service;

import br.com.infnet.msmatches.client.CoreDataClient;
import br.com.infnet.msmatches.client.dto.CoreDataGameResponse;
import br.com.infnet.msmatches.domain.model.Match;
import br.com.infnet.msmatches.dto.response.MatchSyncResponse;
import br.com.infnet.msmatches.exception.MatchNotFoundException;
import br.com.infnet.msmatches.mapper.CoreDataGameMapper;
import br.com.infnet.msmatches.repository.MatchRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MatchSyncService {

    private final CoreDataClient coreDataClient;
    private final CoreDataGameMapper coreDataGameMapper;
    private final MatchRepository matchRepository;
    private final CoreDataReferenceValidator coreDataReferenceValidator;

    public MatchSyncResponse syncAll() {
        List<CoreDataGameResponse> games = coreDataClient.fetchGames();
        int created = 0;
        int updated = 0;

        for (CoreDataGameResponse game : games) {
            if (!StringUtils.hasText(game.id())) {
                continue;
            }

            if (upsertGame(game)) {
                created++;
            } else {
                updated++;
            }
        }

        return new MatchSyncResponse(created, updated, games.size());
    }

    public Match syncGame(String externalMatchId) {
        CoreDataGameResponse game = coreDataClient.fetchGame(externalMatchId);
        if (game == null) {
            throw new MatchNotFoundException(externalMatchId);
        }

        upsertGame(game);
        return matchRepository.findByExternalMatchId(externalMatchId)
                .orElseThrow(() -> new MatchNotFoundException(externalMatchId));
    }

    private boolean upsertGame(CoreDataGameResponse game) {
        Optional<Match> existing = matchRepository.findByExternalMatchId(game.id());

        if (existing.isEmpty()) {
            Match match = coreDataGameMapper.toNewMatch(game);
            coreDataReferenceValidator.validateMatchReferences(match);
            matchRepository.save(match);
            return true;
        }

        Match match = existing.get();
        coreDataGameMapper.applyUpdate(match, game);
        coreDataReferenceValidator.validateMatchReferences(match);
        matchRepository.save(match);
        return false;
    }
}
