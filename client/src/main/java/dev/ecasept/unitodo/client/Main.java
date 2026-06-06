package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.client.api.ApiClient;
import dev.ecasept.unitodo.client.api.HttpClientFactory;
import dev.ecasept.unitodo.models.Task;
import dev.ecasept.unitodo.models.TaskManager;
import dev.ecasept.unitodo.models.TaskPriority;
import dev.ecasept.unitodo.models.TaskState;
import dev.ecasept.unitodo.shared.models.api.ApiResponse;
import dev.ecasept.unitodo.shared.models.api.ApiResponseAdapter;
import dev.ecasept.unitodo.shared.serialization.Serializer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        var serializer = Serializer.createDefault().adapter(ApiResponseAdapter.class, ApiResponse.class);
        var httpClient = HttpClientFactory.createClient();
        var apiClient = new ApiClient(httpClient, "https://localhost:6767/api", serializer);

        // Example
        var res = apiClient.login("testuser", "password123");
        res.on(
                sessionToken -> {
                    Log.i("Main", "Login successful! Session token: " + sessionToken);
                    // Save session token
                },
                errorMessage -> {
                    Log.e("Main", "Login failed: " + errorMessage);
                }
        );


        TaskManager manager = new TaskManager();
        manager.addTask((new Task("Info2 ÜB08", "Blatt erledigen", LocalDateTime.of(2026, 6, 10, 12, 0), TaskPriority.Mid)));
        manager.addTask((new Task("Einkaufen", "4 Äpfel und 1 Brot", LocalDateTime.of(2026, 7, 1, 12, 0), TaskPriority.Low)));
        manager.addTask((new Task("Sport machen", "Laufen gehen", LocalDateTime.of(2026, 5, 5, 12, 0), TaskPriority.High)));
        manager.addTask((new Task("Info ÜB06", "Blatt erledigen", LocalDateTime.of(2026, 6, 1, 12, 0), TaskPriority.Mid)));
        manager.addTask((new Task("Wäsche waschen", "Alles in die Waschmaschine", LocalDateTime.of(2025, 6, 30, 12, 0), TaskPriority.Mid)));
        Task putzen = new Task("Putzen", "So richtig ordentlich inkl. Bad", LocalDateTime.of(2025, 6, 30, 12, 0), TaskPriority.Mid);
        putzen.setState(TaskState.Finished);
        manager.addTask(putzen);

        MainFrame frame = new MainFrame(manager);


    }



}
