package content.activity.event.random.evil_bob

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Unamused
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.instanceOffset
import content.quest.setInstanceLogout
import content.quest.smallInstance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Evil Bob random event: an evil incarnation of Bob the cat whisks the player to the "ScapeRune"
 * island and demands to be fed. A terrified servant hints which of the four fishing spots holds the
 * fish Bob likes; the player nets it (it comes out already cooked), uncooks it at the cold fire, and
 * serves the raw fish to Bob. The right fish sends him to sleep and opens the exit portal; a wrong
 * one makes him more attentive so an extra correct fish is needed. Reward: a random event gift.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Evil_Bob
 */
class EvilBob : Script {

    init {
        RandomEvents.register("evil_bob") { startEvent() }

        npcOperate("Talk-to", "evil_bob") { (bob) ->
            if (bob.owner != this) {
                message("It pays you no attention.")
                return@npcOperate
            }
            evilBobDialogue()
        }

        itemOnNPCOperate("raw_fish_like_thing", "evil_bob") { (bob) ->
            if (bob.owner == this) serveCorrect()
        }
        itemOnNPCOperate("raw_fish_like_thing_incorrect", "evil_bob") { (bob) ->
            if (bob.owner == this) serveWrong()
        }

        npcOperate("Talk-to", "evil_bob_servant") { (servant) ->
            if (servant.owner != this) {
                message("They're too frightened to speak to you.")
                return@npcOperate
            }
            servantDialogue()
        }

        objectOperate("Net", "evil_bob_fishing_spot") {
            netFishingSpot()
        }

        itemOnObjectOperate("fish_like_thing", "evil_bob_uncooking_pot") {
            uncook("fish_like_thing", "raw_fish_like_thing")
        }
        itemOnObjectOperate("fish_like_thing_incorrect", "evil_bob_uncooking_pot") {
            uncook("fish_like_thing_incorrect", "raw_fish_like_thing_incorrect")
        }

        objectOperate("Enter", "evil_bob_exit_portal", arrive = false) { (portal) ->
            if (!get("evil_bob_complete", false)) {
                npc<Angry>("evil_bob", "You're going nowhere, human!")
                return@objectOperate
            }
            walkOverDelay(portal.tile)
            bob()?.let { face(it.tile) }
            delay(2)
            anim("emote_raspberry")
            say("Be seeing you!")
            delay(2)
            reward()
            clearState()
            RandomEvents.complete(this)
            message("Welcome back.")
        }
    }

    private suspend fun Player.startEvent() {
        smallInstance(Region(ISLAND_REGION), levels = 1)
        setInstanceLogout(Tile(this["random_event_origin", tile.id]))
        val offset = instanceOffset()
        for (zone in ZONES) {
            GameObjects.add("evil_bob_fishing_spot", zone.spot.add(offset), collision = false)
        }
        GameObjects.add("evil_bob_uncooking_pot", POT.add(offset), collision = false)
        GameObjects.add("evil_bob_exit_portal", PORTAL.add(offset), collision = false)

        evilBobCatIntro()
        kidnap(ISLAND.add(offset))

        val bob = NPCs.add("evil_bob", BOB_TILE.add(offset), ticks = -1, owner = this)
        bob.watch(this)
        set("evil_bob_npc", bob.index)
        val servant = NPCs.add("evil_bob_servant", SERVANT_TILE.add(offset), ticks = -1, owner = this)
        servant.watch(this)

        assignZone()
        inventory.add("small_fishing_net")
        face(bob.tile)
        message("Welcome to ScapeRune.")
        evilBobDialogue()
    }

    private suspend fun Player.evilBobCatIntro() {
        val cat = NPCs.addRandom("evil_bob_cat", tile.toCuboid(1), ticks = 25, owner = this)
            ?: NPCs.add("evil_bob_cat", tile, ticks = 25, owner = this)
        cat.watch(this)
        cat.say("Meow.")
        delay(2)
    }

    private fun Player.assignZone() {
        set("evil_bob_zone", random.nextInt(ZONES.size) + 1) // 1-based; 0 means "unassigned"
    }

    private fun Player.bob(): NPC? = NPCs.indexed(get("evil_bob_npc", -1))?.takeIf { it.id == "evil_bob" }

    private fun Player.currentZone(): Int {
        val local = tile.minus(instanceOffset())
        return ZONES.indexOfFirst { it.contains(local) } + 1 // 0 when in no zone
    }

    private suspend fun Player.netFishingSpot() {
        when {
            get("evil_bob_complete", false) ->
                statement("Evil Bob's had his fill; there's no need to fish any more.")
            get("evil_bob_new_spot", false) ->
                statement("You don't know if this is a good place to go fishing. Perhaps you should ask someone, like one of the human servants.")
            !inventory.contains("small_fishing_net") ->
                npc<Sad>("evil_bob_servant", "You'll need a fishing net. There are plenty scattered around the beach.")
            inventory.isFull() ->
                message("You don't have enough space in your inventory.")
            holdsFish() ->
                npc<Sad>("evil_bob_servant", "You've already got a fish. Come over here to uncook it, then serve it to Evil Bob.")
            else -> {
                anim("fish_small_fishing_net")
                message("You cast out your net...")
                delay(5)
                val correct = currentZone() == get("evil_bob_zone", 0)
                inventory.add(if (correct) "fish_like_thing" else "fish_like_thing_incorrect")
                clearAnim()
                item("fish_like_thing", "You catch a... what is this?? Is this a fish?? And it's cooked already??")
            }
        }
    }

    private fun Player.holdsFish() = inventory.contains("fish_like_thing") ||
        inventory.contains("fish_like_thing_incorrect") ||
        inventory.contains("raw_fish_like_thing") ||
        inventory.contains("raw_fish_like_thing_incorrect")

    private suspend fun Player.uncook(cooked: String, raw: String) {
        anim("cook_fire")
        delay(2)
        if (inventory.remove(cooked)) {
            inventory.add(raw)
        }
        clearAnim()
    }

    private suspend fun Player.serveCorrect() {
        if (!inventory.remove("raw_fish_like_thing")) {
            return
        }
        npc<Unamused>("evil_bob", "Mmm, mmm... that's delicious.")
        if (get("evil_bob_attentive", false)) {
            set("evil_bob_attentive", false)
            assignZone()
            set("evil_bob_new_spot", true)
            npc<Angry>("evil_bob", "Now get me another, you no-good human.")
            statement("Evil Bob seems slightly less attentive of you.")
        } else {
            npc<Neutral>("evil_bob", "Now, let me take... a little... catnap.")
            set("evil_bob_complete", true)
            bob()?.say("ZZZzzz")
            statement("Evil Bob has fallen asleep. Slip away through the portal while you can!")
        }
    }

    private suspend fun Player.serveWrong() {
        if (!inventory.remove("raw_fish_like_thing_incorrect")) {
            return
        }
        set("evil_bob_attentive", true)
        set("evil_bob_new_spot", true)
        npc<Angry>("evil_bob", "What was this? That was absolutely disgusting!")
        npc<Angry>("evil_bob", "Don't you know what kind of fish I like? Talk to my other servants for some advice.")
        statement("Evil Bob seems more attentive of you.")
    }

    private suspend fun Player.evilBobDialogue() {
        when {
            get("evil_bob_complete", false) ->
                statement("Evil Bob appears to be sleeping, best not to wake him up.")
            inventory.contains("raw_fish_like_thing") -> serveCorrect()
            inventory.contains("raw_fish_like_thing_incorrect") -> serveWrong()
            inventory.contains("fish_like_thing") || inventory.contains("fish_like_thing_incorrect") ->
                npc<Angry>("evil_bob", "What, are you giving me cooked fish? What am I going to do with that? Uncook it first!")
            !get("evil_bob_seen_intro", false) -> {
                set("evil_bob_seen_intro", true)
                introDialogue()
            }
            else -> reasonsDialogue()
        }
    }

    private suspend fun Player.introDialogue() {
        player<Angry>("Where am I?")
        npc<Neutral>("evil_bob", "On my island.")
        player<Angry>("Who brought me here?")
        npc<Unamused>("evil_bob", "That would be telling.")
        player<Angry>("Take me to your leader!")
        npc<Angry>("evil_bob", "I am your leader, you are but a slave.")
        player<Angry>("I am not a slave, I am a free man!")
        npc<Angry>("evil_bob", "Ah-ha-ha-ha-ha-ha!")
        npc<Angry>("evil_bob", "Now catch me some fish, I'm hungry. Talk to my other servants, and hurry it up!")
    }

    private suspend fun Player.reasonsDialogue() {
        player<Angry>("Let me out of here!")
        npc<Angry>("evil_bob", "I will never let you go, $name!")
        choice {
            option<Neutral>("Why not?") {
                npc<Angry>("evil_bob", "Because I say so! And because I can never have enough servants!")
                npc<Angry>("evil_bob", "Now catch me some fish, I'm hungry.")
            }
            option<Neutral>("What's it all about?") {
                npc<Neutral>("evil_bob", "You are a skilled worker. A human like you is worth a great deal as a slave.")
                player<Angry>("A slave?? I will have nothing to do with you.")
                npc<Unamused>("evil_bob", "It's just a matter of time before you do everything I ask. Just ask my servants!")
            }
            option<Neutral>("How is it possible that you're talking?") {
                npc<Quiz>("evil_bob", "How is it possible that you're not meowing?")
                player<Neutral>("Meowing?? Why would I be meowing?")
                npc<Neutral>("evil_bob", "Most humans do; that's why I wear this amulet of Man speak.")
            }
            option<Neutral>("What did you do to Bob?") {
                npc<Neutral>("evil_bob", "Bob? I am Bob! An incarnation of Bob here on ScapeRune.")
                npc<Angry>("evil_bob", "You work just as well for me. Now get to work, human! Fish for me!")
            }
        }
    }

    private suspend fun Player.servantDialogue() {
        when {
            get("evil_bob_complete", false) -> {
                player<Neutral>("Evil Bob has fallen asleep, come quickly!")
                npc<Sad>("evil_bob_servant", "You go, $name. I don't belong there... this is the only place I can ever go.")
            }
            !get("evil_bob_servant_helped", false) -> {
                player<Angry>("I need help, I've been kidnapped by an evil cat!")
                npc<Scared>("evil_bob_servant", "Meow! Errr... I c-c-c-can't help you... He'll kill us all!")
                player<Angry>("He's just a little cat! There must be something I can do!")
                npc<Sad>("evil_bob_servant", "F-f-f-fish... give him the f-f-f-fish he likes and he might f-f-f-fall asleep.")
                set("evil_bob_servant_helped", true)
                showSpot()
            }
            get("evil_bob_new_spot", false) -> {
                npc<Sad>("evil_bob_servant", "Look... over t-t-there! That fishing spot c-c-contains the f-f-f-fish he likes.")
                showSpot()
            }
            else ->
                npc<Sad>("evil_bob_servant", "F-f-f-fish... give him the f-f-f-fish he likes and he might f-f-f-fall asleep.")
        }
    }

    /** Pans the camera to the assigned fishing spot, then hands control back and unlocks netting. */
    private suspend fun Player.showSpot() {
        val zone = ZONES[get("evil_bob_zone", 1).coerceIn(1, ZONES.size) - 1]
        val offset = instanceOffset()
        moveCamera(zone.cameraMove.add(offset), zone.cameraMoveHeight)
        turnCamera(zone.cameraTurn.add(offset), zone.cameraTurnHeight)
        delay(5)
        clearCamera()
        set("evil_bob_new_spot", false)
    }

    private fun Player.reward() {
        addOrDrop("random_event_gift")
    }

    private fun Player.clearState() {
        for (id in FISH_ITEMS) {
            while (inventory.remove(id)) {
                // strip every fish-like thing so none survive the trip home
            }
        }
        inventory.remove("small_fishing_net")
        clear("evil_bob_zone")
        clear("evil_bob_complete")
        clear("evil_bob_attentive")
        clear("evil_bob_new_spot")
        clear("evil_bob_seen_intro")
        clear("evil_bob_servant_helped")
    }

    private data class Zone(
        val minX: Int,
        val minY: Int,
        val maxX: Int,
        val maxY: Int,
        val spot: Tile,
        val cameraMove: Tile,
        val cameraMoveHeight: Int,
        val cameraTurn: Tile,
        val cameraTurnHeight: Int,
    ) {
        fun contains(tile: Tile) = tile.x in minX..maxX && tile.y in minY..maxY
    }

    companion object {
        private const val ISLAND_REGION = 13642
        private val ISLAND = Tile(3419, 4776)
        private val BOB_TILE = Tile(3420, 4777)
        private val SERVANT_TILE = Tile(3423, 4777)
        private val POT = Tile(3423, 4780)
        private val PORTAL = Tile(3416, 4777)

        private val FISH_ITEMS = listOf(
            "fish_like_thing",
            "fish_like_thing_incorrect",
            "raw_fish_like_thing",
            "raw_fish_like_thing_incorrect",
        )

        // (region-13642 base 3392,4736 + local coords). Index order = zone id 1..4.
        private val ZONES = listOf(
            Zone(3421, 4789, 3427, 4792, Tile(3424, 4791), Tile(3422, 4773), 400, Tile(3422, 4786), 400), // north
            Zone(3437, 4774, 3440, 4780, Tile(3438, 4777), Tile(3417, 4777), 440, Tile(3434, 4777), 440), // east
            Zone(3419, 4763, 3426, 4765, Tile(3422, 4764), Tile(3423, 4782), 365, Tile(3421, 4766), 365), // south
            Zone(3405, 4773, 3408, 4779, Tile(3406, 4776), Tile(3426, 4777), 325, Tile(3408, 4776), 300), // west
        )
    }
}
