package dev.ecasept.unitodo.server.security;

import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;
import java.util.UUID;

/** Signed session claims. Expiry is measured in UTC epoch seconds. */
@Serializable
public record SessionToken(@Field(tag=1) UUID userId, @Field(tag=2) long expiresAt) {}
