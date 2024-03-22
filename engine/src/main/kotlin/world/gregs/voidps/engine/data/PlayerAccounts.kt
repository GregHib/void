package world.gregs.voidps.engine.data

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOptions
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.DependentOnItem
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import kotlin.coroutines.CoroutineContext

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
    private val saveQueue = mutableMapOf<String, PlayerSave>()
    private val logger = InlineLogger()

    override fun run() {
        if (saveQueue.isEmpty()) {
            return
        }
        val accounts = saveQueue.values.toList()
        saveQueue.clear()
        launch {
            try {
                storage.save(accounts)
            } catch (e: Exception) {
                logger.error(e) { "Error saving players!" }
            }
        }
    }

    fun queueSave(player: Player) {
        if (player.contains("bot")) {
            return
        }
        saveQueue[player.accountName] = player.copy()
    }

    fun saving(name: String) = saveQueue.containsKey(name)

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

}