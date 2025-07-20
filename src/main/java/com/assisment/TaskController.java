import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        try {
            Task created = service.createTask(task);
            return ResponseEntity.ok(Map.of(
                    "internal_db_id", created.getId(),
                    "task_str_id", created.getTaskStrId(),
                    "status", created.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{taskStrId}")
    public ResponseEntity<?> getTask(@PathVariable String taskStrId) {
        try {
            return ResponseEntity.ok(service.getTask(taskStrId));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{taskStrId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String taskStrId,
                                          @RequestBody Map<String, String> body) {
        try {
            TaskStatus newStatus = TaskStatus.valueOf(body.get("new_status").toUpperCase());
            return ResponseEntity.ok(service.updateStatus(taskStrId, newStatus));
        } catch (IllegalArgumentException | NoSuchElementException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/next-to-process")
    public ResponseEntity<?> getNextToProcess() {
        try {
            return ResponseEntity.ok(service.getNextToProcess());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingTasks(@RequestParam Optional<String> sort_by,
                                             @RequestParam Optional<String> order,
                                             @RequestParam Optional<Integer> limit) {
        List<Task> tasks = service.getPendingTasks(sort_by.orElse("time"), order.orElse("asc"), limit.orElse(null));
        return ResponseEntity.ok(tasks);
    }
}
