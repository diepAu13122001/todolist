package com.diepau.todolist;

import com.diepau.todolist.model.Task;
import com.diepau.todolist.repository.TaskRepository;
import com.diepau.todolist.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Tests for TaskService using a real repository on an in-memory H2 database.
@DataJpaTest
class TaskServiceTest {

    @Autowired
    private TaskRepository taskRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository);
    }

    @Test
    void create_thenListReturnsIt() {
        taskService.create("diepau", "Learn Spring", "Read docs");

        List<Task> tasks = taskService.list("diepau", "all", null);
        assertEquals(1, tasks.size());
        assertEquals("Learn Spring", tasks.get(0).getTitle());
        assertFalse(tasks.get(0).isCompleted());
    }

    @Test
    void list_onlyReturnsOwnTasks() {
        taskService.create("diepau", "Mine", null);
        taskService.create("someone", "Not mine", null);

        List<Task> tasks = taskService.list("diepau", "all", null);
        assertEquals(1, tasks.size());
        assertEquals("Mine", tasks.get(0).getTitle());
    }

    @Test
    void toggle_flipsCompleted() {
        Task task = taskService.create("diepau", "Task", null);
        assertFalse(task.isCompleted());

        taskService.toggle(task.getId(), "diepau");
        assertTrue(taskService.getOwned(task.getId(), "diepau").isCompleted());

        taskService.toggle(task.getId(), "diepau");
        assertFalse(taskService.getOwned(task.getId(), "diepau").isCompleted());
    }

    @Test
    void update_changesTitleAndDescription() {
        Task task = taskService.create("diepau", "Old title", "Old desc");

        taskService.update(task.getId(), "diepau", "New title", "New desc");

        Task updated = taskService.getOwned(task.getId(), "diepau");
        assertEquals("New title", updated.getTitle());
        assertEquals("New desc", updated.getDescription());
    }

    @Test
    void delete_removesTask() {
        Task task = taskService.create("diepau", "Task", null);

        taskService.delete(task.getId(), "diepau");

        assertTrue(taskService.list("diepau", "all", null).isEmpty());
    }

    @Test
    void getOwned_wrongOwner_throws() {
        Task task = taskService.create("diepau", "Task", null);

        assertThrows(IllegalArgumentException.class,
                () -> taskService.getOwned(task.getId(), "someone"));
    }

    @Test
    void getOwned_missingId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> taskService.getOwned(999L, "diepau"));
    }

    @Test
    void list_filterByStatus() {
        Task active = taskService.create("diepau", "Active task", null);
        Task done = taskService.create("diepau", "Done task", null);
        taskService.toggle(done.getId(), "diepau");

        List<Task> activeOnly = taskService.list("diepau", "active", null);
        assertEquals(1, activeOnly.size());
        assertEquals("Active task", activeOnly.get(0).getTitle());

        List<Task> doneOnly = taskService.list("diepau", "done", null);
        assertEquals(1, doneOnly.size());
        assertEquals("Done task", doneOnly.get(0).getTitle());
    }

    @Test
    void list_searchByTitleIgnoreCase() {
        taskService.create("diepau", "Buy milk", null);
        taskService.create("diepau", "Read book", null);

        List<Task> result = taskService.list("diepau", "all", "MILK");
        assertEquals(1, result.size());
        assertEquals("Buy milk", result.get(0).getTitle());
    }
}
