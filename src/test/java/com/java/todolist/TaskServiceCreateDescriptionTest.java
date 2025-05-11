package com.java.todolist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.java.todolist.application.dto.TaskCreateDTO;
import com.java.todolist.core.domain.Task;
import com.java.todolist.core.repositories.ITaskRepository;
import com.java.todolist.core.services.TaskService;

import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

//юнит тесты дескрипшина 
public class TaskServiceCreateDescriptionTest {
    @Mock
    private ITaskRepository repository;

    @InjectMocks
    private TaskService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    //проверка того, что таск применяет значение из dto
    //различные типы данных - в том числе и Null
    @ParameterizedTest
    @MethodSource("provideDescriptionValid")
    void testDescriptionValid(String descriptionInput, String expectedDescription) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDescription(descriptionInput);

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task task = service.createTask(dto);

        assertEquals(task.getDescription(), expectedDescription);
    }

    //длительность дескрипшина гораздо сильнее превышает лимит
    //цель - отловить ошибку валидации
    @ParameterizedTest
    @MethodSource("provideDescriptionInvalidDTO")
    void testDescriptionInvalidDTO(String descriptionInput) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDescription(descriptionInput);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        var violations = validator.validate(dto);

        assert !violations.isEmpty();
    }
    //анализ граничных значений
    //валидные значения на границах - снизу и сверху
    @ParameterizedTest
    @MethodSource("provideDescriptionBorderValid")
    void testDescriptionValidationTrim(String descriptionInput, String expectedDescription) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDescription(descriptionInput);

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task task = service.createTask(dto);

        assertEquals(task.getDescription(), expectedDescription);
    }

    //анализ граничных значений
    //невалидные значения при превышении верхней грани
    @ParameterizedTest
    @MethodSource("provideDescriptionBorderInvalid")
    void testDescriptionBorderInvalid(String descriptionInput) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(descriptionInput);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        var violations = validator.validate(dto);

        assert !violations.isEmpty();
    }


    private static Stream<Arguments> provideDescriptionValid() {
        return Stream.of(
            Arguments.of("Fo", "Fo"),
            Arguments.of(" geeeeoguesser    ", "geeeeoguesser"),
            Arguments.of("Л", "Л"), 
            Arguments.of("____", "____"),
            Arguments.of("1211", "1211"),
            Arguments.of(null, null),
            Arguments.of(" Лs2!   ", "Лs2!"),
            Arguments.of("!".repeat(1000) + " ".repeat(4000), "!".repeat(1000))
        );
    }

    private static Stream<Arguments> provideDescriptionInvalidDTO() {
        return Stream.of(
            Arguments.of("f".repeat(4000)),
            Arguments.of("10".repeat(9000))
        );
    }

    private static Stream<Arguments> provideDescriptionBorderValid() {
        return Stream.of(
            Arguments.of("", ""),
            Arguments.of("1", "1"),
            Arguments.of("f".repeat(2047), "f".repeat(2047))
        );
    }

    private static Stream<Arguments> provideDescriptionBorderInvalid() {
        return Stream.of(
            Arguments.of("f".repeat(2048)),
            Arguments.of("f".repeat(2049))
        );
    }
}