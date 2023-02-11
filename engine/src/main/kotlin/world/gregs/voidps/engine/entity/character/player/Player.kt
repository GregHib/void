package world.gregs.voidps.engine.entity.character.player

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.ui.GameFrame
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.contain.Containers
import world.gregs.voidps.engine.contain.equipment
import world.gregs.voidps.engine.contain.restrict.ValidItemRestriction
import world.gregs.voidps.engine.contain.stack.DependentOnItem
import world.gregs.voidps.engine.data.PlayerBuilder
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.definition.extra.ContainerDefinitions
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions
import world.gregs.voidps.engine.data.definition.extra.VariableDefinitions
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.equip.BodyParts
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.entity.character.turn
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.add
import world.gregs.voidps.engine.map.collision.remove
import world.gregs.voidps.engine.map.region.RegionLogin
import world.gregs.voidps.engine.queue.ActionQueue
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.suspend.suspendDelegate
import world.gregs.voidps.engine.timer.QueuedTimers
import world.gregs.voidps.engine.timer.Timers
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.ClientState
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.encode.login
import world.gregs.voidps.network.encode.logout
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.network.visual.update.player.MoveType

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
    val experience: Experience = Experience(),
    @get:JsonUnwrapped
    override val levels: Levels = Levels(),
    val friends: MutableMap<String, ClanRank> = mutableMapOf(),
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
        set(value) {
            field.stop()
            field = value
            value.start()
        }

    @JsonIgnore
    override lateinit var visuals: PlayerVisuals

    @JsonIgnore
    val instructions = MutableSharedFlow<Instruction>(replay = 20)

    @JsonIgnore
    override val events: Events = Events(this)

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

    @get:JsonIgnore
    override var suspension: Suspension? by suspendDelegate()

    @get:JsonIgnore
    override var queue = ActionQueue(this)

    @get:JsonIgnore
    override var timers: Timers = QueuedTimers()

    @get:JsonIgnore
    var normalTimers = QueuedTimers()

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
        previousTile = tile.add(Direction.WEST.delta)
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
        turn()
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
        collisions.add(this)
        players.add(this)
        setup()
        events.emit(Registered)
    }

    fun logout(safely: Boolean) {
        GlobalScope.launch {
//        strongQueue { // FIXME can't remove players during the player loop. Need to be marked for later
            if (safely) {
                client?.logout()
            }
            client?.disconnect()
            val collisions: Collisions = get()
            collisions.remove(this@Player)
            val players: Players = get()
            val gatekeeper: ConnectionGatekeeper = get()
            val queue: ConnectionQueue = get()
            players.remove(this@Player)
            launch {
                queue.await()
                players.removeIndex(this@Player)
                gatekeeper.releaseIndex(index)
            }
            this@Player.queue.logout()
            events.emit(Unregistered)
            val save: PlayerSave = get()
            save.queue(this@Player)
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