package content.activity.event.random.mime_theatre

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.activity.event.random.mysteriousOldMan
import content.activity.event.random.rewardCostumeOrCoins
import content.quest.instanceOffset
import content.quest.setInstanceLogout
import content.quest.smallInstance
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Mime random event: the player is whisked to a stage where the mime performs emotes for them to
 * copy. Copying four in a row unlocks the mime emotes (glass wall/box, climb rope, lean) and awards
 * a piece of the mime costume (or coins if the set is complete).
 * https://runescape.wiki/w/Random_events?oldid=3667851#Mime
 */
class Mime : Script {

    init {
        RandomEvents.register("mime") { startEvent() }

        npcTimerStart(PERFORM_TIMER) { PERFORM_INTERVAL }
        npcTimerTick(PERFORM_TIMER) {
            val owner = owner
            if (owner == null || owner.get<String>("random_event") != "mime") {
                NPCs.remove(this)
                return@npcTimerTick Timer.CANCEL
            }
            owner.get<String>("mime_emote")?.let { anim("emote_$it") }
            Timer.CONTINUE
        }

        // Runs alongside the emote tab's own handler, checking whether the copied emote matches.
        interfaceOption(id = "emotes:*") {
            if (get<String>("random_event") != "mime") {
                return@interfaceOption
            }
            copyEmote(it.option.toSnakeCase())
        }
    }

    private suspend fun Player.startEvent() {
        smallInstance(Region(MIME_REGION), levels = 1)
        setInstanceLogout(Tile(this["random_event_origin", tile.id]))
        mysteriousOldMan()
        kidnap(STAGE.add(instanceOffset()))
        val mime: NPC = NPCs.add("mime", MIME_TILE.add(instanceOffset()), ticks = -1, owner = this)
        mime.watch(this)
        mime.softTimers.start(PERFORM_TIMER)
        set("mime_correct", 0)
        nextEmote()
        face(mime.tile)
        message("Copy the mime's actions to earn your reward.")
    }

    private fun Player.nextEmote() {
        set("mime_emote", EMOTES.random(random))
    }

    private suspend fun Player.copyEmote(emote: String) {
        if (emote != get<String>("mime_emote")) {
            return // wrong emote earns no credit
        }
        if (inc("mime_correct") >= REQUIRED) {
            finish()
        } else {
            nextEmote()
        }
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
        private const val REQUIRED = 4
        private const val PERFORM_TIMER = "mime_perform"
        private const val PERFORM_INTERVAL = 5
        private const val MIME_REGION = 8010
        private val STAGE = Tile(2011, 4759)
        private val MIME_TILE = Tile(2011, 4762)

        private val EMOTES = listOf(
            "think", "cry", "laugh", "dance", "cheer", "clap", "wave", "bow", "angry", "jump_for_joy",
        )
        private val MIME_EMOTES = listOf("glass_wall", "glass_box", "climb_rope", "lean")
    }
}
