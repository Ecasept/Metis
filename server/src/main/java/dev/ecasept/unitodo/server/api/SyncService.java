package dev.ecasept.unitodo.server.api;

import com.sun.net.httpserver.Headers;
import dev.ecasept.unitodo.server.Configuration;
import dev.ecasept.unitodo.server.db.ServerDatabaseRepository;
import dev.ecasept.unitodo.server.serverlib.Response;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.api.ApiResponse;
import dev.ecasept.unitodo.shared.models.api.ErrorCode;
import dev.ecasept.unitodo.shared.models.api.SyncRequest;
import dev.ecasept.unitodo.shared.models.api.SyncResponse;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.ServerTask;
import dev.ecasept.unitodo.shared.sync.Synchronizer;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SyncService {
    private final String TAG = "SyncService";
    private final Synchronizer synchronizer;
    private final Auth auth;
    private final Configuration config;
    private final ServerDatabaseRepository db;
    public SyncService(ServerDatabaseRepository db, Synchronizer synchronizer, Auth auth, Configuration config) {
        this.db = db;
        this.synchronizer = synchronizer;
        this.auth = auth;
        this.config = config;
    }

    public Response<ApiResponse<SyncResponse>> syncRequest(SyncRequest request, Headers headers) throws DatabaseException {
        var authResult = auth.verifyAuth(headers);
        if (!authResult.isValid()) {
            return new Response<>(401, ApiResponse.error("Unauthorized: " + authResult.errorCode().getMessage(), authResult.errorCode()));
        }
        var userId = authResult.userId();

        try {
            return db.transaction(() -> {
                var serverTaskList = db.getTasks(Arrays.stream(request.tasks()).map(ClientTask::uuid).toList(), userId);

                var serverTasks = serverTaskList.stream().collect(Collectors.toMap(ServerTask::uuid, Function.identity()));
                var clientTasks = Arrays.stream(request.tasks()).collect(Collectors.toMap(ClientTask::uuid, Function.identity()));

                ServerTask[] modifiedServerTasks;
                if (request.lastSyncTime().isPresent()) {
                    modifiedServerTasks = db.getTasksModifiedSince(request.lastSyncTime().get(), userId);
                } else {
                    modifiedServerTasks = db.getAllTasks(userId);
                }

                var newTasks = synchronizer.synchronizeServer(serverTasks, clientTasks, userId);
                db.upsertTasks(newTasks);

                var responseDelta = Arrays.stream(modifiedServerTasks).map(ServerTask::toClientTask).toArray(ClientTask[]::new);

                Optional<List<UUID>> presentList = Optional.empty();
                if (request.lastSyncTime().isEmpty() || request.lastSyncTime().get().isBefore(LocalDateTime.now().minus(config.TOMBSTONE_TTL()))) {
                    presentList = Optional.of(db.getAllTaskUUIDs(userId));
                }

                var response = new SyncResponse(responseDelta, presentList.map(list -> list.toArray(UUID[]::new)));
                return new Response<>(200, ApiResponse.success(response));
            });
        } catch (SQLException e) {
            throw new DatabaseException("Failed to synchronize tasks due to a database error", e);
        }
    }
}
