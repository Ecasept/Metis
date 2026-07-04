package dev.ecasept.unitodo.shared.db.querybuilder.conditions;

import dev.ecasept.unitodo.shared.db.querybuilder.conditions.OrCondition;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConditionCreator {
    private final Consumer<Condition> newConditionFn;
    private Condition condition = null;

    public void addConditionWithOr(Condition newCondition) {
        if (this.condition == null) {
            this.condition = newCondition;
        } else {
            this.condition = new OrCondition(this.condition, newCondition);
        }
    }

    public void addConditionWithAnd(Condition newCondition) {
        if (this.condition == null) {
            this.condition = newCondition;
        } else {
            this.condition = new AndCondition(this.condition, newCondition);
        }
    }

    public ConditionCreator() {
        newConditionFn = this::addConditionWithAnd;
    }
    public ConditionCreator(Function<ConditionCreator, Consumer<Condition>> newConditionFnSupplier) {
        this.newConditionFn = newConditionFnSupplier.apply(this);
    }


    public void addNewCondition(Condition condition) {
        newConditionFn.accept(condition);
    }

    public <T> ConditionCreator eq(String column, T value) {
        addNewCondition(new EqualsCondition<>(column, value));
        return this;
    }
    public ConditionCreator not(Consumer<ConditionCreator> conditionFn) {
        var conditionCreator = new ConditionCreator();
        conditionFn.accept(conditionCreator);
        if (conditionCreator.getCondition() == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        addNewCondition(new NotCondition(conditionCreator.getCondition()));
        return this;
    }
    public <T> ConditionCreator contains(String column, String pattern) {
        addNewCondition(new LikeCondition(column, "%" + pattern + "%"));
        return this;
    }
    public <T> ConditionCreator eqAny(String column, List<T> values) {
        if (values.isEmpty()) {
            addNewCondition(new FalseCondition());
        } else {
            addNewCondition(new InCondition<>(column, values));
        }
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
                (self) -> self::addConditionWithOr
        );
        conditionFn.accept(conditionCreator);
        if (conditionCreator.getCondition() == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }
        addNewCondition(conditionCreator.getCondition());
        return this;
    }
}