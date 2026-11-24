package com.netballmanager.netballmanager.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "players", uniqueConstraints = @UniqueConstraint(
        name = "uk_player_team_jersey", columnNames = {"team_id", "jersey_number"}))
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String firstName;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String lastName;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @Size(max = 30)
    private String phoneNumber;

    @NotNull
    @Column(nullable = false)
    private Integer jerseyNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_preferred_positions", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "position", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Position> preferredPositions = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus status = PlayerStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDate registrationDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @PrePersist
    void onCreate() {
        registrationDate = LocalDate.now();
    }

    protected Player() {
    }

    public Player(String firstName, String lastName, String email, String phoneNumber,
                  Integer jerseyNumber, Set<Position> preferredPositions, Team team) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.jerseyNumber = jerseyNumber;
        this.preferredPositions = new HashSet<>(preferredPositions);
        this.team = team;
        this.status = PlayerStatus.ACTIVE;
    }

    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Integer getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(Integer jerseyNumber) { this.jerseyNumber = jerseyNumber; }
    public Set<Position> getPreferredPositions() { return preferredPositions; }
    public void setPreferredPositions(Set<Position> preferredPositions) {
        this.preferredPositions = new HashSet<>(preferredPositions);
    }
    public PlayerStatus getStatus() { return status; }
    public void setStatus(PlayerStatus status) { this.status = status; }
    public LocalDate getRegistrationDate() { return registrationDate; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}
