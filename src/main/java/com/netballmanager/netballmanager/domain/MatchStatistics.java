package com.netballmanager.netballmanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "match_statistics", uniqueConstraints =
        @UniqueConstraint(name = "uk_match_statistics", columnNames = {"match_id", "player_id"}))
public class MatchStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private GameMatch match;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;
    @Min(0) @Column(nullable = false) private int goalsScored;
    @Min(0) @Column(nullable = false) private int goalAttempts;
    @Min(0) @Column(nullable = false) private int assists;
    @Min(0) @Column(nullable = false) private int interceptions;
    @Min(0) @Column(nullable = false) private int deflections;
    @Min(0) @Column(nullable = false) private int rebounds;
    @Min(0) @Column(nullable = false) private int turnovers;
    @Min(0) @Column(nullable = false) private int penalties;
    @Min(0) @Column(nullable = false) private int minutesPlayed;

    protected MatchStatistics() {
    }

    public MatchStatistics(GameMatch match, Player player, int goalsScored, int goalAttempts,
                           int assists, int interceptions, int deflections, int rebounds,
                           int turnovers, int penalties, int minutesPlayed) {
        this.match = match;
        this.player = player;
        this.goalsScored = goalsScored;
        this.goalAttempts = goalAttempts;
        this.assists = assists;
        this.interceptions = interceptions;
        this.deflections = deflections;
        this.rebounds = rebounds;
        this.turnovers = turnovers;
        this.penalties = penalties;
        this.minutesPlayed = minutesPlayed;
    }

    public Long getId() { return id; }
    public GameMatch getMatch() { return match; }
    public Player getPlayer() { return player; }
    public int getGoalsScored() { return goalsScored; }
    public int getGoalAttempts() { return goalAttempts; }
    public int getAssists() { return assists; }
    public int getInterceptions() { return interceptions; }
    public int getDeflections() { return deflections; }
    public int getRebounds() { return rebounds; }
    public int getTurnovers() { return turnovers; }
    public int getPenalties() { return penalties; }
    public int getMinutesPlayed() { return minutesPlayed; }
}
