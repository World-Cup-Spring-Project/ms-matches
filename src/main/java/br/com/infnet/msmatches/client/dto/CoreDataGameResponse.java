package br.com.infnet.msmatches.client.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoreDataGameResponse(
        @JsonProperty("id") String id,
        @JsonProperty("home_team_id") @JsonAlias("homeTeamId") String homeTeamId,
        @JsonProperty("away_team_id") @JsonAlias("awayTeamId") String awayTeamId,
        @JsonProperty("home_score") @JsonAlias("homeScore") String homeScore,
        @JsonProperty("away_score") @JsonAlias("awayScore") String awayScore,
        @JsonProperty("home_scorers") @JsonAlias("homeScorers") String homeScorers,
        @JsonProperty("away_scorers") @JsonAlias("awayScorers") String awayScorers,
        @JsonProperty("group") @JsonAlias("groupName") String group,
        @JsonProperty("matchday") String matchday,
        @JsonProperty("local_date") @JsonAlias("localDate") String localDate,
        @JsonProperty("stadium_id") @JsonAlias("stadiumId") String stadiumId,
        @JsonProperty("finished") String finished,
        @JsonProperty("time_elapsed") @JsonAlias("timeElapsed") String timeElapsed,
        @JsonProperty("type") String type,
        @JsonProperty("home_team_label") @JsonAlias("homeTeamLabel") String homeTeamLabel,
        @JsonProperty("away_team_label") @JsonAlias("awayTeamLabel") String awayTeamLabel,
        @JsonProperty("home_team_name_en") @JsonAlias({"homeTeamNameEn", "homeTeamName"}) String homeTeamNameEn,
        @JsonProperty("away_team_name_en") @JsonAlias({"awayTeamNameEn", "awayTeamName"}) String awayTeamNameEn
) {
}
