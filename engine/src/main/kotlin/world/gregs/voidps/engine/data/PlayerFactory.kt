package world.gregs.voidps.engine.data

import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOptions
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.strat.FollowTargetStrategy
import world.gregs.voidps.engine.path.strat.RectangleTargetStrategy
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.engine.utility.getIntProperty

class PlayerFactory(
    private val store: EventHandlerStore,
    private val interfaces: InterfaceDefinitions,
    private val collisions: Collisions,
    private val containerDefs: ContainerDefinitions,
    private val accountDefinitions: AccountDefinitions,
    private val fileStorage: FileStorage,
    private val path: String,
    private val collisionStrategyProvider: CollisionStrategyProvider
) {

    private val x = getIntProperty("homeX", 0)
    private val y = getIntProperty("homeY", 0)
    private val plane = getIntProperty("homePlane", 0)
    private val tile = Tile(x, y, plane)

    private fun path(name: String) = "$path${name}.json"

    fun save(name: String, player: Player) {
        fileStorage.save(path(name), player)
    }

    fun getOrElse(name: String, index: Int, block: () -> Player): Player {
        val player = fileStorage.loadOrNull(path(name)) ?: block()
        initPlayer(player, index)
        return player
    }

    fun create(name: String, password: String): Player {
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        return Player(tile = tile, accountName = name, passwordHash = hash).apply {
            this["creation", true] = System.currentTimeMillis()
            this["new_player"] = true
        }
    }

    fun initPlayer(player: Player, index: Int) {
        store.populate(player)
        player.index = index
        player.interfaces = Interfaces(player.events, player.client, interfaces, player.gameFrame)
        player.interfaceOptions = InterfaceOptions(player, interfaces, containerDefs)
        player.options = PlayerOptions(player)
        player.start()
        player.appearance.displayName = player.name
        if (player.contains("new_player")) {
            accountDefinitions.add(player)
        }
        player.interactTarget = RectangleTargetStrategy(collisions, player, allowUnder = false)
        player.followTarget = FollowTargetStrategy(player)
        player.collision = collisionStrategyProvider.get(character = player)
        player.traversal = SmallTraversal
    }

}