package com.java.todolist.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.annotation.PrepareTestInstance;

import com.java.todolist.application.dto.TaskCreateDTO;
import com.java.todolist.application.handlers.ErrorResponse;
import com.java.todolist.core.domain.Priority;
import com.java.todolist.core.domain.Status;
import com.java.todolist.core.domain.Task;
import com.java.todolist.core.repositories.ITaskRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
//апи тесты
public class TaskCreateTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ITaskRepository taskRepository;

    @BeforeEach  
    void cleanup() {
        taskRepository.deleteAll();
    }

    //валидно и Возвращаается код 200 
    @ParameterizedTest
    @MethodSource("provideValid")
    void createTask(String name, String description, ZonedDateTime deadline, 
                                    Optional<Priority> priority, String expectedName, String expectedDescription, 
                                    ZonedDateTime expectedDeadline, Priority expectedPriority, Status expectedStatus, 
                                    ZonedDateTime expectedEdited) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setDeadline(deadline);
        priority.ifPresent(dto::setPriority);

        ResponseEntity<Task> response = restTemplate.postForEntity(
            "/tasks", dto, Task.class
        );
        Task task = response.getBody();

        assertEquals(task.getName(), expectedName);
        assertEquals(task.getDescription(), expectedDescription);
        assertEquals(task.getDeadline(), expectedDeadline);
        assertEquals(task.getPriority(), expectedPriority);
        assertEquals(task.getStatus(), expectedStatus);
        assertEquals(task.getEdited(), expectedEdited);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    //короткое имя невалидно и код 400
    @ParameterizedTest
    @MethodSource("provideInvalidName")
    void createTaskInvalidName(String name) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(name);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/tasks", dto, ErrorResponse.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    //граничные значения но код 200
    @ParameterizedTest
    @MethodSource("provideNameBorderValid")
    void createTaskInvalidNameBorderValid(String name, String expectedName) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(name);

        ResponseEntity<Task> response = restTemplate.postForEntity(
            "/tasks", dto, Task.class
        );

        Task task = response.getBody();

        assertEquals(task.getName(), expectedName);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    //граничные значения но код 400
    @ParameterizedTest
    @MethodSource("provideInvalidNameBorderValid")
    void createTaskInvalidNameBorderInvalid(String name) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName(name);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/tasks", dto, ErrorResponse.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    //валидное описание и код 200
    @ParameterizedTest
    @MethodSource("provideValidDescription")
    void createTaskValidDescription(String description, String expectedDescription) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Valid Name");
        dto.setDescription(description);

        ResponseEntity<Task> response = restTemplate.postForEntity(
            "/tasks", dto, Task.class
        );

        Task task = response.getBody();

        assertEquals(task.getDescription(), expectedDescription);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    //невалидное описание на границе и код 400
    @ParameterizedTest
    @MethodSource("provideInvalidDescriptionBorder")
    void createTaskInvalidDescriptionBorder(String description) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Valid Name");
        dto.setDescription(description);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/tasks", dto, ErrorResponse.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }
    //валидная граница и код 200
    @ParameterizedTest
    @MethodSource("provideValidDescriptionBorder")
    void createTaskValidDescriptionBorder(String description, String expectedDescription) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Valid Name");
        dto.setDescription(description);

        ResponseEntity<Task> response = restTemplate.postForEntity(
            "/tasks", dto, Task.class
        );

        Task task = response.getBody();

        assertEquals(task.getDescription(), expectedDescription);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    //валидный приоритет и код 200
    @ParameterizedTest
    @MethodSource("providePriorityValid")
    void createTaskValidPriority(Optional<Priority> priority, Priority expectedPriority) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        priority.ifPresent(dto::setPriority);

        ResponseEntity<Task> response = restTemplate.postForEntity(
            "/tasks", dto, Task.class
        );

        Task task = response.getBody();

        assertEquals(task.getPriority(), expectedPriority);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    //некорректный формат и код 400
    @ParameterizedTest
    @MethodSource("providePriorityInvalidFormat")
    void createTaskInvalidPriorityFormat(String priority) {
        String dto = String.format("""
            {
                "name": "Test",
                "priority": "%s"
            }
            """, priority);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(dto, headers);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/tasks", request, ErrorResponse.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    //валидный дедлайн и код 200
    @ParameterizedTest
    @MethodSource("provideDeadlineValid")
    void createTaskValidDeadline(ZonedDateTime deadline, ZonedDateTime expectedDeadline) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDeadline(deadline);

        ResponseEntity<Task> response = restTemplate.postForEntity(
            "/tasks", dto, Task.class
        );

        Task task = response.getBody();

        assertEquals(task.getDeadline(), expectedDeadline);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    //Неправильный формат дедлайн - валидация не проходит код 400
    @ParameterizedTest
    @MethodSource("provideDeadlineInvalidFormat")
    void createTaskInvalidFormat(String invalidDeadline) {
        String json = """
            {
                "name": "Test",
                "deadline": "%s"
            }
            """.formatted(invalidDeadline);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/tasks", entity, String.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    //не те чч.мм.сс - код 400
    @ParameterizedTest
    @MethodSource("provideDeadlineInvalidTime")
    void createTaskInvalidTime(ZonedDateTime invalidDeadline) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDeadline(invalidDeadline);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/tasks", dto, String.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    //граничные значения чч.мм.сс дедлайна - код 200
    @ParameterizedTest
    @MethodSource("provideDeadlineBorderValid")
    void createTaskBorderValid(ZonedDateTime deadline, ZonedDateTime expectedDeadline) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDeadline(deadline);

        ResponseEntity<Task> response = restTemplate.postForEntity(
            "/tasks", dto, Task.class
        );

        Task task = response.getBody();

        assertEquals(expectedDeadline, task.getDeadline());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    //граничные значения чч.мм.сс дедлайна - код 400
    @ParameterizedTest
    @MethodSource("provideDeadlineBorderInvalid")
    void createTaskBorderInvalid(ZonedDateTime invalidDeadline) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDeadline(invalidDeadline);

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/tasks", dto, String.class
        );

        assertEquals(response.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    //смена статуса один раз
    @ParameterizedTest
    @MethodSource("providePatchDeadlineAndExpectedStatusSingle")
    void patchTask(ZonedDateTime deadline, Status expectedStatus) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDeadline(deadline);

        ResponseEntity<Task> createResponse = restTemplate.postForEntity(
            "/tasks", dto, Task.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());

        UUID taskId = createResponse.getBody().getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Task> patchResponse = restTemplate.exchange(
            "/tasks/" + taskId,
            HttpMethod.PATCH,
            entity,
            Task.class
        );

        assertEquals(expectedStatus, patchResponse.getBody().getStatus());
        assertEquals(patchResponse.getStatusCode(), HttpStatus.OK);
    }

    //смена статуса дважды - статус становится прежним
    @ParameterizedTest
    @MethodSource("providePatchDeadlineAndExpectedStatusTwice")
    void patchTask(ZonedDateTime deadline, Status expectedStatusFirst, Status expectedStatusSecond) {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");
        dto.setDeadline(deadline);

        ResponseEntity<Task> createResponse = restTemplate.postForEntity(
            "/tasks", dto, Task.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());

        UUID taskId = createResponse.getBody().getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Task> patchResponse = restTemplate.exchange(
            "/tasks/" + taskId,
            HttpMethod.PATCH,
            entity,
            Task.class
        );

        assertEquals(expectedStatusFirst, patchResponse.getBody().getStatus());
        assertEquals(HttpStatus.OK, patchResponse.getStatusCode());

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        entity = new HttpEntity<>(headers);

        patchResponse = restTemplate.exchange(
            "/tasks/" + taskId,
            HttpMethod.PATCH,
            entity,
            Task.class
        );

        assertEquals(expectedStatusSecond, patchResponse.getBody().getStatus());
        assertEquals(patchResponse.getStatusCode(), HttpStatus.OK);
    }

    //удаление существующей таски
    @Test
    void deleteTaskValid() {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");

        ResponseEntity<Task> createResponse = restTemplate.postForEntity(
            "/tasks", dto, Task.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());

        UUID taskId = createResponse.getBody().getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Task> patchResponse = restTemplate.exchange(
            "/tasks/" + taskId,
            HttpMethod.DELETE,
            entity,
            Task.class
        );

        assertEquals(patchResponse.getStatusCode(), HttpStatus.OK);
    }

    //удаление несуществующей таски
    @Test
    void deleteTaskInvalid() {
        UUID taskId = UUID.fromString("33c7fa37-fed6-4217-b3bf-3a25a247a718");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Object> patchResponse = restTemplate.exchange(
            "/tasks/" + taskId,
            HttpMethod.DELETE,
            entity,
            Object.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, patchResponse.getStatusCode());
    }

    //удаление существующей таски и попытаться удалить ее снова
    @Test
    void deleteTaskValidAgain() {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setName("Test");

        ResponseEntity<Task> createResponse = restTemplate.postForEntity(
            "/tasks", dto, Task.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());

        UUID taskId = createResponse.getBody().getId();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Task> deleteResponse = restTemplate.exchange(
            "/tasks/" + taskId,
            HttpMethod.DELETE,
            entity,
            Task.class
        );

        assertEquals(deleteResponse.getStatusCode(), HttpStatus.OK);

        ResponseEntity<Object> deleteResponseSecond = restTemplate.exchange(
            "/tasks/" + taskId,
            HttpMethod.DELETE,
            entity,
            Object.class
        );

        assertEquals(deleteResponseSecond.getStatusCode(), HttpStatus.BAD_REQUEST);
    }

    //проверка сортировки по возрастанию приоритета
    @Test
    void getAllTasksSorted() {
        TaskCreateDTO dto1 = new TaskCreateDTO();
        dto1.setName("Task 1");
        dto1.setPriority(Priority.Low);
        ResponseEntity<Task> createResponse1 = restTemplate.postForEntity(
            "/tasks", dto1, Task.class
        );
    
        TaskCreateDTO dto2 = new TaskCreateDTO();
        dto2.setName("Task 2");
        dto2.setPriority(Priority.Critical);
        ResponseEntity<Task> createResponse2 = restTemplate.postForEntity(
            "/tasks", dto2, Task.class
        );
    
        TaskCreateDTO dto3 = new TaskCreateDTO();
        dto3.setName("Task 3");
        dto3.setPriority(Priority.High);
        ResponseEntity<Task> createResponse3 = restTemplate.postForEntity(
            "/tasks", dto3, Task.class
        );
    
        assertEquals(HttpStatus.OK, createResponse1.getStatusCode());
        assertEquals(HttpStatus.OK, createResponse2.getStatusCode());
        assertEquals(HttpStatus.OK, createResponse3.getStatusCode());
    
        String sortBy = "priority";
        String direction = "asc";
         ResponseEntity<List<Task>> response = restTemplate.exchange(
            "/tasks?sortBy=" + sortBy + "&direction=" + direction,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Task>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    
        List<Task> tasks = response.getBody();
        assertNotNull(tasks);
        assertEquals(3, tasks.size());
    
        assertTrue(tasks.get(0).getPriority().compareTo(tasks.get(1).getPriority()) <= 0);
        assertTrue(tasks.get(1).getPriority().compareTo(tasks.get(2).getPriority()) <= 0);
    }

    //проверка сортировки по убыванию алфавита в имени
    @Test
    void getAllTasksSortedDesc() {
        TaskCreateDTO dto1 = new TaskCreateDTO();
        dto1.setName("Archer");
        ResponseEntity<Task> createResponse1 = restTemplate.postForEntity(
            "/tasks", dto1, Task.class
        );
    
        TaskCreateDTO dto2 = new TaskCreateDTO();
        dto2.setName("Berrington");
        ResponseEntity<Task> createResponse2 = restTemplate.postForEntity(
            "/tasks", dto2, Task.class
        );
    
        TaskCreateDTO dto3 = new TaskCreateDTO();
        dto3.setName("Cypher");
        ResponseEntity<Task> createResponse3 = restTemplate.postForEntity(
            "/tasks", dto3, Task.class
        );
    
        assertEquals(HttpStatus.OK, createResponse1.getStatusCode());
        assertEquals(HttpStatus.OK, createResponse2.getStatusCode());
        assertEquals(HttpStatus.OK, createResponse3.getStatusCode());
    
        String sortBy = "name";
        String direction = "desc";
         ResponseEntity<List<Task>> response = restTemplate.exchange(
            "/tasks?sortBy=" + sortBy + "&direction=" + direction,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Task>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    
        List<Task> tasks = response.getBody();
        assertNotNull(tasks);
        assertEquals(3, tasks.size());
    
        assertTrue(tasks.get(0).getName().compareTo(tasks.get(1).getName()) >= 0);
        assertTrue(tasks.get(1).getName().compareTo(tasks.get(2).getName()) >= 0);
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
            Arguments.of("6")
        );
    }

    private static Stream<Arguments> provideValid() {
        return Stream.of(
            Arguments.of("Success", null, null, Optional.empty(), "Success", null, null, Priority.Medium, Status.Active, null),
            Arguments.of(" Success", "s", null, Optional.empty(), "Success", "s", null, Priority.Medium, Status.Active, null),
            Arguments.of("Success      ", "s", ZonedDateTime.parse("2025-12-03T23:59:59Z"), Optional.empty(), "Success", "s", ZonedDateTime.parse("2025-12-03T16:59:59Z"), Priority.Medium, Status.Active, null),
            Arguments.of("       Success ", "s", ZonedDateTime.parse("2025-01-03T23:59:59Z"), Optional.empty(), "Success", "s", ZonedDateTime.parse("2025-01-03T16:59:59Z"), Priority.Medium, Status.Overdue, null),
            Arguments.of("Success", "s", ZonedDateTime.parse("2025-01-03T23:59:59Z"), Optional.of(Priority.High), "Success", "s", ZonedDateTime.parse("2025-01-03T16:59:59Z"), Priority.High, Status.Overdue, null)
            );
    }

    private static Stream<Arguments> provideInvalidName() {
        return Stream.of(
            Arguments.of("Su"),
            Arguments.of("    c                "),
            Arguments.of("               "),
            Arguments.of("")
            );
    }

    private static Stream<Arguments> provideNameBorderValid() {
        return Stream.of(
            Arguments.of("Thre", "Thre"),
            Arguments.of("        Thre          ", "Thre"),
            Arguments.of("k".repeat(255), "k".repeat(255)),
            Arguments.of("k".repeat(254), "k".repeat(254))
            );
    }

    private static Stream<Arguments> provideInvalidNameBorderValid() {
        return Stream.of(
            Arguments.of("Thr"),
            Arguments.of("Thr         "),
            Arguments.of("                 Thr  "),
            Arguments.of("k".repeat(255) + "k")
            );
    }

    private static Stream<Arguments> provideValidDescription() {
        return Stream.of(
            Arguments.of("valid", "valid"),
            Arguments.of("k".repeat(500), "k".repeat(500)),
            Arguments.of("                 Thr  ", "Thr"),
            Arguments.of(null, null)
            );
    }

    private static Stream<Arguments> provideInvalidDescriptionBorder() {
        return Stream.of(
            Arguments.of("k".repeat(2048))
            );
    }

    private static Stream<Arguments> provideValidDescriptionBorder() {
        return Stream.of(
            Arguments.of("", ""),
            Arguments.of("2", "2"),
            Arguments.of("k".repeat(2047), "k".repeat(2047))
            );
    }

    private static Stream<Arguments> provideDeadlineValid() {
        return Stream.of(
            Arguments.of(ZonedDateTime.parse("2025-05-26T23:59:59Z"), ZonedDateTime.parse("2025-05-26T16:59:59Z"), Status.Active),
            Arguments.of(ZonedDateTime.parse("2025-04-02T23:59:59Z"), ZonedDateTime.parse("2025-04-02T16:59:59Z"), Status.Overdue),
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
            ZonedDateTime.parse("2025-05-26T16:59:59Z"), 
            Status.Active));
    }

    private static Stream<Arguments> provideDeadlineBorderInvalid() {
        return Stream.of(
            Arguments.of(ZonedDateTime.parse("2025-04-27T00:00:00Z")), 
            Arguments.of(ZonedDateTime.parse("2025-04-26T23:59:58Z")));
    }

    private static Stream<Arguments> providePatchDeadlineAndExpectedStatusSingle() {
        return Stream.of(
            Arguments.of(ZonedDateTime.parse("2025-04-27T23:59:59Z"), Status.Late), 
            Arguments.of(ZonedDateTime.parse("2025-05-26T23:59:59Z"), Status.Completed));
    }

    private static Stream<Arguments> providePatchDeadlineAndExpectedStatusTwice() {
        return Stream.of(
            Arguments.of(ZonedDateTime.parse("2025-04-27T23:59:59Z"), Status.Late, Status.Overdue), 
            Arguments.of(ZonedDateTime.parse("2025-05-26T23:59:59Z"), Status.Completed, Status.Active));
    }
}
