package world.gregs.voidps.engine.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.sql.*
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile

fun main() {
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://localhost:5432/testDB?reWriteBatchedInserts=true"
        driverClassName = "org.postgresql.Driver"
        username = "postgres"
        password = "password"
        maximumPoolSize = 6
        // as of version 0.46.0, if these options are set here, they do not need to be duplicated in DatabaseConfig
        isReadOnly = false
        transactionIsolation = "TRANSACTION_SERIALIZABLE"
    }

    // Initialize the postgresql database

    Database.connect(HikariDataSource(config))

    // Create tables if they don't exist
    transaction {
        create(AccountsTable, ExperienceTable, LevelsTable, VariablesTable, InventoriesTable, inBatch = true)
    }

    // Example of saving PlayerSave object to the database
    val playerSaves = listOf(PlayerSave(
        name = "Jane",
        password = "password12345678",
        tile = Tile(123, 456, 1),
        experience = DoubleArray(25).apply {
            this[4] = 1024.0
            this[0] = 300.0
            this[1] = 400.0
        },
        blocked = listOf(Skill.Herblore, Skill.Prayer),
        levels = IntArray(25).apply {
            this[4] = 10
            this[0] = 4
            this[1] = 5
        },
        male = false,
        looks = intArrayOf(1, 2, 3, 5, 6),
        colours = intArrayOf(1, 2, 3, 7, 8),
        variables = mapOf(
            "clan_name" to "clan",
            "display_name" to "test2",
            /*"clan_talk_rank" to "Friend",
            "clan_join_rank" to "Anyone",
            "coin_share_setting" to true,
            "clan_kick_rank" to "Sergeant",
            "variable4" to listOf(1, 2, 3),
            "variable5" to listOf("one", "two", "three")*/
        ),
        inventories = mapOf("inventory1" to arrayOf(Item("item1", 5, def = ItemDefinition.EMPTY), Item.EMPTY, Item("item2", 3, def = ItemDefinition.EMPTY))),
        friends = mapOf("friend11" to ClanRank.Friend, "friend12" to ClanRank.None, "friend24" to ClanRank.Friend),
        ignores = listOf("enemy1", "enemy2")
    ))
}