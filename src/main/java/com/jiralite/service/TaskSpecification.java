package com.jiralite.service;

import com.jiralite.domain.Task;
import com.jiralite.domain.TaskPriority;
import com.jiralite.domain.TaskStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    public static Specification<Task> filter(Long projectId,
                                             TaskStatus status,
                                             TaskPriority priority,
                                             Long assigneeId,
                                             Long sprintId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (assigneeId != null) {
                predicates.add(cb.equal(root.get("assignee").get("id"), assigneeId));
            }
            if (sprintId != null) {
                predicates.add(cb.equal(root.get("sprint").get("id"), sprintId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

