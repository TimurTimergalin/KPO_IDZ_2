package com.restaurant.storage.orm.operators

import org.ktorm.database.Database
import org.ktorm.expression.SqlExpression
import org.ktorm.support.postgresql.PostgreSqlFormatter

class CustomSqlFormatter(database: Database, beautifySql: Boolean, indentSize: Int) :
    PostgreSqlFormatter(database, beautifySql, indentSize) {
    override fun visitUnknown(expr: SqlExpression): SqlExpression {
        if (expr is ExtractExpression) {
            write("extract (${expr.field} from ")
            visit(expr.time)
            removeLastBlank()
            write(") ")
            return expr
        } else if (expr is NowExpression) {
            write("now() ")
            return expr
        } else {
            return super.visitUnknown(expr)
        }
    }
}