package world.gregs.voidps.web.index

import org.jetbrains.exposed.sql.Table

object AuditLogTable  : Table("audit_log") {
    val timestamp = long("timestamp")
    val tick = long("tick")
    val logSource = varchar("source", 255)
    val action = varchar("action", 128)
    val context = text("context").nullable()
}
