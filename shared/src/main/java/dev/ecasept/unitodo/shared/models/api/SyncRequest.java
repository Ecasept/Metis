package dev.ecasept.unitodo.shared.models.api;

import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;

import java.time.LocalDateTime;
import java.util.Optional;

@Serializable
public record SyncRequest(@Field(tag=1) ClientTask[] tasks, @Field(tag=2) Optional<LocalDateTime> lastSyncTime) {

}
