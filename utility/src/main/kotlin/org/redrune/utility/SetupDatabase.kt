package org.redrune.utility

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
object SetupDatabase {
    @JvmStatic
    fun main(args: Array<String>) {
        // TODO get database settings from somewhere else
        Database.connect("jdbc:postgresql://localhost:5432/testdb", driver = "org.postgresql.Driver", user = "postgres", password = "abc123")

        transaction {
            SchemaUtils.create(DetailsTable)
            SchemaUtils.create(PlayersTable)
        }
    }
}

object DetailsTable : IntIdTable() {
    val username = text("username")
    val name = text("name")
    val title = text("title")
    val passwordHash = text("passhash")
}

object PlayersTable : Table() {
    val id = integer("id")
    val x = integer("x")
    val y = integer("y")
    val plane = integer("plane")
    override val primaryKey = PrimaryKey(id, name = "PK_Players_ID")
}