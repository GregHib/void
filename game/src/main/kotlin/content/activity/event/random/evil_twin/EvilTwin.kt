package content.activity.event.random.evil_twin

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.quest.closeTabs
import content.quest.instance
import content.quest.instanceOffset
import content.quest.openTabs
import content.quest.setInstanceLogout
import content.quest.smallInstance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Cuboid
import world.gregs.voidps.type.random

/**
 * Evil Twin random event: Molly appears beside the player, pleads for help and whisks them away to
 * her house, where her evil twin sister hides in a pen of lookalike civilians next door. The
 * player drives a giant mechanical claw from the control panel (interface 240) and has two tries
 * to grab the sister - she's the one dressed exactly like Molly. Success earns a random event
 * gift; running out of tries gets the player thrown out with nothing.
 * https://runescape.wiki/w/Random_events?oldid=3667851#Evil_Twin
 */
class EvilTwin : Script {

    init {
        RandomEvents.register("evil_twin") { startEvent() }

        npcOperate("Talk-to", "molly_*") { (molly) ->
            if (molly.owner != this) {
                message("She isn't interested in talking to you.")
                return@npcOperate
            }
            if (instance() == null) {
                return@npcOperate
            }
            if (get("evil_twin_caught", false)) {
                success()
            } else {
                reminderDialogue()
            }
        }

        objectOperate("Use", "control_panel") {
            if (get<String>("random_event") != "evil_twin") {
                return@objectOperate
            }
            if (get("evil_twin_caught", false)) {
                message("You've already caught the evil twin.")
                return@objectOperate
            }
            open("evil_twin_crane")
        }

        interfaceOpened("evil_twin_crane") {
            tab(Tab.Inventory)
            sendTries()
            clawCamera()
        }

        interfaceClosed("evil_twin_crane") {
            clearCamera()
            if (get<String>("random_event") == "evil_twin" && instance() != null) {
                removeClaw()
                placeClaw(CLAW_HOME)
            }
        }

        // The camera looks south over the pen, so the on-screen directions flip.
        interfaceOption("Up", "evil_twin_crane:up") {
            moveClaw(Direction.SOUTH)
        }

        interfaceOption("Down", "evil_twin_crane:down") {
            moveClaw(Direction.NORTH)
        }

        interfaceOption("Left", "evil_twin_crane:left") {
            moveClaw(Direction.EAST)
        }

        interfaceOption("Right", "evil_twin_crane:right") {
            moveClaw(Direction.WEST)
        }

        interfaceOption("Grab", "evil_twin_crane:grab") {
            grab()
        }

        interfaceOption("Close", "evil_twin_crane:close") {
            close("evil_twin_crane")
        }
    }

    private suspend fun Player.startEvent() {
        // A logout mid-house restarts here on login; rebuild the house and carry on where
        // they left off rather than starting the event over.
        if (get("evil_twin_tries", 0) > 0 || get("evil_twin_caught", false)) {
            setupHouse()
            return
        }
        val hash = random.nextInt(LOOKS)
        set("evil_twin_hash", hash)
        // There's no choice about taking part: Molly appears, pleads, and whisks the player away.
        val molly = NPCs.addRandom("molly_$hash", tile.toCuboid(1), ticks = 25, owner = this)
            ?: NPCs.add("molly_$hash", tile, ticks = 25, owner = this)
        molly.watch(this)
        molly.say(nagLines().random(random))
        delay(3)
        set("evil_twin_tries", TRIES)
        setupHouse()
        houseDialogue()
    }

    /**
     * Copy Molly's house into a private instance and take the player and Molly there. The house
     * furniture (control panel, cage, crates) comes with the map; the claw and its floor marker
     * are spawned at [CLAW_HOME], and the five suspects - one per look sharing the twin's colour,
     * only one matching her exactly - are set wandering the pen. Once the twin has been caught
     * only she remains, waiting in the jail.
     */
    private suspend fun Player.setupHouse() {
        smallInstance(Region(HOUSE_REGION), levels = 1)
        setInstanceLogout(Tile(this["random_event_origin", tile.id]))
        val offset = instanceOffset()
        set("evil_twin_claw_x", CLAW_HOME.x)
        set("evil_twin_claw_y", CLAW_HOME.y)
        if (GameObjects.at(CLAW_HOME.add(offset)).none { it.id == "evil_twin_claw" }) {
            GameObjects.add("evil_twin_claw", CLAW_HOME.add(offset), collision = false)
        }
        if (GameObjects.at(CLAW_HOME.add(offset)).none { it.id == "evil_twin_claw_marker" }) {
            GameObjects.add("evil_twin_claw_marker", CLAW_HOME.add(offset), shape = ObjectShape.GROUND_DECOR, collision = false)
        }
        kidnap(ARRIVAL.add(offset))
        closeTabs()
        minimap(Minimap.HideMap)
        val hash = get("evil_twin_hash", 0)
        val host = NPCs.add("molly_$hash", MOLLY_TILE.add(offset), ticks = -1, owner = this)
        host.watch(this)
        if (get("evil_twin_caught", false)) {
            NPCs.add("twin_suspect_$hash", JAIL.add(offset), ticks = -1, owner = this)
        } else {
            spawnSuspects(hash, PEN.offset(offset))
        }
    }

    private fun Player.spawnSuspects(hash: Int, pen: Cuboid) {
        val colour = hash % COLOURS
        for (model in 0 until MODELS) {
            val id = "twin_suspect_${colour + model * COLOURS}"
            val npc = NPCs.addRandom(id, pen, owner = this) ?: continue
            npc.mode = Wander(npc, npc.tile)
        }
    }

    private fun Player.clawTile(): Tile = Tile(get("evil_twin_claw_x", CLAW_HOME.x), get("evil_twin_claw_y", CLAW_HOME.y))

    private fun Player.clawObject(): GameObject? = GameObjects.at(clawTile().add(instanceOffset()))
        .firstOrNull { it.id == "evil_twin_claw" }

    private fun Player.markerObject(): GameObject? = GameObjects.at(clawTile().add(instanceOffset()))
        .firstOrNull { it.id == "evil_twin_claw_marker" }

    /** Bird's-eye view over the pen, looking at wherever the claw is. */
    private fun Player.clawCamera() {
        val offset = instanceOffset()
        moveCamera(CAMERA.add(offset), CAMERA_HEIGHT)
        turnCamera(clawTile().add(offset), CLAW_HEIGHT)
    }

    private fun Player.moveClaw(direction: Direction) {
        val next = clawTile().add(direction.delta)
        if (!TRAVEL.contains(next.x, next.y, 0)) {
            return
        }
        removeClaw()
        placeClaw(next)
        sound("evil_twin_claw_move")
        clawCamera()
    }

    private fun Player.removeClaw() {
        clawObject()?.let { GameObjects.remove(it, collision = false) }
        markerObject()?.let { GameObjects.remove(it, collision = false) }
    }

    private fun Player.placeClaw(next: Tile) {
        val tile = next.add(instanceOffset())
        if (GameObjects.at(tile).none { it.id == "evil_twin_claw" }) {
            GameObjects.add("evil_twin_claw", tile, collision = false)
        }
        if (GameObjects.at(tile).none { it.id == "evil_twin_claw_marker" }) {
            GameObjects.add("evil_twin_claw_marker", tile, shape = ObjectShape.GROUND_DECOR, collision = false)
        }
        set("evil_twin_claw_x", next.x)
        set("evil_twin_claw_y", next.y)
    }

    /**
     * Drop the claw on whoever is standing on the marker right now - wander a tile off it and the
     * claw comes up empty. A grabbed suspect gets hauled over the cage wall to the jail; the rest
     * puff away if she was the twin, or the tries tick down if she wasn't. Either way the claw
     * swings back to its home corner afterwards, ready for another go.
     */
    private suspend fun Player.grab() {
        if (get("evil_twin_caught", false)) {
            return
        }
        val offset = instanceOffset()
        val target = clawTile().add(offset)
        sound("evil_twin_claw_move")
        clawObject()?.anim("evil_twin_claw_grab")
        val suspect = NPCs.at(target).firstOrNull { it.id.startsWith("twin_suspect_") }
        if (suspect == null) {
            delay(2)
            message("The claw comes up empty.")
            useTry()
            return
        }
        val look = suspect.id.removePrefix("twin_suspect_").toInt()
        val twin = look == get("evil_twin_hash", 0)
        suspect.mode = EmptyMode
        suspect.anim("evil_twin_lifted")
        suspect.gfx("evil_twin_lifted")
        turnCamera(JAIL.add(offset), CLAW_HEIGHT)
        delay(5)
        anim("evil_twin_operate")
        removeClaw() // The claw leaves with her, and swings back once she's been dropped off
        suspect.transform("twin_suspect_carried_$look", collision = false)
        suspect.walkOverDelay(JAIL.add(offset))
        sound("evil_twin_claw_lower")
        suspect.anim("evil_twin_claw_lower")
        delay(3)
        suspect.clearTransform()
        suspect.face(tile)
        sound("evil_twin_claw_drop")
        if (twin) {
            catchTwin(suspect)
        } else {
            message("You caught an innocent civilian!")
            suspect.say("You're putting me in prison?!")
            delay(2)
            placeClaw(CLAW_HOME)
            clawCamera()
            useTry()
        }
    }

    private suspend fun Player.catchTwin(twin: NPC) {
        message("You caught the evil twin!")
        set("evil_twin_caught", true)
        jingle("twin_is_caught")
        twin.face(Direction.NORTH)
        delay(2)
        twin.anim("evil_twin_jailed")
        for (npc in NPCs.at(twin.tile.regionLevel)) {
            if (npc.id.startsWith("twin_suspect_") && npc != twin) {
                npc.gfx("imp_puff")
                NPCs.remove(npc)
            }
        }
        delay(2)
        placeClaw(CLAW_HOME)
        close("evil_twin_crane")
        statement("You've caught Molly's evil twin! Talk to Molly to collect your reward.")
    }

    private suspend fun Player.useTry() {
        val remaining = dec("evil_twin_tries")
        sendTries()
        if (remaining > 0) {
            return
        }
        close("evil_twin_crane")
        npc<Angry>(mollyId(), "Such incompetence! I should never have asked a baboon like you to do a complex task like this!")
        npc<Angry>(mollyId(), "Get out of my sight!")
        clearState()
        openTabs()
        clearMinimap()
        RandomEvents.fail(this)
    }

    private fun Player.sendTries() {
        interfaces.sendText("evil_twin_crane", "tries", "Tries: ${get("evil_twin_tries", 0)}")
    }

    private suspend fun Player.success() {
        player<Happy>("Well, I've managed to get her into the cage.")
        npc<Happy>(mollyId(), "Fantastic! For so many years I've had to put up with her and now she's locked up for good.")
        npc<Happy>(mollyId(), "Thank you for all your help. Take this as a reward.")
        addOrDrop("random_event_gift")
        clearState()
        openTabs()
        clearMinimap()
        RandomEvents.complete(this)
    }

    private suspend fun Player.houseDialogue() {
        npc<Sad>(mollyId(), "Thank you for coming, $name. I'm sorry to drag you away like this, but I really need your help.")
        npc<Sad>(mollyId(), "My evil twin sister has been committing crimes and everyone blames me! We look exactly alike - even our clothes are the same.")
        npc<Neutral>(mollyId(), "I've managed to trap her next door, but she dragged several other women in with her.")
        npc<Neutral>(mollyId(), "Through that door is a control panel that operates a giant mechanical claw. Use it to catch my sister!")
        npc<Sad>(mollyId(), "Be careful though; the claw's magic is running low, so you only have two attempts.")
        choice {
            option<Neutral>("Tell me about the controls.") {
                npc<Neutral>(mollyId(), "The arrow buttons each move the claw one square; the glowing mark on the floor shows where it's aiming.")
                npc<Neutral>(mollyId(), "When my sister stands on the mark, hit the red button to lower the claw and grab her.")
                npc<Sad>(mollyId(), "Make sure it's really her and not an innocent bystander!")
            }
            option<Happy>("I'll get right to it.")
        }
    }

    private suspend fun Player.reminderDialogue() {
        npc<Neutral>(mollyId(), "My sister is still on the loose! Use the control panel next door to catch her.")
        npc<Sad>(mollyId(), "Remember, she's the one that looks exactly like me.")
    }

    private fun Player.mollyId() = "molly_${get("evil_twin_hash", 0)}"

    private fun Player.nagLines() = listOf(
        "Please, could you help me, $name?",
        "I need your help urgently, $name!",
    )

    private fun Player.clearState() {
        clear("evil_twin_hash")
        clear("evil_twin_tries")
        clear("evil_twin_claw_x")
        clear("evil_twin_claw_y")
        clear("evil_twin_caught")
    }

    companion object {
        private const val HOUSE_REGION = 7504 // Region(29, 80), base 1856,5120
        private const val LOOKS = 20 // colour (0-3) + model (0-4) * 4
        private const val COLOURS = 4
        private const val MODELS = 5
        private const val TRIES = 2
        private const val CAMERA_HEIGHT = 800
        private const val CLAW_HEIGHT = 250

        private val ARRIVAL = Tile(1860, 5136)
        private val MOLLY_TILE = Tile(1860, 5135)
        private val CLAW_HOME = Tile(1870, 5132)
        private val JAIL = Tile(1866, 5124)
        private val CAMERA = Tile(1870, 5140)

        // The pen the suspects wander, and the box the claw can be driven anywhere within.
        private val PEN = Cuboid(1867, 5126, 1874, 5131, 0, 0)
        private val TRAVEL = Cuboid(1865, 5124, 1875, 5133, 0, 0)
    }
}
