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
import java.time.LocalDateTime;

@Entity
@Table(name = "substitutions")
public class Substitution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private GameMatch match;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_out_id", nullable = false)
    private Player playerOut;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_in_id", nullable = false)
    private Player playerIn;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position previousPosition;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position newPosition;
    @Column(nullable = false)
    private int period;
    @Column(nullable = false)
    private LocalDateTime substitutedAt = LocalDateTime.now();

    protected Substitution() {
    }

    public Substitution(GameMatch match, Player playerOut, Player playerIn, Position previousPosition,
                        Position newPosition, int period) {
        this.match = match;
        this.playerOut = playerOut;
        this.playerIn = playerIn;
        this.previousPosition = previousPosition;
        this.newPosition = newPosition;
        this.period = period;
    }

    public Long getId() { return id; }
    public GameMatch getMatch() { return match; }
    public Player getPlayerOut() { return playerOut; }
    public Player getPlayerIn() { return playerIn; }
    public Position getPreviousPosition() { return previousPosition; }
    public Position getNewPosition() { return newPosition; }
    public int getPeriod() { return period; }
    public LocalDateTime getSubstitutedAt() { return substitutedAt; }
}
