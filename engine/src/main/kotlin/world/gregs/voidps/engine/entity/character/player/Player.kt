package world.gregs.voidps.engine.entity.character.player

import kotlinx.coroutines.flow.MutableSharedFlow
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.Contexts
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.ui.GameFrame
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.move.AreaEntered
import world.gregs.voidps.engine.entity.character.mode.move.AreaExited
import world.gregs.voidps.engine.entity.character.mode.move.Steps
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.equip.BodyParts
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.Inventories
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
import world.gregs.voidps.type.Tile
import kotlin.coroutines.Continuation

/**
 * A player controlled by client or bot
 */
class Player(
    override var index: Int = -1,
    override var tile: Tile = Tile.EMPTY,
    val inventories: Inventories = Inventories(),
    variables: MutableMap<String, Any> = mutableMapOf(),
    val experience: Experience = Experience(),
    override val levels: Levels = Levels(),
    val friends: MutableMap<String, ClanRank> = mutableMapOf(),
    val ignores: MutableList<String> = mutableListOf(),
    var client: Client? = null,
    var viewport: Viewport? = null,
    var accountName: String = "",
    var passwordHash: String = "",
    val body: BodyParts = BodyParts()
) : Character {

    override var mode: Mode = EmptyMode
        set(value) {
            field.stop()
            field = value
            value.start()
        }

    override lateinit var visuals: PlayerVisuals
    val instructions = MutableSharedFlow<Instruction>(replay = 20)
    override val events: Events = Events(this)
    lateinit var options: PlayerOptions
    val gameFrame = GameFrame()
    lateinit var interfaces: Interfaces
    lateinit var interfaceOptions: InterfaceOptions
    override lateinit var collision: CollisionStrategy

    var changeValue: Int = -1

    val networked: Boolean
        get() = client != null && viewport != null

    override var suspension: Suspension? = null
        set(value) {
            field?.cancel()
            field = value
        }

    override var delay: Continuation<Unit>? = null

    var dialogueSuspension: Suspension? = null
        set(value) {
            field?.cancel()
            field = value
        }

    override var queue = ActionQueue(this)

    /**
     * Always ticks
     */
    override var softTimers: Timers = TimerQueue(events)

    /**
     * Ticks while not delayed or has interface open
     */
    var timers = TimerQueue(events)

    override var variables: Variables = PlayerVariables(events, variables)

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
        }
        events.emit(Registered)
        val definitions = get<AreaDefinitions>()
        for (def in definitions.get(tile.zone)) {
            if (tile in def.area) {
                events.emit(AreaEntered(this, def.name, def.tags, def.area))
            }
        }
    }

    fun logout(safely: Boolean) {
        if (this["logged_out", false]) {
            return
        }
        this["logged_out"] = true
        if (safely) {
            client?.logout()
            strongQueue("logout") {
                // Make sure nothing else starts
            }
        }
        disconnect()
    }

    private fun disconnect() {
        client?.disconnect()
        val queue: ConnectionQueue = get()
        queue.disconnect {
            val players: Players = get()
            World.run("logout", 1) {
                players.remove(this@Player)
                players.removeIndex(this@Player)
                players.releaseIndex(this@Player)
            }
            val definitions = get<AreaDefinitions>()
            for (def in definitions.get(tile.zone)) {
                if (tile in def.area) {
                    events.emit(AreaExited(this@Player, def.name, def.tags, def.area))
                }
            }
            events.emit(Unregistered)
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