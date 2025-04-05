package com.java.todolist.core.repositories;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.java.todolist.core.domain.Status;
import com.java.todolist.core.domain.Task;

@Repository
public interface ITaskSchedulingRepository extends JpaRepository<Task, UUID>{
    List<Task> findAllByStatusAndDeadlineBefore(Status status, ZonedDateTime deadline);
}
