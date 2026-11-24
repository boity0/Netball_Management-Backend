package com.netballmanager.netballmanager.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "match_lineups")
public class MatchLineup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private GameMatch match;

    @OneToMany(mappedBy = "lineup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineupSelection> selections = new ArrayList<>();

    private LocalDateTime updatedAt = LocalDateTime.now();

    protected MatchLineup() {
    }

    public MatchLineup(GameMatch match) {
        this.match = match;
    }

    public Long getId() { return id; }
    public GameMatch getMatch() { return match; }
    public List<LineupSelection> getSelections() { return selections; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void clearSelections() {
        selections.clear();
        updatedAt = LocalDateTime.now();
    }
    public void touch() { updatedAt = LocalDateTime.now(); }
    public void replaceSelections(List<LineupSelection> newSelections) {
        selections.clear();
        newSelections.forEach(selection -> {
            selection.setLineup(this);
            selections.add(selection);
        });
        updatedAt = LocalDateTime.now();
    }
}
