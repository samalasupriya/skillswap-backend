package com.skillswap.service;

import com.skillswap.model.ConnectionRequest;
import com.skillswap.model.ConnectionStatus;
import com.skillswap.model.User;
import com.skillswap.repository.ConnectionRequestRepository;
import com.skillswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ConnectionService {

    private final ConnectionRequestRepository repo;
    private final UserRepository userRepo;

    public ConnectionService(ConnectionRequestRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    public Map<String, Object> send(Long myId, Long receiverId) {
        if (myId.equals(receiverId)) {
            throw new IllegalArgumentException("You cannot connect with yourself");
        }
        User me = userRepo.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User other = userRepo.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));
        if (repo.existsActiveBetween(myId, receiverId)) {
            throw new IllegalArgumentException("A request already exists with this user");
        }
        ConnectionRequest c = new ConnectionRequest();
        c.setSender(me);
        c.setReceiver(other);
        c = repo.save(c);
        return toMap(c, myId);
    }

    public List<Map<String, Object>> incoming(Long myId) {
        return repo.findByReceiverIdAndStatus(myId, ConnectionStatus.PENDING)
                .stream().map(c -> toMap(c, myId)).toList();
    }

    public List<Map<String, Object>> outgoing(Long myId) {
        return repo.findBySenderId(myId)
                .stream().map(c -> toMap(c, myId)).toList();
    }

    public List<Map<String, Object>> accepted(Long myId) {
        return repo.findAcceptedFor(myId)
                .stream().map(c -> toMap(c, myId)).toList();
    }

    public Map<String, Object> accept(Long myId, Long requestId) {
        return respond(myId, requestId, ConnectionStatus.ACCEPTED);
    }

    public Map<String, Object> reject(Long myId, Long requestId) {
        return respond(myId, requestId, ConnectionStatus.REJECTED);
    }

    private Map<String, Object> respond(Long myId, Long requestId, ConnectionStatus newStatus) {
        ConnectionRequest c = repo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!c.getReceiver().getId().equals(myId)) {
            throw new IllegalArgumentException("Only the receiver can respond to this request");
        }
        if (c.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalArgumentException("Request is already " + c.getStatus());
        }
        c.setStatus(newStatus);
        return toMap(repo.save(c), myId);
    }

    // shows the OTHER person's details from my point of view
    private Map<String, Object> toMap(ConnectionRequest c, Long myId) {
        User other = c.getSender().getId().equals(myId) ? c.getReceiver() : c.getSender();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("requestId", c.getId());
        m.put("status", c.getStatus());
        m.put("otherUserId", other.getId());
        m.put("otherUserName", other.getName() == null ? "" : other.getName());
        m.put("sentByMe", c.getSender().getId().equals(myId));
        return m;
    }
}