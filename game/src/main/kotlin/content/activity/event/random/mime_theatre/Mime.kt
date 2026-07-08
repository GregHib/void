package content.activity.event.random.mime_theatre

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.activity.event.random.mysteriousOldMan
import content.activity.event.random.rewardCostumeOrCoins
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.quest.instanceOffset
import content.quest.setInstanceLogout
import content.quest.smallInstance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.suspend.pauseString
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Mime random event: the Mysterious Old Man drops the player into the theatre where the Mime performs
 * emotes under a spotlight. After each performance the Mime bows to the player, who copies it by
 * picking the matching emote on the mime interface (188). Three correct copies unlock the mime emotes
 * and award a piece of the mime costume, then the player is teleported home.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Mime
 */
class Mime : Script {

    init {
        RandomEvents.register("mime") { startEvent() }

        // Each emote button is named after its emote, so the clicked component is the pick; hand it
        // back to the round that's waiting.
        interfaceOption(id = "$INTERFACE:*") {
            if (get<String>("random_event") != "mime") {
                return@interfaceOption
            }
            (suspension as? Suspension.StringEntry)?.resume(it.component)
        }
    }

    private suspend fun Player.startEvent() {
        smallInstance(Region(REGION), levels = 1)
        setInstanceLogout(Tile(this["random_event_origin", tile.id]))
        val offset = instanceOffset()

        mysteriousOldMan()
        kidnap(SPAWN.add(offset))
        npc<Neutral>("mysterious_old_man", "Here's a little challenge for you:<br>Copy the Mime's performance, then you'll be released.")
        walkOverDelay(WATCH.add(offset))
        statement("Watch the Mime.<br>See what emote he performs.")

        val mime = NPCs.add("mime", MIME_TILE.add(offset), ticks = -1, owner = this)
        set("mime_correct", 0)
        runMime(mime, offset)
    }

    private suspend fun Player.runMime(mime: NPC, offset: Delta) {
        while (get("mime_correct", 0) < REQUIRED) {
            val expected = EMOTES.random(random)
            set("mime_emote", expected)

            // Spotlight the Mime and perform the emote facing the audience.
            lightMime(offset, on = true)
            lightPlayer(offset, on = false)
            mime.face(AUDIENCE.add(offset))
            mime.anim("emote_$expected")
            delay(PERFORM_TICKS)

            // Bow to the player, then hand over to them.
            mime.face(tile)
            mime.anim("emote_bow")
            delay(BOW_TICKS)
            lightMime(offset, on = false)
            lightPlayer(offset, on = true)

            val chosen = awaitEmote()
            anim("emote_$chosen") // the player performs the emote they picked
            if (chosen == expected) {
                message("Correct!")
                inc("mime_correct")
                mime.face(AUDIENCE.add(offset))
                delay(CORRECT_TICKS)
            } else {
                message("That wasn't quite right. Watch the Mime again.")
            }
        }
        finish()
    }

    private suspend fun Player.awaitEmote(): String {
        open(INTERFACE)
        val emote = pauseString()
        close(INTERFACE)
        return emote
    }

    /** Spotlight over the Mime (2010, 4761). */
    private fun Player.lightMime(offset: Delta, on: Boolean) = spotlight(MIME_LIGHT.add(offset), on)

    /** Spotlight over the player's spot (2007, 4761). */
    private fun Player.lightPlayer(offset: Delta, on: Boolean) = spotlight(PLAYER_LIGHT.add(offset), on)

    private fun spotlight(tile: Tile, on: Boolean) {
        // TODO: object 3644 (Spotlight) has no baked animation - wire the on/off animation ids here
        // once known (GameObjects.findOrNull(tile) { it.id == "mime_spotlight" }?.anim(...)).
    }

    private suspend fun Player.finish() {
        for (unlock in MIME_EMOTES) {
            set("unlocked_emote_$unlock", true)
        }
        rewardCostumeOrCoins("mime_mask", "mime_top", "mime_legs", "mime_gloves", "mime_boots", coins = 500)
        clear("mime_emote")
        clear("mime_correct")
        RandomEvents.complete(this)
    }

    companion object {
        private const val REQUIRED = 3
        private const val REGION = 8010
        private const val INTERFACE = "dialogue_macro_mime_emotes"
        private const val PERFORM_TICKS = 4
        private const val BOW_TICKS = 3
        private const val CORRECT_TICKS = 13 // ~8 seconds before the next performance

        private val SPAWN = Tile(2008, 4764)
        private val WATCH = Tile(2008, 4762)
        private val MIME_TILE = Tile(2011, 4762)
        private val AUDIENCE = Tile(2011, 4756) // south of the Mime, where the watchers sit
        private val MIME_LIGHT = Tile(2010, 4761)
        private val PLAYER_LIGHT = Tile(2007, 4761)

        // The eight emotes the Mime can perform, matching the buttons on interface 188.
        private val EMOTES = listOf("think", "cry", "laugh", "dance", "climb_rope", "lean", "glass_box", "glass_wall")
        private val MIME_EMOTES = listOf("glass_wall", "glass_box", "climb_rope", "lean")
    }
}
