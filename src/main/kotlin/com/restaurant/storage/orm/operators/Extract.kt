package com.restaurant.storage.orm.operators

import org.ktorm.expression.ScalarExpression
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.IntSqlType
import org.ktorm.schema.SqlType

data class ExtractExpression(
    val field: String,
    val time: ScalarExpression<*>,
    override val sqlType: SqlType<Int> = IntSqlType,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = mapOf()
) : ScalarExpression<Int>()


fun extract(field: String, time: ColumnDeclaring<*>): ExtractExpression {
    return ExtractExpression(field, time.asExpression())
}



