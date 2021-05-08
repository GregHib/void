package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterEffects
import world.gregs.voidps.engine.entity.character.CharacterValues
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.utility.get

/**
 * A non-player character
 * @author GregHib <greg@gregs.world>
 * @since March 28, 2020
 */
data class NPC(
    override val id: Int,
    override var tile: Tile,
    override val size: Size = Size.TILE,
    override val visuals: Visuals = Visuals(),
    override val movement: Movement = Movement(tile.minus(1)),
    override val values: CharacterValues = CharacterValues()
) : Character {

    override val events: Events = Events(this)
    override val action: Action = Action(events)
    override val effects = CharacterEffects()

    val name: String
        get() = get<NPCDefinitions>().getName(id)

    init {
        effects.link(this)
    }

    override var change: LocalChange? = null
    var walkDirection: Int = -1
    var runDirection: Int = -1

    var movementType: NPCMoveType = NPCMoveType.None
    var crawling: Boolean = false

    override lateinit var interactTarget: TileTargetStrategy

    val def: NPCDefinition
        get() = get<NPCDefinitions>().get(id)

    constructor(id: Int = 0, tile: Tile = Tile.EMPTY, index: Int) : this(id, tile) {
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