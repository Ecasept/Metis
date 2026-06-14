package dev.ecasept.unitodo.shared.db.querybuilder.batch;

public class BatcherPlaceholder {
    public Object obj;

    public BatcherPlaceholder(Object obj) {
        this.obj = obj;
    }

    @Override
    public String toString() {
        return obj.toString();
    }
}
