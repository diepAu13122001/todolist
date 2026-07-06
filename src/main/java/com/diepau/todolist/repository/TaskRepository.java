package com.diepau.todolist.repository;

import com.diepau.todolist.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Task queries, all scoped by owner so a user only touches their own tasks.
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByOwnerOrderByCreatedAtDesc(String owner);

    List<Task> findByOwnerAndCompletedOrderByCreatedAtDesc(String owner, boolean completed);

    List<Task> findByOwnerAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(String owner, String keyword);
}
