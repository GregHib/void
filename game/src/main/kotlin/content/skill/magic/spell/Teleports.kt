package content.skill.magic.spell

import world.gregs.voidps.engine.entity.character.sound
import content.quest.quest
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.timer.epochSeconds
import java.util.concurrent.TimeUnit

class Teleports : Script {

    val areas: AreaDefinitions by inject()
    val definitions: SpellDefinitions by inject()

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
            weakQueue("home_teleport") {
                if (!removeSpellItems(component)) {
                    cancel()
                    return@weakQueue
                }
                onCancel = {
                    start("teleport_delay", 1)
                }
                start("teleport_delay", 17)
                repeat(17) {
                    gfx("home_tele_${it + 1}")
                    val ticks = anim("home_tele_${it + 1}")
                    pause(ticks)
                }
                withContext(NonCancellable) {
                    tele(areas["lumbridge_teleport"].random())
                    set("click_your_heels_three_times_task", true)
                    start("home_teleport_timeout", TimeUnit.MINUTES.toSeconds(30).toInt(), epochSeconds())
                }
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

        itemOption("Read", "*_teleport", block = ::teleport)
        itemOption("Break", "*_teleport", block = ::teleport)
    }

    fun teleport(player: Player, option: ItemOption) {
        if (player.contains("delay") || player.queue.contains("teleport")) {
            return
        }
        player.closeInterfaces()
        val definition = areas.getOrNull(option.item.id) ?: return
        val scrolls = areas.getTagged("scroll")
        val type = if (scrolls.contains(definition)) "scroll" else "tablet"
        val map = definition.area
        player.queue("teleport", onCancel = null) {
            if (player.inventory.remove(option.item.id)) {
                player.sound("teleport_$type")
                player.gfx("teleport_$type")
                player.anim("teleport_$type")
                delay(3)
                player.tele(map.random(player)!!)
                player.animDelay("teleport_land")
            }
        }
    }

    fun Player.cast(id: String, component: String) {
        if (contains("delay") || queue.contains("teleport")) {
            return
        }
        closeInterfaces()
        queue("teleport", onCancel = null) {
            if (!removeSpellItems(component)) {
                return@queue
            }
            val definition = definitions.get(component)
            exp(Skill.Magic, definition.experience)
            val book = id.removeSuffix("_spellbook")
            sound("teleport")
            gfx("teleport_$book")
            animDelay("teleport_$book")
            tele(areas[component].random(player)!!)
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
