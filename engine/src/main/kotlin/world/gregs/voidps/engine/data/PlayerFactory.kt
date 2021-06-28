package world.gregs.voidps.engine.data

import org.koin.dsl.module
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.Died
import world.gregs.voidps.engine.entity.character.contain.ContainerUpdate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOptions
import world.gregs.voidps.engine.entity.character.player.skill.GrantExp
import world.gregs.voidps.engine.entity.character.player.skill.LevelChanged
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.TraversalType
import world.gregs.voidps.engine.path.strat.FollowTargetStrategy
import world.gregs.voidps.engine.path.strat.RectangleTargetStrategy
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.network.encode.sendInterfaceItemUpdate
import world.gregs.voidps.network.encode.skillLevel
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.getIntProperty

class PlayerFactory(
    private val store: EventHandlerStore,
    private val interfaces: InterfaceDefinitions,
    private val collisions: Collisions,
    private val containerDefs: ContainerDefinitions,
    private val itemDefs: ItemDefinitions,
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
        store.populate(player)
        player.index = index
        player.interfaces = Interfaces(player.events, player.client, interfaces, player.gameFrame)
        player.interfaceOptions = InterfaceOptions(player, interfaces, containerDefs)
        player.options = PlayerOptions(player)
        player.appearance.displayName = player.name
        player.start()
        player.events.on<Player, ContainerUpdate> {
            player.sendInterfaceItemUpdate(
                key = containerId,
                updates = updates.map { Triple(it.index, itemDefs.getIdOrNull(it.item.name) ?: -1, it.item.amount) },
                primary = secondary
            )
        }
        player.events.on<Player, GrantExp> {
            val level = player.levels.get(skill)
            player.client?.skillLevel(skill.ordinal, if (skill == Skill.Constitution) level / 10 else level, to.toInt())
        }
        player.events.on<Player, LevelChanged> {
            val exp = player.experience.get(skill)
            player.client?.skillLevel(skill.ordinal, if (skill == Skill.Constitution) to / 10 else to, exp.toInt())
            if (skill == Skill.Constitution) {
                player.setVar("life_points", player.levels.get(Skill.Constitution))
                if (to <= 0) {
                    player.events.emit(Died)
                }
            }
        }
        player.interactTarget = RectangleTargetStrategy(collisions, player)
        player.followTarget = FollowTargetStrategy(player)
        player.movement.traversal = small
    }

}

val playerLoaderModule = module {
    single { PlayerFactory(get(), get(), get(), get(), get(), get()) }
}