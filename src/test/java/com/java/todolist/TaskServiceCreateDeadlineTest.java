package com.java.todolist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.java.todolist.application.dto.TaskCreateDTO;
import com.java.todolist.core.domain.Status;
import com.java.todolist.core.domain.Task;
import com.java.todolist.core.repositories.ITaskRepository;
import com.java.todolist.core.services.TaskService;

//юнит тесты дедлайнов
public class TaskServiceCreateDeadlineTest {
    @Mock
    private ITaskRepository repository;

    @InjectMocks
    private TaskService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    //два типа записи дедлайнов - и .
    //чч.мм.сс - всегда 23:59:59
    //если иначе - то неправильно
    @ParameterizedTest
    @MethodSource("provideDeadlineValid")
    void testDeadlineValid(ZonedDateTime deadline, ZonedDateTime expectedDeadline, Status expectedStatus) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDeadline(deadline);

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task task = service.createTask(dto);

        assertEquals(task.getDeadline(), expectedDeadline);
        assertEquals(task.getStatus(), expectedStatus);
    }
    //нарушение формата даты - цель отловить ошибку валидации типа данных 
    @ParameterizedTest
    @MethodSource("provideDeadlineInvalidFormat")
    void testDeadlineInvalidFormat(String deadline) {
        Exception exception = assertThrows(DateTimeParseException.class, () -> {
            ZonedDateTime.parse(deadline);
        });

        String expectedMessagePart = "could not be parsed";
        String actualMessage = exception.getMessage();

        assert actualMessage.contains(expectedMessagePart);
    }

    //время не соответствует шаблоны 23:59:59
    //ошибка отлавливается в сервисе
    @ParameterizedTest
    @MethodSource("provideDeadlineInvalidTime")
    void testDeadlineInvalidTime(ZonedDateTime deadline) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDeadline(deadline);

        assertThrows(IllegalArgumentException.class, () -> {
            service.createTask(dto);
        });
    }
    //граничные значения секунд
    @ParameterizedTest
    @MethodSource("provideDeadlineBorderValid")
    void testDeadlineBorderValid(ZonedDateTime deadline, ZonedDateTime expectedDeadline, Status expectedStatus) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDeadline(deadline);

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task task = service.createTask(dto);

        assertEquals(task.getDeadline(), expectedDeadline);
        assertEquals(task.getStatus(), expectedStatus);
    }
    //граничные значения секунд, которые нарушают шаблон 23:59:59
    @ParameterizedTest
    @MethodSource("provideDeadlineBorderInvalid")
    void testDeadlineBorderInvalid(ZonedDateTime deadline) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDeadline(deadline);

        assertThrows(IllegalArgumentException.class, () -> {
            service.createTask(dto);
        });
    }

    private static Stream<Arguments> provideDeadlineValid() {
        return Stream.of(
            Arguments.of(ZonedDateTime.parse("2025-05-26T23:59:59Z"), ZonedDateTime.parse("2025-05-26T23:59:59+07:00[Asia/Tomsk]"), Status.Active),
            Arguments.of(ZonedDateTime.parse("2025-04-02T23:59:59Z"), ZonedDateTime.parse("2025-04-02T23:59:59+07:00[Asia/Tomsk]"), Status.Overdue),
            Arguments.of(null, null, Status.Active)
        );
    }

    private static Stream<Arguments> provideDeadlineInvalidFormat() {
        return Stream.of(
            Arguments.of("sad face"),
            Arguments.of("2025-02-29T23:59:59Z"),
            Arguments.of("2025/02/03T22:22:22Z"));
    }

    private static Stream<Arguments> provideDeadlineInvalidTime() {
        return Stream.of(
            Arguments.of(ZonedDateTime.parse("2025-04-26T22:45:47Z")),
            Arguments.of(ZonedDateTime.parse("2025-03-26T12:45:47Z")));
    }

    private static Stream<Arguments> provideDeadlineBorderValid() {
        return Stream.of(Arguments.of(
            ZonedDateTime.parse("2025-05-26T23:59:59Z"), 
            ZonedDateTime.parse("2025-05-26T23:59:59+07:00[Asia/Tomsk]"), 
            Status.Active));
    }

    private static Stream<Arguments> provideDeadlineBorderInvalid() {
        return Stream.of(
            Arguments.of(ZonedDateTime.parse("2025-04-27T00:00:00Z")), 
            Arguments.of(ZonedDateTime.parse("2025-04-26T23:59:58Z")));
    }
}
