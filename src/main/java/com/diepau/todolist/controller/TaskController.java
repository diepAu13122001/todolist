package com.diepau.todolist.controller;

import com.diepau.todolist.model.Task;
import com.diepau.todolist.service.TaskService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

// All todo endpoints. principal.getName() is the logged-in username (set by JwtCookieFilter).
@Controller
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/tasks";
    }

    // List tasks with search (q) and status filter (all | active | done).
    @GetMapping("/tasks")
    public String list(@RequestParam(defaultValue = "all") String status,
                       @RequestParam(required = false) String q,
                       Principal principal,
                       Model model) {
        String owner = principal.getName();
        List<Task> tasks = taskService.list(owner, status, q);

        model.addAttribute("tasks", tasks);
        model.addAttribute("status", status);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("username", owner);
        model.addAttribute("total", taskService.list(owner, "all", null).size());
        model.addAttribute("doneCount", taskService.list(owner, "done", null).size());
        return "tasks";
    }

    @PostMapping("/tasks")
    public String create(@RequestParam String title,
                         @RequestParam(required = false) String description,
                         Principal principal,
                         RedirectAttributes ra) {
        if (title == null || title.isBlank()) {
            ra.addFlashAttribute("error", "Title must not be empty");
            return "redirect:/tasks";
        }
        taskService.create(principal.getName(), title, description);
        ra.addFlashAttribute("message", "Task added");
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("task", taskService.getOwned(id, principal.getName()));
        return "edit";
    }

    @PostMapping("/tasks/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam String title,
                         @RequestParam(required = false) String description,
                         Principal principal,
                         RedirectAttributes ra) {
        if (title == null || title.isBlank()) {
            ra.addFlashAttribute("error", "Title must not be empty");
            return "redirect:/tasks/" + id + "/edit";
        }
        taskService.update(id, principal.getName(), title, description);
        ra.addFlashAttribute("message", "Task updated");
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/{id}/toggle")
    public String toggle(@PathVariable Long id, Principal principal) {
        taskService.toggle(id, principal.getName());
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        taskService.delete(id, principal.getName());
        ra.addFlashAttribute("message", "Task deleted");
        return "redirect:/tasks";
    }

    // Turn expected errors (wrong owner, missing id) into a friendly redirect instead of a 500 page.
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleError(IllegalArgumentException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("error", ex.getMessage());
        return "redirect:/tasks";
    }
}
