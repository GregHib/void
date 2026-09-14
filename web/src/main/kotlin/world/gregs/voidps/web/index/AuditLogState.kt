package world.gregs.voidps.web.index

import org.jetbrains.exposed.sql.Table

object AuditLogState : Table("audit_log_state") {
    val key = varchar("key", 64)
    val value = varchar("value", 128)
    override val primaryKey = PrimaryKey(key)
}