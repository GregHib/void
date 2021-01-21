package world.gregs.void.engine.entity.character.player

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import world.gregs.void.engine.action.Action
import world.gregs.void.engine.client.ui.InterfaceOptions
import world.gregs.void.engine.client.ui.Interfaces
import world.gregs.void.engine.client.ui.dialogue.Dialogues
import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.entity.character.Character
import world.gregs.void.engine.entity.character.CharacterEffects
import world.gregs.void.engine.entity.character.CharacterValues
import world.gregs.void.engine.entity.character.contain.Container
import world.gregs.void.engine.entity.character.move.Movement
import world.gregs.void.engine.entity.character.player.delay.Delays
import world.gregs.void.engine.entity.character.player.req.Requests
import world.gregs.void.engine.entity.character.player.skill.Experience
import world.gregs.void.engine.entity.character.player.skill.Levels
import world.gregs.void.engine.entity.character.update.LocalChange
import world.gregs.void.engine.entity.character.update.Visuals
import world.gregs.void.engine.entity.character.update.visual.player.appearance
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.path.TargetStrategy

/**
 * A player controlled by client or bot
 * @author GregHib <greg@gregs.world>
 * @since March 28, 2020
 */
@Serializable
class Player(
    @Transient
    override var index: Int = -1,
    @Transient
    override var id: Int = -1,
    override var tile: Tile = Tile.EMPTY,
    @Transient
    override var size: Size = Size.TILE,
    @Transient
    val viewport: Viewport = Viewport(),
    @Transient
    override val visuals: Visuals = Visuals(),
    @Transient
    override val movement: Movement = Movement(),
    @Transient
    override val action: Action = Action(),
    val containers: MutableMap<Int, Container> = mutableMapOf(),
    @Transient// Temp
    val variables: MutableMap<Int, Any> = mutableMapOf(),
    @Transient
    override val values: CharacterValues = CharacterValues(),
    @Transient
    val delays: Delays = Delays(),
    @Transient
    val dialogues: Dialogues = Dialogues(),
    val experience: Experience = Experience(),
    val levels: Levels = Levels()
) : Character {

    override val effects = CharacterEffects()

    init {
        movement.previousTile = tile
        levels.link(experience)
        effects.link(this)
    }

    @Transient
    val requests: Requests = Requests(this)

    @Transient
    lateinit var options: PlayerOptions

    @Transient
    val gameFrame = PlayerGameFrame()

    @Transient
    lateinit var interfaces: Interfaces

    @Transient
    lateinit var interfaceOptions: InterfaceOptions

    @Transient
    override lateinit var interactTarget: TargetStrategy

    @Transient
    lateinit var followTarget: TargetStrategy

    @Transient
    override var change: LocalChange? = null

    @Transient
    var changeValue: Int = -1

    fun start() {
        options.set(2, "Follow")
        options.set(4, "Trade with")
        options.set(7, "Req Assist")
    }

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
        return "Player(${appearance.displayName}, index=$index, tile=$tile)"
    }
}