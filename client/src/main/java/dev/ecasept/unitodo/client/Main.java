package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.client.api.ApiClient;
import dev.ecasept.unitodo.client.api.HttpClientFactory;
import dev.ecasept.unitodo.client.db.ClientDatabaseRepository;
import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.QueryBuilder;
import dev.ecasept.unitodo.shared.models.api.ApiResponse;
import dev.ecasept.unitodo.shared.models.api.ApiResponseAdapter;
import dev.ecasept.unitodo.shared.serialization.Serializer;
import dev.ecasept.unitodo.shared.utils.Log;

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

        try (databaseController) {
            var queryBuilder = new QueryBuilder(databaseController);
            var db = new ClientDatabaseRepository(queryBuilder);

            MainFrame frame = new MainFrame(db);

        } catch (DatabaseException e) {
            Log.e("Main", "An error occurred while running the application", e);
        }
    }
}
