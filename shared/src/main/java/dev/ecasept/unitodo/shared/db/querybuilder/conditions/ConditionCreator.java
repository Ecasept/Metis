package dev.ecasept.unitodo.shared.db.querybuilder.conditions;

import java.util.List;
import java.util.function.Consumer;

public class ConditionCreator {
    private final Consumer<Condition> newConditionFn;
    private Condition condition = null;
    public ConditionCreator() {
        newConditionFn = (condition) -> {
            if (this.condition == null) {
                this.condition = condition;
            } else {
                this.condition = new AndCondition(this.condition, condition);
            }
        };
    }
    public ConditionCreator(Consumer<Condition> newConditionFn) {
        this.newConditionFn = newConditionFn;
    }


    public void addNewCondition(Condition condition) {
        newConditionFn.accept(condition);
    }

    public <T> ConditionCreator eq(String column, T value) {
        addNewCondition(new EqualsCondition<>(column, value));
        return this;
    }
    public <T> ConditionCreator eqAny(String column, List<T> values) {
        addNewCondition(new InCondition<>(column, values));
        return this;
    }
    public <T> ConditionCreator ge(String column, T value) {
        addNewCondition(new GreaterThanOrEqualsCondition<>(column, value));
        return this;
    }
    public Condition getCondition() {
        return this.condition;
    }
    @SafeVarargs
    public final ConditionCreator or(Consumer<ConditionCreator>... conditionFns) {
        if (conditionFns.length == 0) return this;
        Condition combined = null;
        for (Consumer<ConditionCreator> conditionFn : conditionFns) {
            var conditionCreator = new ConditionCreator();
            conditionFn.accept(conditionCreator);
            if (conditionCreator.getCondition() == null) {
                throw new IllegalArgumentException("Condition cannot be null");
            }
            if (combined == null) {
                combined = conditionCreator.getCondition();
            } else {
                combined = new OrCondition(combined, conditionCreator.getCondition());
            }
        }
        addNewCondition(combined);
        return this;
    }
    public final ConditionCreator defaultOr(Consumer<ConditionCreator> conditionFn) {
        var conditionCreator = new ConditionCreator(
                (condition) -> {
                    if (this.condition == null) {
                        this.condition = condition;
                    } else {
                        this.condition = new OrCondition(this.condition, condition);
                    }
                }
        );
        conditionFn.accept(conditionCreator);
        if (conditionCreator.getCondition() == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        addNewCondition(conditionCreator.getCondition());
        return this;
    }
}
