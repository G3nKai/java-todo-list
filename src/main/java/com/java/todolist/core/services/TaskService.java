package com.java.todolist.core.services;

import com.java.todolist.core.domain.Priority;
import com.java.todolist.core.domain.Status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.java.todolist.application.dto.TaskCreateDTO;
import com.java.todolist.application.dto.TaskPutDTO;
import com.java.todolist.core.domain.Task;
import com.java.todolist.core.repositories.ITaskRepository;

@Service
public class TaskService {//починить сортировку

    @Autowired
    private ITaskRepository taskRepository;

    public List<Task> getAllTasks(String sortBy, String direction) {
        Sort sort = Sort.unsorted();

        if (sortBy != null && direction != null) {
            try {
                direction = direction.trim().toLowerCase();
                sortBy = sortBy.trim().toLowerCase();

                if (!isValidSortDirection(direction)) {
                    throw new IllegalArgumentException();
                }

                if (!isValidSortField(sortBy)) {
                    throw new IllegalArgumentException();
                }

                System.out.println("Sorting by: " + sortBy + " Direction: " + direction);
                Sort.Direction directionSort = Sort.Direction.fromString(direction);
                Sort.Order order = new Sort.Order(directionSort, sortBy);
                System.out.println(order);
                sort = Sort.by(order);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid sort field or direction: sortBy - " + sortBy + ", Direction - " + direction, e);
            }
        }

        System.out.println("Sort applied: " + sort);

        List<Task> tasks = taskRepository.findAll(sort);

        if ("priority".equals(sortBy)) {
            tasks.sort(Comparator.comparingInt(task -> task.getPriority().ordinal()));
        } else if ("status".equals(sortBy)) {
            tasks.sort(Comparator.comparingInt(task -> task.getStatus().ordinal()));
        }

        return tasks;
    }

    private boolean isValidSortDirection(String direction) {
        return Arrays.asList("asc", "desc").contains(direction);
    }

    private boolean isValidSortField(String sortBy) {
        return Arrays.asList("name", "priority", "status", "created").contains(sortBy);
    }

    private final DateTimeFormatter DEADLINE_V1 = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter DEADLINE_V2 = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private Optional<Pair<ZonedDateTime, String>> extractBeforeDate(String input) {
        int index = input.indexOf("!before ");
        if (index == -1) return Optional.empty();
        
        String possibleDate = input.substring(index + 8).trim();
        if (possibleDate.length() < 10) return Optional.empty();
    
        String dateStr = possibleDate.substring(0, 10);
        String originalMacro = "!before " + dateStr;
    
        try {
            LocalDate date = LocalDate.parse(dateStr, DEADLINE_V1);
            return Optional.of(Pair.of(date.atTime(23, 59, 59).atZone(ZoneOffset.UTC),
                                       originalMacro));
        } catch (DateTimeParseException e1) {
            try {
                LocalDate date = LocalDate.parse(dateStr, DEADLINE_V2);
                return Optional.of(Pair.of(date.atTime(23, 59, 59).atZone(ZoneOffset.UTC),
                                                originalMacro));
            } catch (DateTimeParseException e2) {
                return Optional.empty();
            }
        }
    }

    public Task createTask(TaskCreateDTO taskCreateDTO) {
        String name = taskCreateDTO.getName();

        Optional<Pair<ZonedDateTime, String>> deadlineMacroResult = extractBeforeDate(name);
        Optional<ZonedDateTime> deadlineMacro = deadlineMacroResult.map(Pair::getFirst);
        Optional<String> macroToRemove = deadlineMacroResult.map(Pair::getSecond);

        if (macroToRemove.isPresent()) {
            name = name.replaceFirst(Pattern.quote(macroToRemove.get()), "").trim();
            if (name.length() < 4) throw new IllegalArgumentException("Macros deadline made the name less than 4 characters long");
        }
        System.out.printf("MACROS DEADLINE: %s%n", deadlineMacroResult.orElse(null));

        ZonedDateTime deadline = deadlineMacro.orElse(null);
        validateDeadline(deadline);

        deadline = taskCreateDTO.getDeadline() != null
                                 ? taskCreateDTO.getDeadline()
                                 : deadline;
        deadline = deadline != null ? isEndOfDay(deadline) : deadline;
    
        Status status = deadline != null
                        ? (deadline.isBefore(ZonedDateTime.now()) ? Status.Overdue : Status.Active)
                        : Status.Active;
    
        Priority priority = taskCreateDTO.getPriority() == null ? Priority.Medium : taskCreateDTO.getPriority();
    
        String regex = "!([1-4])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);
    
        if (matcher.find()) {
            String macro = matcher.group(1);
            switch (macro) {
                case "1":
                    priority = Priority.Critical;
                    break;
                case "2":
                    priority = Priority.High;
                    break;
                case "3":
                    priority = Priority.Medium;
                    break;
                case "4":
                    priority = Priority.Low;
                    break;
            }
            name = name.replaceFirst("!" + macro, "").trim();

            if (name.length() < 4) throw new IllegalArgumentException("Macros shorted name to less than 4 characters long");
        }
    
        ZonedDateTime created = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    
        Task task = new Task(
            name,
            taskCreateDTO.getDescription(),
            deadline,
            status,
            priority,
            created,
            null
        );
    
        return taskRepository.save(task);
    }

    private void validateDeadline(ZonedDateTime deadline) {
        if (deadline != null) 
            if (deadline.getYear() > 2035 || deadline.getYear() < 2023) {
                deadline = null;
            }
    }

    public void deleteTask(UUID id) {
        taskRepository.findById(id)
                      .ifPresentOrElse(
                        _task -> taskRepository.deleteById(id), 
                        () -> {
                            throw new RuntimeException(String.format("Task with id = %s does not exist", id));
                        });
    }

    public Task completeTask(UUID id) {
        Task task = taskRepository.findById(id)
                      .orElseThrow(() -> new RuntimeException("Task does not exist with id = " + id));

        task.setStatus(statusComparator(task.getStatus(), task.getDeadline()));

        return taskRepository.save(task);
    } 

    private Status statusComparator(Status status, ZonedDateTime deadline) {
        if (deadline != null) {
            boolean isPastDeadline = deadline.isBefore(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));

            return switch(status) {
                case Active -> isPastDeadline ? Status.Late : Status.Completed;
                case Completed -> isPastDeadline ? Status.Overdue : Status.Active;
                case Overdue -> !isPastDeadline ? Status.Completed : Status.Late;
                case Late -> !isPastDeadline ? Status.Active : Status.Overdue;
                default -> status;
            };   
        }
        return switch(status) {
            case Active -> Status.Completed;
            case Completed -> Status.Active;
            case Overdue -> Status.Late;
            case Late -> Status.Overdue;
            default -> status;
        };   
    }
    
    private ZonedDateTime isEndOfDay(ZonedDateTime deadline) {
        if (!(deadline.getMinute() == 59 && deadline.getSecond() == 59 && deadline.getHour() == 23)) {
            throw new IllegalArgumentException("Deadline should be at 23:59:59");
        }

        return deadline.withZoneSameLocal(ZoneId.systemDefault());
    }

    public Task getTask(UUID id) {
        return taskRepository.findById(id)
                             .orElseThrow(() -> new RuntimeException(String.format("Task with id = %s does not exist", id)));
    }

    public Task putTask(UUID id, TaskPutDTO taskPutDTO) {
        String name = taskPutDTO.getName();
    
        Optional<Pair<ZonedDateTime, String>> deadlineMacroResult = extractBeforeDate(name);
        Optional<ZonedDateTime> deadlineMacro = deadlineMacroResult.map(Pair::getFirst);
        Optional<String> macroToRemove = deadlineMacroResult.map(Pair::getSecond);
    
        if (macroToRemove.isPresent()) {
            name = name.replaceFirst(Pattern.quote(macroToRemove.get()), "").trim();
            if (name.length() < 4) {
                throw new IllegalArgumentException("Macros deadline made the name less than 4 characters long");
            }
        }
        System.out.printf("MACROS DEADLINE: %s%n", deadlineMacroResult.orElse(null));
    
        ZonedDateTime deadline = deadlineMacro.orElse(null);
        validateDeadline(deadline);
    
        deadline = taskPutDTO.getDeadline() != null
                   ? taskPutDTO.getDeadline()
                   : deadline;
        deadline = deadline != null ? isEndOfDay(deadline) : deadline;
    
        Priority priority = taskPutDTO.getPriority() != null
                            ? taskPutDTO.getPriority()
                            : Priority.Medium;
    
        String regex = "!([1-4])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);
    
        if (matcher.find()) {
            String macro = matcher.group(1);
            switch (macro) {
                case "1":
                    priority = Priority.Critical;
                    break;
                case "2":
                    priority = Priority.High;
                    break;
                case "3":
                    priority = Priority.Medium;
                    break;
                case "4":
                    priority = Priority.Low;
                    break;
            }
            name = name.replaceFirst("!" + macro, "").trim();
            if (name.length() < 4) {
                throw new IllegalArgumentException("Macros shorted name to less than 4 characters long");
            }
        }
    
        Task task = taskRepository.findById(id)
                                  .orElseThrow(() ->
                                      new RuntimeException(String.format("Task with id = %s does not exist", id)));
    
        task.setName(name);
        task.setDescription(taskPutDTO.getDescription());
        task.setPriority(priority);
        task.setDeadline(deadline != null ? deadline : null);
        task.setStatus(putCheckStatus(Optional.ofNullable(deadline), task.getStatus()));
        task.setEdited(ZonedDateTime.now());
    
        return taskRepository.save(task);
    }
    
    private Status putCheckStatus(Optional<ZonedDateTime> next, Status status) {
        if (next.isPresent()) {
            ZonedDateTime deadline = next.get();
            ZonedDateTime now = ZonedDateTime.now();

            if (deadline.isBefore(now) && status == Status.Active) 
                return Status.Overdue;
            else if (deadline.isAfter(now) && status == Status.Overdue) 
                return Status.Active; 
        }
        
        return status;
    }
}