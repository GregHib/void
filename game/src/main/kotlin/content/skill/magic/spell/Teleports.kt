package content.skill.magic.spell

import content.quest.quest
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.strongQueue
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
            val seconds = remaining("home_teleport_timeout", epochSeconds())
            if (seconds > 0) {
                val remaining = TimeUnit.SECONDS.toMinutes(seconds.toLong())
                message("You have to wait $remaining ${"minute".plural(remaining)} before trying this again.")
                return@interfaceOption
            }
            if (hasClock("teleport_delay")) {
                return@interfaceOption
            }
            if (!removeSpellItems(component)) {
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

        interfaceOption("Cast", "*_spellbook:ardougne_teleport") {
            if (quest("plague_city") != "completed_with_spell") {
                message("You haven't learnt how to cast this spell yet.")
                return@interfaceOption
            } else {
                cast(it.id, it.component)
            }
        }

        itemOption("Read", "*_teleport", handler = ::teleport)
        itemOption("Break", "*_teleport", handler = ::teleport)
    }

    fun teleport(player: Player, option: ItemOption) {
        if (player.contains("delay") || player.queue.contains("teleport")) {
            return
        }
        player.closeInterfaces()
        val definition = Areas.getOrNull(option.item.id) ?: return
        val scrolls = Areas.tagged("scroll")
        val type = if (scrolls.contains(definition)) "scroll" else "tablet"
        val map = definition.area
        player.steps.clear()
        player.strongQueue("teleport") {
            if (player.inventory.remove(option.item.id)) {
                player.sound("teleport_$type")
                player.gfx("teleport_$type")
                player.anim("teleport_$type")
                player.delay(3)
                player.tele(map.random(player)!!)
                player.animDelay("teleport_land")
            }
        }
    }

    fun Player.cast(id: String, component: String) {
        if (contains("delay") || queue.contains("teleport")) {
            return
        }
        if (component == "ape_atoll_teleport" && !questCompleted("recipe_for_disaster")) {
            return
        }
        closeInterfaces()
        strongQueue("teleport") {
            if (!removeSpellItems(component)) {
                return@strongQueue
            }
            exp(Skill.Magic, Tables.int("spells.$component.xp") / 10.0)
            val book = id.removeSuffix("_spellbook")
            sound("teleport")
            gfx("teleport_$book")
            animDelay("teleport_$book")
            tele(Areas[component].random(this)!!)
            delay(1)
            sound("teleport_land")
            gfx("teleport_land_$book")
            animDelay("teleport_land_$book")
            if (book == "ancient") {
                delay(1)
                clearAnim()
            }
        }
    }
}
