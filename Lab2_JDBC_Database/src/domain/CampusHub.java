package domain;

import java.util.*;

public class CampusHub {
    private final Map<Integer, Learner> students = new HashMap<>();
    private final Set<Subject> courses = new HashSet<>();
    private final Map<Subject, List<Learner>> rosters = new HashMap<>();
    private final Map<Lecturer, Set<Subject>> assignments = new HashMap<>();

    public void addLearner(Learner s) {
        students.put(s.getId(), s);
    }

    public void addCourse(Subject c) {
        if (courses.add(c)) {              // add() returns false if it already exists
            rosters.put(c, new ArrayList<>());
        }
    }

    public boolean enroll(Learner s, Subject c) {
        if (!courses.contains(c)) {
            throw new IllegalArgumentException("Course does not exist: " + c.getCode());
        }
        List<Learner> roster = rosters.get(c);
        if (roster.contains(s)) return false;   // already enrolled
        roster.add(s);
        return true;
    }

    public void assignLecturer(Lecturer i, Subject c) {
        assignments.computeIfAbsent(i, k -> new HashSet<>()).add(c);
        c.setLecturer(i);
    }

    public List<Learner> getRoster(Subject c) {
        return rosters.getOrDefault(c, new ArrayList<>());
    }

    public List<Subject> getCoursesOf(Learner s) {
        List<Subject> result = new ArrayList<>();
        for (Map.Entry<Subject, List<Learner>> entry : rosters.entrySet()) {
            if (entry.getValue().contains(s)) result.add(entry.getKey());
        }
        return result;
    }

    public Set<Subject> getCoursesTaughtBy(Lecturer i) {
        return assignments.getOrDefault(i, new HashSet<>());
    }
}
