package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Values
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Levels
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy
import world.gregs.voidps.engine.utility.get

/**
 * A non-player character
 */
data class NPC(
    val id: String,
    override var tile: Tile,
    override val size: Size = Size.ONE,
    override val visuals: Visuals = Visuals(),
    override val movement: Movement = Movement(tile.minus(1)),
    override val values: Values = Values(),
    override val levels: Levels = Levels()
) : Character {

    override val events: Events = Events(this)
    override val action: Action = Action(events)
    override lateinit var collision: CollisionStrategy
    override lateinit var traversal: TileTraversalStrategy

    override var change: LocalChange? = null
    var walkDirection: Int = -1
    var runDirection: Int = -1

    override lateinit var interactTarget: TileTargetStrategy

    override lateinit var followTarget: TileTargetStrategy

    val def: NPCDefinition
        get() = get<NPCDefinitions>().get(id)

    constructor(id: String = "", tile: Tile = Tile.EMPTY, index: Int) : this(id, tile) {
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