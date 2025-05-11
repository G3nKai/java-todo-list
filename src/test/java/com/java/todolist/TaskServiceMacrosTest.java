package com.java.todolist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import com.java.todolist.application.dto.TaskCreateDTO;
import com.java.todolist.core.domain.Priority;
import com.java.todolist.core.domain.Task;
import com.java.todolist.core.repositories.ITaskRepository;
import com.java.todolist.core.services.TaskService;
//здесь содержатся все юнит-тесты, связанные с макросами
public class TaskServiceMacrosTest {
    @Mock
    private ITaskRepository repository;

    @InjectMocks
    private TaskService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    //валидный тест макроса приоритета
    //из имени удаляется макрос
    //корректно устанавливается приоритет при наличии/отсутствии поля с приоритетом
    @ParameterizedTest
    @MethodSource("provideValidMacrosPriority")
    void testValidPriority(String input, Optional<Priority> priority, Priority expectedPriority, String expectedName) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(input);
        priority.ifPresent(dto::setPriority);

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task task = service.createTask(dto);

        assertEquals(expectedPriority, task.getPriority());
        assertEquals(expectedName, task.getName());
    }

    //валидный тест макроса дедлайна
    //из имени удаляется макрос
    //корректно устанавливается дедлайн при наличии/отсутствии поля с дедлайном
    //в двух разных форматах: DD.MM.YYYY и DD-MM-YYYY
    @ParameterizedTest
    @MethodSource("provideValidMacrosDeadline")
    void testValidDeadline(String input, ZonedDateTime deadline, ZonedDateTime expectedDeadline, String expectedName) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(input);
        dto.setDeadline(deadline);

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task task = service.createTask(dto);

        assertEquals(expectedDeadline, task.getDeadline());
        assertEquals(expectedName, task.getName());
    }

    //НЕвалидный тест макроса приоритета
    //макрос составлен неправильно - он просто не будет применяться и останется неизменным в имени
    @ParameterizedTest
    @MethodSource("provideInvalidMacrosPriority")
    void testInvalidPriority(String input, Optional<Priority> priority, Priority expectedPriority, String expectedName) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(input);
        priority.ifPresent(dto::setPriority);

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task task = service.createTask(dto);

        assertEquals(expectedPriority, task.getPriority());
        assertEquals(expectedName, task.getName());
    }

    //НЕвалидный тест макроса дедлайна
    //макрос составлен неправильно - он просто не будет применяться и останется неизменным в имени
    @ParameterizedTest
    @MethodSource("provideInvalidMacrosDeadline")
    void testInvalidDeadline(String input, ZonedDateTime deadline, ZonedDateTime expectedDeadline, String expectedName) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(input);
        dto.setDeadline(deadline);

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task task = service.createTask(dto);

        assertEquals(expectedDeadline, task.getDeadline());
        assertEquals(expectedName, task.getName());
    }

    //валидный тест макроса дедлайна И приоритета
    //из имени удаляется макрос
    //приоритет и дедлайн применяют значения из макросов
    @ParameterizedTest
    @MethodSource("provideValidMacrosPriorityAndDeadline")
    void testValidDeadlineAndPriority(String input, Optional<Priority> priority, ZonedDateTime deadline, Priority expectedPriority, ZonedDateTime expectedDeadline, String expectedName) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(input);
        dto.setDeadline(deadline);
        priority.ifPresent(dto::setPriority);

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task task = service.createTask(dto);

        assertEquals(expectedDeadline, task.getDeadline());
        assertEquals(expectedName, task.getName());
    }

    //НЕвалидный приоритет И дедлайн
    //имеется в виду, что имя становится меньше 4 символов, что нарушает валидацию поля
    //ожидается ошибка
    @ParameterizedTest
    @MethodSource("provideInvalidMacrosShortName")
    void testInvalidPriorityAndDeadline(String input, Optional<Priority> priority, ZonedDateTime deadline) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(input);
        dto.setDeadline(deadline);
        priority.ifPresent(dto::setPriority);

        assertThrows(RuntimeException.class, () -> {
            service.createTask(dto);
        });
    }

    private static Stream<Arguments> provideValidMacrosPriority() {
        return Stream.of(
            Arguments.of("!1гоол", Optional.empty(), Priority.Critical, "гоол"),
            Arguments.of("!2гоол", Optional.of(Priority.Low), Priority.Low, "гоол")
            );
    }
    private static Stream<Arguments> provideValidMacrosDeadline() {
        return Stream.of(
            Arguments.of("!before 26-07-2025гоол", null, ZonedDateTime.parse("2025-07-26T23:59:59+07:00[Asia/Tomsk]"), "гоол"),
            Arguments.of("!before 26-07-2025гоол", null, ZonedDateTime.parse("2025-07-26T23:59:59+07:00[Asia/Tomsk]"), "гоол"),
            Arguments.of("!before 26.07.2025гооол", ZonedDateTime.parse("2025-12-03T23:59:59Z"), ZonedDateTime.parse("2025-12-03T23:59:59+07:00[Asia/Tomsk]"), "гооол"),
            Arguments.of("!before 26-07-2025гооол", ZonedDateTime.parse("2025-12-03T23:59:59Z"), ZonedDateTime.parse("2025-12-03T23:59:59+07:00[Asia/Tomsk]"), "гооол")
            );
    }
    private static Stream<Arguments> provideInvalidMacrosPriority() {
        return Stream.of(
            Arguments.of("!5гоол", Optional.empty(), Priority.Medium, "!5гоол"),
            Arguments.of("!0гоол", Optional.of(Priority.Critical), Priority.Critical, "!0гоол")
        );
    }

    private static Stream<Arguments> provideInvalidMacrosDeadline() {
        return Stream.of(
            Arguments.of("!before 26.13.2025гоол", null, null, "!before 26.13.2025гоол"),
            Arguments.of("!before 46-07-2024", null, null, "!before 46-07-2024"),
            Arguments.of("!before 99.07.2025гоол", ZonedDateTime.parse("2025-12-03T23:59:59Z"), ZonedDateTime.parse("2025-12-03T23:59:59+07:00[Asia/Tomsk]"), "!before 99.07.2025гоол")
        );
    }
    private static Stream<Arguments> provideValidMacrosPriorityAndDeadline() {
        return Stream.of(
            Arguments.of("!before 26.12.2025!2гоол", Optional.empty(), null, Priority.High, ZonedDateTime.parse("2025-12-26T23:59:59+07:00[Asia/Tomsk]"), "гоол"),
            Arguments.of("гоол!3!before 26.12.2025!2", Optional.empty(), null, Priority.High, ZonedDateTime.parse("2025-12-26T23:59:59+07:00[Asia/Tomsk]"), "гоол!2")
        );
    }
    private static Stream<Arguments> provideInvalidMacrosShortName() {
        return Stream.of(
            Arguments.of("!before 26.12.2025!4              ", Optional.empty(), null),
            Arguments.of("!before 26.12.2025!1ггг", Optional.empty(), null)
        );
    }
}