package dev.ecasept.unitodo.models;

import dev.ecasept.unitodo.shared.serialization.annotations.Field;
import dev.ecasept.unitodo.shared.serialization.annotations.Serializable;

import java.util.Arrays;

@Serializable
public class Password {
    @Field(tag=1)
    public char[] pw;
    public Password(char[] pw) {
        this.pw = pw;
    }
    public void shred() {
        Arrays.fill(pw, ' ');
    }
}
