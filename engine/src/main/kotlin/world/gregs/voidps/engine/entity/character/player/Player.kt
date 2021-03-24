package world.gregs.voidps.engine.entity.character.player

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Contexts
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.dialogue.Dialogues
import world.gregs.voidps.engine.data.serializer.PlayerBuilder
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.CharacterEffects
import world.gregs.voidps.engine.entity.character.CharacterValues
import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.player.delay.Delays
import world.gregs.voidps.engine.entity.character.player.login.LoginQueue
import world.gregs.voidps.engine.entity.character.player.req.Requests
import world.gregs.voidps.engine.entity.character.player.skill.Experience
import world.gregs.voidps.engine.entity.character.player.skill.Levels
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.character.update.visual.player.*
import world.gregs.voidps.engine.event.*
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.region.RegionLogin
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.logout
import world.gregs.voidps.utility.get

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
    @JsonIgnore
    override val action: Action = Action(),
    val containers: MutableMap<String, Container> = mutableMapOf(),
    val variables: MutableMap<String, Any> = mutableMapOf(),
    @JsonIgnore
    val temporaryVariables: MutableMap<String, Any> = mutableMapOf(),
    @JsonIgnore
    override val values: CharacterValues = CharacterValues(),
    @JsonIgnore
    val delays: Delays = Delays(),
    @JsonIgnore
    val dialogues: Dialogues = Dialogues(),
    val experience: Experience = Experience(),
    val levels: Levels = Levels(),
    override val effects: CharacterEffects = CharacterEffects(),
    @JsonIgnore
    var client: Client? = null,
    var name: String = "",
    var passwordHash: String = ""
) : Character {

    @JsonIgnore
    override val events: Events = Events(this)

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
        movement.previousTile = tile
        levels.link(experience)
        effects.link(this)
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
        face()
    }

    fun login(client: Client? = null) {
        this.client = client
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
        return "Player(${appearance.displayName}, index=$index, tile=$tile)"
    }
}