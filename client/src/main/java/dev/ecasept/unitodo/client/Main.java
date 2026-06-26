package dev.ecasept.unitodo.client;

import dev.ecasept.unitodo.build.BuildConfig;
import dev.ecasept.unitodo.client.api.ApiClient;
import dev.ecasept.unitodo.client.api.HttpClientFactory;
import dev.ecasept.unitodo.client.db.ClientDatabaseRepository;
import dev.ecasept.unitodo.client.sync.Synchronizer;
import dev.ecasept.unitodo.shared.db.DatabaseController;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.QueryBuilder;
import dev.ecasept.unitodo.shared.models.api.ApiResponse;
import dev.ecasept.unitodo.shared.models.api.ApiResponseAdapter;
import dev.ecasept.unitodo.shared.serialization.Serializer;
import dev.ecasept.unitodo.shared.utils.Log;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        var serializer = Serializer.createDefault().adapter(ApiResponseAdapter.class, ApiResponse.class);
        var httpClient = HttpClientFactory.createClient();
        var apiClient = new ApiClient(httpClient, BuildConfig.BASE_URL, serializer);

        DatabaseController databaseController;
        try {
            databaseController = new DatabaseController(Main.class.getClassLoader(), "jdbc:sqlite:data.db");
        } catch (DatabaseException e) {
            Log.e("Main", "Failed to open database", e);
            return;
        }

        var queryBuilder = new QueryBuilder(databaseController);
        var db = new ClientDatabaseRepository(queryBuilder);
        var synchronizer = new Synchronizer(db, apiClient);
        var dataManager = new DataManager(db, apiClient, synchronizer);

        MainFrame frame = new MainFrame(dataManager);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    databaseController.close();
                } catch (DatabaseException ex) {
                    Log.e("Main", "Failed to close database", ex);
                }
            }
        });
    }
}
