package br.com.infnet.msmatches.repository;

import br.com.infnet.msmatches.domain.model.Match;
import br.com.infnet.msmatches.domain.enums.MatchStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MatchRepository extends MongoRepository<Match, String> {

    Optional<Match> findByExternalMatchId(String externalMatchId);

    List<Match> findByStatus(MatchStatus status);

    List<Match> findByLocalDate(LocalDate localDate);
}
