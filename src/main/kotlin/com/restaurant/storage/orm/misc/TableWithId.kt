package com.restaurant.storage.orm.misc

import org.ktorm.entity.Entity
import org.ktorm.schema.Column
import org.ktorm.schema.Table

interface EntityWithId<T: EntityWithId<T>> : Entity<T> {
    val id: Long
}

abstract class TableWithId<T: EntityWithId<T>>(tableName: String, alias: String?) : Table<T>(tableName, alias) {
    abstract val id: Column<Long>
}