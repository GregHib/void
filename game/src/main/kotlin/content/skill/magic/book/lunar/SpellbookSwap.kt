package content.skill.magic.book.lunar

import content.entity.player.dialogue.type.choice
import content.quest.questCompleted
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer

class SpellbookSwap : Script {

    init {
        interfaceOption("Cast", "lunar_spellbook:spellbook_swap") {
            if (hasClock("action_delay")) {
                return@interfaceOption
            }
            choice("Select a Spellbook") {
                option("Ancient Magicks.") {
                    if (!questCompleted("desert_treasure")) {
                        message("You need to have completed Desert Treasure to use the Ancient Magicks.")
                        return@option
                    }
                    swap("ancient_spellbook")
                }
                option("Normal Magicks.") {
                    swap("modern_spellbook")
                }
                option("Neither, thank you.")
            }
        }

        interfaceOpened("*_spellbook") { id ->
            // A book change that isn't part of the swap itself ends the swap early
            if (contains("spellbook_swap") && id != get<String>("spellbook_swap")) {
                clear("spellbook_swap")
                softTimers.stop("spellbook_swap")
            }
        }

        timerStart("spellbook_swap") { 200 }

        timerTick("spellbook_swap") {
            if (contains("spellbook_swap")) {
                revertSpellbookSwap()
            }
            Timer.CANCEL
        }

        playerDespawn {
            if (contains("spellbook_swap")) {
                clear("spellbook_swap")
                set("spellbook_config", 2 or (get("defensive_cast", false).toInt() shl 8))
            }
        }
    }

    private suspend fun Player.swap(book: String) {
        if (!removeSpellItems("spellbook_swap")) {
            return
        }
        start("action_delay", 3)
        anim("spellbook_swap")
        gfx("spellbook_swap")
        exp(Skill.Magic, Tables.int("spells.spellbook_swap.xp") / 10.0)
        set("spellbook_swap", book)
        softTimers.start("spellbook_swap")
        open(book)
        message("You have 2 minutes before your spellbook changes back to the Lunar spellbook!")
    }
}

internal fun Player.revertSpellbookSwap() {
    clear("spellbook_swap")
    softTimers.stop("spellbook_swap")
    open("lunar_spellbook")
    message("Your spellbook has changed back to the Lunar spellbook.")
}

internal fun Player.checkSpellbookSwapCast(spell: String) {
    if (spell == "spellbook_swap" || !contains("spellbook_swap")) {
        return
    }
    // The swap only lasts for a single spell cast; revert once it completes
    queue("spellbook_swap_revert", 1) {
        if (contains("spellbook_swap")) {
            revertSpellbookSwap()
        }
    }
}
