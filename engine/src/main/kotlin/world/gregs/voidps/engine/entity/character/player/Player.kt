package world.gregs.voidps.engine.entity.character.player

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import kotlinx.coroutines.flow.MutableSharedFlow
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.client.ConnectionGatekeeper
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.ui.GameFrame
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.Containers
import world.gregs.voidps.engine.data.PlayerBuilder
import world.gregs.voidps.engine.data.serial.MapSerializer
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.move.Steps
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.equip.BodyParts
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.region.RegionLogin
import world.gregs.voidps.engine.queue.ActionQueue
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.timer.TimerQueue
import world.gregs.voidps.engine.timer.Timers
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.ClientState
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.encode.login
import world.gregs.voidps.network.encode.logout
import world.gregs.voidps.network.visual.PlayerVisuals
import kotlin.coroutines.Continuation

/**
 * A player controlled by client or bot
 */
@JsonDeserialize(builder = PlayerBuilder::class)
@JsonPropertyOrder(value = ["accountName", "passwordHash", "tile", "experience", "levels", "body", "variables", "containers", "friends", "ignores"])
class Player(
    @JsonIgnore
    override var index: Int = -1,
    override var tile: Tile = Tile.EMPTY,
    @JsonIgnore
    override var size: Size = Size.ONE,
    @get:JsonUnwrapped
    val containers: Containers = Containers(),
    @JsonSerialize(using = MapSerializer::class)
    variables: MutableMap<String, Any> = mutableMapOf(),
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
    override var suspension: Suspension? = null
        set(value) {
            field?.cancel()
            field = value
        }

    @get:JsonIgnore
    override var delay: Continuation<Unit>? = null

    @get:JsonIgnore
    var dialogueSuspension: Suspension? = null
        set(value) {
            field?.cancel()
            field = value
        }

    @get:JsonIgnore
    override var queue = ActionQueue(this)

    /**
     * Always ticks
     */
    @get:JsonIgnore
    override var softTimers: Timers = TimerQueue(events)

    /**
     * Ticks while not delayed or has interface open
     */
    @get:JsonIgnore
    var timers = TimerQueue(events)

    @get:JsonUnwrapped
    override var variables: Variables = PlayerVariables(events, variables)

    @get:JsonIgnore
    override val steps = Steps(this)

    fun login(client: Client? = null, displayMode: Int = 0) {
        gameFrame.displayMode = displayMode
        if (client != null) {
            this.viewport = Viewport()
            client.login(name, index, rights.ordinal, membersWorld = World.members)
            this.client = client
            interfaces.client = client
            (variables as PlayerVariables).client = client
            client.on(Contexts.Game, ClientState.Disconnecting) {
                logout(false)
            }
            events.emit(RegionLogin)
            viewport?.players?.addSelf(this)
        }
        events.emit(Registered)
    }

    fun logout(safely: Boolean) {
        if (this["logged_out", false]) {
            return
        }
        this["logged_out"] = true
        strongQueue("logout") {
            if (safely) {
                client?.logout()
            }
            client?.disconnect()
            val queue: ConnectionQueue = get()
            val gatekeeper: ConnectionGatekeeper = get()
            queue.disconnect {
                val players: Players = get()
                World.run("logout", 1) {
                    players.remove(this@Player)
                    players.removeIndex(this@Player)
                    gatekeeper.releaseIndex(index)
                }
                events.emit(Unregistered)
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
        return "Player(${accountName}, index=$index, tile=$tile)"
    }
}