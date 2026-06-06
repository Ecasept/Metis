package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.client.api.ApiClient;
import dev.ecasept.unitodo.client.api.HttpClientFactory;
import dev.ecasept.unitodo.client.db.DatabaseRepository;
import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.api.ApiResponse;
import dev.ecasept.unitodo.shared.models.api.ApiResponseAdapter;
import dev.ecasept.unitodo.shared.models.db.Task;
import dev.ecasept.unitodo.shared.models.db.TaskPriority;
import dev.ecasept.unitodo.shared.models.db.TaskState;
import dev.ecasept.unitodo.shared.serialization.Serializer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        var serializer = Serializer.createDefault().adapter(ApiResponseAdapter.class, ApiResponse.class);
        var httpClient = HttpClientFactory.createClient();
        var apiClient = new ApiClient(httpClient, "https://localhost:6767/api", serializer);

        DatabaseController databaseController;
        try {
            databaseController = new DatabaseController("data.db");
        } catch (DatabaseException e) {
            Log.e("Main", "Failed to open database", e);
            return;
        }
        var db = new DatabaseRepository(databaseController);


//        TaskManager manager = new TaskManager();
//        manager.addTask((new Task("Info2 ÜB08", "Blatt erledigen", LocalDateTime.of(2026, 6, 10, 12, 0), TaskPriority.Mid)));
//        manager.addTask((new Task("Einkaufen", "4 Äpfel und 1 Brot", LocalDateTime.of(2026, 7, 1, 12, 0), TaskPriority.Low)));
//        manager.addTask((new Task("Sport machen", "Laufen gehen", LocalDateTime.of(2026, 5, 5, 12, 0), TaskPriority.High)));
//        manager.addTask((new Task("Info ÜB06", "Blatt erledigen", LocalDateTime.of(2026, 6, 1, 12, 0), TaskPriority.Mid)));
//        manager.addTask((new Task("Wäsche waschen", "Alles in die Waschmaschine", LocalDateTime.of(2025, 6, 30, 12, 0), TaskPriority.Mid)));
//        Task putzen = new Task("Putzen", "So richtig ordentlich inkl. Bad", LocalDateTime.of(2025, 6, 30, 12, 0), TaskPriority.Mid);
//        putzen.setState(TaskState.Finished);
//        manager.addTask(putzen);

        /*
        try {
            db.upsertTask(Task.create("Info2 ÜB08", "Blatt erledigen", TaskState.Pending, TaskPriority.Mid, LocalDateTime.of(2026, 6, 10, 12, 0)));
            db.upsertTask(Task.create("Einkaufen", "4 Äpfel und 1 Brot", TaskState.Pending, TaskPriority.Low, LocalDateTime.of(2026, 7, 1, 12, 0)));
            db.upsertTask(Task.create("Sport machen", "Laufen gehen", TaskState.Pending, TaskPriority.High, LocalDateTime.of(2026, 5, 5, 12, 0)));
            db.upsertTask(Task.create("Info ÜB06", "Blatt erledigen", TaskState.Pending, TaskPriority.Mid, LocalDateTime.of(2026, 6, 1, 12, 0)));
            db.upsertTask(Task.create("Wäsche waschen", "Alles in die Waschmaschine", TaskState.Pending, TaskPriority.Mid, LocalDateTime.of(2025, 6, 30, 12, 0)));
            Task putzen = Task.create("Putzen", "So richtig ordentlich inkl. Bad", TaskState.Finished, TaskPriority.Mid, LocalDateTime.of(2025, 6, 30, 12, 0));
            db.upsertTask(putzen);
        } catch (DatabaseException e) {
            Log.e("Main", "Failed to create tasks", e);
            return;
        }
         */



        MainFrame frame = new MainFrame(db);


    }



}
