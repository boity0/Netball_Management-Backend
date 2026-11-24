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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_sessions")
public class TrainingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    @NotBlank
    @Column(nullable = false)
    private String title;
    @NotBlank
    @Column(nullable = false)
    private String venue;
    @NotNull
    @Column(nullable = false)
    private LocalDateTime startsAt;

    protected TrainingSession() {
    }

    public TrainingSession(Team team, String title, String venue, LocalDateTime startsAt) {
        this.team = team;
        this.title = title;
        this.venue = venue;
        this.startsAt = startsAt;
    }

    public Long getId() { return id; }
    public Team getTeam() { return team; }
    public String getTitle() { return title; }
    public String getVenue() { return venue; }
    public LocalDateTime getStartsAt() { return startsAt; }
}
