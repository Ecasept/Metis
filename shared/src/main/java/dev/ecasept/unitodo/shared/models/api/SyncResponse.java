package dev.ecasept.unitodo.shared.models.api;

import dev.ecasept.unitodo.shared.models.db.ClientTask;
import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;

import java.util.Optional;
import java.util.UUID;

@Serializable
public record SyncResponse(@Field(tag=1) ClientTask[] tasks, @Field(tag=2) Optional<UUID[]> presentList) {
    
}
