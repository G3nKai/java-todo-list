package com.java.todolist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.java.todolist.application.dto.TaskCreateDTO;
import com.java.todolist.core.domain.Priority;
import com.java.todolist.core.domain.Task;
import com.java.todolist.core.repositories.ITaskRepository;
import com.java.todolist.core.services.TaskService;

//юнит тесты нацеленные на поле priority
public class TaskServiceCreatePriorityTest {
    @Mock
    private ITaskRepository repository;

    @InjectMocks
    private TaskService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    //приоритет применяет то значение, которое передано в DTO
    //а также дефолтный Medium, если null
    @ParameterizedTest
    @MethodSource("providePriorityValid")
    void testDeadlineValid(Optional<Priority> priority, Priority expectedPriority) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        priority.ifPresent(dto::setPriority);

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task task = service.createTask(dto);

        assertEquals(task.getPriority(), expectedPriority);
    }

    //введены неожиданные данные.
    //отлов ошибки валидации
    @ParameterizedTest
    @MethodSource("providePriorityInvalidFormat")
    void testDeadlineInvalidFormat(String priority) {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Priority.valueOf(priority);
        });

        String expectedMessagePart = "enum constant";
        String actualMessage = exception.getMessage();

        assert actualMessage.contains(expectedMessagePart);
    }

    private static Stream<Arguments> providePriorityValid() {
        return Stream.of(
            Arguments.of(Optional.of(Priority.Low), Priority.Low),
            Arguments.of(Optional.of(Priority.Critical), Priority.Critical),
            Arguments.of(Optional.empty(), Priority.Medium)
        );
    }

    private static Stream<Arguments> providePriorityInvalidFormat() {
        return Stream.of(
            Arguments.of("sad face"),
            Arguments.of("1"));
    }
}
