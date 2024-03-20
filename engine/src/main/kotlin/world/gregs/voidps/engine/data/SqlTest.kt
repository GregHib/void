package world.gregs.voidps.engine.data

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
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
    val ignores = array<String>("ignores")

    override val primaryKey = PrimaryKey(id, name = "pk_player_name")
}

object Experience : Table() {
    val playerId = integer("player_id").references(PlayerSaves.id)
    val attack = double("attack")
    val defence = double("defence")
    val strength = double("strength")
    val constitution = double("constitution")
    val ranged = double("ranged")
    val prayer = double("prayer")
    val magic = double("magic")
    val cooking = double("cooking")
    val woodcutting = double("woodcutting")
    val fletching = double("fletching")
    val fishing = double("fishing")
    val firemaking = double("firemaking")
    val crafting = double("crafting")
    val smithing = double("smithing")
    val mining = double("mining")
    val herblore = double("herblore")
    val agility = double("agility")
    val thieving = double("thieving")
    val slayer = double("slayer")
    val farming = double("farming")
    val runecrafting = double("runecrafting")
    val hunter = double("hunter")
    val construction = double("construction")
    val summoning = double("summoning")
    val dungeoneering = double("dungeoneering")
}

object Levels : Table() {
    val playerId = integer("player_id").references(PlayerSaves.id)
    val attack = integer("attack")
    val defence = integer("defence")
    val strength = integer("strength")
    val constitution = integer("constitution")
    val ranged = integer("ranged")
    val prayer = integer("prayer")
    val magic = integer("magic")
    val cooking = integer("cooking")
    val woodcutting = integer("woodcutting")
    val fletching = integer("fletching")
    val fishing = integer("fishing")
    val firemaking = integer("firemaking")
    val crafting = integer("crafting")
    val smithing = integer("smithing")
    val mining = integer("mining")
    val herblore = integer("herblore")
    val agility = integer("agility")
    val thieving = integer("thieving")
    val slayer = integer("slayer")
    val farming = integer("farming")
    val runecrafting = integer("runecrafting")
    val hunter = integer("hunter")
    val construction = integer("construction")
    val summoning = integer("summoning")
    val dungeoneering = integer("dungeoneering")
}

object Variables : Table() {
    val playerId = integer("player_id").references(PlayerSaves.id)
    val variableName = varchar("variable_name", 100)
    val variableType = varchar("variable_type", 20)
    val variableValue = varchar("variable_value", 100)
}

object Inventories : Table() {
    val playerId = integer("player_id").references(PlayerSaves.id)
    val inventoryName = varchar("inventory_name", 100)
    val items = array<String>("items")
    val amounts = array<Int>("amounts")
}

object Friends : Table() {
    val playerId = integer("player_id").references(PlayerSaves.id)
    val friendName = varchar("friend_name", 100)
    val friendRank = varchar("friend_rank", 100)
}

fun main() {
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://localhost:5432/testDB"
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
        create(PlayerSaves, Experience, Levels, Variables, Inventories, Friends)
    }


    // Example of saving PlayerSave object to the database
    val playerSaves = mapOf("John" to PlayerSave(
        name = "John",
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
        friends = mapOf("friend1" to "12345", "friend2" to "54321"),
        ignores = listOf()
    ))

    var start = System.currentTimeMillis()
    val clans = loadClans() // 2000 - 5s
    println(clans.first())
    println("${clans.size} clans took: ${System.currentTimeMillis() - start}ms")
    start = System.currentTimeMillis()
//    savePlayers(Array(2000) { playerSaves["John"]!!.copy(name = "John${it}") }.associateBy { it.name })
//    println("Save 2000 took: ${System.currentTimeMillis() - start}ms")
    // insert - 22.6s, update - 22.7s
//    repeat(50) {
        start = System.currentTimeMillis()
        savePlayers(playerSaves)
        println("Save took: ${System.currentTimeMillis() - start}ms")
//    }

    // Example of loading PlayerSave object from the database
//    repeat(50) {
        start = System.currentTimeMillis()
        val loadedPlayer = loadPlayer("John5")
        println("Load took: ${System.currentTimeMillis() - start}ms")
//    println(loadedPlayer)
//    }
}

fun savePlayers(playerSaves: Map<String, PlayerSave>) {
    transaction {
        // Update existing players
        val updated = PlayerSaves.select(PlayerSaves.id, PlayerSaves.name).where {
            PlayerSaves.name inList playerSaves.keys
        }.mapNotNull { result ->
            val name = result[PlayerSaves.name]
            val playerSave = playerSaves[name] ?: return@mapNotNull null
            val playerId = result[PlayerSaves.id]
            PlayerSaves.update({ PlayerSaves.id eq playerId }) {
                updatePlayer(it, playerSave)
            }
            saveExperience(playerId, playerSave.experience)
            saveLevels(playerId, playerSave.levels)
            saveVariables(playerId, playerSave.variables)
            saveInventories(playerId, playerSave.inventories)
            saveFriends(playerId, playerSave.friends)
            name
        }.toSet()
        // Insert new players
        playerSaves.keys.subtract(updated).forEach { name ->
            val playerSave = playerSaves[name] ?: return@forEach
            val playerId = PlayerSaves.insert {
                it[this.name] = playerSave.name
                updatePlayer(it, playerSave)
            } get PlayerSaves.id
            saveExperience(playerId, playerSave.experience)
            saveLevels(playerId, playerSave.levels)
            saveVariables(playerId, playerSave.variables)
            saveInventories(playerId, playerSave.inventories)
            saveFriends(playerId, playerSave.friends)
        }
    }
}

private fun PlayerSaves.updatePlayer(builder: UpdateBuilder<Number>, playerSave: PlayerSave) {
    builder[passwordHash] = playerSave.password
    builder[tile] = playerSave.tile.id
    builder[blockedSkills] = playerSave.blocked.map { skill -> skill.ordinal }
    builder[male] = playerSave.male
    builder[looks] = playerSave.looks.toList()
    builder[colours] = playerSave.colours.toList()
    builder[ignores] = playerSave.ignores
}

fun saveVariables(playerId: Int, variables: Map<String, Any>) {
    Variables.deleteWhere { Variables.playerId eq playerId }
    Variables.batchInsert(variables.toList()) { (name, value) ->
        val type = when (value) {
            is String -> "String"
            is Int -> "Integer"
            is Boolean -> "Boolean"
            is List<*> -> if (value.all { it is Int }) "IntList" else "StringList"
            else -> throw IllegalArgumentException("Unsupported variable type: ${value::class.simpleName}")
        }
        this[Variables.playerId] = playerId
        this[Variables.variableName] = name
        this[Variables.variableType] = type
        this[Variables.variableValue] = value.toString()
    }
}

fun saveExperience(playerId: Int, experience: DoubleArray) {
    Experience.update({ Experience.playerId eq playerId }) {
        it[this.attack] = experience[0]
        it[this.defence] = experience[1]
        it[this.strength] = experience[2]
        it[this.constitution] = experience[3]
        it[this.ranged] = experience[4]
        it[this.prayer] = experience[5]
        it[this.magic] = experience[6]
        it[this.cooking] = experience[7]
        it[this.woodcutting] = experience[8]
        it[this.fletching] = experience[9]
        it[this.fishing] = experience[10]
        it[this.firemaking] = experience[11]
        it[this.crafting] = experience[12]
        it[this.smithing] = experience[13]
        it[this.mining] = experience[14]
        it[this.herblore] = experience[15]
        it[this.agility] = experience[16]
        it[this.thieving] = experience[17]
        it[this.slayer] = experience[18]
        it[this.farming] = experience[19]
        it[this.runecrafting] = experience[20]
        it[this.hunter] = experience[21]
        it[this.construction] = experience[22]
        it[this.summoning] = experience[23]
        it[this.dungeoneering] = experience[24]
    }
}

fun saveLevels(playerId: Int, levels: IntArray) {
    Levels.update({ Levels.playerId eq playerId }) {
        it[this.attack] = levels[0]
        it[this.defence] = levels[1]
        it[this.strength] = levels[2]
        it[this.constitution] = levels[3]
        it[this.ranged] = levels[4]
        it[this.prayer] = levels[5]
        it[this.magic] = levels[6]
        it[this.cooking] = levels[7]
        it[this.woodcutting] = levels[8]
        it[this.fletching] = levels[9]
        it[this.fishing] = levels[10]
        it[this.firemaking] = levels[11]
        it[this.crafting] = levels[12]
        it[this.smithing] = levels[13]
        it[this.mining] = levels[14]
        it[this.herblore] = levels[15]
        it[this.agility] = levels[16]
        it[this.thieving] = levels[17]
        it[this.slayer] = levels[18]
        it[this.farming] = levels[19]
        it[this.runecrafting] = levels[20]
        it[this.hunter] = levels[21]
        it[this.construction] = levels[22]
        it[this.summoning] = levels[23]
        it[this.dungeoneering] = levels[24]
    }
}

fun saveInventories(playerId: Int, inventories: Map<String, Array<Item>>) {
    Inventories.deleteWhere { Inventories.playerId eq playerId }
    Inventories.batchInsert(inventories.toList()) { (inventory, items) ->
        this[Inventories.playerId] = playerId
        this[Inventories.inventoryName] = inventory
        this[Inventories.items] = items.map { it.id }
        this[Inventories.amounts] = items.map { it.amount }
    }
}

fun saveFriends(playerId: Int, friends: Map<String, String>) {
    Friends.deleteWhere { Friends.playerId eq playerId }
    Friends.batchInsert(friends.toList()) { (friend, rank) ->
        this[Friends.playerId] = playerId
        this[Friends.friendName] = friend
        this[Friends.friendRank] = rank
    }
}

fun loadClans(): List<Clan> {
    val transaction = transaction {
        val variableNames = setOf("clan_name", "display_name", "clan_join_rank", "clan_talk_rank", "clan_kick_rank", "clan_loot_rank", "coin_share_setting")
        val allFriends = Friends.selectAll().groupBy { it[Friends.playerId] }
        PlayerSaves.select(PlayerSaves.id, PlayerSaves.name)
            .adjustColumnSet { leftJoin(Variables, { PlayerSaves.id }, { playerId }, { Variables.variableName inList variableNames }) }
            .adjustSelect { select(fields + Variables.columns) }
            .groupBy { it[PlayerSaves.id] }
            .mapNotNull { (playerId, rows) ->
                val playerName = rows.firstOrNull()?.get(PlayerSaves.name) ?: return@mapNotNull null
                val variablesMap = rows.associate { it[Variables.variableName] to it[Variables.variableValue] }
                val playerFriends = allFriends[playerId]?.associate { it[Friends.friendName] to ClanRank.of(it[Friends.friendRank]) } ?: emptyMap()
                Clan(
                    owner = playerName,
                    ownerDisplayName = variablesMap["display_name"] ?: playerName,
                    name = variablesMap["clan_name"] ?: "",
                    friends = playerFriends,
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
            val friends = loadFriends(playerId)
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
                friends = friends,
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

fun loadFriends(playerId: Int): Map<String, String> {
    return Friends.selectAll().where { Friends.playerId eq playerId }.associate { row ->
        row[Friends.friendName] to row[Friends.friendRank]
    }
}