package dev.ecasept.unitodo.shared.models.api;

import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;

@Serializable
public record UsernameAndPassword(@Field(tag=1) String username, @Field(tag=2) Password password) {}
