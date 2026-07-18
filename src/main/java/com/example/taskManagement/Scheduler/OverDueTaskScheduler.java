package com.example.taskManagement.Scheduler;

import com.example.taskManagement.Enums.TaskStatus;
import com.example.taskManagement.Model.Task;
import com.example.taskManagement.Repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class OverDueTaskScheduler {

    private final TaskRepository taskRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(OverDueTaskScheduler.class);

    public OverDueTaskScheduler(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // cron - seconds minutes hours days-month month days-week
    @Scheduled(cron = "0 0 0 * * *")
    public void markOverDueTasks() {

        LOGGER.info("Running overdue task check");
        List<Task> overdueTasks = taskRepository.findTasksDueBeforeDate(LocalDate.now());

        if(overdueTasks.isEmpty()) {
            LOGGER.info("No overdue tasks found");
            return;
        }

        overdueTasks.forEach(task -> task.setStatus(TaskStatus.OVERDUE));
        taskRepository.saveAll(overdueTasks);

        LOGGER.info("Marked {} tasks as overdue", overdueTasks.size());
    }
}
