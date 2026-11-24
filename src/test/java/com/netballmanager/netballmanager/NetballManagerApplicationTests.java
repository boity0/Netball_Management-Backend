package com.netballmanager.netballmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import com.netballmanager.netballmanager.domain.Position;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.bootstrap-coach.email=coach@test.local",
        "app.bootstrap-coach.password=Coach-test-password-2026"
})
@AutoConfigureMockMvc
class NetballManagerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthEndpointReturnsUpStatus() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void coachCanCreateTeamAndDuplicateJerseyNumbersAreRejected() throws Exception {
        String token = coachToken();
        long teamId = createTeam(token);
        createPlayer(token, teamId, 1, Position.GS);

        mockMvc.perform(post("/api/teams/{teamId}/players", teamId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson(UUID.randomUUID().toString(), 1, Position.GA)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("That jersey number is already assigned in this team"));
    }

    @Test
    void lineupRequiresUniquePlayersAndEveryCourtPosition() throws Exception {
        String token = coachToken();
        long teamId = createTeam(token);
        List<Map<String, Object>> starting = new ArrayList<>();
        long firstPlayerId = -1;
        int jersey = 1;
        for (Position position : Position.values()) {
            long playerId = createPlayer(token, teamId, jersey++, position);
            if (firstPlayerId < 0) {
                firstPlayerId = playerId;
            }
            starting.add(Map.of("playerId", playerId, "position", position.name()));
        }
        long matchId = createMatch(token, teamId);
        Map<String, Object> lineup = new LinkedHashMap<>();
        lineup.put("starting", starting);
        lineup.put("standby", List.of());
        mockMvc.perform(post("/api/matches/{matchId}/lineup", matchId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lineup)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.players.length()").value(7));
        mockMvc.perform(post("/api/matches/{matchId}/lineup", matchId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lineup)))
                .andExpect(status().isCreated());

        starting.set(1, Map.of("playerId", firstPlayerId, "position", Position.GA.name()));
        mockMvc.perform(post("/api/matches/{matchId}/lineup", matchId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lineup)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recommendationCoversEveryPositionAndExcludesUnavailablePlayers() throws Exception {
        String token = coachToken();
        long teamId = createTeam(token);
        List<Long> playerIds = new ArrayList<>();
        int jersey = 1;
        for (Position position : Position.values()) {
            playerIds.add(createPlayer(token, teamId, jersey++, position));
        }
        long reserveId = createPlayer(token, teamId, jersey, Position.GS);
        long matchId = createMatch(token, teamId);
        mockMvc.perform(post("/api/matches/{matchId}/availability", matchId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "playerId", reserveId, "status", "UNAVAILABLE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCount").value(0));
        mockMvc.perform(post("/api/matches/{matchId}/generate-lineup", matchId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players.length()").value(7))
                .andExpect(jsonPath("$.players[?(@.playerId == " + reserveId + ")]").isEmpty());
    }

    @Test
    void playerRegistrationLinksToExistingRosterProfileAndLimitsAvailabilityToSelf() throws Exception {
        String coachToken = coachToken();
        long teamId = createTeam(coachToken);
        String email = UUID.randomUUID() + "@test.local";
        long playerId = createPlayer(coachToken, teamId, 20, Position.C, email);
        long matchId = createMatch(coachToken, teamId);
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", email, "password", "player-test-password"))))
                .andExpect(status().isCreated())
                .andReturn();
        String playerToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
        mockMvc.perform(get("/api/players/me").header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(playerId));
        mockMvc.perform(post("/api/matches/{matchId}/availability", matchId)
                        .header("Authorization", "Bearer " + playerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("playerId", playerId, "status", "AVAILABLE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCount").value(1));
        mockMvc.perform(get("/api/teams/{teamId}/players", teamId)
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(playerId));
    }

    @Test
    void coachCanRecordSubstitutionAttendanceTrainingAndPerformance() throws Exception {
        String token = coachToken();
        long teamId = createTeam(token);
        List<Long> players = new ArrayList<>();
        int jersey = 1;
        for (Position position : Position.values()) {
            players.add(createPlayer(token, teamId, jersey++, position));
        }
        long standbyId = createPlayer(token, teamId, jersey, Position.GS);
        long matchId = createMatch(token, teamId);
        List<Map<String, Object>> starting = new ArrayList<>();
        for (int i = 0; i < Position.values().length; i++) {
            starting.add(Map.of("playerId", players.get(i), "position", Position.values()[i].name()));
        }
        Map<String, Object> lineup = Map.of(
                "starting", starting,
                "standby", List.of(Map.of("playerId", standbyId, "position", "GS")));
        mockMvc.perform(post("/api/matches/{matchId}/lineup", matchId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lineup)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/matches/{matchId}/substitutions", matchId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "playerOutId", players.getFirst(), "playerInId", standbyId,
                                "position", "GS", "period", 2))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.previousPosition").value("GS"))
                .andExpect(jsonPath("$.newPosition").value("GS"));

        mockMvc.perform(post("/api/matches/{matchId}/statistics", matchId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "playerId", standbyId, "goalsScored", 10, "goalAttempts", 12,
                                "assists", 2, "interceptions", 1, "deflections", 1,
                                "rebounds", 0, "turnovers", 1, "penalties", 0, "minutesPlayed", 30))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalsScored").value(10));

        mockMvc.perform(post("/api/matches/{matchId}/attendance", matchId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "playerId", players.getFirst(), "status", "PRESENT"))))
                .andExpect(status().isOk());

        MvcResult training = mockMvc.perform(post("/api/teams/{teamId}/training-sessions", teamId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Training", "venue", "Main court",
                                "startsAt", LocalDateTime.now().plusDays(1).withNano(0).toString()))))
                .andExpect(status().isCreated())
                .andReturn();
        long trainingId = objectMapper.readTree(training.getResponse().getContentAsString())
                .get("id").asLong();
        mockMvc.perform(post("/api/training-sessions/{sessionId}/attendance", trainingId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "playerId", players.getFirst(), "status", "LATE"))))
                .andExpect(status().isOk());
    }

    @Test
    void apiRejectsUnauthenticatedTeamRequestsAndSupportsPlayerRegistration() throws Exception {
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isUnauthorized());
        String email = UUID.randomUUID() + "@test.local";
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", email, "password", "player-test-password"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();
        String playerToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
        mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + playerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Forbidden team\"}"))
                .andExpect(status().isForbidden());
    }

    private String coachToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "coach@test.local", "password", "Coach-test-password-2026"))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private long createTeam(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/teams")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Test " + UUID.randomUUID(), "description", "Integration test team"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createPlayer(String token, long teamId, int jersey, Position position) throws Exception {
        return createPlayer(token, teamId, jersey, position, UUID.randomUUID() + "@test.local");
    }

    private long createPlayer(String token, long teamId, int jersey, Position position, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/teams/{teamId}/players", teamId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(playerJson(email, jersey, position)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String playerJson(String suffix, int jersey, Position position) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "firstName", "Test",
                "lastName", "Player",
                "email", suffix.contains("@") ? suffix : suffix + "@test.local",
                "phoneNumber", "555-0100",
                "jerseyNumber", jersey,
                "preferredPositions", List.of(position.name())));
    }

    private long createMatch(String token, long teamId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/teams/{teamId}/matches", teamId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "opponentName", "Rivals",
                                "venue", "Home court",
                                "matchDateTime", LocalDateTime.now().plusDays(2).withNano(0).toString(),
                                "venueType", "HOME"))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
