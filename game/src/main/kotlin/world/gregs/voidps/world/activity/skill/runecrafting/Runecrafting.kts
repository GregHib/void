package world.gregs.voidps.world.activity.skill.runecrafting

import com.github.michaelbull.logging.InlineLogger
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Rune
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import content.entity.sound.playSound
import kotlin.math.min

val itemDefinitions: ItemDefinitions by inject()
val logger = InlineLogger()

itemOnObjectOperate("*_essence", "*_altar") {
    val id = target.id.replace("_altar", "_rune")
    bindRunes(player, id, itemDefinitions.get(id))
}

objectOperate("Craft-rune", "*_altar") {
    val id = target.id.replace("_altar", "_rune")
    bindRunes(player, id, itemDefinitions.get(id))
}

fun Runecrafting.bindRunes(player: Player, id: String, itemDefinition: ItemDefinition) {
    val rune: Rune = itemDefinition.getOrNull("runecrafting") ?: return
    if (!player.has(Skill.Runecrafting, rune.levels.first(), message = true)) {
        return
    }
    player.softTimers.start("runecrafting")
    val pure = rune.pure || !player.inventory.contains("rune_essence")
    val essenceId = if (pure) "pure_essence" else "rune_essence"
    val essence = player.inventory.count(essenceId)
    player.inventory.transaction {
        remove(essenceId, essence)
        val count = rune.multiplier(player)
        add(id, essence * count)
    }
    player.start("movement_delay", 3)
    when (player.inventory.transaction.error) {
        is TransactionError.Deficient, is TransactionError.Invalid -> {
            player.message("You don't have any rune essences to bind.")
        }
        TransactionError.None -> {
            player.exp(Skill.Runecrafting, rune.xp * essence)
            player.anim("bind_runes")
            player.gfx("bind_runes")
            player.playSound("bind_runes")
            player.message("You bind the temple's power into ${id.toSentenceCase().plural()}.", ChatType.Filter)
        }
        else -> logger.warn { "Error binding runes $player $rune ${player.levels.get(Skill.Runecrafting)} $essence" }
    }
    player.softTimers.stop("runecrafting")
}

itemOnObjectOperate("*_rune", "*_altar") {
    val element = item.id.removeSuffix("_rune")
    val objectElement = target.id.removeSuffix("_altar")
    val rune: Rune? = item.def.getOrNull("runecrafting")
    val list = rune?.combinations?.get(objectElement)
    if (rune == null || list == null || !World.members) {
        player.noInterest()
        return@itemOnObjectOperate
    }
    val combination = list[0] as String
    val xp = list[1] as Double
    if (!player.holdsItem("pure_essence")) {
        player.message("You need pure essence to bind $combination runes.")
        return@itemOnObjectOperate
    }
    if (!player.holdsItem("${element}_talisman") && !player.hasClock("magic_imbue")) {
        player.message("You need a $element talisman to bind $combination runes.")
        return@itemOnObjectOperate
    }
    val level = rune.levels.first()
    if (!player.has(Skill.Runecrafting, level, message = false)) {
        player.message("You need a Runecrafting level of $level to bind $combination runes.")
        return@itemOnObjectOperate
    }
    val count = min(player.inventory.count("pure_essence"), player.inventory.count(item.id))
    val bindingNecklace = player.equipped(EquipSlot.Amulet).id == "binding_necklace" && player.equipment.charges(player, EquipSlot.Amulet.index) > 0
    val successes = if (bindingNecklace) count else (0 until count).sumOf { random.nextBoolean().toInt() }
    player.inventory.transaction {
        if (!player.hasClock("magic_imbue")) {
            remove("${element}_talisman")
        }
        remove("pure_essence", count)
        remove("${element}_rune", count)
        if (successes > 0) {
            add("${combination}_rune", successes)
        }
    }
    player.start("movement_delay", 3)
    when (player.inventory.transaction.error) {
        is TransactionError.Deficient, is TransactionError.Invalid -> {
            player.message("You need pure essence to bind $combination runes.")
        }
        TransactionError.None -> {
            player.exp(Skill.Runecrafting, xp * successes)
            if (bindingNecklace && player.equipment.discharge(player, EquipSlot.Amulet.index)) {
                val charge = player.equipment.charges(player, EquipSlot.Amulet.index)
                if (charge > 0) {
                    player.message("You have $charge ${"charge".plural(charge)} left before your Binding necklace disintegrates.")
                }
            }
            player.anim("bind_runes")
            player.gfx("bind_runes")
            player.playSound("bind_runes")
            if (successes != count) {
                player.message("You partially succeed to bind the temple's power into $combination runes.", ChatType.Filter)
            } else {
                player.message("You bind the temple's power into $combination runes.", ChatType.Filter)
            }
        }
        else -> logger.warn { "Error binding runes $player $rune ${player.levels.get(Skill.Runecrafting)}" }
    }
}