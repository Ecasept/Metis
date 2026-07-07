package dev.ecasept.unitodo.shared.db.querybuilder.expressions.caseexpr;

import dev.ecasept.unitodo.shared.db.DatabaseException;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.SqlExpression;
import dev.ecasept.unitodo.shared.db.querybuilder.expressions.conditions.Condition;

import java.util.ArrayList;
import java.util.List;

public class CaseExpressionWithoutBase implements SqlExpression {
    record CaseWhen(Condition condition, SqlExpression result) {}
    private final List<CaseWhen> cases = new ArrayList<>();
    private SqlExpression elseCase = null;
    public CaseExpressionWithoutBase() { }

    @Override
    public String asParameterizedSql() {
        if (cases.isEmpty()) {
            throw new IllegalStateException("No cases defined for CASE expression");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("CASE");
        for (var c : cases) {
            sb.append(" WHEN ").append(c.condition.asParameterizedSql()).append(" THEN ").append(c.result.asParameterizedSql());
        }
        if (elseCase != null) {
            sb.append(" ELSE ").append(elseCase.asParameterizedSql());
        }
        return sb.append(" END").toString();
    }

    @Override
    public int fillParameters(java.sql.PreparedStatement statement, int i) throws DatabaseException {
        for (var c : cases) {
            i = c.condition.fillParameters(statement, i);
            i = c.result.fillParameters(statement, i);
        }
        if (elseCase != null) {
            i = elseCase.fillParameters(statement, i);
        }
        return i;
    }

    public CaseExpressionWithoutBase when(Condition condition, SqlExpression result) {
        cases.add(new CaseWhen(condition, result));
        return this;
    }

    public CaseExpressionWithoutBase elseCase(SqlExpression elseCase) {
        this.elseCase = elseCase;
        return this;
    }
}
