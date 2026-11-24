package com.netballmanager.netballmanager.service;

import com.netballmanager.netballmanager.factory.DomainFactory;
import com.netballmanager.netballmanager.dto.AttendanceRequest;
import com.netballmanager.netballmanager.dto.AttendanceResponse;
import com.netballmanager.netballmanager.dto.PlayerAttendanceSummary;
import com.netballmanager.netballmanager.domain.Attendance;
import com.netballmanager.netballmanager.domain.AttendanceStatus;
import com.netballmanager.netballmanager.domain.GameMatch;
import com.netballmanager.netballmanager.domain.Player;
import com.netballmanager.netballmanager.domain.TrainingSession;
import com.netballmanager.netballmanager.exception.BadRequestException;
import com.netballmanager.netballmanager.repository.AttendanceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AttendanceService {
    private final AttendanceRepository attendance;
    private final PlayerService players;
    private final GameMatchService matches;
    private final TrainingService training;

    public AttendanceService(AttendanceRepository attendance, PlayerService players,
                             GameMatchService matches, TrainingService training) {
        this.attendance = attendance;
        this.players = players;
        this.matches = matches;
        this.training = training;
    }

    public AttendanceResponse recordMatch(Long matchId, AttendanceRequest request) {
        GameMatch match = matches.require(matchId);
        Player player = players.require(request.playerId());
        validateTeam(player, match.getTeam().getId());
        Attendance record = attendance.findByMatchIdAndPlayerId(matchId, player.getId())
                .orElseGet(() -> DomainFactory.createMatchAttendance(player, match, request.status()));
        record.setStatus(request.status());
        return toResponse(attendance.save(record));
    }

    public AttendanceResponse recordTraining(Long trainingId, AttendanceRequest request) {
        TrainingSession session = training.require(trainingId);
        Player player = players.require(request.playerId());
        validateTeam(player, session.getTeam().getId());
        Attendance record = attendance.findByTrainingIdAndPlayerId(trainingId, player.getId())
                .orElseGet(() -> DomainFactory.createTrainingAttendance(player, session, request.status()));
        record.setStatus(request.status());
        return toResponse(attendance.save(record));
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> history(Long playerId) {
        players.require(playerId);
        return attendance.findAllByPlayerIdOrderByIdDesc(playerId).stream()
                .map(AttendanceService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlayerAttendanceSummary summary(Long playerId) {
        players.require(playerId);
        List<Attendance> records = attendance.findAllByPlayerId(playerId);
        long attended = records.stream().filter(record -> record.getStatus() == AttendanceStatus.PRESENT
                || record.getStatus() == AttendanceStatus.LATE).count();
        long counted = records.stream().filter(record -> record.getStatus() != AttendanceStatus.EXCUSED).count();
        double percentage = counted == 0 ? 0 : Math.round((double) attended * 10000 / counted) / 100.0;
        return new PlayerAttendanceSummary(playerId, attended, counted, percentage);
    }

    private static void validateTeam(Player player, Long teamId) {
        if (!player.getTeam().getId().equals(teamId)) {
            throw new BadRequestException("Player is not registered with this team");
        }
    }

    private static AttendanceResponse toResponse(Attendance item) {
        return new AttendanceResponse(item.getId(), item.getPlayer().getId(),
                item.getMatch() == null ? null : item.getMatch().getId(),
                item.getTraining() == null ? null : item.getTraining().getId(), item.getStatus());
    }
}
