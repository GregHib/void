package rs.dusk.engine.model.entity.character.player

import rs.dusk.engine.action.Action
import rs.dusk.engine.client.ui.GameFrame
import rs.dusk.engine.client.ui.Interfaces
import rs.dusk.engine.client.ui.PlayerInterfaceIO
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.character.Character
import rs.dusk.engine.model.entity.character.CharacterValues
import rs.dusk.engine.model.entity.character.LocalChange
import rs.dusk.engine.model.entity.character.Movement
import rs.dusk.engine.model.entity.character.contain.Container
import rs.dusk.engine.model.entity.character.update.Visuals
import rs.dusk.engine.model.entity.character.update.visual.player.getAppearance
import rs.dusk.engine.model.world.Tile
import rs.dusk.utility.get

/**
 * A player controlled by client or bot
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class Player(
    @Transient override var index: Int = -1,
    override var id: Int = -1,
    override var tile: Tile = Tile.EMPTY,
    override var size: Size = Size.TILE,
    @Transient val viewport: Viewport = Viewport(),
    @Transient override val visuals: Visuals = Visuals(),
    @Transient override val movement: Movement = Movement(tile),
    @Transient override val action: Action = Action(),
    val containers: MutableMap<Int, Container> = mutableMapOf(),
    val variables: MutableMap<Int, Any> = mutableMapOf(),
    @Transient override val values: CharacterValues = CharacterValues()
) : Character {

    @Transient
    val gameframe: GameFrame = GameFrame()

    @Transient
    val interfaces: Interfaces = Interfaces(PlayerInterfaceIO(this), get(), gameframe)

    @Transient
    override var change: LocalChange? = null

    @Transient
    var changeValue: Int = -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Player
        return index == other.index
    }

    override fun hashCode(): Int {
        return index
    }

    override fun toString(): String {
        return "Player(${getAppearance().displayName}, index=$index, tile=$tile)"
    }
}