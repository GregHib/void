package content.activity.event.random.beekeeper

import content.activity.event.random.RandomEvents
import content.activity.event.random.kidnap
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.smallInstance
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendInterfaceSettings
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.random

/**
 * Beekeeper random event: the Bee keeper drags the player to his field of beehives and asks for help
 * building a new hive. The four hive components (interface 420, driven by varp 805's varbits) start
 * jumbled beside the frame; drag them into the frame in the right order and click Build. Six wrong
 * builds and the bees lose patience, chasing the player off empty-handed; success earns a random
 * event gift before the trip home.
 * https://runescape.wiki/w/Beekeeper
 */
class Beekeeper : Script {

    init {
        RandomEvents.register("beekeeper") { startEvent() }

        // The Bee keeper's cache option is "Talk-To" (capital O), like the pinball trolls.
        npcOperate("Talk-To", "bee_keeper") { (keeper) ->
            if (get<String>("random_event") != "beekeeper" || keeper.owner != this) {
                npc<Neutral>("bee_keeper", "Sorry, you're not the person I need.")
                return@npcOperate
            }
            beekeeperTalk()
        }

        interfaceSwap("beehive_build:*", "beehive_build:*") { fromId, toId, _, _ ->
            if (get<String>("random_event") != "beekeeper") {
                return@interfaceSwap
            }
            swapParts(fromId.substringAfter(":"), toId.substringAfter(":"))
        }

        interfaceOption("Build", "beehive_build:build") {
            if (get<String>("random_event") != "beekeeper") {
                return@interfaceOption
            }
            build()
        }
    }

    private suspend fun Player.startEvent() {
        shuffleParts()
        set("beekeeper_tries", TRIES)
        clear("beekeeper_intro")
        herald()
        val offset = copyField()
        kidnap(FIELD.add(offset))
        NPCs.add("bee_keeper", KEEPER.add(offset), Direction.SOUTH, ticks = -1, owner = this)
    }

    /**
     * Allocate a private instance and copy only the apiary's zones (the box between [AREA_SW] and
     * [AREA_NE]) into it, leaving the rest of the instance empty. Returns the tile offset from the
     * real field to its copy.
     */
    private fun Player.copyField(): Delta {
        val instance = smallInstance()
        val offset = instance.offset(REGION)
        val zones = get<DynamicZones>()
        for (zoneX in (AREA_SW.x shr 3)..(AREA_NE.x shr 3)) {
            for (zoneY in (AREA_SW.y shr 3)..(AREA_NE.y shr 3)) {
                val from = Zone(zoneX, zoneY, 0)
                val base = from.tile.add(offset)
                zones.copy(from, Zone(base.x shr 3, base.y shr 3, base.level))
            }
        }
        set("instance_offset", offset.id)
        return offset
    }

    private suspend fun Player.herald() {
        val keeper = NPCs.addRandom("bee_keeper", tile.toCuboid(1), ticks = 25, owner = this)
            ?: NPCs.add("bee_keeper", tile, ticks = 25, owner = this)
        keeper.watch(this)
        keeper.say("I need some help, $name!")
        delay(2)
    }

    private suspend fun Player.beekeeperTalk() {
        if (!get("beekeeper_intro", false)) {
            set("beekeeper_intro", true)
            npc<Neutral>("bee_keeper", "Ah, $name. I'm sorry to drag you away like this, but I need some help building a new hive for my bees.")
            player<Quiz>("What do you want me to do?")
            npc<Neutral>("bee_keeper", "All the components of the beehive are jumbled up. You've got to put them in the correct order for building a hive.")
            player<Neutral>("Oh, very well. Let's take a look...")
        }
        openHive()
    }

    /** Deal the four hive parts randomly across the source slots, leaving the frame empty. */
    private fun Player.shuffleParts() {
        val parts = (1..4).shuffled(random)
        for (slot in 1..4) {
            set("beekeeper_part_$slot", parts[slot - 1])
            set("beekeeper_slot_$slot", 0)
        }
    }

    private fun Player.openHive() {
        open("beehive_build")
        // Send the access masks the cache doesn't: the Build button's click option, and drag flags
        // (drag depth + drag-onto) for the part and frame slots so the client reports the drags.
        interfaceOptions.unlockAll("beehive_build", "build", 0..0)
        for (component in 12..19) {
            sendInterfaceSettings(InterfaceDefinition.pack(HIVE_ID, component), 0, 0, DRAG_SETTINGS)
        }
    }

    /** Dragging one slot onto another swaps their contents; every slot pairing is allowed. */
    private fun Player.swapParts(from: String, to: String) {
        val source = get("beekeeper_$from", 0)
        val target = get("beekeeper_$to", 0)
        set("beekeeper_$from", target)
        set("beekeeper_$to", source)
    }

    private suspend fun Player.build() {
        if ((1..4).any { get("beekeeper_slot_$it", 0) == 0 }) {
            statement("You need to move all 4 of the spinning components to the frame in the centre of the display.", clickToContinue = false)
            return
        }
        if ((1..4).all { get("beekeeper_slot_$it", 0) == it }) {
            succeed()
        } else {
            fail()
        }
    }

    private suspend fun Player.succeed() {
        close("beehive_build")
        npc<Happy>("bee_keeper", "That's perfect! I'll get some bees moved in immediately. Now, I'm sure I must have something to offer you for all your help...")
        addOrDrop("random_event_gift")
        message("You've been given a gift!")
        clearState()
        RandomEvents.complete(this)
    }

    private suspend fun Player.fail() {
        val tries = dec("beekeeper_tries")
        if (tries > 0) {
            npc<Sad>("bee_keeper", "No, that doesn't look right. You have to put the components in the right order, otherwise it'll be no good as a beehive. I'll let you have $tries more ${if (tries == 1) "try" else "tries"}.")
            return
        }
        close("beehive_build")
        npc<Sad>("bee_keeper", "Uh-oh, the bees are fed up with waiting for you to build them a home!")
        say("Aaaaargh - BEES!")
        delay(2)
        clearState()
        RandomEvents.complete(this)
    }

    private fun Player.clearState() {
        clear("beekeeper_intro")
        clear("beekeeper_tries")
        for (slot in 1..4) {
            clear("beekeeper_part_$slot")
            clear("beekeeper_slot_$slot")
        }
    }

    companion object {
        private const val HIVE_ID = 420
        private const val TRIES = 6
        private val REGION = Region(30, 78)

        // Only this box is copied into the instance: south-west and north-east corners of the apiary.
        private val AREA_SW = Tile(1920, 5031)
        private val AREA_NE = Tile(1943, 5055)

        private val FIELD = Tile(1931, 5044)
        private val KEEPER = Tile(1931, 5045)

        // Access mask flags: drag depth 1 (bit 17) and drag-onto (bit 19).
        private const val DRAG_SETTINGS = (1 shl 17) or (1 shl 19)
    }
}
