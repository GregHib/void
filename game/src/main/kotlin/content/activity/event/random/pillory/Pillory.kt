package content.activity.event.random.pillory

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import kotlin.math.min

/**
 * Pillory random event: a guard arrests the player and locks them in a pillory. To escape they
 * unlock it (interface 189) by picking the swinging key whose shape matches the spinning lock, a set
 * number of times in a row. A wrong pick resets the streak and raises the target (up to six). Escaping
 * rewards a random event gift.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Pillory
 */
class Pillory : Script {

    init {
        RandomEvents.register("pillory") { startEvent() }

        objectOperate("Unlock", "pillory_cage", arrive = false) {
            if (get<String>("random_event") != "pillory") {
                message("You can't unlock the pillory, you'll let all the prisoners out!")
                return@objectOperate
            }
            renderPuzzle()
            open("pillory_lock")
            message("Pick the swinging key that matches the hole in the spinning lock.")
        }

        // The key selectors (components 8-10) are option buttons (setting=2 in the cache).
        interfaceOption("Select", "pillory_lock:button_*") {
            if (get<String>("random_event") != "pillory") {
                return@interfaceOption
            }
            answer(it.component.removePrefix("button_").toInt())
        }
    }

    private suspend fun Player.startEvent() {
        set("pillory_target", 3)
        set("pillory_correct", 0)
        pilloryGuard()
        kidnap(LOCATIONS.random(random))
        message("Solve the pillory puzzle to be returned to where you came from.")
    }

    private suspend fun Player.pilloryGuard() {
        val guard = NPCs.addRandom("pillory_guard", tile.toCuboid(1), ticks = 25, owner = this)
            ?: NPCs.add("pillory_guard", tile, ticks = 25, owner = this)
        guard.watch(this)
        guard.say("$name, you're under arrest!")
        delay(2)
    }

    /** Shuffle three distinct keys onto the interface, pick one as the lock, and update the padlocks. */
    private fun Player.renderPuzzle() {
        val keys = (0..3).shuffled(random).take(3)
        val lock = keys.random(random)
        set("pillory_answer", keys.indexOf(lock) + 1) // 1-based slot of the matching key
        interfaces.sendModel("pillory_lock", "lock", LOCK_MODEL + lock)
        for (slot in 1..3) {
            interfaces.sendModel("pillory_lock", "key_$slot", KEY_MODEL + keys[slot - 1])
        }
        val target = get("pillory_target", 3)
        val correct = get("pillory_correct", 0)
        for (i in 1..6) {
            interfaces.sendModel("pillory_lock", "padlock_$i", if (i <= correct) PADLOCK_GREEN else PADLOCK_RED)
            interfaces.sendVisibility("pillory_lock", "padlock_$i", i <= target)
        }
    }

    private suspend fun Player.answer(slot: Int) {
        if (slot == get("pillory_answer", 0)) {
            val correct = inc("pillory_correct")
            if (correct >= get("pillory_target", 3)) {
                escape()
            } else {
                renderPuzzle() // the interface stays open for the next round
                message("Correct! $correct down, ${get("pillory_target", 3) - correct} to go!")
            }
        } else {
            set("pillory_correct", 0)
            set("pillory_target", min(MAX_TARGET, get("pillory_target", 3) + 1))
            close("pillory_lock")
            message("Bah, that's not right. Use the key that matches the hole in the spinning lock.")
        }
    }

    private suspend fun Player.escape() {
        close("pillory_lock")
        message("You've escaped!")
        addOrDrop("random_event_gift")
        clear("pillory_target")
        clear("pillory_correct")
        clear("pillory_answer")
        anim("teleport_modern")
        sound("teleport")
        gfx("teleport_modern")
        delay(3)
        RandomEvents.complete(this)
        anim("teleport_land_modern")
        gfx("teleport_land_modern")
        sound("teleport_land")
    }

    companion object {
        private const val LOCK_MODEL = 9753 // lock type L -> model 9753 + L
        private const val KEY_MODEL = 9749 // key type K -> model 9749 + K
        private const val PADLOCK_RED = 9757
        private const val PADLOCK_GREEN = 9758
        private const val MAX_TARGET = 6

        // The pillory cages (object 777) as placed in the 634 map: Varrock, Seers' Village, Yanille.
        private val LOCATIONS = listOf(
            Tile(3228, 3407),
            Tile(3230, 3407),
            Tile(2681, 3489),
            Tile(2683, 3489),
            Tile(2685, 3489),
            Tile(2608, 3105),
        )
    }
}
