package dev.ecasept.unitodo.shared.db.querybuilder.batch;

import java.util.ArrayList;

public class Batcher {
    private int placeholderCount = 0;
    private final ArrayList<BatcherPlaceholder> placeholders = new ArrayList<>();
    public BatcherPlaceholder placeholder() {
        var placeholder = new BatcherPlaceholder(placeholderCount++);
        placeholders.add(placeholder);
        return placeholder;
    }
    public void fill(Object... values) {
        if (values.length != placeholders.size()) {
            throw new IllegalArgumentException("Expected " + placeholders.size() + " values, got " + values.length);
        }
        for (int i = 0; i < values.length; i++) {
            placeholders.get(i).obj = values[i];
        }
    }
}
