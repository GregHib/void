package world.gregs.voidps.engine.data.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import world.gregs.voidps.engine.data.AccountStorage
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile

class DatabaseStorage : AccountStorage {
    private val variableNames = setOf("clan_name", "display_name", "clan_join_rank", "clan_talk_rank", "clan_kick_rank", "clan_loot_rank", "coin_share_setting")

    override fun names(): Map<String, AccountDefinition> = transaction {
        val display = VariablesTable.alias("display_name")
        val history = VariablesTable.alias("name_history")
        AccountsTable
            .leftJoin(display) {
                AccountsTable.id eq display[VariablesTable.playerId] and
                        (display[VariablesTable.name] eq stringLiteral("display_name"))
            }
            .leftJoin(history) {
                AccountsTable.id eq history[VariablesTable.playerId] and
                        (history[VariablesTable.name] eq stringLiteral("name_history"))
            }
            .select(AccountsTable.name,
                AccountsTable.passwordHash,
                AccountsTable.friends,
                AccountsTable.ranks,
                AccountsTable.ignores,
                display[VariablesTable.string],
                history[VariablesTable.stringList])
            .associate { row ->
                val accountName = row[AccountsTable.name]
                val displayName = row.getOrNull(display[VariablesTable.string]) ?: accountName
                val previousName = row.getOrNull(history[VariablesTable.stringList])?.lastOrNull() ?: ""
                accountName.lowercase() to AccountDefinition(accountName, displayName, previousName, row[AccountsTable.passwordHash])
            }
    }

    override fun clans(): Map<String, Clan> = transaction {
        val names = CustomFunction<List<String>>("ARRAY_AGG", ArrayColumnType(VarCharColumnType()), VariablesTable.name).alias("variable_names")
        val strings = CustomFunction<List<String>>("ARRAY_AGG", ArrayColumnType(VarCharColumnType()), VariablesTable.string).alias("string_values")
        val booleans = CustomFunction<List<Boolean>>("ARRAY_AGG", ArrayColumnType(VarCharColumnType()), VariablesTable.boolean).alias("bool_values")
        (AccountsTable innerJoin VariablesTable)
            .select(AccountsTable.id, AccountsTable.name, AccountsTable.friends, AccountsTable.ignores, AccountsTable.ranks, names, strings, booleans)
            .where { (AccountsTable.id eq VariablesTable.playerId) and (VariablesTable.name inList variableNames) }
            .groupBy(AccountsTable.id)
            .associate { row ->
                val variables: Map<String, Any> = row[names].mapIndexed { index, s -> s to if (s == "coin_share_setting") row[booleans][index] else row[strings][index] }.toMap()
                val accountName = row[AccountsTable.name]
                val displayName = variables["display_name"] as? String ?: accountName
                accountName.lowercase() to Clan(
                    owner = accountName,
                    ownerDisplayName = displayName,
                    name = variables["clan_name"] as? String ?: "",
                    friends = row[AccountsTable.friends].zip(row[AccountsTable.ranks]) { friend, rank -> friend to ClanRank.valueOf(rank) }.toMap(),
                    ignores = row[AccountsTable.ignores],
                    joinRank = (variables["clan_join_rank"] as? String)?.let { ClanRank.valueOf(it) } ?: ClanRank.Anyone,
                    talkRank = (variables["clan_talk_rank"] as? String)?.let { ClanRank.valueOf(it) } ?: ClanRank.Anyone,
                    kickRank = (variables["clan_kick_rank"] as? String)?.let { ClanRank.valueOf(it) } ?: ClanRank.Corporeal,
                    lootRank = (variables["clan_loot_rank"] as? String)?.let { ClanRank.valueOf(it) } ?: ClanRank.None,
                    coinShare = variables["coin_share_setting"] as? Boolean ?: false
                )
            }
    }

    override fun save(accounts: List<PlayerSave>) {
        transaction {
            saveAccounts(accounts)
            val names = accounts.map { it.name }
            val playerIds = AccountsTable
                .select(AccountsTable.id, AccountsTable.name)
                .where { LowerCase(AccountsTable.name) inList names.map { it.lowercase() } }
                .associate { it[AccountsTable.name].lowercase() to it[AccountsTable.id] }
            saveExperience(accounts, playerIds)
            saveLevels(accounts, playerIds)
            saveVariables(accounts, playerIds)
            saveInventories(accounts, playerIds)
        }
    }

    override fun exists(accountName: String): Boolean = transaction {
        val lower = accountName.lowercase()
        AccountsTable
            .select(AccountsTable.id)
            .where { LowerCase(AccountsTable.name) eq lower }
            .count() > 0
    }

    override fun load(accountName: String): PlayerSave? = transaction {
        val lower = accountName.lowercase()
        val playerRow = AccountsTable
            .selectAll()
            .where { LowerCase(AccountsTable.name) eq lower }
            .singleOrNull() ?: return@transaction null
        val playerId = playerRow[AccountsTable.id]
        val experience = loadExperience(playerId)
        val blocked = playerRow[AccountsTable.blockedSkills]
        val levels = loadLevels(playerId)
        val looks = playerRow[AccountsTable.looks]
        val colours = playerRow[AccountsTable.colours]
        val variables = loadVariables(playerId)
        val inventories = loadInventories(playerId)
        val friends = playerRow[AccountsTable.friends]
        val ranks = playerRow[AccountsTable.ranks]
        return@transaction PlayerSave(
            name = playerRow[AccountsTable.name],
            password = playerRow[AccountsTable.passwordHash],
            tile = Tile(playerRow[AccountsTable.tile]),
            experience = experience,
            blocked = blocked.map { Skill.entries[it] },
            levels = levels,
            male = playerRow[AccountsTable.male],
            looks = looks.toIntArray(),
            colours = colours.toIntArray(),
            variables = variables,
            inventories = inventories,
            friends = friends.zip(ranks) { name, rank -> name to ClanRank.valueOf(rank) }.toMap(),
            ignores = playerRow[AccountsTable.ignores]
        )
    }

    private fun saveInventories(accounts: List<PlayerSave>, playerIds: Map<String, Int>) {
        InventoriesTable.deleteWhere { playerId inList playerIds.values }
        val invData = accounts.flatMap { save -> save.inventories.toList().map { Triple(save.name, it.first, it.second) } }
        InventoriesTable.batchUpsert(invData, InventoriesTable.playerId, InventoriesTable.inventoryName) { (id, inventory, items) ->
            this[InventoriesTable.playerId] = playerIds.getValue(id.lowercase())
            this[InventoriesTable.inventoryName] = inventory
            this[InventoriesTable.items] = items.map { it.id }
            this[InventoriesTable.amounts] = items.map { it.value }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun saveVariables(accounts: List<PlayerSave>, playerIds: Map<String, Int>) {
        VariablesTable.deleteWhere { playerId inList playerIds.values }
        val varData = accounts.flatMap { save -> save.variables.toList().map { Triple(save.name, it.first, it.second) } }
        VariablesTable.batchUpsert(varData, VariablesTable.playerId, VariablesTable.name) { (id, name, value) ->
            this[VariablesTable.playerId] = playerIds.getValue(id.lowercase())
            this[VariablesTable.name] = name
            when (value) {
                is String -> {
                    this[VariablesTable.type] = TYPE_STRING
                    this[VariablesTable.string] = value
                }
                is Int -> {
                    this[VariablesTable.type] = TYPE_INT
                    this[VariablesTable.int] = value
                }
                is Boolean -> {
                    this[VariablesTable.type] = TYPE_BOOLEAN
                    this[VariablesTable.boolean] = value
                }
                is Double -> {
                    this[VariablesTable.type] = TYPE_DOUBLE
                    this[VariablesTable.double] = value
                }
                is Long -> {
                    this[VariablesTable.type] = TYPE_LONG
                    this[VariablesTable.long] = value
                }
                is List<*> -> {
                    if (value.isNotEmpty() && value.all { it is Int }) {
                        this[VariablesTable.type] = TYPE_INT_LIST
                        this[VariablesTable.intList] = value as List<Int>
                    } else {
                        this[VariablesTable.type] = TYPE_STRING_LIST
                        this[VariablesTable.stringList] = value as List<String>
                    }
                }
                else -> throw IllegalArgumentException("Unsupported variable type: ${value::class.simpleName}")
            }
        }
    }

    private fun saveLevels(accounts: List<PlayerSave>, playerIds: Map<String, Int>) {
        LevelsTable.batchUpsert(accounts, LevelsTable.playerId) { playerSave ->
            this[LevelsTable.playerId] = playerIds.getValue(playerSave.name.lowercase())
            val levels = playerSave.levels
            this[LevelsTable.attack] = levels[0]
            this[LevelsTable.defence] = levels[1]
            this[LevelsTable.strength] = levels[2]
            this[LevelsTable.constitution] = levels[3]
            this[LevelsTable.ranged] = levels[4]
            this[LevelsTable.prayer] = levels[5]
            this[LevelsTable.magic] = levels[6]
            this[LevelsTable.cooking] = levels[7]
            this[LevelsTable.woodcutting] = levels[8]
            this[LevelsTable.fletching] = levels[9]
            this[LevelsTable.fishing] = levels[10]
            this[LevelsTable.firemaking] = levels[11]
            this[LevelsTable.crafting] = levels[12]
            this[LevelsTable.smithing] = levels[13]
            this[LevelsTable.mining] = levels[14]
            this[LevelsTable.herblore] = levels[15]
            this[LevelsTable.agility] = levels[16]
            this[LevelsTable.thieving] = levels[17]
            this[LevelsTable.slayer] = levels[18]
            this[LevelsTable.farming] = levels[19]
            this[LevelsTable.runecrafting] = levels[20]
            this[LevelsTable.hunter] = levels[21]
            this[LevelsTable.construction] = levels[22]
            this[LevelsTable.summoning] = levels[23]
            this[LevelsTable.dungeoneering] = levels[24]
        }
    }

    private fun saveExperience(accounts: List<PlayerSave>, playerIds: Map<String, Int>) {
        ExperienceTable.batchUpsert(accounts, ExperienceTable.playerId) { playerSave ->
            this[ExperienceTable.playerId] = playerIds.getValue(playerSave.name.lowercase())
            val experience = playerSave.experience
            this[ExperienceTable.attack] = experience[0]
            this[ExperienceTable.defence] = experience[1]
            this[ExperienceTable.strength] = experience[2]
            this[ExperienceTable.constitution] = experience[3]
            this[ExperienceTable.ranged] = experience[4]
            this[ExperienceTable.prayer] = experience[5]
            this[ExperienceTable.magic] = experience[6]
            this[ExperienceTable.cooking] = experience[7]
            this[ExperienceTable.woodcutting] = experience[8]
            this[ExperienceTable.fletching] = experience[9]
            this[ExperienceTable.fishing] = experience[10]
            this[ExperienceTable.firemaking] = experience[11]
            this[ExperienceTable.crafting] = experience[12]
            this[ExperienceTable.smithing] = experience[13]
            this[ExperienceTable.mining] = experience[14]
            this[ExperienceTable.herblore] = experience[15]
            this[ExperienceTable.agility] = experience[16]
            this[ExperienceTable.thieving] = experience[17]
            this[ExperienceTable.slayer] = experience[18]
            this[ExperienceTable.farming] = experience[19]
            this[ExperienceTable.runecrafting] = experience[20]
            this[ExperienceTable.hunter] = experience[21]
            this[ExperienceTable.construction] = experience[22]
            this[ExperienceTable.summoning] = experience[23]
            this[ExperienceTable.dungeoneering] = experience[24]
        }
    }

    private fun saveAccounts(accounts: List<PlayerSave>) {
        AccountsTable.batchUpsert(accounts, AccountsTable.name) { playerSave ->
            this[AccountsTable.name] = playerSave.name
            this[AccountsTable.passwordHash] = playerSave.password
            this[AccountsTable.tile] = playerSave.tile.id
            this[AccountsTable.blockedSkills] = playerSave.blocked.map { skill -> skill.ordinal }
            this[AccountsTable.male] = playerSave.male
            this[AccountsTable.looks] = playerSave.looks.toList()
            this[AccountsTable.colours] = playerSave.colours.toList()
            this[AccountsTable.ignores] = playerSave.ignores
            val friends = playerSave.friends.toList()
            this[AccountsTable.friends] = friends.map { it.first }
            this[AccountsTable.ranks] = friends.map { it.second.name }
        }
    }

    private fun loadExperience(playerId: Int): DoubleArray {
        val it = ExperienceTable.selectAll().where { ExperienceTable.playerId eq playerId }.first()
        return doubleArrayOf(
            it[ExperienceTable.attack],
            it[ExperienceTable.defence],
            it[ExperienceTable.strength],
            it[ExperienceTable.constitution],
            it[ExperienceTable.ranged],
            it[ExperienceTable.prayer],
            it[ExperienceTable.magic],
            it[ExperienceTable.cooking],
            it[ExperienceTable.woodcutting],
            it[ExperienceTable.fletching],
            it[ExperienceTable.fishing],
            it[ExperienceTable.firemaking],
            it[ExperienceTable.crafting],
            it[ExperienceTable.smithing],
            it[ExperienceTable.mining],
            it[ExperienceTable.herblore],
            it[ExperienceTable.agility],
            it[ExperienceTable.thieving],
            it[ExperienceTable.slayer],
            it[ExperienceTable.farming],
            it[ExperienceTable.runecrafting],
            it[ExperienceTable.hunter],
            it[ExperienceTable.construction],
            it[ExperienceTable.summoning],
            it[ExperienceTable.dungeoneering]
        )
    }

    private fun loadLevels(playerId: Int): IntArray {
        val it = LevelsTable.selectAll().where { LevelsTable.playerId eq playerId }.first()
        return intArrayOf(
            it[LevelsTable.attack],
            it[LevelsTable.defence],
            it[LevelsTable.strength],
            it[LevelsTable.constitution],
            it[LevelsTable.ranged],
            it[LevelsTable.prayer],
            it[LevelsTable.magic],
            it[LevelsTable.cooking],
            it[LevelsTable.woodcutting],
            it[LevelsTable.fletching],
            it[LevelsTable.fishing],
            it[LevelsTable.firemaking],
            it[LevelsTable.crafting],
            it[LevelsTable.smithing],
            it[LevelsTable.mining],
            it[LevelsTable.herblore],
            it[LevelsTable.agility],
            it[LevelsTable.thieving],
            it[LevelsTable.slayer],
            it[LevelsTable.farming],
            it[LevelsTable.runecrafting],
            it[LevelsTable.hunter],
            it[LevelsTable.construction],
            it[LevelsTable.summoning],
            it[LevelsTable.dungeoneering]
        )
    }

    private fun loadVariables(playerId: Int): Map<String, Any> {
        return VariablesTable.selectAll().where { VariablesTable.playerId eq playerId }.associate { row ->
            val variableName = row[VariablesTable.name]
            val variableType = row[VariablesTable.type]
            variableName to when (variableType) {
                TYPE_STRING -> row[VariablesTable.string]!!
                TYPE_INT -> row[VariablesTable.int]!!
                TYPE_BOOLEAN -> row[VariablesTable.boolean]!!
                TYPE_DOUBLE -> row[VariablesTable.double]!!
                TYPE_LONG -> row[VariablesTable.long]!!
                TYPE_STRING_LIST -> row[VariablesTable.stringList]!!
                TYPE_INT_LIST -> row[VariablesTable.intList]!!
                else -> throw IllegalArgumentException("Unsupported variable type: $variableType")
            }
        }
    }

    private fun loadInventories(playerId: Int): Map<String, Array<Item>> {
        return InventoriesTable.selectAll().where { InventoriesTable.playerId eq playerId }.associate { row ->
            val inventoryName = row[InventoriesTable.inventoryName]
            val itemIds = row[InventoriesTable.items]
            val amounts = row[InventoriesTable.amounts]

            val items = itemIds.zip(amounts).map { (itemId, amount) ->
                Item(itemId, amount)
            }.toTypedArray()

            inventoryName to items
        }
    }

    companion object {

        fun connect(username: String, password: String, driver: String, url: String, poolSize: Int) {
            val config = HikariConfig().apply {
                jdbcUrl = url
                driverClassName = driver
                this.username = username
                this.password = password
                maximumPoolSize = poolSize
                isReadOnly = false
                transactionIsolation = "TRANSACTION_SERIALIZABLE"
            }
            Database.connect(HikariDataSource(config))
            transaction {
                SchemaUtils.create(*tables, inBatch = true)
            }
        }

        internal val tables = arrayOf(AccountsTable, ExperienceTable, LevelsTable, VariablesTable, InventoriesTable)

        private const val TYPE_STRING = 0.toByte()
        private const val TYPE_INT = 1.toByte()
        private const val TYPE_BOOLEAN = 2.toByte()
        private const val TYPE_DOUBLE = 3.toByte()
        private const val TYPE_LONG = 4.toByte()
        private const val TYPE_INT_LIST = 5.toByte()
        private const val TYPE_STRING_LIST = 6.toByte()
    }
}