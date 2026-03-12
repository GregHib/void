package content.skill.magic.book.modern

import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.engine.queue.weakQueue

class EnchantCrossbowBolt(
    val spellDefinitions: SpellDefinitions,
) : Script {
    val set = setOf(
        "opal",
        "sapphire",
        "jade",
        "pearl",
        "emerald",
        "topaz",
        "ruby",
        "diamond",
        "dragon",
        "onyx",
    )

    init {
        interfaceOption("Cast", "modern_spellbook:enchant_crossbow_bolt") {
            open("enchant_crossbow_bolts")
        }

        interfaceOpened("enchant_crossbow_bolts") {
            for (it in set) {
                interfaces.sendItem("enchant_crossbow_bolts", "${it}_bolt", EnumDefinitions.int("enchant_bolt_models", "${it}_bolts"))
            }
        }

        interfaceOption("Enchant 1 stack of ", "enchant_crossbow_bolts:*") {
            enchant(it.component, 1)
        }

        interfaceOption("Enchant 5 stacks  of ", "enchant_crossbow_bolts:*") {
            enchant(it.component, 5)
        }

        interfaceOption("Enchant 10 stacks of ", "enchant_crossbow_bolts:*") {
            enchant(it.component, 10)
        }
    }

    fun Player.enchant(type: String, repeat: Int) {
        if (repeat < 1) {
            return
        }
        if (hasClock("action_delay")) {
            return
        }
        if (!has(Skill.Magic, EnumDefinitions.int("enchant_bolt_levels", "${type}_bolts"))) {
            return
        }
        val runes = when (type) {
            "opal" -> mapOf("cosmic_rune" to 1, "air_rune" to 2)
            "sapphire" -> mapOf("cosmic_rune" to 1, "water_rune" to 1, "mind_rune" to 1)
            "jade" -> mapOf("cosmic_rune" to 1, "earth_rune" to 2)
            "pearl" -> mapOf("cosmic_rune" to 1, "water_rune" to 2)
            "emerald" -> mapOf("cosmic_rune" to 1, "air_rune" to 3, "nature_rune" to 1)
            "topaz" -> mapOf("cosmic_rune" to 1, "fire_rune" to 2)
            "ruby" -> mapOf("cosmic_rune" to 1, "fire_rune" to 5, "blood_rune" to 1)
            "diamond" -> mapOf("cosmic_rune" to 1, "earth_rune" to 10, "law_rune" to 2)
            "dragon" -> mapOf("cosmic_rune" to 1, "earth_rune" to 15, "soul_rune" to 1)
            "onyx" -> mapOf("cosmic_rune" to 1, "fire_rune" to 20, "death_rune" to 1)
            else -> return
        }.toMutableMap()
        inventory.transaction {
            removeItems(this@enchant, runes, "enchant_${type}_bolts", message = false)
            val removed = removeToLimit("${type}_bolts", 10)
            if (removed == 0) {
                error = TransactionError.Deficient(10)
            }
            add("${type}_bolts_e", removed)
        }
        when (inventory.transaction.error) {
            is TransactionError.Deficient -> {
                message("You do not have the required items to cast this spell.")
            }
            TransactionError.None -> {
                closeInterfaces()
                anim("enchanted_tipping")
                gfx("enchanted_tipping")
                sound("enchanted_tipping")
                val exp = spellDefinitions.get("enchant_${type}_bolts").experience
                exp(Skill.Magic, exp)
                start("action_delay", 1)
                weakQueue("bolt_enchant", 3) {
                    enchant(type, repeat - 1)
                }
            }
            else -> return
        }
    }
}
