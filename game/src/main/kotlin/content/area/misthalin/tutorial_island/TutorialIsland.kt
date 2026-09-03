package content.area.misthalin.tutorial_island

import content.entity.player.modal.tabComponent
import world.gregs.voidps.engine.client.clearHints
import world.gregs.voidps.engine.client.markHint
import world.gregs.voidps.engine.client.ui.close
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
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import kotlin.math.roundToInt

private const val NO_FLASH = "None"

/** Text lines interface 372 lays out below its title. */
const val TUTORIAL_TEXT_LINES = 6

/**
 * Segments in the interface 371 bar. Client script 1437 lights segment n only when varp 406 is
 * greater than n, so the varp is the segment count plus one and 1 means empty.
 */
private const val PROGRESS_SEGMENTS = 20

/**
 * Tutorial Island runs off a single persistent stage counter. Every row of the
 * `tutorial_island` table is one stage, in order, and a stage only ever advances to the
 * next index, so an interaction fired from anywhere else is a no-op.
 */
object TutorialIsland {

    const val TABLE: String = "tutorial_island"

    val stages: Int
        get() = Tables.get(TABLE).rows().size

    fun row(stage: Int): RowDefinition? = Rows.getOrNull("$TABLE.stage_$stage")

    /** Whether [component] has been revealed on or before [stage]. */
    fun unlocked(stage: Int, component: String): Boolean {
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
 * Game frame components that aren't sidebar tabs, and so stay visible for the whole
 * tutorial.
 */
private val alwaysOpen = setOf(
    "chat_box",
    "chat_background",
    "filter_buttons",
    "private_chat",
    "health_orb",
    "prayer_orb",
    "summoning_orb",
    "task_popup",
    "area_status_icon",
)

fun Player.tutorialUnlocked(component: String): Boolean {
    if (!inTutorial) {
        return true
    }
    if (alwaysOpen.contains(component)) {
        return true
    }
    return TutorialIsland.unlocked(tutorialStage, component)
}

/**
 * Rewrites every piece of client state the current stage owns. Safe to call repeatedly;
 * login restore and stage advancement both go through here.
 */
fun Player.renderTutorial() {
    val row = TutorialIsland.row(tutorialStage) ?: return
    renderTutorialProgress()
    renderTutorialUnlock(row)
    // After the unlock, so the client is told to flash something it already has.
    set("tab_flash", row.stringOrNull("flash") ?: NO_FLASH)
    renderTutorialHint(row)
    renderTutorialText()
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
    val percent = tutorialStage.toDouble() / TutorialIsland.stages
    set("tutorial_progress", (percent * PROGRESS_SEGMENTS).roundToInt() + 1)
    interfaces.sendText("tutorial_overlay", "percent", "${(percent * 100).roundToInt()}% Done")
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
fun Player.renderTutorialText(): Boolean {
    val row = TutorialIsland.row(tutorialStage) ?: return false
    if (dialogue != null) {
        return false
    }
    if (hasOpen("tutorial_text")) {
        // A dialogue may have taken the chat box slot on the client. Re-assert the box without
        // closing anything, because closing clears the player's weak queue.
        interfaces.refresh("tutorial_text")
    } else if (!open("tutorial_text")) {
        return false
    }
    interfaces.sendText("tutorial_text", "title", serverName(row.string("title")))
    val lines = row.stringListOrNull("lines") ?: emptyList()
    for (index in 1..TUTORIAL_TEXT_LINES) {
        interfaces.sendText("tutorial_text", "line$index", serverName(lines.getOrElse(index - 1) { "" }))
    }
    return true
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
private fun Player.renderTutorialUnlock(row: RowDefinition) {
    val unlock = row.stringOrNull("unlock") ?: return
    interfaces.sendVisibility(interfaces.gameFrame, tabComponent(unlock), true)
    open(unlock)
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

private fun serverName(text: String): String = text.replace("%server%", Settings["server.name"])

/**
 * Advances only when the player is on [from], so a handler can be registered without
 * re-checking the stage itself.
 */
fun Player.advanceTutorial(from: Int) {
    if (tutorialStage != from) {
        return
    }
    set("tutorial_stage", from + 1)
    renderTutorial()
}

/**
 * Hands back any of [items] the player no longer has. Every instructor offers this, so a lost,
 * dropped or ruined attempt can never strand a stage that's waiting on one of them.
 */
fun Player.resupply(vararg items: String): Boolean {
    var missing = false
    for (item in items) {
        if (carriesItem(item)) {
            continue
        }
        inventory.add(item)
        missing = true
    }
    return missing
}

fun Player.resupply(item: String, amount: Int): Boolean {
    if (carriesItem(item)) {
        return false
    }
    inventory.add(item, amount)
    return true
}

fun Player.leaveTutorial() {
    set("tutorial_stage", -1)
    set("tutorial_complete", true)
    // Marks the introduction as done so `Introduction` never re-runs character creation
    // or hands out a second starter kit.
    this["creation"] = System.currentTimeMillis()
    clear("tab_flash")
    clear("tutorial_progress")
    clearHints()
    close("tutorial_text")
    close("tutorial_overlay")
}
