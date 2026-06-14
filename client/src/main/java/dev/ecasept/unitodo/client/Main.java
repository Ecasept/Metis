package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.client.api.ApiClient;
import dev.ecasept.unitodo.client.api.HttpClientFactory;
import dev.ecasept.unitodo.client.db.ClientDatabaseRepository;
import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.QueryBuilder;
import dev.ecasept.unitodo.shared.models.api.ApiResponse;
import dev.ecasept.unitodo.shared.models.api.ApiResponseAdapter;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
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
            databaseController = new DatabaseController(Main.class.getClassLoader(), "jdbc:sqlite:data.db");
        } catch (DatabaseException e) {
            Log.e("Main", "Failed to open database", e);
            return;
        }
        try {
            var queryBuilder = new QueryBuilder(databaseController);
            var db = new ClientDatabaseRepository(queryBuilder);

            try {
                db.upsertTask(ClientTask.create("Info2 ÜB08", "Blatt erledigen", TaskState.Pending, TaskPriority.Mid, LocalDateTime.of(2026, 6, 10, 12, 0)));
                db.upsertTask(ClientTask.create("Einkaufen", "4 Äpfel und 1 Brot", TaskState.Pending, TaskPriority.Low, LocalDateTime.of(2026, 7, 1, 12, 0)));
                db.upsertTask(ClientTask.create("Sport machen", "Laufen gehen", TaskState.Pending, TaskPriority.High, LocalDateTime.of(2026, 5, 5, 12, 0)));
                db.upsertTask(ClientTask.create("Info ÜB06", "Blatt erledigen", TaskState.Pending, TaskPriority.Mid, LocalDateTime.of(2026, 6, 1, 12, 0)));
                db.upsertTask(ClientTask.create("Wäsche waschen", "Alles in die Waschmaschine", TaskState.Pending, TaskPriority.Mid, LocalDateTime.of(2025, 6, 30, 12, 0)));
                ClientTask putzen = ClientTask.create("Putzen", "So richtig ordentlich inkl. Bad", TaskState.Finished, TaskPriority.Mid, LocalDateTime.of(2025, 6, 30, 12, 0));
                db.upsertTask(putzen);
            } catch (DatabaseException e) {
                Log.e("Main", "Failed to create tasks", e);
                return;
            }

            MainFrame frame = new MainFrame(db);

        } catch (Exception e) {
            try {
                databaseController.close();
            } catch (Exception closeException) {
                e.addSuppressed(closeException);
            }
             Log.e("Main", "An error occurred", e);
        }
    }
}
