package world.gregs.voidps.engine.entity.character.player

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.data.StorageStrategy
import world.gregs.voidps.engine.data.serializer.PlayerBuilder
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.Levels
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.player.req.Requests
import world.gregs.voidps.engine.entity.character.player.skill.Experience
import world.gregs.voidps.engine.entity.character.player.skill.GrantExp
import world.gregs.voidps.engine.entity.character.player.skill.Leveled
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.character.update.visual.player.*
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.RegionLogin
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.encode.logout

/**
 * A player controlled by client or bot
 */
@JsonDeserialize(builder = PlayerBuilder::class)
class Player(
    @JsonIgnore
    override var index: Int = -1,
    @JsonIgnore
    override var id: Int = -1,
    @get:JsonProperty("tile")
    override var tile: Tile = Tile.EMPTY,
    @JsonIgnore
    override var size: Size = Size.TILE,
    @JsonIgnore
    val viewport: Viewport = Viewport(),
    @JsonIgnore
    override val visuals: Visuals = Visuals(),
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
    @JsonIgnore
    var client: Client? = null,
    var name: String = "",
    var passwordHash: String = ""
) : Character {

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
    lateinit var followTarget: TileTargetStrategy

    @JsonIgnore
    override var change: LocalChange? = null

    @JsonIgnore
    var changeValue: Int = -1

    fun start() {
        movement.previousTile = tile.add(Direction.WEST.delta)
        experience.events = events
        levels.link(events, PlayerLevels(experience))
        events.on<Player, GrantExp> {
            val previousLevel = PlayerLevels.getLevel(from)
            val currentLevel = PlayerLevels.getLevel(to)
            if (currentLevel > previousLevel) {
                events.emit(Leveled(skill, previousLevel, currentLevel))
            }
        }
        variables.link(this, get())
    }

    fun setup() {
        options.set(2, "Follow")
        options.set(4, "Trade with")
        options.set(7, "Req Assist")
        val players: Players = get()
        players.add(this)
        viewport.players.add(this)
        temporaryMoveType = PlayerMoveType.None
        movementType = PlayerMoveType.None
        flagMovementType()
        flagTemporaryMoveType()
        flagAppearance()
        face()
    }

    fun login(client: Client? = null) {
        this.client = client
        interfaces.client = client
        client?.exit = {
            logout(false)
        }
        if (client != null) {
            events.emit(RegionLogin)
        }
        val collisions: Collisions = get()
        collisions.add(this)
        setup()
        events.emit(Registered)
    }

    fun logout(safely: Boolean) {
        val loginQueue: LoginQueue = get()
        GlobalScope.launch(Contexts.Game) {
            loginQueue.await()
            action.run(ActionType.Logout) {
                await<Unit>(Suspension.Infinite)
            }
            if (safely) {
                client?.logout()
            }
            client?.disconnect()
            loginQueue.logout(name, client?.address ?: "", index)
            val collisions: Collisions = get()
            collisions.remove(this@Player)
            val players: Players = get()
            players.remove(tile, this@Player)
            players.remove(tile.chunk, this@Player)
            delay(1) {
                players.removeAtIndex(index)
            }
            events.emit(Unregistered)
            val storage: StorageStrategy<Player> = get()
            storage.save(name, this@Player)
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