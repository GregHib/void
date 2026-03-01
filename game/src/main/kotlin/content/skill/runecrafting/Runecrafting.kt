package content.skill.runecrafting

import com.github.michaelbull.logging.InlineLogger
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import kotlin.math.min

class Runecrafting : Script {

    val logger = InlineLogger()

    init {
        itemOnObjectOperate("*_essence", "*_altar") { (target) ->
            val id = target.id.replace("_altar", "_rune")
            bindRunes(this, id)
        }

        objectOperate("Craft-rune", "*_altar") { (target) ->
            val id = target.id.replace("_altar", "_rune")
            bindRunes(this, id)
        }

        itemOnObjectOperate("*_rune", "*_altar") { (target, item) ->
            val element = item.id.removeSuffix("_rune")
            val xp = EnumDefinitions.intOrNull("runecrafting_combination_${target.id}_xp", item.id)
            val combination = EnumDefinitions.stringOrNull("runecrafting_combination_${target.id}", item.id)
            if (xp == null || combination == null || !World.members) {
                noInterest()
                return@itemOnObjectOperate
            }
            if (!carriesItem("pure_essence")) {
                message("You need pure essence to bind $combination runes.")
                return@itemOnObjectOperate
            }
            if (!carriesItem("${element}_talisman") && !hasClock("magic_imbue")) {
                message("You need a $element talisman to bind $combination runes.")
                return@itemOnObjectOperate
            }
            val level = EnumDefinitions.int("runecrafting_level", item.id)
            if (!has(Skill.Runecrafting, level, message = false)) {
                message("You need a Runecrafting level of $level to bind $combination runes.")
                return@itemOnObjectOperate
            }
            val count = min(inventory.count("pure_essence"), inventory.count(item.id))
            val bindingNecklace = equipped(EquipSlot.Amulet).id == "binding_necklace" && equipment.charges(this, EquipSlot.Amulet.index) > 0
            val successes = if (bindingNecklace) count else (0 until count).sumOf { random.nextBoolean().toInt() }
            inventory.transaction {
                if (!hasClock("magic_imbue")) {
                    remove("${element}_talisman")
                }
                remove("pure_essence", count)
                remove("${element}_rune", count)
                if (successes > 0) {
                    add("${combination}_rune", successes)
                }
            }
            start("movement_delay", 3)
            when (inventory.transaction.error) {
                is TransactionError.Deficient, is TransactionError.Invalid -> {
                    message("You need pure essence to bind $combination runes.")
                }
                TransactionError.None -> {
                    exp(Skill.Runecrafting, (xp / 10.0) * successes)
                    if (bindingNecklace && equipment.discharge(this, EquipSlot.Amulet.index)) {
                        val charge = equipment.charges(this, EquipSlot.Amulet.index)
                        if (charge > 0) {
                            message("You have $charge ${"charge".plural(charge)} left before your Binding necklace disintegrates.")
                        }
                    }
                    anim("bind_runes")
                    gfx("bind_runes")
                    sound("bind_runes")
                    if (successes != count) {
                        message("You partially succeed to bind the temple's power into $combination runes.", ChatType.Filter)
                    } else {
                        message("You bind the temple's power into $combination runes.", ChatType.Filter)
                    }
                }
                else -> logger.warn { "Error binding runes $this ${item.id} ${levels.get(Skill.Runecrafting)}" }
            }
        }
    }

    fun Runecrafting.bindRunes(player: Player, id: String) {
        val xp = EnumDefinitions.intOrNull("runecrafting_xp", id) ?: return
        val level = EnumDefinitions.int("runecrafting_level", id)
        if (!player.has(Skill.Runecrafting, level, message = true)) {
            return
        }
        player.softTimers.start("runecrafting")
        val pure = EnumDefinitions.contains("runecrafting_pure", id) || !player.inventory.contains("rune_essence")
        val essenceId = if (pure) "pure_essence" else "rune_essence"
        val essence = player.inventory.count(essenceId)
        player.inventory.transaction {
            remove(essenceId, essence)
            val count = multiplier(player, id)
            add(id, essence * count)
        }
        player.start("movement_delay", 3)
        when (player.inventory.transaction.error) {
            is TransactionError.Deficient, is TransactionError.Invalid -> {
                player.message("You don't have any rune essences to bind.")
            }
            TransactionError.None -> {
                player.exp(Skill.Runecrafting, (xp / 10.0) * essence)
                player.anim("bind_runes")
                player.gfx("bind_runes")
                player.sound("bind_runes")
                player.message("You bind the temple's power into ${id.toSentenceCase().plural()}.", ChatType.Filter)
            }
            else -> logger.warn { "Error binding runes $player $id ${player.levels.get(Skill.Runecrafting)} $essence" }
        }
        player.softTimers.stop("runecrafting")
    }

    private fun multiplier(player: Player, id: String): Int {
        val map = EnumDefinitions.getOrNull("runecrafting_multiplier_$id")?.map ?: return 1
        var multiplier = 1
        val sorted = map.toList().sortedByDescending { it.first }
        val rc = player.levels.get(Skill.Runecrafting)
        for ((index, level) in sorted) {
            if (rc >= level as Int) {
                multiplier = index + 1
                break
            }
        }
        return multiplier
    }
}
