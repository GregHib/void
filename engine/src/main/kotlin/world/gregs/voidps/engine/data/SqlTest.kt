package world.gregs.voidps.engine.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile

object PlayerSaves : Table("players") {
    val id = integer("id").autoIncrement().uniqueIndex()
    val name = varchar("name", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val tile = integer("tile")
    val blockedSkills = array<Int>("blocked")
    val male = bool("male")
    val looks = array<Int>("looks")
    val colours = array<Int>("colours")
    val friends = array<String>("friends")
    val ranks = array<String>("ranks")
    val ignores = array<String>("ignores")

    override val primaryKey = PrimaryKey(id, name = "pk_player_name")
}

object Experience : Table() {
    val playerId = integer("player_id").references(PlayerSaves.id).uniqueIndex()
    val attack = double("attack").default(0.0)
    val defence = double("defence").default(0.0)
    val strength = double("strength").default(0.0)
    val constitution = double("constitution").default(0.0)
    val ranged = double("ranged").default(0.0)
    val prayer = double("prayer").default(0.0)
    val magic = double("magic").default(0.0)
    val cooking = double("cooking").default(0.0)
    val woodcutting = double("woodcutting").default(0.0)
    val fletching = double("fletching").default(0.0)
    val fishing = double("fishing").default(0.0)
    val firemaking = double("firemaking").default(0.0)
    val crafting = double("crafting").default(0.0)
    val smithing = double("smithing").default(0.0)
    val mining = double("mining").default(0.0)
    val herblore = double("herblore").default(0.0)
    val agility = double("agility").default(0.0)
    val thieving = double("thieving").default(0.0)
    val slayer = double("slayer").default(0.0)
    val farming = double("farming").default(0.0)
    val runecrafting = double("runecrafting").default(0.0)
    val hunter = double("hunter").default(0.0)
    val construction = double("construction").default(0.0)
    val summoning = double("summoning").default(0.0)
    val dungeoneering = double("dungeoneering").default(0.0)
}

object Levels : Table() {
    val playerId = integer("player_id").references(PlayerSaves.id).uniqueIndex()
    val attack = integer("attack").default(1)
    val defence = integer("defence").default(1)
    val strength = integer("strength").default(1)
    val constitution = integer("constitution").default(1)
    val ranged = integer("ranged").default(1)
    val prayer = integer("prayer").default(1)
    val magic = integer("magic").default(1)
    val cooking = integer("cooking").default(1)
    val woodcutting = integer("woodcutting").default(1)
    val fletching = integer("fletching").default(1)
    val fishing = integer("fishing").default(1)
    val firemaking = integer("firemaking").default(1)
    val crafting = integer("crafting").default(1)
    val smithing = integer("smithing").default(1)
    val mining = integer("mining").default(1)
    val herblore = integer("herblore").default(1)
    val agility = integer("agility").default(1)
    val thieving = integer("thieving").default(1)
    val slayer = integer("slayer").default(1)
    val farming = integer("farming").default(1)
    val runecrafting = integer("runecrafting").default(1)
    val hunter = integer("hunter").default(1)
    val construction = integer("construction").default(1)
    val summoning = integer("summoning").default(1)
    val dungeoneering = integer("dungeoneering").default(1)
}

object Variables : Table() {
    val playerId = integer("player_id").references(PlayerSaves.id)
    val variableName = varchar("variable_name", 100)
    val variableType = varchar("variable_type", 20)
    val variableValue = varchar("variable_value", 100)

    init {
        index(true, playerId, variableName)
    }
}

object Inventories : Table() {
    val playerId = integer("player_id").references(PlayerSaves.id)
    val inventoryName = varchar("inventory_name", 100)
    val items = array<String>("items")
    val amounts = array<Int>("amounts")

    init {
        index(true, playerId, inventoryName)
    }
}

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
        create(PlayerSaves, Experience, Levels, Variables, Inventories, inBatch = true)
    }


    // Example of saving PlayerSave object to the database
    val playerSaves = listOf(PlayerSave(
        name = "John2",
        password = "123",
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
        male = true,
        looks = intArrayOf(1, 2, 3),
        colours = intArrayOf(1, 2, 3),
        variables = mapOf("clan_name" to "value1", "display_name" to "test", "variable2" to 123, "variable3" to true, "variable4" to listOf(1, 2, 3), "variable5" to listOf("one", "two", "three")),
        inventories = mapOf("inventory1" to arrayOf(Item("item1", 5, def = ItemDefinition.EMPTY), Item.EMPTY, Item("item2", 3, def = ItemDefinition.EMPTY))),
        friends = mapOf("friend1" to ClanRank.Friend, "friend2" to ClanRank.None),
        ignores = listOf()
    ))

    var start = System.currentTimeMillis()
    val clans = loadClans() // 2000 - 5s
    println("${clans.size} clans took: ${System.currentTimeMillis() - start}ms")
    println(clans.firstOrNull())
    start = System.currentTimeMillis()
    savePlayers(Array(2000) { playerSaves.first().copy(name = "John${it}") }.toList())
    println("Save 2000 took: ${System.currentTimeMillis() - start}ms")
    // insert - 22.6s, update - 22.7s
    repeat(50) {
        start = System.currentTimeMillis()
        savePlayers(playerSaves)
        println("Save took: ${System.currentTimeMillis() - start}ms")
    }

    // Example of loading PlayerSave object from the database
//    repeat(50) {
    start = System.currentTimeMillis()
    val loadedPlayer = loadPlayer("John5")
    println("Load took: ${System.currentTimeMillis() - start}ms")
//    println(loadedPlayer)
//    }
}

fun savePlayers(playerSaves: List<PlayerSave>) {
    transaction {
        PlayerSaves.batchUpsert(playerSaves, PlayerSaves.name) { playerSave ->
            this[PlayerSaves.name] = playerSave.name
            this[PlayerSaves.passwordHash] = playerSave.password
            this[PlayerSaves.tile] = playerSave.tile.id
            this[PlayerSaves.blockedSkills] = playerSave.blocked.map { skill -> skill.ordinal }
            this[PlayerSaves.male] = playerSave.male
            this[PlayerSaves.looks] = playerSave.looks.toList()
            this[PlayerSaves.colours] = playerSave.colours.toList()
            this[PlayerSaves.ignores] = playerSave.ignores
            val friends = playerSave.friends.toList()
            this[PlayerSaves.friends] = friends.map { it.first }
            this[PlayerSaves.ranks] = friends.map { it.second.name }
        }
        val names = playerSaves.map { it.name }
        val playerIds = PlayerSaves
            .select(PlayerSaves.id, PlayerSaves.name)
            .where { PlayerSaves.name inList names }
            .associate { it[PlayerSaves.name] to it[PlayerSaves.id] }
        Experience.batchUpsert(playerSaves, Experience.playerId) { playerSave ->
            this[Experience.playerId] = playerIds.getValue(playerSave.name)
            val experience = playerSave.experience
            this[Experience.attack] = experience[0]
            this[Experience.defence] = experience[1]
            this[Experience.strength] = experience[2]
            this[Experience.constitution] = experience[3]
            this[Experience.ranged] = experience[4]
            this[Experience.prayer] = experience[5]
            this[Experience.magic] = experience[6]
            this[Experience.cooking] = experience[7]
            this[Experience.woodcutting] = experience[8]
            this[Experience.fletching] = experience[9]
            this[Experience.fishing] = experience[10]
            this[Experience.firemaking] = experience[11]
            this[Experience.crafting] = experience[12]
            this[Experience.smithing] = experience[13]
            this[Experience.mining] = experience[14]
            this[Experience.herblore] = experience[15]
            this[Experience.agility] = experience[16]
            this[Experience.thieving] = experience[17]
            this[Experience.slayer] = experience[18]
            this[Experience.farming] = experience[19]
            this[Experience.runecrafting] = experience[20]
            this[Experience.hunter] = experience[21]
            this[Experience.construction] = experience[22]
            this[Experience.summoning] = experience[23]
            this[Experience.dungeoneering] = experience[24]
        }
        Levels.batchUpsert(playerSaves, Levels.playerId) { playerSave ->
            this[Levels.playerId] = playerIds.getValue(playerSave.name)
            val levels = playerSave.levels
            this[Levels.attack] = levels[0]
            this[Levels.defence] = levels[1]
            this[Levels.strength] = levels[2]
            this[Levels.constitution] = levels[3]
            this[Levels.ranged] = levels[4]
            this[Levels.prayer] = levels[5]
            this[Levels.magic] = levels[6]
            this[Levels.cooking] = levels[7]
            this[Levels.woodcutting] = levels[8]
            this[Levels.fletching] = levels[9]
            this[Levels.fishing] = levels[10]
            this[Levels.firemaking] = levels[11]
            this[Levels.crafting] = levels[12]
            this[Levels.smithing] = levels[13]
            this[Levels.mining] = levels[14]
            this[Levels.herblore] = levels[15]
            this[Levels.agility] = levels[16]
            this[Levels.thieving] = levels[17]
            this[Levels.slayer] = levels[18]
            this[Levels.farming] = levels[19]
            this[Levels.runecrafting] = levels[20]
            this[Levels.hunter] = levels[21]
            this[Levels.construction] = levels[22]
            this[Levels.summoning] = levels[23]
            this[Levels.dungeoneering] = levels[24]
        }
        Variables.deleteWhere { playerId inList playerIds.values }
        val varData = playerSaves.flatMap { save -> save.variables.toList().map { Triple(save.name, it.first, it.second) } }
        Variables.batchUpsert(varData, Variables.playerId, Variables.variableName) { (id, name, value) ->
            val type = when (value) {
                is String -> "String"
                is Int -> "Integer"
                is Boolean -> "Boolean"
                is List<*> -> if (value.all { it is Int }) "IntList" else "StringList"
                else -> throw IllegalArgumentException("Unsupported variable type: ${value::class.simpleName}")
            }
            this[Variables.playerId] = playerIds.getValue(id)
            this[Variables.variableName] = name
            this[Variables.variableType] = type
            this[Variables.variableValue] = value.toString()
        }
        Inventories.deleteWhere { playerId inList playerIds.values }
        val invData = playerSaves.flatMap { save -> save.inventories.toList().map { Triple(save.name, it.first, it.second) } }
        Inventories.batchUpsert(invData, Inventories.playerId, Inventories.inventoryName) { (id, inventory, items) ->
            this[Inventories.playerId] = playerIds.getValue(id)
            this[Inventories.inventoryName] = inventory
            this[Inventories.items] = items.map { it.id }
            this[Inventories.amounts] = items.map { it.amount }
        }
    }
}

fun loadClans(): List<Clan> {
    val transaction = transaction {
        val variableNamesSet = setOf("clan_name", "display_name", "clan_join_rank", "clan_talk_rank", "clan_kick_rank", "clan_loot_rank", "coin_share_setting")
        val variableNames = CustomFunction<List<String>>("ARRAY_AGG", ArrayColumnType(VarCharColumnType()), Variables.variableName).alias("variable_names")
        val variableValues = CustomFunction<List<String>>("ARRAY_AGG", ArrayColumnType(VarCharColumnType()), Variables.variableValue).alias("variable_values")
        (PlayerSaves innerJoin Variables)
            .select(PlayerSaves.id, PlayerSaves.name, PlayerSaves.friends, PlayerSaves.ranks, variableNames, variableValues)
            .where { (PlayerSaves.id eq Variables.playerId) and (Variables.variableName inList variableNamesSet) }
            .groupBy(PlayerSaves.id)
            .map {
                val values = it[variableValues]
                val variablesMap = it[variableNames].mapIndexed { index, s -> s to values[index]  }.toMap()
                val playerName = it[PlayerSaves.name]
                val playerFriends = it[PlayerSaves.friends]
                val playerRanks = it[PlayerSaves.ranks]
                Clan(
                    owner = playerName,
                    ownerDisplayName = variablesMap["display_name"] ?: playerName,
                    name = variablesMap["clan_name"] ?: "",
                    friends = playerFriends.mapIndexed { index, s -> s to ClanRank.valueOf(playerRanks[index]) }.toMap(),
                    ignores = variablesMap["ignores"]?.split(",") ?: emptyList(),
                    joinRank = ClanRank.valueOf(variablesMap["clan_join_rank"] ?: "Anyone"),
                    talkRank = ClanRank.valueOf(variablesMap["clan_talk_rank"] ?: "Anyone"),
                    kickRank = ClanRank.valueOf(variablesMap["clan_kick_rank"] ?: "Corporeal"),
                    lootRank = ClanRank.valueOf(variablesMap["clan_loot_rank"] ?: "None"),
                    coinShare = variablesMap["coin_share_setting"]?.toBoolean() ?: false
                )
            }
    }
    return transaction
}


fun loadPlayer(name: String): PlayerSave? {
    return transaction {
        PlayerSaves.selectAll().where { PlayerSaves.name eq name }.mapNotNull { playerRow ->
            val playerId = playerRow[PlayerSaves.id]
            val experience = loadExperience(playerId)
            val blocked = playerRow[PlayerSaves.blockedSkills]
            val levels = loadLevels(playerId)
            val looks = playerRow[PlayerSaves.looks]
            val colours = playerRow[PlayerSaves.colours]
            val variables = loadVariables(playerId)
            val inventories = loadInventories(playerId)
            val friends = playerRow[PlayerSaves.friends]
            val ranks = playerRow[PlayerSaves.ranks]
            PlayerSave(
                name = playerRow[PlayerSaves.name],
                password = playerRow[PlayerSaves.passwordHash],
                tile = Tile(playerRow[PlayerSaves.tile]),
                experience = experience,
                blocked = blocked.map { Skill.entries[it] },
                levels = levels,
                male = playerRow[PlayerSaves.male],
                looks = looks.toIntArray(),
                colours = colours.toIntArray(),
                variables = variables,
                inventories = inventories,
                friends = friends.mapIndexed { index, s -> s to ClanRank.of(ranks[index]) }.toMap(),
                ignores = playerRow[PlayerSaves.ignores]
            )
        }.singleOrNull()
    }
}


fun loadExperience(playerId: Int): DoubleArray {
    return Experience.selectAll().where { Experience.playerId eq playerId }.map {
        it[Experience.attack]
        it[Experience.defence]
        it[Experience.strength]
        it[Experience.constitution]
        it[Experience.ranged]
        it[Experience.prayer]
        it[Experience.magic]
        it[Experience.cooking]
        it[Experience.woodcutting]
        it[Experience.fletching]
        it[Experience.fishing]
        it[Experience.firemaking]
        it[Experience.crafting]
        it[Experience.smithing]
        it[Experience.mining]
        it[Experience.herblore]
        it[Experience.agility]
        it[Experience.thieving]
        it[Experience.slayer]
        it[Experience.farming]
        it[Experience.runecrafting]
        it[Experience.hunter]
        it[Experience.construction]
        it[Experience.summoning]
        it[Experience.dungeoneering]
    }.toDoubleArray()
}

fun loadLevels(playerId: Int): IntArray {
    return Levels.selectAll().where { Levels.playerId eq playerId }.map {
        it[Levels.attack]
        it[Levels.defence]
        it[Levels.strength]
        it[Levels.constitution]
        it[Levels.ranged]
        it[Levels.prayer]
        it[Levels.magic]
        it[Levels.cooking]
        it[Levels.woodcutting]
        it[Levels.fletching]
        it[Levels.fishing]
        it[Levels.firemaking]
        it[Levels.crafting]
        it[Levels.smithing]
        it[Levels.mining]
        it[Levels.herblore]
        it[Levels.agility]
        it[Levels.thieving]
        it[Levels.slayer]
        it[Levels.farming]
        it[Levels.runecrafting]
        it[Levels.hunter]
        it[Levels.construction]
        it[Levels.summoning]
        it[Levels.dungeoneering]
    }.toIntArray()
}

fun loadVariables(playerId: Int): Map<String, Any> {
    return Variables.selectAll().where { Variables.playerId eq playerId }.associate { row ->
        val variableName = row[Variables.variableName]
        val variableType = row[Variables.variableType]
        val variableValue = row[Variables.variableValue]

        variableName to when (variableType) {
            "String" -> variableValue
            "Integer" -> variableValue.toIntOrNull() ?: throw IllegalArgumentException("Invalid integer value for variable $variableName")
            "Boolean" -> variableValue.toBoolean()
            "StringList" -> variableValue.removeSurrounding("[", "]").split(",").map { it.trim() }
            "IntList" -> variableValue.removeSurrounding("[", "]").split(",").map { it.trim().toInt() }
            else -> throw IllegalArgumentException("Unsupported variable type: $variableType")
        }
    }
}

fun loadInventories(playerId: Int): Map<String, Array<Item>> {
    return Inventories.selectAll().where { Inventories.playerId eq playerId }.associate { row ->
        val inventoryName = row[Inventories.inventoryName]
        val itemIds = row[Inventories.items]
        val amounts = row[Inventories.amounts]

        val items = itemIds.zip(amounts).map { (itemId, amount) ->
            Item(itemId, amount, def = ItemDefinition.EMPTY)
        }.toTypedArray()

        inventoryName to items
    }
}