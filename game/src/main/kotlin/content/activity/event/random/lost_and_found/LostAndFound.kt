package content.activity.event.random.lost_and_found

import content.activity.event.random.RandomEvents
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.closeTabs
import content.quest.openTabs
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Lost and Found random event: a magic spellbook teleport can fault, dropping the player onto the
 * Abyssal plane where four strange appendages surround them. Three appendages share the same shape;
 * operating the odd one out forwards the player back to where they came from, while a wrong pick
 * reshuffles the shapes and drains some Magic. Players snatched from the rune essence mine receive
 * a handful of essence as recompense.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Lost_and_Found
 */
class LostAndFound : Script {

    init {
        RandomEvents.register("lost_and_found") { startEvent() }

        // Only a magic spellbook teleport can slip through; the destination becomes the return point.
        teleportLand("modern") {
            RandomEvents.roll(this, "lost_and_found")
        }

        objectOperate("Operate", "abyss_appendage_*") { (appendage) ->
            if (get<String>("random_event") != "lost_and_found") {
                return@objectOperate
            }
            operate(appendage)
        }
    }

    private suspend fun Player.startEvent() {
        open("fade_out")
        delay(2)
        shuffleAppendages()
        closeTabs()
        minimap(Minimap.HideMap)
        tele(PLANE)
        message("You have slipped through to the Abyssal plane!")
        open("fade_in")
        statement("There has been a fault in the teleportation matrix. Please operate the odd appendage out to be forwarded to your destination.", clickToContinue = false)
    }

    /** Show three appendages with one shape and a single odd one with another; the odd slot is 1-based. */
    private fun Player.shuffleAppendages() {
        val odd = random.nextInt(APPENDAGES.size)
        val shape = random.nextInt(4) * 2
        val flip = random.nextInt(2)
        for (index in APPENDAGES.indices) {
            set("lost_and_found_appendage_${index + 1}", shape + if (index == odd) 1 - flip else flip)
        }
        set("laf_odd", odd + 1)
    }

    private suspend fun Player.operate(appendage: GameObject) {
        val index = APPENDAGES.indexOf(appendage.tile) + 1
        if (index == 0) {
            return
        }
        if (index == get("laf_odd", 0)) {
            escape()
        } else {
            shuffleAppendages()
            message("That was not the correct appendage!")
            levels.drain(Skill.Magic, if (levels.get(Skill.Magic) >= 10) random.nextInt(1, 11) else 1)
        }
    }

    private suspend fun Player.escape() {
        open("fade_out")
        delay(2)
        reward()
        clear("laf_odd")
        openTabs()
        clearMinimap()
        RandomEvents.complete(this)
        open("fade_in")
        statement("The Abyssal Services Department apologises for the inconvenience.", clickToContinue = false)
    }

    /** Players taken from the rune essence mine are compensated with a handful of essence. */
    private fun Player.reward() {
        val origin = Tile(this["random_event_origin", tile.id])
        if (origin.region.id != ESSENCE_MINE_REGION) {
            return
        }
        val essence = if (levels.getMax(Skill.Mining) > 30) "pure_essence" else "rune_essence"
        addOrDrop(essence, random.nextInt(8, 37))
    }

    companion object {
        private const val ESSENCE_MINE_REGION = 11595
        private val PLANE = Tile(2332, 4770)

        // The four appendage tiles, in the same order as the lost_and_found_appendage_1-4 varbits.
        private val APPENDAGES = listOf(
            Tile(2336, 4771),
            Tile(2332, 4775),
            Tile(2327, 4771),
            Tile(2332, 4766),
        )
    }
}
