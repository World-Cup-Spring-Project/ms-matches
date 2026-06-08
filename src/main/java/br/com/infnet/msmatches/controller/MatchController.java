package br.com.infnet.msmatches.controller;

import br.com.infnet.msmatches.domain.Match;
import br.com.infnet.msmatches.domain.enums.MatchStatus;
import br.com.infnet.msmatches.dto.request.AddTimelineEventRequest;
import br.com.infnet.msmatches.dto.request.ChangeMatchStatusRequest;
import br.com.infnet.msmatches.dto.request.CreateMatchRequest;
import br.com.infnet.msmatches.dto.response.MatchResponse;
import br.com.infnet.msmatches.mapper.MatchMapper;
import br.com.infnet.msmatches.service.MatchService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final MatchMapper matchMapper;

    @PostMapping
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody CreateMatchRequest request) {
        Match match = matchService.create(matchMapper.toDomain(request));
        return ResponseEntity.created(URI.create("/matches/" + match.getId()))
                .body(matchMapper.toResponse(match));
    }

    @GetMapping
    public List<MatchResponse> findAll(@RequestParam(required = false) MatchStatus status) {
        return matchMapper.toResponses(matchService.findAll(status));
    }

    @GetMapping("/{id}")
    public MatchResponse findById(@PathVariable String id) {
        return matchMapper.toResponse(matchService.findById(id));
    }

    @PatchMapping("/{id}/status")
    public MatchResponse changeStatus(
            @PathVariable String id,
            @Valid @RequestBody ChangeMatchStatusRequest request
    ) {
        return matchMapper.toResponse(matchService.changeStatus(id, request.status()));
    }

    @PostMapping("/{id}/timeline-events")
    public MatchResponse addTimelineEvent(
            @PathVariable String id,
            @Valid @RequestBody AddTimelineEventRequest request
    ) {
        return matchMapper.toResponse(matchService.addTimelineEvent(id, matchMapper.toDomain(request)));
    }
}
