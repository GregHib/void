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
import content.quest.instanceOffset
import content.quest.setInstanceLogout
import content.quest.smallInstance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Direction
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

        itemOption("Eat", "fish_like_thing*") {
            message("It looks vile and smells even worse. You're not eating that!")
        }

        itemOnObjectOperate("fish_like_thing", "evil_bob_uncooking_pot") { (range) ->
            walkOverDelay(range.tile)
            uncook("fish_like_thing", "raw_fish_like_thing")
        }
        itemOnObjectOperate("fish_like_thing_incorrect", "evil_bob_uncooking_pot") { (range) ->
            walkOverDelay(range.tile)
            uncook("fish_like_thing_incorrect", "raw_fish_like_thing_incorrect")
        }

        objectOperate("Enter", "evil_bob_exit_portal", arrive = false) { (portal) ->
            if (!get("evil_bob_complete", false)) {
                npc<Angry>("evil_bob", "You're going nowhere, human!")
                return@objectOperate
            }
            walkOverDelay(portal.tile)
            delay(1)
            face(Direction.EAST)
            delay(2)
            anim("emote_raspberry")
            say("Be seeing you!")
            delay(2)
            clearState()
            anim("teleport_modern")
            sound("teleport")
            gfx("teleport_modern")
            delay(3)
            RandomEvents.complete(this, "random_event_gift")
            anim("teleport_land_modern")
            gfx("teleport_land_modern")
            sound("teleport_land")
            message("Welcome back.")
        }
    }

    private suspend fun Player.startEvent() {
        smallInstance(Region(ISLAND_REGION), levels = 1)
        setInstanceLogout(Tile(this["random_event_origin", tile.id]))
        // The region copy brings the island's static objects (fishing spots, uncooking
        // pots, exit portal, deposit box) with it; only the nets need spawning.
        val offset = instanceOffset()
        for (net in NET_TILES) {
            FloorItems.add(net.add(offset), "small_fishing_net_evil_bobs_island", revealTicks = FloorItems.NEVER, disappearTicks = FloorItems.NEVER, owner = this)
        }

        evilBobCatIntro()
        kidnap(ISLAND.add(offset))

        val bob = NPCs.add("evil_bob", BOB_TILE.add(offset), ticks = -1, owner = this)
        bob.watch(this)
        set("evil_bob_npc", bob.index)
        val servant = NPCs.add("evil_bob_servant", SERVANT_TILE.add(offset), ticks = -1, owner = this)
        servant.watch(this)

        if (get("evil_bob_zone", 0) == 0) {
            assignZone()
        } else {
            // Relog resume: the camera hint died with the old instance, so have the
            // servant point out the fishing spot again when spoken to.
            set("evil_bob_new_spot", true)
        }
        if (!hasNet()) {
            inventory.add("small_fishing_net_evil_bobs_island")
        }
        face(bob.tile)
        message("Welcome to ScapeRune.")
        talkWith(bob)
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
            !hasNet() ->
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

    private fun Player.hasNet() = inventory.contains("small_fishing_net") || inventory.contains("small_fishing_net_evil_bobs_island")

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
        npc<Unamused>("Mmm, mmm... that's delicious.")
        if (get("evil_bob_attentive", false)) {
            set("evil_bob_attentive", false)
            assignZone()
            set("evil_bob_new_spot", true)
            npc<Angry>("Now get me another, you no-good human.")
            statement("Evil Bob seems slightly less attentive of you.")
        } else {
            npc<Neutral>("Now, let me take... a little... catnap.")
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
        npc<Angry>("What was this? That was absolutely disgusting!")
        npc<Angry>("Don't you know what kind of fish I like? Talk to my other servants for some advice.")
        statement("Evil Bob seems more attentive of you.")
    }

    private suspend fun Player.evilBobDialogue() {
        when {
            get("evil_bob_complete", false) ->
                statement("Evil Bob appears to be sleeping, best not to wake him up.")
            inventory.contains("raw_fish_like_thing") -> serveCorrect()
            inventory.contains("raw_fish_like_thing_incorrect") -> serveWrong()
            inventory.contains("fish_like_thing") || inventory.contains("fish_like_thing_incorrect") ->
                npc<Angry>("What, are you giving me cooked fish? What am I going to do with that? Uncook it first!")
            !get("evil_bob_seen_intro", false) -> {
                set("evil_bob_seen_intro", true)
                introDialogue()
            }
            else -> reasonsDialogue()
        }
    }

    private suspend fun Player.introDialogue() {
        player<Angry>("Where am I?")
        npc<Neutral>("On my island.")
        player<Angry>("Who brought me here?")
        npc<Unamused>("That would be telling.")
        player<Angry>("Take me to your leader!")
        npc<Angry>("I am your leader, you are but a slave.")
        player<Angry>("I am not a slave, I am a free man!")
        npc<Angry>("Ah-ha-ha-ha-ha-ha!")
        npc<Angry>("Now catch me some fish, I'm hungry. Talk to my other servants, and hurry it up!")
    }

    private suspend fun Player.reasonsDialogue() {
        player<Angry>("Let me out of here!")
        npc<Angry>("I will never let you go, $name!")
        choice {
            option<Neutral>("Why not?") {
                npc<Angry>("Because I say so! And because I can never have enough servants!")
                npc<Angry>("Now catch me some fish, I'm hungry.")
            }
            option<Neutral>("What's it all about?") {
                npc<Neutral>("You are a skilled worker. A human like you is worth a great deal as a slave.")
                player<Angry>("A slave?? I will have nothing to do with you.")
                npc<Unamused>("It's just a matter of time before you do everything I ask. Just ask my servants!")
            }
            option<Neutral>("How is it possible that you're talking?") {
                npc<Quiz>("How is it possible that you're not meowing?")
                player<Neutral>("Meowing?? Why would I be meowing?")
                npc<Neutral>("Most humans do; that's why I wear this amulet of Man speak.")
            }
            option<Neutral>("What did you do to Bob?") {
                npc<Neutral>("Bob? I am Bob! An incarnation of Bob here on ScapeRune.")
                npc<Angry>("You work just as well for me. Now get to work, human! Fish for me!")
            }
        }
    }

    private suspend fun Player.servantDialogue() {
        when {
            get("evil_bob_complete", false) -> {
                player<Neutral>("Evil Bob has fallen asleep, come quickly!")
                npc<Sad>("You go, $name. I don't belong there... this is the only place I can ever go.")
            }
            !get("evil_bob_servant_helped", false) -> {
                player<Angry>("I need help, I've been kidnapped by an evil cat!")
                npc<Scared>("Meow! Errr... I c-c-c-can't help you... He'll kill us all!")
                player<Angry>("He's just a little cat! There must be something I can do!")
                // No continue prompt: the pan starts on the same tick this line shows,
                // locking the player so they can't wander off mid-cutscene.
                npc<Sad>("F-f-f-fish... give him the f-f-f-fish he likes and he might f-f-f-fall asleep.", clickToContinue = false)
                set("evil_bob_servant_helped", true)
                showSpot()
            }
            get("evil_bob_new_spot", false) -> {
                npc<Sad>("Look... over t-t-there! That fishing spot c-c-contains the f-f-f-fish he likes.", clickToContinue = false)
                showSpot()
            }
            else ->
                // Already shown the spot; just a reminder, no repeat cutscene.
                npc<Sad>("F-f-f-fish... give him the f-f-f-fish he likes and he might f-f-f-fall asleep.")
        }
    }

    /** Pans the camera to the assigned fishing spot, then hands control back and unlocks netting. */
    private suspend fun Player.showSpot() {
        val zone = ZONES[get("evil_bob_zone", 1).coerceIn(1, ZONES.size) - 1]
        val offset = instanceOffset()
        moveCamera(zone.cameraMove.add(offset), zone.cameraMoveHeight, CAMERA_SPEED, CAMERA_ACCELERATION)
        turnCamera(zone.cameraTurn.add(offset), zone.cameraTurnHeight, CAMERA_SPEED, CAMERA_ACCELERATION)
        delay(10)
        clearCamera()
        closeDialogue()
        set("evil_bob_new_spot", false)
    }

    private fun Player.clearState() {
        for (id in FISH_ITEMS) {
            while (inventory.remove(id)) {
                // strip every fish-like thing so none survive the trip home
            }
        }
        // Only confiscate the island's net; one the player brought themselves stays theirs.
        inventory.remove("small_fishing_net_evil_bobs_island")
        clear("evil_bob_npc")
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

        // Nets scattered around the beaches for the player to pick up.
        private val NET_TILES = listOf(
            Tile(3412, 4785),
            Tile(3417, 4787),
            Tile(3430, 4784),
            Tile(3434, 4782),
            Tile(3429, 4769),
            Tile(3426, 4766),
            Tile(3413, 4768),
        )

        private val FISH_ITEMS = listOf(
            "fish_like_thing",
            "fish_like_thing_incorrect",
            "raw_fish_like_thing",
            "raw_fish_like_thing_incorrect",
        )

        // Slow cinematic pan towards the fishing spot; 232 = instant snap.
        private const val CAMERA_SPEED = 1
        private const val CAMERA_ACCELERATION = 10

        // (region-13642 base 3392,4736 + local coords). Index order = zone id 1..4.
        // Each zone covers that side's static fishing spots from the region copy.
        private val ZONES = listOf(
            // NORTH_CAMERA: sits just north of the island centre, panning north towards the spots.
            Zone(3421, 4789, 3427, 4792, Tile(3422, 4779), 400, Tile(3422, 4786), 400),
            // EAST_CAMERA: sits east of the island centre, panning east towards the spots.
            Zone(3437, 4774, 3440, 4780, Tile(3426, 4777), 440, Tile(3434, 4777), 440),
            // SOUTH_CAMERA: sits south of the island centre, panning south towards the spots.
            Zone(3419, 4763, 3426, 4765, Tile(3421, 4774), 365, Tile(3421, 4766), 365),
            // WEST_CAMERA: sits west of the island centre, panning west towards the spots.
            Zone(3405, 4773, 3408, 4779, Tile(3416, 4776), 325, Tile(3408, 4776), 300),
        )
    }
}
