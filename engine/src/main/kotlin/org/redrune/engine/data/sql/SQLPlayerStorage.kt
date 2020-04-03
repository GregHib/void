package org.redrune.engine.data.sql

import com.github.michaelbull.logging.InlineLogger
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.dsl.module
import org.redrune.engine.data.StorageStrategy
import org.redrune.engine.entity.model.Player
import org.redrune.engine.model.Tile
import org.redrune.utility.DetailsTable
import org.redrune.utility.PlayersTable

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
class SQLPlayerStorage : StorageStrategy<Player> {

    private val logger = InlineLogger()

    init {
        // TODO should this be external?
        //      How to get details from a secure place. Program args?
        Database.connect("jdbc:postgresql://localhost:5432/testdb", driver = "org.postgresql.Driver", user = "postgres", password = "abc123")
        logger.info { "Database connection established." }
    }

    override fun load(name: String): Player? {
        return transaction {
            val details = DetailsTable.select { DetailsTable.username eq name }.firstOrNull()
            if (details == null) {
                return@transaction null
            } else {
                val playerId = details[DetailsTable.id]
                val player = PlayersTable.select { PlayersTable.id eq playerId }.firstOrNull() ?: throw IllegalStateException("Unable to find player $name")
                val x = player[PlayersTable.x]
                val y = player[PlayersTable.y]
                val plane = player[PlayersTable.plane]
                val tile = Tile(x, y, plane)
                Player(playerId.value, tile)
            }
        }
    }

    override fun save(name: String, data: Player) {
        //TODO update everything
        PlayersTable.update({ PlayersTable.id eq data.id }) {
            it[x] = data.tile.x
            it[y] = data.tile.y
            it[plane] = data.tile.plane
        }
    }

}

val sqlPlayerModule = module {
    single { SQLPlayerStorage() as StorageStrategy<Player> }
}
