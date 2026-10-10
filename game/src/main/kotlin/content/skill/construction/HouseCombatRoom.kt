package content.skill.construction

import content.entity.player.dialogue.type.statement
import content.skill.construction.House.Companion.houseBase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.cantReach
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.move
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import kotlin.math.abs
import kotlin.math.sign

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

        // Approached rather than operated so the player isn't walked on to the beam before it's known they can climb on
        objectApproach("Stand-on", BALANCE_BEAMS) { (target) ->
            if (get("house_ring", "") == "beam") {
                step(target)
                return@objectApproach
            }
            if (target.id != "balance_beam_end") {
                message("You should get on the balance beam at one end.")
                return@objectApproach
            }
            approachRange(1)
            message("Climbing on to beam.")
            if (!freeHands()) {
                return@objectApproach
            }
            val from = tile
            anim("balance_beam_getup")
            exactMoveDelay(target.tile, delay = 37, direction = facing(from, target.tile))
            equipment.transaction { set(EquipSlot.Weapon.index, Item("pugel")) }
            enter("beam", from)
            renderEmote("beam_balance")
            walkTrigger { blockWalking() }
        }

        // Operated on the beam being stood on, which can't be approached from underneath, and approached along the rest of it
        objectOperate("Get-down", BALANCE_BEAMS) {
            getDown()
        }

        objectApproach("Get-down", BALANCE_BEAMS) {
            getDown()
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
        if (ring == "ranging") {
            delay(1)
            target.replace("magic_barrier_off", ticks = 2)
            walkOverDelay(destination)
        } else {
            anim(climbAnimation(), delay = 30)
            exactMoveDelay(destination, startDelay = 30, delay = 69, direction = facing(from, destination))
            delay(1)
        }
        if (entering) {
            enter(ring, from)
        }
    }

    private fun Player.climbAnimation() = when (equipped(EquipSlot.Weapon).id) {
        "boxing_gloves_red" -> "human_get_over_combatring_redgloves"
        "boxing_gloves_blue" -> "human_get_over_combatring_bluegloves"
        else -> "human_get_over_combatring_nogloves"
    }

    /**
     * Pugel sticks are two-handed so both hands have to be free before climbing on to a balance beam
     */
    private suspend fun Player.freeHands(): Boolean {
        if (equipped(EquipSlot.Weapon).isEmpty() && equipped(EquipSlot.Shield).isEmpty()) {
            return true
        }
        val text = "You must free your hands so you can wield the pugel stick."
        message(text, ChatType.Broadcast)
        statement(text)
        for (slot in listOf(EquipSlot.Weapon, EquipSlot.Shield)) {
            if (equipped(slot).isNotEmpty() && !equipment.move(slot.index, inventory)) {
                inventoryFull()
                break
            }
        }
        return false
    }

    private suspend fun Player.getDown() {
        if (get("house_ring", "") != "beam") {
            cantReach()
            return
        }
        val free = dismount() ?: return
        val from = tile
        leave()
        anim("balance_beam_jumpoff")
        areaSound("jump", from, radius = 5)
        exactMoveDelay(free, startDelay = 34, delay = 46, direction = facing(from, free))
    }

    /**
     * The tile to jump down on to, beside the beam rather than along it
     */
    private fun Player.dismount(): Tile? {
        val axis = Direction.cardinal.firstOrNull { onBeam(tile.add(it)) }
        return Direction.cardinal
            .filter { it != axis && it != axis?.inverse() }
            .map { tile.add(it) }
            .firstOrNull { !onBeam(it) }
    }

    /**
     * Moves one tile along the beam towards [target]
     */
    private suspend fun Player.step(target: GameObject) {
        val delta = target.tile.delta(tile)
        val direction = if (abs(delta.x) >= abs(delta.y)) Direction.of(delta.x.sign, 0) else Direction.of(0, delta.y.sign)
        val next = tile.add(direction)
        if (direction == Direction.NONE || !onBeam(next)) {
            return
        }
        walkToDelay(next, forceWalk = true)
    }

    /**
     * Stops the player walking off the beam, getting down is the only way off
     */
    private fun Player.blockWalking() {
        val mode = mode
        // Interacting with the beam itself is how the player gets down and moves along it
        val interacting = mode is PlayerOnObjectInteract && mode.target.id in BEAM_IDS
        if (mode is Movement && !interacting) {
            steps.clear()
            this.mode = EmptyMode
        }
        if (get("house_ring", "") == "beam") {
            walkTrigger { blockWalking() }
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
                    message("You can't wear weapons or armour in the boxing ring (except boxing gloves).")
                    return false
                }
            }
            "fencing" -> if (equipment.items.withIndex().any { (slot, item) -> item.isNotEmpty() && slot != EquipSlot.Weapon.index }) {
                message("You can't wear any armour in the fencing ring.")
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
     * The direction faced when moving from [from] to [to]
     */
    private fun facing(from: Tile, to: Tile): Direction {
        val delta = to.delta(from)
        return when {
            delta.y > 0 -> Direction.NORTH
            delta.y < 0 -> Direction.SOUTH
            delta.x > 0 -> Direction.EAST
            else -> Direction.WEST
        }
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
        val ring: String = remove("house_ring") ?: return
        if (ring == "beam") {
            equipment.remove(EquipSlot.Weapon.index, "pugel")
            clearRenderEmote()
            clearWalkTrigger()
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
        private val BEAM_IDS = BALANCE_BEAMS.split(",")
        private val sides = arrayOf(Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH)
        private val corners = arrayOf(Direction.NORTH_WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST, Direction.SOUTH_WEST)
    }
}
