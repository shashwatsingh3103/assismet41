import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task createTask(Task task) {
        if (task.getEstimatedTimeMinutes() <= 0) {
            throw new IllegalArgumentException("Estimated time must be > 0");
        }
        if (repository.findByTaskStrId(task.getTaskStrId()).isPresent()) {
            throw new IllegalArgumentException("Task ID must be unique");
        }
        task.setStatus(TaskStatus.PENDING);
        task.setSubmissionTimestamp(LocalDateTime.now());
        return repository.save(task);
    }

    public Task getTask(String taskStrId) {
        return repository.findByTaskStrId(taskStrId)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
    }

    public Task updateStatus(String taskStrId, TaskStatus newStatus) {
        Task task = getTask(taskStrId);
        if (!isValidTransition(task.getStatus(), newStatus)) {
            throw new IllegalArgumentException("Invalid status transition");
        }
        task.setStatus(newStatus);
        return repository.save(task);
    }

    private boolean isValidTransition(TaskStatus current, TaskStatus next) {
        return switch (current) {
            case PENDING -> true;
            case PROCESSING -> next != TaskStatus.PENDING;
            case COMPLETED -> next == TaskStatus.COMPLETED;
        };
    }

    public Task getNextToProcess() {
        return repository.findByStatus(TaskStatus.PENDING).stream()
                .sorted(Comparator.comparing(Task::getEstimatedTimeMinutes)
                        .thenComparing(Task::getSubmissionTimestamp))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No pending task found"));
    }

    public List<Task> getPendingTasks(String sortBy, String order, Integer limit) {
        List<Task> pendingTasks = repository.findByStatus(TaskStatus.PENDING);
        Comparator<Task> comparator = switch (sortBy) {
            case "time" -> Comparator.comparing(Task::getEstimatedTimeMinutes);
            case "submitted" -> Comparator.comparing(Task::getSubmissionTimestamp);
            default -> Comparator.comparing(Task::getId);
        };
        if ("desc".equalsIgnoreCase(order)) comparator = comparator.reversed();
        return pendingTasks.stream()
                .sorted(comparator)
                .limit(limit != null ? limit : 100)
                .collect(Collectors.toList());
    }
}
