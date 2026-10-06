package content.skill.construction

import content.skill.construction.House.Companion.houseBase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

/**
 * Combat room rings and the barriers and balance beams built inside them, players inside can fight each other.
 * Boxing rings only allow boxing gloves, fencing rings only weapons, ranging pedestals only ranged and magic and balance beams only melee.
 * https://oldschool.runescape.wiki/w/Combat_room
 */
class HouseCombatRoom : Script {
    init {
        objectOperate("Climb-over", "boxing_ring,fencing_ring,combat_ring") { (target) ->
            cross(target, target.id.removeSuffix("_ring"))
        }

        objectOperate("Walk-through", "magic_barrier") { (target) ->
            cross(target, "ranging")
        }

        objectOperate("Stand-on", BALANCE_BEAMS) { (target) ->
            if (get("house_ring", "") == "beam") {
                return@objectOperate
            }
            val from = tile
            anim("climb_up")
            exactMoveDelay(target.tile, 30, direction = target.tile.delta(tile).toDirection())
            enter("beam", from)
        }

        objectOperate("Get-down", BALANCE_BEAMS) { (target) ->
            if (get("house_ring", "") != "beam") {
                return@objectOperate
            }
            val free = Direction.cardinal.map { target.tile.add(it) }.firstOrNull { !onBeam(it) } ?: return@objectOperate
            leave()
            exactMoveDelay(free, 30, direction = free.delta(tile).toDirection())
        }

        moved {
            val ring: String = get("house_ring") ?: return@moved
            if (houseBase() == null || !inside(ring, tile)) {
                leave()
            }
        }

        combatPrepare("range") {
            meleeOnly()
        }

        combatPrepare("magic") {
            meleeOnly()
        }

        combatPrepare("melee") {
            if (get("house_ring", "") == "ranging") {
                message("You can only use ranged or magic from the pedestals.") // TODO proper message
                false
            } else {
                true
            }
        }
    }

    /**
     * Climbs over the rope or walks through the barrier [target] into or out of a [ring]
     */
    private suspend fun Player.cross(target: GameObject, ring: String) {
        val inside = target.tile.add(side(target))
        val entering = tile != inside
        if (entering && !canEnter(ring)) {
            return
        }
        val destination = if (entering) inside else target.tile
        val from = tile
        if (!entering) {
            leave()
        }
        anim(if (ring == "ranging") "pass_through_barrier" else "climb_over_wall")
        exactMoveDelay(destination, 30, direction = destination.delta(tile).toDirection())
        if (entering) {
            enter(ring, from)
        }
    }

    private fun Player.canEnter(ring: String): Boolean {
        when (ring) {
            "boxing" -> {
                val gloves = equipped(EquipSlot.Weapon).id
                if (gloves != "boxing_gloves_red" && gloves != "boxing_gloves_blue") {
                    message("You need to wear boxing gloves to box.") // TODO proper message
                    return false
                }
                if (equipment.items.any { it.isNotEmpty() && it.id != gloves }) {
                    message("You can only wear boxing gloves in the boxing ring.") // TODO proper message
                    return false
                }
            }
            "fencing" -> if (equipment.items.withIndex().any { (slot, item) -> item.isNotEmpty() && slot != EquipSlot.Weapon.index }) {
                message("You can only use a weapon in the fencing ring.") // TODO proper message
                return false
            }
        }
        return true
    }

    private fun Player.meleeOnly(): Boolean {
        val ring = get("house_ring", "")
        if (ring == "boxing" || ring == "fencing" || ring == "beam") {
            message("You can only use melee in here.") // TODO proper message
            return false
        }
        return true
    }

    /**
     * Enters a [ring] from [from], where players are sent back to when they die inside it
     */
    private fun Player.enter(ring: String, from: Tile) {
        set("house_ring", ring)
        set("house_ring_exit", from)
        set("in_pvp", true)
        options.set(1, "Attack")
    }

    private fun Player.leave() {
        if (remove<String>("house_ring") == null) {
            return
        }
        clear("house_ring_exit")
        clear("in_pvp")
        options.remove("Attack")
    }

    /**
     * Whether [tile] is inside a [ring], rings have mats on each tile inside the ropes
     */
    private fun inside(ring: String, tile: Tile): Boolean = when (ring) {
        "beam" -> onBeam(tile)
        "ranging" -> GameObjects.at(tile).any { it.id == "ranging_spot" }
        else -> GameObjects.at(tile).any { it.shape == ObjectShape.GROUND_DECOR && (it.id.startsWith("${ring}_ring") || it.id.startsWith("${ring}_mat")) }
    }

    private fun onBeam(tile: Tile) = GameObjects.at(tile).any { it.id == "balance_beam" || it.id == "balance_beam_end" }

    /**
     * The side of the rope or barrier [target] facing into the ring, corners face diagonally
     */
    private fun side(target: GameObject): Direction = when (target.shape) {
        ObjectShape.WALL_STRAIGHT -> sides[target.rotation and 0x3]
        else -> corners[target.rotation and 0x3]
    }

    companion object {
        private const val BALANCE_BEAMS = "balance_beam,balance_beam_end"
        private val sides = arrayOf(Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH)
        private val corners = arrayOf(Direction.NORTH_WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH_WEST)
    }
}
