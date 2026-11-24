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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "lineup_selections", uniqueConstraints =
        @UniqueConstraint(name = "uk_lineup_player", columnNames = {"lineup_id", "player_id"}))
public class LineupSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lineup_id", nullable = false)
    private MatchLineup lineup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position position;

    @Column(nullable = false)
    private boolean starting;

    protected LineupSelection() {
    }

    public LineupSelection(Player player, Position position, boolean starting) {
        this.player = player;
        this.position = position;
        this.starting = starting;
    }

    public Long getId() { return id; }
    public MatchLineup getLineup() { return lineup; }
    public void setLineup(MatchLineup lineup) { this.lineup = lineup; }
    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
    public Position getPosition() { return position; }
    public void setPosition(Position position) { this.position = position; }
    public boolean isStarting() { return starting; }
    public void setStarting(boolean starting) { this.starting = starting; }
}
