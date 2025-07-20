import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByTaskStrId(String taskStrId);
    List<Task> findByStatus(TaskStatus status);
}
