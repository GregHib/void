package content.quest.miniquest.barbarian_training

import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.skill.woodcutting.Hatchet
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.map.collision.canFit
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

/**
 * Burning barbarian remains on a pyre ship by Otto's lake.
 * Chewed bones lay a hero to rest, blessing future bone burying; mangled bones release a ferocious spirit.
 */
class PyreShips(val drops: DropTables, val stepValidator: StepValidator) : Script {

    init {
        objectOperate("Construct", "pyre_site") { (target) ->
            if (!barbarianTrainingStarted("pyre")) {
                statement(BarbarianTraining.NOT_STARTED_MESSAGE)
                return@objectOperate
            }
            val spirit = NPCs.findOrNull(tile.regionLevel) { it.id == "ferocious_barbarian_spirit" && it["owner", ""] == accountName }
            if (spirit != null) {
                // TODO proper message
                message("You must defeat the barbarian spirit first.")
                return@objectOperate
            }
            val bones = listOf("chewed_bones", "mangled_bones").firstOrNull { inventory.contains(it) }
            if (bones == null) {
                // TODO proper message
                message("You need chewed bones or mangled bones in order to do this.")
                return@objectOperate
            }
            val hatchet = Hatchet.best(this)
            if (hatchet == null) {
                // TODO proper message
                message("You need a hatchet in order to do this.")
                return@objectOperate
            }
            if (!inventory.contains("tinderbox") && !BOWS.any { carriesItem(it) }) {
                // TODO proper message
                message("You need a tinderbox or a bow in order to do this.")
                return@objectOperate
            }
            val held = Tables.get("pyre_logs").rows().filter { inventory.contains(it.rowId) }
            if (held.isEmpty()) {
                message("You don't have any logs.")
                return@objectOperate
            }
            val log = held.firstOrNull { levels.get(Skill.Firemaking) >= it.int("level") && levels.get(Skill.Crafting) >= it.int("level") }
            if (log == null) {
                val lowest = held.last().int("level")
                has(Skill.Firemaking, lowest, true) && has(Skill.Crafting, lowest, true)
                return@objectOperate
            }
            val station = station(target)
            if (station == null) {
                // TODO proper message
                message("This pyre site is in use currently.")
                return@objectOperate
            }
            face(target)
            // Each carving stage reverts by itself if the player stops part way through
            // Object timers run before players so the previous stage has reverted when the next is placed
            val carve = "carve_pyre_${hatchet.id.removeSuffix("_hatchet")}"
            station.replace("pyre_carved_log", ticks = CARVE_TICKS)
            anim(carve)
            delay(CARVE_TICKS)
            station.replace("pyre_carved_log_2", ticks = CARVE_TICKS)
            delay(2)
            anim(carve)
            delay(2)
            station.replace("pyre_boat", ticks = CARVE_TICKS)
            delay(CARVE_TICKS)
            inventory.transaction {
                remove(log.rowId)
                remove(bones)
            }
            if (inventory.transaction.failed) {
                return@objectOperate
            }
            exp(Skill.Crafting, log.int("crafting_xp") / 10.0)
            exp(Skill.Firemaking, log.int("firemaking_xp") / 10.0)
            if (bones == "chewed_bones") {
                // Removed once it has ascended
                val npc = NPCs.add("peaceful_barbarian_spirit", target.tile, ticks = 7)
                npc.face(this)
                npc.anim("peaceful_barbarian_spirit_ascend")
                set("barbarian_pyre_bones", (get("barbarian_pyre_bones", 0) + log.int("bones")).coerceAtMost(MAX_BLESSED_BONES))
                // TODO proper message
                message("The ancient barbarian is laid to rest. Your future prayer training is blessed, as his spirit ascends to a glorious afterlife. Spirits drop an object into your pack.")
                val reward = drops.get("peaceful_barbarian_spirit_drop_table")?.roll()?.firstOrNull()?.toItem()
                if (reward != null) {
                    addOrDrop(reward.id, reward.amount)
                }
                barbarianTaskDone("pyre")
            } else {
                // The site is under the boat so it appears beside the player instead, never diagonally as the boat could block it
                val spawn = Direction.cardinal.map { tile.add(it) }.filter { stepValidator.canFit(it, collision, size, blockMove) }.randomOrNull(random) ?: tile
                val npc = NPCs.add("ferocious_barbarian_spirit", spawn, ticks = TimeUnit.MINUTES.toTicks(5), owner = this)
                npc.anim("ferocious_barbarian_spirit_appear")
                val player = this
                npc.queue("appear") {
                    // Delayed so hunting doesn't cut its appearance short
                    npc.delay(2)
                    npc.interactPlayer(player, "Attack")
                }
            }
            burn(station)
        }
    }

    fun station(site: GameObject): GameObject? {
        for (x in -2..0) {
            for (y in -2..0) {
                return GameObjects.findOrNull(site.tile.add(x, y), "pyre_boat_station") ?: continue
            }
        }
        return null
    }

    /**
     * The pyre ship burns, launches and sinks in place of the boat [station] before it reverts
     */
    fun burn(station: GameObject) {
        station.replace("pyre_boat_bones", ticks = 4) {
            val lit = station.replace("pyre_boat_lit", ticks = 8) {
                // The boat has sunk 30 cycles into the 57 cycle sink, reverting before it ends stops the boat reappearing
                val burning = station.replace("pyre_boat_burning", ticks = 4)
                animate(burning, "pyre_boat_sink", 2)
            }
            animate(lit, "pyre_boat_launch", 3)
        }
    }

    /**
     * The launch and sink are played on the stage before (same skeleton) instead of their own objects (25293, 25295)
     * as the client starts an object's animation part way through, then shows its model again once a one-off animation ends.
     * An animation sent with a new object is played on the old one, so it's sent once the stage is in place.
     * Queued blocks run after the tick's zone updates are sent, so the animation reaches the client a tick after [delay].
     */
    fun animate(stage: GameObject, anim: String, delay: Int) {
        World.queue("pyre_ship_${stage.tile.id}", delay) {
            stage.anim(anim)
        }
    }

    companion object {
        const val MAX_BLESSED_BONES = 200

        // TODO confirm the stage timings, carving follows 2009scape and the launch and sink their animation lengths
        private const val CARVE_TICKS = 4
        val BOWS = listOf("shortbow", "longbow", "oak_shortbow", "oak_longbow", "willow_shortbow", "willow_longbow", "maple_shortbow", "maple_longbow", "yew_shortbow", "yew_longbow", "magic_shortbow", "magic_longbow", "training_bow", "seercull", "sacred_clay_bow")
    }
}
