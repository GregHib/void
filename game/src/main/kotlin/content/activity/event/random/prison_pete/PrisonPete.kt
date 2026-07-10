package content.activity.event.random.prison_pete

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Prison Pete random event: Evil Bob's cat throws the player into the ScapeRune prison alongside
 * Pete. Pulling the big lever shows one of the four balloon animal shapes; popping a balloon of that
 * shape yields a key that opens one of the door's locks, while popping the wrong shape yields a key
 * that doesn't fit. Hand Pete three correct keys to unlock the door, escape and receive a random event gift.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Prison_Pete
 */
class PrisonPete : Script {

    init {
        RandomEvents.register("prison_pete") { startEvent() }

        objectOperate("Pull", "prison_pete_lever") { (lever) ->
            if (get<String>("random_event") != "prison_pete") {
                return@objectOperate
            }
            pullLever(lever)
        }

        // The prison's decoy doors ("This doesn't look to be the way out.") never open.
        objectOperate("Open", "locked_door_10_closed") {
            message("The doors won't open.")
        }

        // The exit gates only open once Pete has unlocked all three locks.
        objectOperate("Open", "prison_pete_gate_*") { (gate) ->
            if (get<String>("random_event") == "prison_pete" && get("prison_pete_keys", 0) >= REQUIRED) {
                escape(gate)
            } else {
                message("The doors won't open.")
            }
        }

        npcOperate("Pop", "balloon_animal_*") { (balloon) ->
            if (get<String>("random_event") != "prison_pete") {
                return@npcOperate
            }
            popBalloon(balloon)
        }

        npcOperate("Talk-to", "prison_pete") { (pete) ->
            if (get<String>("random_event") != "prison_pete") {
                return@npcOperate
            }
            peteTalk(pete)
        }

        itemOnNPCOperate("prison_key_prison_pete", "prison_pete") { (pete) ->
            if (get<String>("random_event") != "prison_pete") {
                return@itemOnNPCOperate
            }
            returnKey(pete)
        }
    }

    private suspend fun Player.startEvent() {
        set("prison_pete_keys", 0)
        clear("prison_pete_target")
        clear("prison_pete_wrong")
        catIntro()
        kidnap(PRISON)
        message("Welcome to ScapeRune.")
    }

    /** The prison is Evil Bob's, so his cat does the kidnapping instead of the Mysterious Old Man. */
    private suspend fun Player.catIntro() {
        val cat = NPCs.addRandom("evil_bob_cat", tile.toCuboid(1), ticks = 25, owner = this)
            ?: NPCs.add("evil_bob_cat", tile, ticks = 25, owner = this)
        cat.watch(this)
        cat.say("Meow.")
        delay(2)
    }

    private suspend fun Player.pullLever(lever: GameObject) {
        if (get("prison_pete_keys", 0) >= REQUIRED) {
            message("You've found all the keys.")
            return
        }
        face(lever)
        delay(2)
        anim("prison_pete_pull_lever")
        lever.anim("prison_pete_lever_pull")
        delay(2)
        val balloon = BALLOONS.random(random)
        set("prison_pete_target", balloon.npc)
        open("prison_pete_balloons")
        interfaces.sendModel("prison_pete_balloons", "model", balloon.model)
    }

    private suspend fun Player.popBalloon(balloon: NPC) {
        val target: String? = get("prison_pete_target")
        if (target == null) {
            message("Pull the lever to see which balloon to pop.")
            return
        }
        if (get("prison_pete_keys", 0) >= REQUIRED) {
            message("You've found all the keys.")
            return
        }
        if (inventory.isFull()) {
            message("You don't have enough inventory space.")
            return
        }
        walkOverDelay(balloon.tile)
        clear("prison_pete_target")
        anim("prison_pete_stomp")
        sound("prison_pete_balloon_pop")
        balloon.gfx("prison_pete_balloon_pop")
        if (balloon.id == target) {
            inc("prison_pete_keys")
        } else {
            set("prison_pete_wrong", true)
        }
        delay(1)
        val spawn = balloon.tile
        NPCs.remove(balloon)
        World.queue("prison_pete_balloon_respawn_${balloon.index}", BALLOON_RESPAWN_TICKS) {
            NPCs.add(balloon.id, spawn)
        }
        anim("prison_pete_pick_up")
        delay(1)
        inventory.add("prison_key_prison_pete")
        npc<Neutral>("prison_pete", "Great, now you've got a key! Bring it to me so I can try it on the door.")
    }

    private suspend fun Player.peteTalk(pete: NPC) {
        if (inventory.contains("prison_key_prison_pete")) {
            returnKey(pete)
            return
        }
        choice("What would you like to say?") {
            option<Quiz>("What is this place?") {
                npc<Neutral>("prison_pete", "Don't you remember? This is ScapeRune's prison. Evil Bob caught you and brought you here.")
                player<Angry>("What gives him the right to lock me up? I demand to see a lawyer! I know my rights!")
                npc<Sad>("prison_pete", "Evil Bob doesn't care about people's rights. He's cruel and utterly merciless. He's a cat.")
                player<Quiz>("How do I get out of here? I can't be held captive by a cat!")
                escapeInfo()
            }
            option<Quiz>("How do I get out of here?") {
                escapeInfo()
            }
        }
    }

    private suspend fun Player.escapeInfo() {
        npc<Neutral>("prison_pete", "Some of these balloon animals have keys in them, and if you pull the big lever it tells you which shape animal contains the correct key, but I can never find it.")
        npc<Neutral>("prison_pete", "You need to pull the lever to find out which shape animal contains the key, then pop that sort of animal to get the key.")
        npc<Neutral>("prison_pete", "Bring me any keys you get and I'll try them on the doors.")
        player<Quiz>("What happens if I get it wrong?")
        npc<Neutral>("prison_pete", "You haven't got a life sentence, so they'll let you out eventually. You should be able to escape much faster if you go pull that lever and pop the right balloon animals.")
    }

    private suspend fun Player.returnKey(pete: NPC) {
        if (!inventory.remove("prison_key_prison_pete")) {
            return
        }
        face(pete)
        pete.face(this)
        npc<Neutral>("prison_pete", "Ooh, thanks! I'll see if it's the right one...")
        when {
            get("prison_pete_keys", 0) >= REQUIRED -> {
                npc<Happy>("prison_pete", "You did it, you got all the keys right!")
                npc<Happy>("prison_pete", "Thank you! You're my friend FOREVER!")
                player<Neutral>("Let's get out of here before that cat notices.")
            }
            get("prison_pete_wrong", false) -> {
                clear("prison_pete_wrong")
                jingle("prison_pete_failure")
                npc<Sad>("prison_pete", "Aww, that was the wrong key! Try the lever again to see which balloon you need.")
            }
            else -> {
                pete.face(Direction.SOUTH_EAST)
                pete.anim("pick_pocket")
                delay(2)
                pete.face(this)
                npc<Happy>("prison_pete", "Hooray, you got the right one! Now pull the lever again and let's get the next lock unlocked.")
            }
        }
    }

    private suspend fun Player.escape(gate: GameObject) {
        openGates()
        walkToDelay(gate.tile.addY(-1), forceWalk = true)
        message("You quickly escape the prison with Pete.")
        npc<Happy>("prison_pete", "Thanks a lot for your help! Here, have a present:")
        addOrDrop("random_event_gift")
        player<Happy>("Thanks! See you around!")
        while (inventory.remove("prison_key_prison_pete")) {
            // strip any dud keys so none survive the trip home
        }
        clear("prison_pete_keys")
        clear("prison_pete_target")
        clear("prison_pete_wrong")
        anim("teleport_modern")
        sound("teleport")
        gfx("teleport_modern")
        delay(3)
        RandomEvents.complete(this)
        anim("teleport_land_modern")
        gfx("teleport_land_modern")
        sound("teleport_land")
        message("Welcome back.")
    }

    /** Swing both gate leaves outward (south); they swing shut again behind the player. */
    private fun openGates() {
        swingOpen("prison_pete_gate_west", GATE_WEST, rotation = 0)
        swingOpen("prison_pete_gate_east", GATE_EAST, rotation = 2)
    }

    private fun swingOpen(id: String, tile: Tile, rotation: Int) {
        val gate = GameObjects.getShape(tile, 0) ?: return
        if (gate.id != id) {
            return
        }
        GameObjects.replace(gate, id, rotation = rotation, ticks = GATE_OPEN_TICKS)
    }

    private data class Balloon(val npc: String, val model: Int)

    companion object {
        private const val REQUIRED = 3
        private const val GATE_OPEN_TICKS = 15
        private const val BALLOON_RESPAWN_TICKS = 25
        private val PRISON = Tile(2086, 4462)
        private val GATE_WEST = Tile(2085, 4459)
        private val GATE_EAST = Tile(2086, 4459)

        // Interface models paired with the balloon shape they depict.
        private val BALLOONS = listOf(
            Balloon("balloon_animal_dog", 10749),
            Balloon("balloon_animal_cat", 10750),
            Balloon("balloon_animal_sheep", 10751),
            Balloon("balloon_animal_goat", 10752),
        )
    }
}
