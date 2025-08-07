package world.gregs.voidps.engine.data.sql

import org.jetbrains.exposed.sql.Table
import world.gregs.voidps.engine.data.sql.AccountsTable.autoIncrement
import world.gregs.voidps.engine.data.sql.AccountsTable.uniqueIndex
import world.gregs.voidps.engine.data.sql.ExperienceTable.references
import world.gregs.voidps.engine.data.sql.ExperienceTable.uniqueIndex

internal object AccountsTable : Table("accounts") {
    val id = integer("id").autoIncrement().uniqueIndex()
    val name = varchar("name", 12).uniqueIndex()
    val passwordHash = text("password_hash")
    val tile = integer("tile")
    val blockedSkills = array<Int>("blocked")
    val male = bool("male")
    val looks = array<Int>("looks")
    val colours = array<Int>("colours")
    val friends = array<String>("friends")
    val ranks = array<String>("ranks")
    val ignores = array<String>("ignores")

    override val primaryKey = PrimaryKey(id, name = "pk_account_id")
}

internal object ExperienceTable : Table("experience") {
    val playerId = integer("player_id").references(AccountsTable.id).uniqueIndex()
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

internal object LevelsTable : Table("levels") {
    val playerId = integer("player_id").references(AccountsTable.id).uniqueIndex()
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

internal object VariablesTable : Table("variables") {
    val playerId = integer("player_id").references(AccountsTable.id)
    val name = text("name")
    val type = byte("type")
    val string = text("string_value").nullable().default(null)
    val int = integer("int_value").nullable().default(null)
    val boolean = bool("boolean_value").nullable().default(null)
    val double = double("double_value").nullable().default(null)
    val long = long("long_value").nullable().default(null)
    val stringList = array<String>("string_list_value").nullable().default(null)
    val intList = array<Int>("int_list_value").nullable().default(null)

    init {
        index(true, playerId, name)
    }
}

internal object InventoriesTable : Table("inventories") {
    val playerId = integer("player_id").references(AccountsTable.id)
    val inventoryName = text("inventory_name")
    val items = array<String>("items")
    val amounts = array<Int>("amounts")

    init {
        index(true, playerId, inventoryName)
    }
}

internal object OffersTable : Table("grand_exchange_offers") {
    val id = integer("id").autoIncrement().uniqueIndex()
    val sell = bool("sell").default(false)
    val item = text("item")
    val amount = integer("amount")
    val price = integer("price")
    val lastActive = long("last_active")
    val remaining = integer("remaining")
    val excess = integer("excess")
    val account = integer("account").references(AccountsTable.id).uniqueIndex()

}