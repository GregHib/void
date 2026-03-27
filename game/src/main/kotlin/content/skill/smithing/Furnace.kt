package content.skill.smithing

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.type.makeAmount
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.remove
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class Furnace : Script {

    val bars = listOf(
        "bronze_bar",
        "blurite_bar",
        "iron_bar",
        "silver_bar",
        "steel_bar",
        "gold_bar",
        "mithril_bar",
        "adamant_bar",
        "rune_bar",
    )

    val logger = InlineLogger()

    init {
        objectOperate("Smelt", "furnace*", arrive = false) { (target) ->
            smeltingOptions(target, bars)
        }

        itemOnObjectOperate("*_ore", "furnace*", arrive = false) { (target, item) ->
            val list = mutableListOf<String>()
            list.add(oreToBar(item.id))
            if (item.id == "iron_ore" && inventory.contains("coal")) {
                list.add("steel_bar")
            }
            smeltingOptions(target, list)
        }
    }

    private fun oreToBar(ore: String): String {
        if (ore == "copper_ore" || ore == "tin_ore") {
            return "bronze_bar"
        }
        if (ore == "adamantite_ore") {
            return "adamant_bar"
        }
        if (ore == "runite_ore") {
            return "rune_bar"
        }
        return ore.replace("_ore", "_bar")
    }

    suspend fun Player.smeltingOptions(
        gameObject: GameObject,
        bars: List<String>,
    ) {
        set("face_entity", furnaceSide(this, gameObject))
        val available = mutableListOf<String>()
        var max = 0
        for (bar in bars) {
            val items = requiredOres(bar)
            val min = items.minOf { item -> inventory.count(item.id, item.amount) }
            if (min <= 0) {
                continue
            }
            available.add(bar)
            if (min > max) {
                max = min
            }
        }
        softTimers.start("smelting")
        if (available.isEmpty()) {
            softTimers.stop("smelting")
            message("You don't have any ores to smelt.")
            return
        }
        val (item, amount) = makeAmount(available, "Make", max)
        smelt(this, gameObject, item, amount)
    }

    fun smelt(player: Player, target: GameObject, id: String, amount: Int) {
        if (amount <= 0) {
            player.softTimers.stop("smelting")
            return
        }

        val row = Rows.getOrNull("bars.${id}") ?: return
        val level = row.int("level")
        if (!player.has(Skill.Smithing, level, message = true)) {
            player.softTimers.stop("smelting")
            return
        }
        player.face(furnaceSide(player, target))
        player.anim("furnace_smelt")
        player.sound("smelt_bar")
        val message = row.stringOrNull("message")
        if (message != null) {
            player.message(message, ChatType.Filter)
        }
        player.weakQueue("smelting", 4) {
            val chance = row.int("chance")
            val success = random.nextInt(255) < chance
            val items = requiredOres(id)
            player.inventory.transaction {
                remove(items)
                if (success) {
                    add(id)
                }
            }
            when (player.inventory.transaction.error) {
                TransactionError.None -> {
                    var removed = 1
                    if (success) {
                        val xp = row.int("xp") / 10.0
                        player.exp(Skill.Smithing, goldXp(player, id, xp))
                        player.message("You retrieve a bar of ${id.removeSuffix("_bar")}.")
                        if (amount - 1 > 0 && varrockArmour(player, target, id, items, xp)) {
                            removed = 2
                        }
                    } else {
                        player.message("The ore is too impure and you fail to refine it.", ChatType.Filter)
                    }
                    pause(1)
                    smelt(player, target, id, amount - removed)
                }
                else -> logger.warn { "Smelting transaction error $player $id $amount ${player.inventory.transaction.error}" }
            }
        }
    }

    fun varrockArmour(
        player: Player,
        target: GameObject,
        id: String,
        items: List<Item>,
        xp: Double
    ): Boolean {
        if (target.id != "furnace_edgeville" || !player.inventory.contains(items)) {
            return false
        }
        val chest = player.equipped(EquipSlot.Chest).id
        if (!chest.startsWith("varrock_armour")) {
            return false
        }
        val armour1 = id == "bronze_bar" || id == "iron_bar" || id == "steel_bar"
        val armour2 = id == "mithril_bar" && chest != "varrock_armour_1"
        val armour3 = id == "adamant_bar" && chest != "varrock_armour_1" && chest != "varrock_armour_2"
        val armour4 = id == "rune_bar" && chest == "varrock_armour_4"
        if (armour1 || armour2 || armour3 || armour4) {
            // 10% chance
            if (random.nextInt(10) != 0) {
                return false
            }
            player.inventory.transaction {
                remove(items)
                add(id)
            }
            player.exp(Skill.Smithing, xp)
            player.message("The magic of the Varrock armour enables you to smelt 2 bars at the same time.")
            return true
        }
        return false
    }

    companion object {
        internal fun goldXp(player: Player, bar: String, default: Double): Double {
            if (bar == "gold_bar" && (player.equipped(EquipSlot.Hands).id == "goldsmith_gauntlets" || player.equipped(EquipSlot.Cape).id.startsWith("smithing_cape"))) {
                return 56.2
            }
            return default
        }

        internal fun requiredOres(id: String): MutableList<Item> {
            val row = Rows.getOrNull("bars.${id}") ?: return mutableListOf()
            val items = mutableListOf<Item>()
            val ores = row.itemList("ore")
            val amounts = row.intList("amount")
            for (i in ores.indices) {
                val ore = ores[i]
                val amount = amounts[i]
                items.add(Item(ore, amount))
            }
            return items
        }

        internal fun furnaceSide(player: Player, target: GameObject): Tile = when {
            player.tile.x > target.tile.x + target.width -> target.tile.add(target.width, target.height / 2)
            player.tile.y > target.tile.y + target.height -> target.tile.add(target.width / 2, target.height)
            player.tile.x < target.tile.x -> target.tile.addY(target.height / 2)
            player.tile.y < target.tile.y -> target.tile.addX(target.width / 2)
            else -> target.tile.add(target.width / 2, target.height / 2)
        }
    }
}
