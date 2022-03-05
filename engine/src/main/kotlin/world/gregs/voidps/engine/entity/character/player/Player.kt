package world.gregs.voidps.engine.entity.character.player

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.data.PlayerBuilder
import world.gregs.voidps.engine.data.PlayerFactory
import world.gregs.voidps.engine.data.TileSerializer
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Levels
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.player.chat.Rank
import world.gregs.voidps.engine.entity.character.player.req.Requests
import world.gregs.voidps.engine.entity.character.player.skill.Experience
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.RegionLogin
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.ClientState
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.encode.login
import world.gregs.voidps.network.encode.logout
import world.gregs.voidps.network.visual.BodyPart
import world.gregs.voidps.network.visual.MoveType
import world.gregs.voidps.network.visual.PlayerVisuals

/**
 * A player controlled by client or bot
 */
@JsonDeserialize(builder = PlayerBuilder::class)
@JsonPropertyOrder(value = ["accountName", "passwordHash", "tile", "experience", "levels", "values", "variables", "containers", "friends", "ignores"])
class Player(
    @JsonIgnore
    override var index: Int = -1,
    @get:JsonSerialize(using = TileSerializer::class)
    override var tile: Tile = Tile.EMPTY,
    @JsonIgnore
    override var size: Size = Size.ONE,
    @JsonIgnore
    val viewport: Viewport = Viewport(),
    @JsonIgnore
    override val movement: Movement = Movement(),
    val containers: MutableMap<String, Container> = mutableMapOf(),
    @get:JsonUnwrapped
    val variables: Variables = Variables(),
    override val values: Values = Values(),
    @JsonIgnore
    val dialogues: Dialogues = Dialogues(),
    val experience: Experience = Experience(),
    @get:JsonUnwrapped
    override val levels: Levels = Levels(),
    val friends: MutableMap<String, Rank> = mutableMapOf(),
    val ignores: MutableList<String> = mutableListOf(),
    @JsonIgnore
    var client: Client? = null,
    var accountName: String = "",
    var passwordHash: String = ""
) : Character {

    @JsonIgnore
    override lateinit var visuals: PlayerVisuals

    @JsonIgnore
    val instructions = MutableSharedFlow<Instruction>(replay = 20)

    @JsonIgnore
    override val events: Events = Events(this)

    @JsonIgnore
    override val action: Action = Action(events)

    @JsonIgnore
    val requests: Requests = Requests(this)

    @JsonIgnore
    lateinit var options: PlayerOptions

    @JsonIgnore
    val gameFrame = PlayerGameFrame()

    @JsonIgnore
    lateinit var interfaces: Interfaces

    @JsonIgnore
    lateinit var interfaceOptions: InterfaceOptions

    @JsonIgnore
    override lateinit var interactTarget: TileTargetStrategy

    @JsonIgnore
    override lateinit var followTarget: TileTargetStrategy

    @JsonIgnore
    override lateinit var collision: CollisionStrategy

    @JsonIgnore
    override lateinit var traversal: TileTraversalStrategy

    @JsonIgnore
    var changeValue: Int = -1

    fun start() {
        movement.previousTile = tile.add(Direction.WEST.delta)
        experience.events = events
        levels.link(events, PlayerLevels(experience))
        variables.link(this, get())
        visuals = PlayerVisuals(body = BodyParts(equipment, intArrayOf(3, 14, 18, 26, 34, 38, 42)).apply {
            BodyPart.all.forEach {
                this.updateConnected(it)
            }
        })
    }

    fun setup() {
        options.set(2, "Follow")
        options.set(4, "Trade with")
        options.set(7, "Req Assist")
        val players: Players = get()
        players.add(this)
        viewport.players.addSelf(this)
        temporaryMoveType = MoveType.None
        movementType = MoveType.None
        flagMovementType()
        flagTemporaryMoveType()
        flagAppearance()
        face()
    }

    fun login(client: Client? = null, displayMode: Int = 0) {
        client?.login(name, index, rights.ordinal, membersWorld = World.members)
        gameFrame.displayMode = displayMode
        this.client = client
        interfaces.client = client
        if (client != null) {
            client.on(Contexts.Game, ClientState.Disconnecting) {
                logout(false)
            }
            events.emit(RegionLogin)
        }
        val collisions: Collisions = get()
        collisions.add(this)
        setup()
        events.emit(Registered)
    }

    fun logout(safely: Boolean) {
        action.run(ActionType.Logout) {
            withContext(NonCancellable) {
                if (safely) {
                    client?.logout()
                }
                client?.disconnect()
                val collisions: Collisions = get()
                collisions.remove(this@Player)
                val players: Players = get()
                players.remove(this@Player)
                delay(1) {
                    players.removeIndex(this@Player)
                }
                events.emit(Unregistered)
                val factory: PlayerFactory = get()
                factory.save(accountName, this@Player)
            }
        }
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