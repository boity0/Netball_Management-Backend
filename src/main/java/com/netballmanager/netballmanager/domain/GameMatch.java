package com.netballmanager.netballmanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class GameMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @NotBlank
    @Column(nullable = false)
    private String opponentName;

    @NotBlank
    @Column(nullable = false)
    private String venue;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime matchDateTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VenueType venueType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.SCHEDULED;

    private Integer teamScore;
    private Integer opponentScore;

    protected GameMatch() {
    }

    public GameMatch(Team team, String opponentName, String venue, LocalDateTime matchDateTime,
                     VenueType venueType) {
        this.team = team;
        this.opponentName = opponentName;
        this.venue = venue;
        this.matchDateTime = matchDateTime;
        this.venueType = venueType;
    }

    public Long getId() { return id; }
    public Team getTeam() { return team; }
    public String getOpponentName() { return opponentName; }
    public void setOpponentName(String opponentName) { this.opponentName = opponentName; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public LocalDateTime getMatchDateTime() { return matchDateTime; }
    public void setMatchDateTime(LocalDateTime matchDateTime) { this.matchDateTime = matchDateTime; }
    public VenueType getVenueType() { return venueType; }
    public void setVenueType(VenueType venueType) { this.venueType = venueType; }
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
    public Integer getTeamScore() { return teamScore; }
    public Integer getOpponentScore() { return opponentScore; }
    public void setResult(Integer teamScore, Integer opponentScore) {
        this.teamScore = teamScore;
        this.opponentScore = opponentScore;
        this.status = MatchStatus.COMPLETED;
    }
}
