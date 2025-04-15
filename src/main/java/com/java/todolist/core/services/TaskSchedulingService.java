package com.java.todolist.core.services;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.java.todolist.core.domain.Status;
import com.java.todolist.core.domain.Task;
import com.java.todolist.core.repositories.ITaskSchedulingRepository;

@Service
public class TaskSchedulingService {

    @Autowired
    private ITaskSchedulingRepository taskRepository;

    @Scheduled(cron = "0 0 * * * ?")
    @Async
    public void checkActiveTasks() {
        ZonedDateTime now = ZonedDateTime.now();
        List<Task> overdueTasks = taskRepository.findAllByStatusAndDeadlineBefore(Status.Active, now);

        if (!overdueTasks.isEmpty()) {
            System.out.println("Активные задачи с просроченным дедлайном теперь overdue.");
            overdueTasks.forEach(task -> {
                task.setStatus(Status.Overdue);
                taskRepository.save(task);
            });
        }
    }
}