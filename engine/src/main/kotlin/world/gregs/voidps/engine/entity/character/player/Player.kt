package world.gregs.voidps.engine.entity.character.player

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import org.rsmod.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ui.GameFrame
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.data.PlayerBuilder
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Levels
import world.gregs.voidps.engine.entity.character.contain.Containers
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.restrict.ValidItemRestriction
import world.gregs.voidps.engine.entity.character.contain.stack.DependentOnItem
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.player.chat.Rank
import world.gregs.voidps.engine.entity.character.player.req.Requests
import world.gregs.voidps.engine.entity.character.player.skill.Experience
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.definition.VariableDefinitions
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.nav.Edge
import world.gregs.voidps.engine.map.region.RegionLogin
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.ClientState
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.encode.login
import world.gregs.voidps.network.encode.logout
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.network.visual.update.player.MoveType
import java.util.*

/**
 * A player controlled by client or bot
 */
@JsonDeserialize(builder = PlayerBuilder::class)
@JsonPropertyOrder(value = ["accountName", "passwordHash", "tile", "experience", "levels", "body", "values", "variables", "containers", "friends", "ignores"])
class Player(
    @JsonIgnore
    override var index: Int = -1,
    override var tile: Tile = Tile.EMPTY,
    @JsonIgnore
    override var size: Size = Size.ONE,
    @get:JsonUnwrapped
    val containers: Containers = Containers(),
    @get:JsonUnwrapped
    val variables: Variables = Variables(),
    override var values: Values? = Values(),
    @JsonIgnore
    val dialogues: Dialogues = Dialogues(),
    val experience: Experience = Experience(),
    @get:JsonUnwrapped
    override val levels: Levels = Levels(),
    val friends: MutableMap<String, Rank> = mutableMapOf(),
    val ignores: MutableList<String> = mutableListOf(),
    @JsonIgnore
    var client: Client? = null,
    @JsonIgnore
    var viewport: Viewport? = null,
    var accountName: String = "",
    var passwordHash: String = "",
    @get:JsonUnwrapped
    val body: BodyParts = BodyParts()
) : Character {

    @JsonIgnore
    override var mode: Mode = EmptyMode

    @JsonIgnore
    override val movement: Movement = Movement(this)

    @JsonIgnore
    val waypoints: LinkedList<Edge> = LinkedList()

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
    val gameFrame = GameFrame()

    @JsonIgnore
    lateinit var interfaces: Interfaces

    @JsonIgnore
    lateinit var interfaceOptions: InterfaceOptions

    @JsonIgnore
    override lateinit var collision: CollisionStrategy

    @JsonIgnore
    var changeValue: Int = -1

    @get:JsonIgnore
    val networked: Boolean
        get() = client != null && viewport != null

    fun start(
        variableDefinitions: VariableDefinitions,
        containerDefinitions: ContainerDefinitions,
        itemDefinitions: ItemDefinitions,
        validItem: ValidItemRestriction
    ) {
        containers.definitions = containerDefinitions
        containers.itemDefinitions = itemDefinitions
        containers.validItemRule = validItem
        containers.normalStack = DependentOnItem(itemDefinitions)
        containers.events = events
        movement.previousTile = tile.add(Direction.WEST.delta)
        experience.events = events
        levels.link(events, PlayerLevels(experience))
        variables.link(this, variableDefinitions)
        body.link(equipment)
        body.updateAll()
    }

    fun setup() {
        options.set(2, "Follow")
        options.set(4, "Trade with")
        options.set(7, "Req Assist")
        viewport?.players?.addSelf(this)
        temporaryMoveType = MoveType.None
        movementType = MoveType.None
        flagMovementType()
        flagTemporaryMoveType()
        flagAppearance()
        face()
    }

    fun login(client: Client? = null, displayMode: Int = 0, collisions: Collisions, players: Players) {
        client?.login(name, index, rights.ordinal, membersWorld = World.members)
        gameFrame.displayMode = displayMode
        this.client = client
        interfaces.client = client
        if (client != null) {
            this.viewport = Viewport()
            client.on(Contexts.Game, ClientState.Disconnecting) {
                logout(false)
            }
            events.emit(RegionLogin)
        }
        set("logged_in", true)
        collisions.add(this)
        players.add(this)
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
                val gatekeeper: ConnectionGatekeeper = get()
                players.remove(this@Player)
                World.delay(1) {
                    players.removeIndex(this@Player)
                    gatekeeper.releaseIndex(index)
                }
                events.emit(Unregistered)
                val save: PlayerSave = get()
                save.queue(this@Player)
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