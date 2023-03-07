package world.gregs.voidps.engine.data

import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.client.variable.contains
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.equipment
import world.gregs.voidps.engine.contain.restrict.ValidItemRestriction
import world.gregs.voidps.engine.contain.stack.DependentOnItem
import world.gregs.voidps.engine.data.definition.extra.*
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOptions
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.network.visual.PlayerVisuals

class PlayerFactory(
    private val store: EventHandlerStore,
    private val interfaces: InterfaceDefinitions,
    private val containerDefs: ContainerDefinitions,
    private val itemDefs: ItemDefinitions,
    private val accountDefinitions: AccountDefinitions,
    private val fileStorage: FileStorage,
    private val path: String,
    private val collisionStrategyProvider: CollisionStrategyProvider,
    private val variableDefinitions: VariableDefinitions,
    private val homeTile: Tile
) : Runnable {

    private val validItems = ValidItemRestriction(itemDefs)

    private val saveQueue = mutableSetOf<Player>()

    private fun path(name: String) = "$path${name}.json"

    override fun run() {
        for (player in saveQueue) {
            save(player.accountName, player)
        }
        saveQueue.clear()
    }

    fun queueSave(player: Player) {
        saveQueue.add(player)
    }

    fun saving(name: String) = saveQueue.any { it.accountName == name }

    fun save(name: String, player: Player) {
        player.variables.data.persist = true
        fileStorage.save(path(name), player)
    }

    fun getOrElse(name: String, index: Int, block: () -> Player): Player {
        val player = fileStorage.loadOrNull(path(name)) ?: block()
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
        store.populate(player)
        player.index = index
        player.visuals = PlayerVisuals(index, player.body)
        player.interfaces = Interfaces(player.events, player.client, interfaces, player.gameFrame)
        player.interfaceOptions = InterfaceOptions(player, interfaces, containerDefs)
        player.options = PlayerOptions(player)
        (player.variables as PlayerVariables).definitions = variableDefinitions
        player.containers.definitions = containerDefs
        player.containers.itemDefinitions = itemDefs
        player.containers.validItemRule = validItems
        player.containers.normalStack = DependentOnItem(itemDefs)
        player.containers.events = player.events
        player.previousTile = player.tile.add(Direction.WEST.delta)
        player.experience.events = player.events
        player.levels.link(player.events, PlayerLevels(player.experience))
        player.body.link(player.equipment)
        player.body.updateAll()
        player.appearance.displayName = player.name
        if (player.contains("new_player")) {
            accountDefinitions.add(player)
        }
        player.collision = collisionStrategyProvider.get(character = player)
    }

}