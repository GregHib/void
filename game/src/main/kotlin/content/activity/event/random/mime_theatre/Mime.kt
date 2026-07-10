package content.activity.event.random.mime_theatre

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.activity.event.random.mysteriousOldMan
import content.activity.event.random.rewardCostumePoint
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.skill.magic.jewellery.teleport
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.suspend.pauseString
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Mime random event: the Mysterious Old Man drops the player into the theatre where the Mime performs
 * emotes under a spotlight. After each performance the Mime bows to the player, who copies it by
 * picking the matching emote on the mime interface (188). Three correct copies unlock the mime emotes
 * and award a random event gift, then the player is teleported home.
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
        set("mime_correct", 0)
        mysteriousOldMan()
        kidnap(SPAWN)
        npc<Neutral>("mysterious_old_man", "Here's a little challenge for you:<br>Copy the Mime's performance, then you'll be released.")
        walkOverDelay(WATCH)

        val mime = NPCs.firstOrNull(MIME_TILE) { it.id == "mime" } ?: NPCs.add("mime", MIME_TILE, ticks = -1, owner = this)
        runMime(mime)
    }

    private suspend fun Player.runMime(mime: NPC) {
        while (get("mime_correct", 0) < REQUIRED) {
            val expected = EMOTES.random(random)
            set("mime_emote", expected)

            // Spotlight the Mime and perform the emote facing the audience; the prompt sits in the
            // chatbox (no continue button) while the player watches.
            centeredMessage("Watch the Mime.", "See what emote he performs.")
            lightMime(on = true)
            lightPlayer(on = false)
            delay(SPOTLIGHT_TICKS) // let the spotlight finish turning on before the Mime performs
            mime.face(AUDIENCE)
            mime.anim("emote_$expected")
            delay(PERFORM_TICKS)

            // Bow to the player, then hand over to them.
            mime.face(tile)
            mime.anim("emote_bow")
            delay(BOW_TICKS)
            lightMime(on = false)
            lightPlayer(on = true)
            delay(SPOTLIGHT_TICKS) // let the spotlight finish turning on before the player performs

            val chosen = awaitEmote()
            anim("emote_$chosen")
            if (chosen == expected) {
                centeredMessage("Correct!")
                inc("mime_correct")
                mime.face(AUDIENCE)
                delay(CORRECT_TICKS)
            } else {
                statement("That wasn't quite right. Watch the Mime again.", clickToContinue = false)
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

    /** Show a no-prompt message with an empty first line so its (centred) text sits a line lower. */
    private fun Player.centeredMessage(vararg lines: String) {
        val padded = listOf("", *lines)
        val id = "dialogue_message_np${padded.size}"
        if (!open(id)) {
            return
        }
        padded.forEachIndexed { index, line -> interfaces.sendText(id, "line${index + 1}", line) }
    }

    /** Spotlight over the Mime (2010, 4761). */
    private fun lightMime(on: Boolean) = spotlight(MIME_LIGHT, on)

    /** Spotlight over the player's spot (2007, 4761). */
    private fun lightPlayer(on: Boolean) = spotlight(PLAYER_LIGHT, on)

    private fun spotlight(tile: Tile, on: Boolean) {
        GameObjects.at(tile).firstOrNull { it.id == "mime_spotlight" }
            ?.anim(if (on) "mime_spotlight_on" else "mime_spotlight_off")
    }

    private fun Player.finish() {
        for (unlock in MIME_EMOTES) {
            set("unlocked_emote_$unlock", true)
        }
        addOrDrop("random_event_gift")
        rewardCostumePoint("mime")
        clear("mime_emote")
        clear("mime_correct")

        // Clear the event state (so the teleport isn't blocked) then modern-teleport home.
        val origin = Tile(this["random_event_origin", tile.id])
        RandomEvents.completeInPlace(this)
        teleport(origin, "modern")
    }

    companion object {
        private const val REQUIRED = 3
        private const val INTERFACE = "dialogue_macro_mime_emotes"
        private const val PERFORM_TICKS = 4
        private const val BOW_TICKS = 3
        private const val SPOTLIGHT_TICKS = 3 // duration of the spotlight on/off animation
        private const val CORRECT_TICKS = 8

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
