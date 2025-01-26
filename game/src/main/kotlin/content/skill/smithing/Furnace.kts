package content.skill.smithing

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Smelting
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.remove
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import content.entity.player.dialogue.type.makeAmount
import content.entity.sound.playSound

val bars = listOf(
    "bronze_bar",
    "blurite_bar",
    "iron_bar",
    "silver_bar",
    "steel_bar",
    "gold_bar",
    "mithril_bar",
    "adamant_bar",
    "rune_bar"
)

val logger = InlineLogger()
val itemDefinitions: ItemDefinitions by inject()

objectOperate("Smelt", "furnace*", arrive = false) {
    smeltingOptions(player, target, bars)
}

itemOnObjectOperate("*_ore", "furnace*", arrive = false) {
    val list = mutableListOf<String>()
    list.add(oreToBar(item.id))
    if (item.id == "iron_ore" && player.inventory.contains("coal")) {
        list.add("steel_bar")
    }
    smeltingOptions(player, target, list)
}

suspend fun Context<Player>.smeltingOptions(
    player: Player,
    gameObject: GameObject,
    bars: List<String>
) {
    player["face_entity"] = furnaceSide(player, gameObject)
    val available = mutableListOf<String>()
    var max = 0
    for (bar in bars) {
        val smelt: Smelting = itemDefinitions.getOrNull(bar)?.get("smelting") ?: continue
        val min = smelt.items.minOf { item -> player.inventory.count(item.id, item.amount) }
        if (min <= 0) {
            continue
        }
        available.add(bar)
        if (min > max) {
            max = min
        }
    }
    player.softTimers.start("smelting")
    if (available.isEmpty()) {
        player.softTimers.stop("smelting")
        player.message("You don't have any ores to smelt.")
        return
    }
    val (item, amount) = makeAmount(available, "Make", max)
    smelt(player, gameObject, item, amount)
}

fun smelt(player: Player, target: GameObject, id: String, amount: Int) {
    if (amount <= 0) {
        player.softTimers.stop("smelting")
        return
    }

    val definition = itemDefinitions.get(id)
    val smelting: Smelting = definition["smelting"]
    if (!player.has(Skill.Smithing, smelting.level, message = true)) {
        player.softTimers.stop("smelting")
        return
    }
    player.face(furnaceSide(player, target))
    player.anim("furnace_smelt")
    player.playSound("smelt_bar")
    player.message(smelting.message, ChatType.Filter)
    player.weakQueue("smelting", 4) {
        val success = random.nextInt(255) < smelting.chance
        player.inventory.transaction {
            remove(smelting.items)
            if (success) {
                add(id)
            }
        }
        when (player.inventory.transaction.error) {
            TransactionError.None -> {
                var removed = 1
                if (success) {
                    player.exp(Skill.Smithing, smelting.exp(player, id))
                    player.message("You retrieve a bar of ${id.removeSuffix("_bar")}.")
                    if (varrockArmour(player, target, id, smelting)) {
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
    smelting: Smelting
): Boolean {
    if (target.id != "furnace_edgeville" || !player.inventory.contains(smelting.items)) {
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
            remove(smelting.items)
            add(id)
        }
        player.exp(Skill.Smithing, smelting.xp)
        player.message("The magic of the Varrock armour enables you to smelt 2 bars at the same time.")
        return true
    }
    return false
}