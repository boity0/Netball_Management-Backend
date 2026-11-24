package com.netballmanager.netballmanager.service;

import com.netballmanager.netballmanager.factory.DomainFactory;
import com.netballmanager.netballmanager.dto.TrainingRequest;
import com.netballmanager.netballmanager.dto.TrainingResponse;
import com.netballmanager.netballmanager.domain.TrainingSession;
import com.netballmanager.netballmanager.repository.TrainingSessionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TrainingService {
    private final TrainingSessionRepository sessions;
    private final TeamService teams;

    public TrainingService(TrainingSessionRepository sessions, TeamService teams) {
        this.sessions = sessions;
        this.teams = teams;
    }

    public TrainingResponse create(Long teamId, TrainingRequest request) {
        return toResponse(sessions.save(DomainFactory.createTraining(teams.require(teamId), request.title().trim(),
                request.venue().trim(), request.startsAt())));
    }

    @Transactional(readOnly = true)
    public List<TrainingResponse> list(Long teamId) {
        teams.require(teamId);
        return sessions.findAllByTeamIdOrderByStartsAt(teamId).stream()
                .map(TrainingService::toResponse).toList();
    }

    public TrainingSession require(Long id) {
        return sessions.findById(id).orElseThrow(() ->
                new com.netballmanager.netballmanager.exception.NotFoundException("Training session " + id + " was not found"));
    }

    private static TrainingResponse toResponse(TrainingSession session) {
        return new TrainingResponse(session.getId(), session.getTeam().getId(), session.getTitle(),
                session.getVenue(), session.getStartsAt());
    }
}
