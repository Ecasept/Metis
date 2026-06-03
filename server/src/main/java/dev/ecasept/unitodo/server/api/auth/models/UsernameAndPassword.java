package dev.ecasept.unitodo.server.api.auth.models;

import dev.ecasept.unitodo.models.Password;
import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;

@Serializable
public record UsernameAndPassword(@Field(tag=1) String username, @Field(tag=2) Password password) {}
