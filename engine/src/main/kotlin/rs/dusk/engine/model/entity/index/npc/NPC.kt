package rs.dusk.engine.model.entity.index.npc

import rs.dusk.engine.model.entity.index.Indexed
import rs.dusk.engine.model.entity.index.LocalChange
import rs.dusk.engine.model.entity.index.Movement
import rs.dusk.engine.model.entity.index.update.Visuals
import rs.dusk.engine.model.world.Tile

/**
 * A non-player character
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class NPC(
    override val id: Int,
    override var tile: Tile,
    override val visuals: Visuals = Visuals(),
    override val movement: Movement = Movement()
) : Indexed {

    override var change: LocalChange? = null
    var walkDirection: Int = -1
    var runDirection: Int = -1

    var movementType: NPCMoveType = NPCMoveType.Walk

    constructor(id: Int = 0, tile: Tile = Tile(0), index: Int) : this(id, tile) {
        this.index = index
    }

    override var index: Int = -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NPC
        return index == other.index
    }

    override fun hashCode(): Int {
        return index
    }

    override fun toString(): String {
        return "NPC(id=$id, index=$index, tile=$tile)"
    }
}