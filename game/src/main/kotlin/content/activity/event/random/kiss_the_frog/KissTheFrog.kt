package content.activity.event.random.kiss_the_frog

import content.activity.event.random.RandomEvents
import content.activity.event.random.endInPlaceEvent
import content.activity.event.random.kidnap
import content.activity.event.random.rewardCostumePoint
import content.activity.event.random.startInPlaceEvent
import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.closeTabs
import content.quest.openTabs
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Kiss the Frog (Frog Princess) random event: the Frog Herald hops up to the player and whisks them
 * to the Land of the Frogs. Among the identical frogs one wears a crown - the Frog Prince (for female
 * players) or Princess (for male players). Kissing the crowned frog restores it to human form and
 * earns a gift box.
 *
 * Talking to the other frogs offends the royal; do it too often and the royal turns the player into a
 * frog and banishes them to a small frog cave, where a second crowned royal will send them home (empty
 * handed, dumped somewhere random in Gielinor) if spoken to.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Frog
 */
class KissTheFrog : Script {

    init {
        RandomEvents.register("kiss_the_frog") { startEvent() }

        npcOperate("Talk-to", "frog_herald") { (herald) ->
            if (get<String>("random_event") != "kiss_the_frog") {
                message("It hops away from you.")
                return@npcOperate
            }
            if (herald.owner == this) {
                // The herald that appeared beside the player: whisk them to the Land of the Frogs.
                npc<Neutral>("Hey, $name, the Frog ${royal()} needs your help!")
                endInPlaceEvent(herald)
                kidnap(LAND)
                spawnCrown()
                restrictTabs()
                explain()
            } else {
                explain()
            }
        }

        // `frog_*frogland` (no mandatory underscore) matches the bare `frog_frogland` baked into the
        // region as well as the numbered `frog_N_frogland` variants; `frog_*_frogland` would miss the
        // bare frogs, so talking to them gave "Nothing interesting happens".
        npcOperate("Talk-to", "frog_*frogland") { (frog) ->
            if (get<String>("random_event") != "kiss_the_frog") {
                return@npcOperate
            }
            when (frog.index) {
                get("ktf_crown", -1) -> royalFrog(frog)
                get("ktf_escape", -1) -> escapeFrog()
                else -> plainFrog()
            }
        }

        // The crown and escape royals have no lifetime timer, so remove them on logout; the
        // relog resume in startEvent spawns fresh ones.
        playerDespawn {
            if (get<String>("random_event") == "kiss_the_frog") {
                removeRoyals()
            }
        }
    }

    private fun Player.startEvent() {
        when {
            get("ktf_fail", false) -> {
                // Relogged inside the frog cave: restore the frog form and the escape royal.
                transform(PLAYER_FROG)
                tele(FAIL_CAVE)
                restrictTabs()
                val escape = NPCs.add(ROYAL, ESCAPE_TILE, ticks = -1, owner = this)
                set("ktf_escape", escape.index)
            }
            tile.region == LAND.region -> {
                // Relogged in the Land of the Frogs: hide a fresh crowned royal among the frogs.
                spawnCrown()
                restrictTabs()
            }
            else -> {
                clear("ktf_offended")
                startInPlaceEvent("frog_herald", nagLines())
            }
        }
    }

    /** The royal the player must rescue - a male player helps the Princess, a female player the Prince. */
    private fun Player.royal() = if (male) "Princess" else "Prince"

    private fun Player.crownFrog() = if (male) "frog_princess" else "frog_prince"

    /** Spawn the crowned royal (npc 3300) hidden among the plain frogs of the Land of the Frogs. */
    private fun Player.spawnCrown() {
        val frog = NPCs.add(ROYAL, CROWN_TILES.random(random), ticks = -1, owner = this)
        set("ktf_crown", frog.index)
    }

    /**
     * In the Land of the Frogs the player only keeps six tabs: Friends List, Ignore List, Clan Chat,
     * Options, Music Player and Notes. [closeTabs] closes everything else (and Notes), so re-open Notes.
     */
    private fun Player.restrictTabs() {
        closeTabs()
        open("notes")
    }

    private suspend fun Player.explain() {
        val royal = royal()
        val subject = if (male) "She" else "He"
        val him = if (male) "Her" else "Him"
        npc<Neutral>("Welcome to the Land of the Frogs.")
        player<Quiz>("What am I doing here?")
        npc<Neutral>("The Frog $royal sent for you.")
        player<Quiz>("Who is the Frog $royal?")
        npc<Neutral>("$subject is the frog with the crown. Make sure you speak to $him, not the other frogs, or $subject'll be offended.")
    }

    /** The crowned royal in the Land of the Frogs: kiss to win. */
    private suspend fun Player.royalFrog(frog: NPC) {
        npc<Sad>("$name, you must help me! I have been turned into a frog by a well-meaning wizard with an unfortunate obsession with frogs.")
        npc<Neutral>("The only thing that will restore my true form is a kiss.")
        player<Laugh>("Excuses, excuses! Okay, if that's what you want...")
        kiss(frog)
    }

    private suspend fun Player.kiss(frog: NPC) {
        // If the player clicks off the "Thank you" dialogue (which cancels this handler), the walk fires
        // this trigger and they still fade out, collect the reward and teleport home; reading it through
        // does the same at the end.
        walkTrigger = { queue("ktf_kiss") { completeKiss() } }
        // Turn to face each other before leaning in.
        face(frog.tile)
        frog.face(tile)
        delay(1)
        frog.anim("frog_kiss")
        anim("human_kiss_the_frog")
        delay(3)
        frog.gfx("spell_splash")
        frog.anim("morph_from_frog")
        delay(2)
        frog.transform(crownFrog()) // the frog stands up as the human royal
        delay(2)
        npc<Happy>("Thank you so much, $name. I must return to my fairy tale kingdom now, but I will leave you a reward for your kindness.")
        frog.anim("emote_blow_kiss")
        frog.gfx("emote_blow_kiss")
        delay(3)
        completeKiss()
    }

    /** Fade to black, hand over the reward and teleport the player home. */
    private suspend fun Player.completeKiss() {
        if (get<String>("random_event") != "kiss_the_frog") {
            return
        }
        walkTrigger = null
        open("fade_out")
        delay(2)
        rewardCostumePoint("frog")
        message("You've been given a gift!")
        finishEvent()
        open("fade_in")
    }

    /** The crowned royal in the frog cave: talking to it releases a hexed player (no reward). */
    private suspend fun Player.escapeFrog() {
        // The cave is enclosed, so the player must not be able to strand themselves by walking off. If
        // they click off the dialogue (which cancels this handler), the walk fires this trigger and they
        // fade out and teleport home anyway; reading the dialogue through does the same at the end.
        walkTrigger = { queue("ktf_escape_cave") { escapeCave() } }
        npc<Sad>("Oh, another poor soul hexed into a frog for their bad manners.")
        npc<Neutral>("Here, I'll send you on your way. Do be more polite next time.")
        escapeCave()
    }

    /** Fade to black and dump the hexed player somewhere random (no reward). */
    private suspend fun Player.escapeCave() {
        if (get<String>("random_event") != "kiss_the_frog") {
            return
        }
        walkTrigger = null
        open("fade_out")
        delay(2)
        failEvent()
        open("fade_in")
    }

    private suspend fun Player.plainFrog() {
        if (get("ktf_fail", false)) {
            // Already a frog - the plain frogs have nothing to say to another frog.
            npc<Neutral>("Ribbit.")
            return
        }
        // Ignoring the royal to talk to plain frogs offends them; do it too often and the royal turns
        // the player into a frog and banishes them to the frog cave.
        val offences = inc("ktf_offended")
        if (offences < OFFENCE_LIMIT) {
            npc<Neutral>("Ribbit.")
            message("The Frog ${royal()} looks offended that you spoke to another frog.")
            return
        }
        npc<Neutral>("Well, we'll see how you like being a frog!")
        banishToCave()
    }

    /** Third offence: morph the player into a frog and teleport them to the frog cave to escape from. */
    private suspend fun Player.banishToCave() {
        set("ktf_fail", true)
        message("You've been turned into a frog!")
        anim("morph_to_frog")
        delay(2)
        transform(PLAYER_FROG)
        delay(2)
        tele(FAIL_CAVE)
        val escape = NPCs.add(ROYAL, ESCAPE_TILE, ticks = -1, owner = this)
        set("ktf_escape", escape.index)
    }

    /** Successful kiss: return the player home. */
    private fun Player.finishEvent() {
        cleanup()
        RandomEvents.complete(this, "random_event_gift")
    }

    /** Escaped the frog cave: dump the player somewhere random with no reward. */
    private fun Player.failEvent() {
        cleanup()
        RandomEvents.fail(this)
    }

    private fun Player.cleanup() {
        openTabs()
        clearTransform()
        removeRoyals()
        clear("ktf_fail")
        clear("ktf_offended")
    }

    private fun Player.removeRoyals() {
        NPCs.indexed(get("ktf_crown", -1))?.let { if (it.owner == this) NPCs.remove(it) }
        NPCs.indexed(get("ktf_escape", -1))?.let { if (it.owner == this) NPCs.remove(it) }
        clear("ktf_crown")
        clear("ktf_escape")
    }

    private fun Player.nagLines() = listOf(
        "$name, the Frog ${royal()} needs your help.",
        "Greetings from the Frog ${royal()}, $name!",
        "Talk to the Frog ${royal()}, $name!",
        "Please respond to the Frog ${royal()}, $name!",
    )

    companion object {
        // Number of times the player can talk to the wrong frogs before the royal turns them into one.
        private const val OFFENCE_LIMIT = 3

        // The crowned royal frog (Frog Prince/Princess) and the plain frog the player is morphed into.
        private const val ROYAL = "frog_9_frogland"
        private const val PLAYER_FROG = "frog_frogland"

        private val LAND = Tile(2445, 4770)

        // The small cave the player is banished to on failure, and the escape royal's spot within it.
        private val FAIL_CAVE = Tile(2464, 4782)
        private val ESCAPE_TILE = Tile(2464, 4784)

        // Spots among the plain frogs near the arrival tile [LAND] where the crowned royal is hidden.
        private val CROWN_TILES = listOf(
            Tile(2442, 4774),
        )
    }
}
