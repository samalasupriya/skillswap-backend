package com.skillswap.service;

import com.skillswap.dto.ProposeSessionRequest;
import com.skillswap.model.*;
import com.skillswap.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SwapSessionService {

    private final SwapSessionRepository sessionRepo;
    private final ConnectionRequestRepository connRepo;
    private final UserRepository userRepo;

    public SwapSessionService(SwapSessionRepository sessionRepo,
                              ConnectionRequestRepository connRepo,
                              UserRepository userRepo) {
        this.sessionRepo = sessionRepo;
        this.connRepo = connRepo;
        this.userRepo = userRepo;
    }

    public Map<String, Object> propose(Long myId, ProposeSessionRequest req) {
        if (req.connectionId() == null || req.scheduledAt() == null) {
            throw new IllegalArgumentException("connectionId and scheduledAt are required");
        }
        ConnectionRequest c = connRepo.findById(req.connectionId())
                .orElseThrow(() -> new IllegalArgumentException("Connection not found"));
        requireParticipant(c, myId);
        if (c.getStatus() != ConnectionStatus.ACCEPTED) {
            throw new IllegalArgumentException("Connection is not accepted yet");
        }
        User me = userRepo.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SwapSession s = new SwapSession();
        s.setConnection(c);
        s.setProposedBy(me);
        s.setTopic(req.topic());
        s.setScheduledAt(req.scheduledAt());
        s.setMeetingLink(req.meetingLink());
        return toMap(sessionRepo.save(s), myId);
    }

    public List<Map<String, Object>> mine(Long myId) {
        return sessionRepo.findForUser(myId).stream().map(s -> toMap(s, myId)).toList();
    }

    // only the person who did NOT propose can confirm
    public Map<String, Object> confirm(Long myId, Long sessionId) {
        SwapSession s = load(sessionId, myId);
        if (s.getProposedBy().getId().equals(myId)) {
            throw new IllegalArgumentException("The other person must confirm your proposal");
        }
        if (s.getStatus() != SessionStatus.PROPOSED) {
            throw new IllegalArgumentException("Session is already " + s.getStatus());
        }
        s.setStatus(SessionStatus.CONFIRMED);
        return toMap(sessionRepo.save(s), myId);
    }

    public Map<String, Object> complete(Long myId, Long sessionId) {
        SwapSession s = load(sessionId, myId);
        if (s.getStatus() != SessionStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only a confirmed session can be completed");
        }
        s.setStatus(SessionStatus.COMPLETED);
        return toMap(sessionRepo.save(s), myId);
    }

    public Map<String, Object> cancel(Long myId, Long sessionId) {
        SwapSession s = load(sessionId, myId);
        if (s.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalArgumentException("A completed session cannot be cancelled");
        }
        s.setStatus(SessionStatus.CANCELLED);
        return toMap(sessionRepo.save(s), myId);
    }

    private SwapSession load(Long sessionId, Long myId) {
        SwapSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        requireParticipant(s.getConnection(), myId);
        return s;
    }

    private void requireParticipant(ConnectionRequest c, Long myId) {
        if (!c.getSender().getId().equals(myId) && !c.getReceiver().getId().equals(myId)) {
            throw new IllegalArgumentException("You are not part of this connection");
        }
    }

    private Map<String, Object> toMap(SwapSession s, Long myId) {
        ConnectionRequest c = s.getConnection();
        User other = c.getSender().getId().equals(myId) ? c.getReceiver() : c.getSender();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("sessionId", s.getId());
        m.put("otherUserId", other.getId());
        m.put("otherUserName", other.getName() == null ? "" : other.getName());
        m.put("topic", s.getTopic() == null ? "" : s.getTopic());
        m.put("scheduledAt", s.getScheduledAt());
        m.put("meetingLink", s.getMeetingLink() == null ? "" : s.getMeetingLink());
        m.put("status", s.getStatus());
        m.put("proposedByMe", s.getProposedBy().getId().equals(myId));
        return m;
    }
}
