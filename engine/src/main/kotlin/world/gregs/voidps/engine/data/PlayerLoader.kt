package world.gregs.voidps.engine.data

import org.koin.dsl.module
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.ui.InterfaceManager
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.PlayerInterfaceIO
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetails
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOptions
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.event.EventBus
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.TraversalType
import world.gregs.voidps.engine.path.strat.FollowTargetStrategy
import world.gregs.voidps.engine.path.strat.RectangleTargetStrategy
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.network.encode.skillLevel
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getIntProperty

/**
 * @author GregHib <greg@gregs.world>
 * @since April 03, 2020
 */
class PlayerLoader(
    private val bus: EventBus,
    private val interfaces: InterfaceDetails,
    private val collisions: Collisions,
    private val definitions: ContainerDefinitions,
    strategy: StorageStrategy<Player>
) : DataLoader<Player>(strategy) {

    private val small = SmallTraversal(TraversalType.Land, false, get())
    private val x = getIntProperty("homeX", 0)
    private val y = getIntProperty("homeY", 0)
    private val plane = getIntProperty("homePlane", 0)
    private val tile = Tile(x, y, plane)

    fun create(name: String, password: String): Player {
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        return Player(id = -1, tile = tile, name = name, passwordHash = hash)
    }

    fun initPlayer(player: Player, index: Int) {
        player.index = index
        val interfaceIO = PlayerInterfaceIO(player, bus)
        player.interfaces = InterfaceManager(interfaceIO, interfaces, player.gameFrame)
        player.interfaceOptions = InterfaceOptions(player, interfaces, definitions)
        player.options = PlayerOptions(player)
        player.appearance.displayName = player.name
        player.start()
        player.experience.addListener { skill, _, experience ->
            val level = player.levels.get(skill)
            player.client?.skillLevel(skill.ordinal, level, experience.toInt())
        }
        player.interactTarget = RectangleTargetStrategy(collisions, player)
        player.followTarget = FollowTargetStrategy(player)
        player.movement.traversal = small
    }

}

val playerLoaderModule = module {
    single { PlayerLoader(get(), get(), get(), get(), get()) }
}