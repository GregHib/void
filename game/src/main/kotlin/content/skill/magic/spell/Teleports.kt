package content.skill.magic.spell

import content.quest.quest
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.timer.epochSeconds
import java.util.concurrent.TimeUnit

class Teleports : Script {

    init {
        interfaceOption("Cast", "*_spellbook:*_teleport") {
            val component = it.component
            if (component != "lumbridge_home_teleport") {
                cast(it.id, it.component)
                return@interfaceOption
            }
            val book = it.id.removeSuffix("_spellbook")
            if (!Teleport.takeOff(this, book, component)) {
                return@interfaceOption
            }
            if (!Teleport.removeItems(this, book, component)) {
                return@interfaceOption
            }
            var total = 0
            for (i in 1 until 18) {
                val delay = AnimationDefinitions.get("home_tele_$i")["ticks", 0]
                weakQueue("home_teleport", total) {
                    start("teleport_delay", 1)
                    gfx("home_tele_$i")
                    anim("home_tele_$i")
                }
                total += delay
            }
            weakQueue("home_teleport", total) {
                start("teleport_delay", 1)
                tele(Areas["lumbridge_teleport"].random())
                set("click_your_heels_three_times_task", true)
                start("home_teleport_timeout", TimeUnit.MINUTES.toSeconds(30).toInt(), epochSeconds())
            }
        }

        teleportTakeOff("lumbridge_home_teleport") {
            val seconds = remaining("home_teleport_timeout", epochSeconds())
            if (seconds > 0) {
                val remaining = TimeUnit.SECONDS.toMinutes(seconds.toLong())
                message("You have to wait $remaining ${"minute".plural(remaining)} before trying this again.")
                false
            } else {
                !hasClock("teleport_delay")
            }
        }

        itemOption("Read", "*_teleport", handler = ::teleport)
        itemOption("Break", "*_teleport", handler = ::teleport)

        teleportRemoveItems("modern") { removeSpellItems(it) }
        teleportRemoveItems("ancient") { removeSpellItems(it) }
        teleportRemoveItems("lunar") { removeSpellItems(it) }
        teleportRemoveItems("dungeoneering") { removeSpellItems(it) }

        teleportTakeOff("modern") {
            when (it) {
                "ardougne_teleport" if (quest("plague_city") != "completed_with_spell") -> {
                    message("You haven't learnt how to cast this spell yet.")
                    false
                }
                "ape_atoll_teleport" -> questCompleted("recipe_for_disaster")
                else -> true
            }
        }

        teleportRemoveItems("scroll", ::removeItem)
        teleportRemoveItems("tablet", ::removeItem)
    }

    fun removeItem(player: Player, item: String): Boolean {
        return player.inventory.remove(item)
    }

    fun teleport(player: Player, option: ItemOption) {
        val definition = Areas.getOrNull(option.item.id) ?: return
        val type = if (definition.tags.contains("scroll")) "scroll" else "tablet"
        Teleport.teleport(player, definition.area.random(player)!!, type, spell = option.item.id)
    }

    fun Player.cast(id: String, component: String) {
        val xp = Tables.intOrNull("spells.$component.xp") ?: 0
        val book = id.removeSuffix("_spellbook")
        Teleport.teleport(this, area = component, type = book, xp = xp / 10.0, spell = component)
    }
}
