package dev.ecasept.unitodo.server.api;

import com.sun.net.httpserver.Headers;
import dev.ecasept.unitodo.server.Configuration;
import dev.ecasept.unitodo.server.db.ServerDatabaseRepository;
import dev.ecasept.unitodo.server.security.SignedTokenService;
import dev.ecasept.unitodo.server.serverlib.Response;
import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.models.api.ApiResponse;
import dev.ecasept.unitodo.shared.models.api.SyncRequest;
import dev.ecasept.unitodo.shared.models.api.SyncResponse;
import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.models.db.ServerTask;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SyncService {
    private final String TAG = "SyncService";
    private final ServerSynchronizer synchronizer;
    private final SignedTokenService tokenService;
    private final Configuration config;
    private final ServerDatabaseRepository db;
    public SyncService(ServerDatabaseRepository db, ServerSynchronizer synchronizer, SignedTokenService tokenService, Configuration config) {
        this.db = db;
        this.synchronizer = synchronizer;
        this.tokenService = tokenService;
        this.config = config;
    }

    public Response<ApiResponse<SyncResponse>> syncRequest(SyncRequest request, Headers headers) throws DatabaseException {
        var userIdOptional = Auth.verifyAuth(headers, tokenService, config.SECRET_KEY());
        if (userIdOptional.isEmpty()) {
            return new Response<>(401, ApiResponse.error("Unauthorized: Invalid session token"));
        }
        var userId = userIdOptional.get();

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

                var newTasks = synchronizer.synchronize(serverTasks, clientTasks, userId);
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
