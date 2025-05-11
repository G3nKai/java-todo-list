package com.java.todolist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

//валидация имени при различных сценариях
public class TaskServiceCreateNameTest {
    @Mock
    private ITaskRepository repository;

    @InjectMocks
    private TaskService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    //проверка того, что таск действительно применяет переданное имя
    //используются различные типы данных - однако все это UTF-8
    @ParameterizedTest
    @MethodSource("provideNameValid")
    void testNameValid(String nameInput, String expectedName) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(nameInput);

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task task = service.createTask(dto);

        assertEquals(task.getName(), expectedName);
    }

    //нарушение валидации о длине имени
    //цель - отловить ошибку валидации DTO
    @ParameterizedTest
    @MethodSource("provideNameInvalidDTO")
    void testNameInvalidDTO(String nameInput) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(nameInput);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        var violations = validator.validate(dto);

        assert !violations.isEmpty();
    }

    //множество пробелов в имени - dto не отлавливает, происходит это в сервисе
    //отлавливает ошибку внезапного уменьшения длины имени уже в сервисе
    @ParameterizedTest
    @MethodSource("provideNameInvalidTrim")
    void testNameInvalidTrim(String nameInput) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(nameInput);

        assertThrows(IllegalArgumentException.class, () -> {
            service.createTask(dto);
        });
    }

    //анализ граничных значений
    //здесь представлены валидные граничные значения
    @ParameterizedTest
    @MethodSource("provideNameBorderValid")
    void testNameBorderValid(String nameInput, String expectedName) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(nameInput);

        when(repository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));
        Task task = service.createTask(dto);

        assertEquals(task.getName(), expectedName);
    }

    //анализ граничных значений
    //здесь представлены некорректные граничные значения, которые отлавливаются валидатором dto
    @ParameterizedTest
    @MethodSource("provideNameBorderinValid")
    void testNameBorderInvalid(String nameInput) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(nameInput);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        var violations = validator.validate(dto);

        assert !violations.isEmpty();
    }

    //анализ граничных значений
    //эти данные имеют в себе множество пробелов в граничных значениях, но валидатором dto не отлавливаются 
    //ошибка отлавливается внутри сервиса
    @ParameterizedTest
    @MethodSource("provideNameBorderInvalidTrim")
    void testNameBorderInvalidTrim(String nameInput) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(nameInput);

        assertThrows(IllegalArgumentException.class, () -> {
            service.createTask(dto);
        });
    }

    private static Stream<Arguments> provideNameValid() {
        return Stream.of(
            Arguments.of("Fourss", "Fourss"),
            Arguments.of(" geeeeoguesser    ", "geeeeoguesser"),
            Arguments.of("Лого", "Лого"), 
            Arguments.of("____", "____"),
            Arguments.of("1211", "1211"),
            Arguments.of("22333333     ", "22333333"),
            Arguments.of(" Лs2!   ", "Лs2!")
        );
    }

    private static Stream<Arguments> provideNameInvalidDTO() {
        return Stream.of(
            Arguments.of("t"),
            Arguments.of("le"), 
            Arguments.of(""),
            Arguments.of("длинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-названиедлинное-название")
        );
    }

    private static Stream<Arguments> provideNameInvalidTrim() {
        return Stream.of(
            Arguments.of(" two   "),
            Arguments.of("             ")
        );
    }

    private static Stream<Arguments> provideNameBorderValid() {
        return Stream.of(
            Arguments.of("k".repeat(255), "k".repeat(255)),
            Arguments.of("    Гран            ", "Гран"), 
            Arguments.of("        " + "_".repeat(255) + "            ", "_".repeat(255))
        );
    }

    private static Stream<Arguments> provideNameBorderinValid() {
        return Stream.of(
            Arguments.of("k".repeat(256)),
            Arguments.of("два")
            );
    }
    
    private static Stream<Arguments> provideNameBorderInvalidTrim() {
        return Stream.of(
            Arguments.of("три     "),
            Arguments.of("tw")
        );
    }    
}