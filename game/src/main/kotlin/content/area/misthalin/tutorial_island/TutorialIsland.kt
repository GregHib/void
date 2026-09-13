package content.area.misthalin.tutorial_island

import content.entity.player.modal.GameFrame
import world.gregs.voidps.engine.client.clearHints
import world.gregs.voidps.engine.client.markHint
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.hasTypeOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.math.roundToInt

object TutorialIsland {

    /**
     * Segments in the interface 371 bar. Client script 1437 lights segment n only when varp 406 is
     * greater than n, so the varp is the segment count plus one and 1 means empty.
     */
    private const val PROGRESS_SEGMENTS = 20

    val stages: Int
        get() = Tables.get("tutorial_island").rows().size

    fun row(stage: Int): RowDefinition? = Rows.getOrNull("tutorial_island.stage_$stage")

    /** The closing stage, where the player casts Home Teleport to leave the island. */
    val lastStage: Int
        get() = stages - 1

    /**
     * Rewrites every piece of client state the current stage owns. Safe to call repeatedly;
     * login restore and stage advancement both go through here.
     */
    fun refresh(player: Player) {
        val row = row(player.tutorialStage) ?: return
        player.renderTutorialProgress()
        player.unlockTab(row)
        // After the unlock, so the client is told to flash something it already has.
        player["tab_flash"] = row.stringOrNull("flash") ?: "None"
        player.renderTutorialHint(row)
        sendInstruction(player)
    }

    private fun Player.renderTutorialHint(row: RowDefinition) {
        clearHints()
        val npcId = row.stringOrNull("hint_npc")
        if (npcId != null) {
            val npc = NPCs.findOrNull(tile.regionLevel, npcId) ?: return
            markHint(npc)
            return
        }
        val target = row.tileOrNull("hint_tile") ?: return
        markHint(target, radius = 2, height = row.intOrNull("hint_height") ?: 0)
    }

    /**
     * Text may only be sent once the client has the interface loaded. `sendText` resolves the
     * component against the server's own definitions, so it can't tell; the client applies the
     * packet a tick later and dies on a missing component, taking the whole client with it.
     *
     * `open` returns false both when the interface can't be opened and when it already is, so the
     * check has to be `hasOpen` or the bar would stop updating after the first stage.
     */
    private fun Player.renderTutorialProgress() {
        if (!ensureOpen("tutorial_overlay", "above_chat_box")) {
            return
        }
        interfaces.sendVisibility("tutorial_overlay", "welcome", false)
        val percent = tutorialStage.toDouble() / stages
        set("tutorial_progress", (percent * PROGRESS_SEGMENTS).roundToInt() + 1)
        interfaces.sendText("tutorial_overlay", "percent", "${(percent * 100).roundToInt()}% Done")
    }

    /**
     * Opens [id] only when it isn't already open and nothing else holds its slot.
     *
     * `Player.open` closes whatever currently occupies the slot first - including [id] itself on a
     * repeat call - and `Interfaces.remove` clears the player's weak queue. Re-opening blindly would
     * therefore cancel any delayed action in progress; smelting, for one, defers its transaction by
     * four ticks, so the animation would play and no bar would ever appear.
     */
    private fun Player.ensureOpen(id: String, type: String): Boolean {
        if (hasOpen(id)) {
            return true
        }
        if (hasTypeOpen(type)) {
            return false
        }
        return open(id)
    }

    /** Reveals the component this stage unlocks; earlier ones are already open. */
    private fun Player.unlockTab(row: RowDefinition) {
        val unlock = row.stringOrNull("unlock") ?: return
        interfaces.sendVisibility(interfaces.gameFrame, GameFrame.tabComponent(unlock), true)
        open(unlock)
    }

    /**
     * Redraws the instruction box, unless a conversation is using the chat box.
     *
     * The box shares the chat box slot with dialogue on the client but has its own type, so opening
     * a dialogue doesn't evict it server-side. Re-asserting it while one is up would paint the
     * instructions straight over the NPC's words. Whatever closes that dialogue schedules another
     * redraw, so the box comes back on its own.
     *
     * @return whether the box was drawn
     */
    fun sendInstruction(player: Player): Boolean {
        val row = row(player.tutorialStage) ?: return false
        if (player.dialogue != null) {
            return false
        }
        if (player.hasOpen("tutorial_text")) {
            // A dialogue may have taken the chat box slot on the client. Re-assert the box without
            // closing anything, because closing clears the player's weak queue.
            player.interfaces.refresh("tutorial_text")
        } else if (!player.open("tutorial_text")) {
            return false
        }
        player.interfaces.sendText("tutorial_text", "title", serverName(row.string("title")))
        val lines = row.stringListOrNull("lines") ?: emptyList()
        for (index in 1..6) {
            player.interfaces.sendText("tutorial_text", "line$index", serverName(lines.getOrElse(index - 1) { "" }))
        }
        return true
    }

    private fun serverName(text: String): String = text.replace("%server%", Settings["server.name"])

    /**
     * Game frame components that aren't sidebar tabs. The orbs are all present from the start,
     * only the sidebar is revealed a tab at a time.
     */
    private val alwaysOpen = setOf(
        "chat_box",
        "chat_background",
        "filter_buttons",
        "private_chat",
        "health_orb",
        "prayer_orb",
        "energy_orb",
        "summoning_orb",
        "task_popup",
        "area_status_icon",
    )

    fun unlockedTab(player: Player, component: String): Boolean {
        if (!player.inTutorial) {
            return true
        }
        if (alwaysOpen.contains(component)) {
            return true
        }
        return unlocked(player.tutorialStage, component)
    }

    private fun unlocked(stage: Int, component: String): Boolean {
        for (index in 0..stage) {
            if (row(index)?.stringOrNull("unlock") == component) {
                return true
            }
        }
        return false
    }
}

val Player.tutorialStage: Int
    get() = get("tutorial_stage", -1)

val Player.inTutorial: Boolean
    get() = tutorialStage >= 0

/**
 * Advances only when the player is on [from], so a handler can be registered without
 * re-checking the stage itself.
 */
fun Player.advanceTutorial(from: Int) {
    if (tutorialStage != from) {
        return
    }
    set("tutorial_stage", from + 1)
    TutorialIsland.refresh(this)
}
