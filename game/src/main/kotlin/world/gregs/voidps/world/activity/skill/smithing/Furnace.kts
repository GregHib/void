package world.gregs.voidps.world.activity.skill.smithing

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Smelting
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.remove
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.random.Random

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

on<ObjectOption>({ operate && target.id.startsWith("furnace") && option == "Smelt" }) { player: Player ->
    smeltingOptions(player, target, bars)
}

on<ItemOnObject>({ operate && target.id.startsWith("furnace") && item.id.endsWith("_ore") }) { player: Player ->
    smeltingOptions(player, target, listOf(item.id.replace("_ore", "_bar")))
}

suspend fun CharacterContext.smeltingOptions(
    player: Player,
    gameObject: GameObject,
    bars: List<String>
) {
    player["face_entity"] = getSide(player, gameObject)
    val available = mutableListOf<String>()
    var max = 0
    for (bar in bars) {
        val smelt: Smelting = itemDefinitions.getOrNull(bar)?.get("smelting") ?: continue
        val min = smelt.items.minOf { (id, amount) -> player.inventory.count(id, amount) }
        if (min <= 0) {
            continue
        }
        available.add(bar)
        if (min > max) {
            max = min
        }
    }
    if (available.isEmpty()) {
        player.message("You don't have any ores to smelt.")
        return
    }
    val (item, amount) = makeAmount(available, "Make", max)
    smelt(player, gameObject, item, amount)
}

fun smelt(player: Player, target: GameObject, id: String, amount: Int) {
    if (amount <= 0) {
        return
    }

    val definition = itemDefinitions.get(id)
    val smelting: Smelting = definition["smelting"]
    if (!player.has(Skill.Smithing, smelting.level, message = true)) {
        return
    }
    player.face(getSide(player, target))
    player.setAnimation("furnace_smelt")
    player.playSound("smelt_bar")
    player.message(smelting.message, ChatType.Filter)
    player.weakQueue("smelting", 4) {
        val success = Random.nextInt(255) < smelting.chance
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
                player.weakQueue("smelting", 1) {
                    smelt(player, target, id, amount - removed)
                }
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
        if (Random.nextInt(10) != 0) {
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

fun getSide(player: Player, target: GameObject): Tile {
   return if (player.tile.x > target.tile.x + target.width) {
        target.tile.add(target.width, target.height / 2)
    } else if (player.tile.y > target.tile.y + target.height) {
        target.tile.add(target.width / 2, target.height)
    } else if (player.tile.x < target.tile.x) {
        target.tile.addY(target.height / 2)
    } else if (player.tile.y < target.tile.y) {
        target.tile.addX(target.width / 2)
    } else {
        target.tile.add(target.width / 2, target.height / 2)
    }
}