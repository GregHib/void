package rs.dusk.engine.model.entity.character.npc

import rs.dusk.cache.definition.data.NPCDefinition
import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.action.Action
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.character.Character
import rs.dusk.engine.model.entity.character.CharacterValues
import rs.dusk.engine.model.entity.character.LocalChange
import rs.dusk.engine.model.entity.character.Movement
import rs.dusk.engine.model.entity.character.update.Visuals
import rs.dusk.engine.model.map.Tile
import rs.dusk.utility.get

/**
 * A non-player character
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class NPC(
    override val id: Int,
    override var tile: Tile,
    override val size: Size = Size.TILE,
    override val visuals: Visuals = Visuals(),
    override val movement: Movement = Movement(),
    override val action: Action = Action(),
    override val values: CharacterValues = CharacterValues()
) : Character {

    override var change: LocalChange? = null
    var walkDirection: Int = -1
    var runDirection: Int = -1

    var movementType: NPCMoveType = NPCMoveType.None

    val def: NPCDefinition
        get() = get<NPCDecoder>().getSafe(id)

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