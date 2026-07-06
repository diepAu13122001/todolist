package com.diepau.todolist.service;

import com.diepau.todolist.model.Task;
import com.diepau.todolist.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// Task logic: create, update, delete, toggle, search/filter. Every method is scoped by owner.
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // List tasks by filter: status = all | active | done, optional title keyword.
    public List<Task> list(String owner, String status, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return taskRepository.findByOwnerAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(owner, keyword.trim());
        }
        if ("active".equals(status)) {
            return taskRepository.findByOwnerAndCompletedOrderByCreatedAtDesc(owner, false);
        }
        if ("done".equals(status)) {
            return taskRepository.findByOwnerAndCompletedOrderByCreatedAtDesc(owner, true);
        }
        return taskRepository.findByOwnerOrderByCreatedAtDesc(owner);
    }

    public Task create(String owner, String title, String description) {
        Task task = new Task();
        task.setOwner(owner);
        task.setTitle(title.trim());
        task.setDescription(description == null ? null : description.trim());
        task.setCompleted(false);
        return taskRepository.save(task);
    }

    // Load a task and verify it belongs to the caller, blocking access to other users' tasks.
    public Task getOwned(Long id, String owner) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        if (!task.getOwner().equals(owner)) {
            throw new IllegalArgumentException("You are not allowed to access this task");
        }
        return task;
    }

    public Task update(Long id, String owner, String title, String description) {
        Task task = getOwned(id, owner);
        task.setTitle(title.trim());
        task.setDescription(description == null ? null : description.trim());
        return taskRepository.save(task);
    }

    public Task toggle(Long id, String owner) {
        Task task = getOwned(id, owner);
        task.setCompleted(!task.isCompleted());
        return taskRepository.save(task);
    }

    public void delete(Long id, String owner) {
        Task task = getOwned(id, owner);
        taskRepository.delete(task);
    }
}
