package com.restaurant.storage.orm.operators

import org.ktorm.expression.ScalarExpression
import org.ktorm.schema.IntSqlType
import org.ktorm.schema.SqlType
import org.ktorm.schema.TimestampSqlType
import java.sql.Timestamp
import java.time.LocalDateTime

data class NowExpression(
    override val sqlType: SqlType<Timestamp> = TimestampSqlType,
    override val isLeafNode: Boolean = false,
    override val extraProperties: Map<String, Any> = mapOf()
): ScalarExpression<Timestamp>()

fun now(): NowExpression = NowExpression()
