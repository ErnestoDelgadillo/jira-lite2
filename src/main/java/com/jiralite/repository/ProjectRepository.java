package com.jiralite.repository;

import com.jiralite.domain.Project;
import com.jiralite.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByMembersContaining(User user);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.members WHERE p.id = :id")
    java.util.Optional<Project> findByIdWithMembers(@Param("id") Long id);
}
