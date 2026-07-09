package content.activity.event.random.kiss_the_frog

import content.activity.event.random.RandomEvents
import content.activity.event.random.endInPlaceEvent
import content.activity.event.random.kidnap
import content.activity.event.random.startInPlaceEvent
import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Kiss the Frog (Frog Princess) random event: the Frog Herald hops up to the player and whisks them
 * to the Land of the Frogs. Among the identical frogs one wears a crown - the Frog Prince (for female
 * players) or Princess (for male players). Kissing the crowned frog restores it to human form and
 * earns a gift box; kissing (or talking to) the wrong frog turns the player into a frog until they
 * apologise to the crowned one.
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
                npc<Neutral>("frog_herald", "Hey, $name, the Frog ${royal()} needs your help!")
                endInPlaceEvent(herald)
                kidnap(LAND)
                spawnCrown()
                explain()
            } else {
                explain()
            }
        }

        npcOperate("Talk-to", "frog_*_frogland") { (frog) ->
            if (get<String>("random_event") != "kiss_the_frog") {
                return@npcOperate
            }
            if (frog.index == get("ktf_crown", -1)) {
                royalFrog(frog)
            } else {
                plainFrog(frog)
            }
        }
    }

    private fun Player.startEvent() {
        clear("ktf_fail")
        startInPlaceEvent("frog_herald", nagLines())
    }

    /** The royal the player must rescue - a male player helps the Princess, a female player the Prince. */
    private fun Player.royal() = if (male) "Princess" else "Prince"

    private fun Player.crownFrog() = if (male) "frog_princess" else "frog_prince"

    private fun Player.spawnCrown() {
        val frog = NPCs.add("frog_10_frogland", CROWN_TILES.random(random), ticks = -1, owner = this)
        set("ktf_crown", frog.index)
    }

    private suspend fun Player.explain() {
        val royal = royal()
        val subject = if (male) "She" else "He"
        val him = if (male) "Her" else "Him"
        npc<Neutral>("frog_herald", "Welcome to the Land of the Frogs.")
        player<Quiz>("What am I doing here?")
        npc<Neutral>("frog_herald", "The Frog $royal sent for you.")
        player<Quiz>("Who is the Frog $royal?")
        npc<Neutral>("frog_herald", "$subject is the frog with the crown. Make sure you speak to $him, not the other frogs, or $subject'll be offended.")
    }

    private suspend fun Player.royalFrog(frog: NPC) {
        val id = frog.id
        if (get("ktf_fail", false)) {
            npc<Sad>(id, "Hmph. Have you come to apologise for ignoring me?")
            player<Neutral>("I'm very sorry. Please change me back!")
            npc<Neutral>(id, "All right. But in future be more polite.")
            clearTransform()
            finishEvent()
            return
        }
        npc<Sad>(id, "$name, you must help me! I have been turned into a frog by a well-meaning wizard with an unfortunate obsession with frogs.")
        npc<Neutral>(id, "The only thing that will restore my true form is a kiss.")
        player<Laugh>("Excuses, excuses! Okay, if that's what you want...")
        kiss(frog)
    }

    private suspend fun Player.kiss(frog: NPC) {
        face(frog.tile)
        frog.face(tile)
        frog.anim("frog_kiss")
        anim("human_kiss_the_frog")
        delay(3)
        frog.gfx("spell_splash")
        frog.anim("morph_from_frog")
        delay(2)
        frog.transform(crownFrog()) // the frog stands up as the human royal
        delay(2)
        npc<Happy>(crownFrog(), "Thank you so much, $name. I must return to my fairy tale kingdom now, but I will leave you a reward for your kindness.")
        frog.anim("emote_blow_kiss")
        frog.gfx("emote_blow_kiss")
        delay(3)
        open("fade_out")
        delay(2)
        addOrDrop("random_event_gift")
        message("You've been given a gift!")
        NPCs.remove(frog)
        finishEvent()
        open("fade_in")
    }

    private suspend fun Player.plainFrog(frog: NPC) {
        if (get("ktf_fail", false)) {
            npc<Angry>(frog.id, "Don't talk to me! Speak to the frog ${royal()}!")
            return
        }
        npc<Neutral>(frog.id, "Well, we'll see how you like being a frog!")
        set("ktf_fail", true)
        anim("morph_to_frog")
        delay(1)
        transform("frog_9_frogland")
    }

    private fun Player.finishEvent() {
        clearTransform()
        NPCs.indexed(get("ktf_crown", -1))?.let { if (it.owner == this) NPCs.remove(it) }
        clear("ktf_fail")
        clear("ktf_crown")
        RandomEvents.complete(this)
    }

    private fun Player.nagLines() = listOf(
        "$name, the Frog ${royal()} needs your help.",
        "Greetings from the Frog ${royal()}, $name!",
        "Talk to the Frog ${royal()}, $name!",
        "Please respond to the Frog ${royal()}, $name!",
    )

    companion object {
        private val LAND = Tile(2463, 4781)

        // Spots among the plain frogs where the crowned royal frog can be hidden.
        private val CROWN_TILES = listOf(
            Tile(2456, 4787),
            Tile(2458, 4774),
            Tile(2463, 4774),
            Tile(2464, 4782),
            Tile(2470, 4774),
            Tile(2454, 4783),
        )
    }
}
