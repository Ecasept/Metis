package dev.ecasept.unitodo.server.serverlib;

public record Response<T>(int code, T body) {
}
