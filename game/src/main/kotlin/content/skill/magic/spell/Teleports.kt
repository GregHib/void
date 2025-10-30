package content.skill.magic.spell

import content.entity.player.inv.inventoryItem
import content.entity.sound.sound
import content.quest.quest
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
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
        interfaceOption("Cast", "*_teleport", "*_spellbook") {
            if (component != "lumbridge_home_teleport") {
                cast()
                return@interfaceOption
            }
            val seconds = player.remaining("home_teleport_timeout", epochSeconds())
            if (seconds > 0) {
                val remaining = TimeUnit.SECONDS.toMinutes(seconds.toLong())
                player.message("You have to wait $remaining ${"minute".plural(remaining)} before trying this again.")
                return@interfaceOption
            }
            if (player.hasClock("teleport_delay")) {
                return@interfaceOption
            }
            player.weakQueue("home_teleport") {
                if (!player.removeSpellItems(component)) {
                    cancel()
                    return@weakQueue
                }
                onCancel = {
                    player.start("teleport_delay", 1)
                }
                player.start("teleport_delay", 17)
                repeat(17) {
                    player.gfx("home_tele_${it + 1}")
                    val ticks = player.anim("home_tele_${it + 1}")
                    pause(ticks)
                }
                withContext(NonCancellable) {
                    player.tele(areas["lumbridge_teleport"].random())
                    player["click_your_heels_three_times_task"] = true
                    player.start("home_teleport_timeout", TimeUnit.MINUTES.toSeconds(30).toInt(), epochSeconds())
                }
            }
        }

        interfaceOption("Cast", "ardougne_teleport", "*_spellbook") {
            if (player.quest("plague_city") != "completed_with_spell") {
                player.message("You haven't learnt how to cast this spell yet.")
                return@interfaceOption
            } else {
                cast()
            }
        }

        inventoryItem("*", "*_teleport") {
            if (player.contains("delay") || player.queue.contains("teleport")) {
                return@inventoryItem
            }
            player.closeInterfaces()
            val definition = areas.getOrNull(item.id) ?: return@inventoryItem
            val scrolls = areas.getTagged("scroll")
            val type = if (scrolls.contains(definition)) "scroll" else "tablet"
            val map = definition.area
            player.queue("teleport", onCancel = null) {
                if (player.inventory.remove(item.id)) {
                    player.sound("teleport_$type")
                    player.gfx("teleport_$type")
                    player.anim("teleport_$type")
                    delay(3)
                    player.tele(map.random(player)!!)
                    player.animDelay("teleport_land")
                }
            }
        }
    }

    fun InterfaceOption.cast() {
        if (player.contains("delay") || player.queue.contains("teleport")) {
            return
        }
        player.closeInterfaces()
        player.queue("teleport", onCancel = null) {
            if (!player.removeSpellItems(component)) {
                cancel()
                return@queue
            }
            val definition = definitions.get(component)
            player.exp(Skill.Magic, definition.experience)
            val book = id.removeSuffix("_spellbook")
            player.sound("teleport")
            player.gfx("teleport_$book")
            player.animDelay("teleport_$book")
            player.tele(areas[component].random(player)!!)
            delay(1)
            player.sound("teleport_land")
            player.gfx("teleport_land_$book")
            player.animDelay("teleport_land_$book")
            if (book == "ancient") {
                delay(1)
                player.clearAnim()
            }
        }
    }
}
