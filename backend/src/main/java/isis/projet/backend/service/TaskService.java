package isis.projet.backend.service;

import isis.projet.backend.entity.Task;
import isis.projet.backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(Integer id) {
        return taskRepository.findById(id);
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task updateTask(Task task) {
        if (task.getId() == null || !taskRepository.existsById(task.getId())) {
            throw new RuntimeException("Tâche introuvable");
        }
        return taskRepository.save(task);
    }

    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }

    public List<Task> getTasksByRepetition(String repetition) {
        return taskRepository.findByRepetition(repetition);
    }
}