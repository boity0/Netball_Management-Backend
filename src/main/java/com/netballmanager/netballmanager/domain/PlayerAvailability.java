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
import java.time.LocalDateTime;

@Entity
@Table(name = "player_availability", uniqueConstraints =
        @UniqueConstraint(name = "uk_availability_match_player", columnNames = {"match_id", "player_id"}))
public class PlayerAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private GameMatch match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus status;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected PlayerAvailability() {
    }

    public PlayerAvailability(GameMatch match, Player player, AvailabilityStatus status) {
        this.match = match;
        this.player = player;
        this.status = status;
    }

    public Long getId() { return id; }
    public GameMatch getMatch() { return match; }
    public Player getPlayer() { return player; }
    public AvailabilityStatus getStatus() { return status; }
    public void setStatus(AvailabilityStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
