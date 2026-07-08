package dev.ecasept.unitodo.shared.db.querybuilder.expressions;

import dev.ecasept.unitodo.shared.db.querybuilder.expressions.caseexpr.CaseExpressionWithoutBase;

/**
 * Utility class for creating SQL expressions.
 */
public class E {
    /** Returns a builder for a new CASE expression that does not have a base expression. */
    public static CaseExpressionWithoutBase caseWithoutBase() {
        return new CaseExpressionWithoutBase();
    }
}
