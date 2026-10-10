package com.skillswap.service;

import com.skillswap.model.SkillType;
import com.skillswap.model.UserSkill;
import com.skillswap.repository.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchService {

    private final UserSkillRepository userSkillRepo;

    public MatchService(UserSkillRepository userSkillRepo) {
        this.userSkillRepo = userSkillRepo;
    }

    public List<Map<String, Object>> findMatches(Long myId) {
        List<UserSkill> mine = userSkillRepo.findByUserId(myId);

        Set<String> myTeach = namesOf(mine, SkillType.TEACH);
        Set<String> myLearn = namesOf(mine, SkillType.LEARN);

        // other users only (never me), grouped by user
        Map<Long, List<UserSkill>> byUser = userSkillRepo.findByUserIdNot(myId).stream()
                .collect(Collectors.groupingBy(us -> us.getUser().getId()));

        List<Map<String, Object>> result = new ArrayList<>();

        for (List<UserSkill> theirs : byUser.values()) {
            Set<String> theirTeach = namesOf(theirs, SkillType.TEACH);
            Set<String> theirLearn = namesOf(theirs, SkillType.LEARN);

            Set<String> theyTeachMe = new TreeSet<>(theirTeach);
            theyTeachMe.retainAll(myLearn);          // they teach what I want

            Set<String> iTeachThem = new TreeSet<>(theirLearn);
            iTeachThem.retainAll(myTeach);           // they want what I teach

            if (theyTeachMe.isEmpty() && iTeachThem.isEmpty()) continue;

            var user = theirs.get(0).getUser();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", user.getId());
            m.put("userName", user.getName() == null ? "" : user.getName());
            m.put("theyTeachMe", theyTeachMe);
            m.put("iTeachThem", iTeachThem);
            m.put("mutual", !theyTeachMe.isEmpty() && !iTeachThem.isEmpty());
            result.add(m);
        }

        // mutual matches first
        result.sort((a, b) -> Boolean.compare((boolean) b.get("mutual"), (boolean) a.get("mutual")));
        return result;
    }

    private Set<String> namesOf(List<UserSkill> list, SkillType type) {
        return list.stream()
                .filter(us -> us.getType() == type)
                .map(us -> us.getSkill().getName().toLowerCase())
                .collect(Collectors.toSet());
    }
}