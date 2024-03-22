package world.gregs.voidps.engine.data

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.ConnectionQueue
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.move.AreaEntered
import world.gregs.voidps.engine.entity.character.mode.move.AreaExited
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.DependentOnItem
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.zone.RegionLoad
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.encode.login
import world.gregs.voidps.network.encode.logout
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis

class PlayerAccounts(
    private val interfaceDefinitions: InterfaceDefinitions,
    private val inventoryDefinitions: InventoryDefinitions,
    private val itemDefinitions: ItemDefinitions,
    private val accountDefinitions: AccountDefinitions,
    private val collisionStrategyProvider: CollisionStrategyProvider,
    private val variableDefinitions: VariableDefinitions,
    private val homeTile: Tile,
    private val storage: AccountStorage
) : Runnable, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val validItems = ValidItemRestriction(itemDefinitions)
    private val pending = ConcurrentHashMap<String, PlayerSave>()
    private val logger = InlineLogger()

    override fun run() {
        if (pending.isEmpty()) {
            return
        }
        val accounts = pending.values.toList()
        launch {
            try {
                val took = measureTimeMillis {
                    storage.save(accounts)
                    for (account in accounts) {
                        pending.remove(account.name)
                    }
                }
                logger.info { "Saved ${accounts.size} ${"account".plural(accounts.size)} in ${took}ms" }
            } catch (e: Exception) {
                logger.error(e) { "Error saving players!" }
            }
        }
    }

    fun save(player: Player) {
        if (player.contains("bot")) {
            return
        }
        pending[player.accountName] = player.copy()
    }

    fun saving(name: String) = pending.containsKey(name)

    fun getOrElse(name: String, index: Int, block: () -> Player): Player {
        val player = storage.load(name)?.toPlayer() ?: block()
        initPlayer(player, index)
        return player
    }

    fun create(name: String, password: String): Player {
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        return Player(tile = homeTile, accountName = name, passwordHash = hash).apply {
            this["creation"] = System.currentTimeMillis()
            this["new_player"] = true
        }
    }

    fun initPlayer(player: Player, index: Int) {
        player.index = index
        player.visuals = PlayerVisuals(index, player.body)
        player.interfaces = Interfaces(player, player.client, interfaceDefinitions)
        player.interfaceOptions = InterfaceOptions(player, interfaceDefinitions, inventoryDefinitions)
        player.options = PlayerOptions(player)
        (player.variables as PlayerVariables).definitions = variableDefinitions
        player.inventories.definitions = inventoryDefinitions
        player.inventories.itemDefinitions = itemDefinitions
        player.inventories.validItemRule = validItems
        player.inventories.normalStack = DependentOnItem(itemDefinitions)
        player.inventories.events = player
        player.inventories.start()
        player.previousTile = player.tile.add(Direction.WEST.delta)
        player.experience.events = player
        player.levels.link(player, PlayerLevels(player.experience))
        player.body.link(player.equipment)
        player.body.updateAll()
        player.appearance.displayName = player.name
        if (player.contains("new_player")) {
            accountDefinitions.add(player)
        }
        player.collision = collisionStrategyProvider.get(character = player)
    }

    fun login(player: Player, client: Client? = null, displayMode: Int = 0) {
        player.interfaces.displayMode = displayMode
        if (client != null) {
            player.viewport = Viewport()
            client.login(player.name, player.index, player.rights.ordinal, membersWorld = World.members)
            player.client = client
            player.interfaces.client = client
            (player.variables as PlayerVariables).client = client
            client.onDisconnecting {
                logout(player, false)
            }
        }
        player.emit(RegionLoad)
        player.emit(Spawn)
        val definitions = get<AreaDefinitions>()
        for (def in definitions.get(player.tile.zone)) {
            if (player.tile in def.area) {
                player.emit(AreaEntered(player, def.name, def.tags, def.area))
            }
        }
    }

    fun logout(player: Player, safely: Boolean) {
        if (player["logged_out", false]) {
            return
        }
        player["logged_out"] = true
        if (safely) {
            player.client?.logout()
            player.strongQueue("logout") {
                // Make sure nothing else starts
            }
        }
        player.client?.disconnect()
        val queue: ConnectionQueue = get()
        queue.disconnect {
            val players: Players = get()
            World.queue("logout", 1) {
                players.remove(player)
                players.removeIndex(player)
                players.releaseIndex(player)
            }
            val definitions = get<AreaDefinitions>()
            for (def in definitions.get(player.tile.zone)) {
                if (player.tile in def.area) {
                    player.emit(AreaExited(player, def.name, def.tags, def.area))
                }
            }
            player.emit(Despawn)
            player.queue.logout()
            player.softTimers.stopAll()
            player.timers.stopAll()
            save(player)
        }
    }
}