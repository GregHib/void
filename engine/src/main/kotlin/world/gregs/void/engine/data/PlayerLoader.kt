package world.gregs.void.engine.data

import org.koin.dsl.module
import world.gregs.void.engine.client.ui.InterfaceManager
import world.gregs.void.engine.client.ui.InterfaceOptions
import world.gregs.void.engine.client.ui.PlayerInterfaceIO
import world.gregs.void.engine.client.ui.detail.InterfaceDetails
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerOptions
import world.gregs.void.engine.entity.definition.ContainerDefinitions
import world.gregs.void.engine.event.EventBus
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.map.collision.Collisions
import world.gregs.void.engine.path.strat.FollowTargetStrategy
import world.gregs.void.engine.path.strat.RectangleTargetStrategy
import world.gregs.void.network.codec.game.encode.*
import world.gregs.void.utility.get
import world.gregs.void.utility.getIntProperty

/**
 * @author GregHib <greg@gregs.world>
 * @since April 03, 2020
 */
class PlayerLoader(
    private val bus: EventBus,
    private val interfaces: InterfaceDetails,
    private val collisions: Collisions,
    private val definitions: ContainerDefinitions,
    strategy: StorageStrategy<Player>,
    private val levelEncoder: SkillLevelEncoder,
    private val openEncoder: InterfaceOpenEncoder,
    private val updateEncoder: InterfaceUpdateEncoder,
    private val animationEncoder: InterfaceAnimationEncoder,
    private val closeEncoder: InterfaceCloseEncoder,
    private val playerHeadEncoder: InterfaceHeadPlayerEncoder,
    private val npcHeadEncoder: InterfaceHeadNPCEncoder,
    private val textEncoder: InterfaceTextEncoder,
    private val visibleEncoder: InterfaceVisibilityEncoder,
    private val spriteEncoder: InterfaceSpriteEncoder,
    private val itemEncoder: InterfaceItemEncoder
) : DataLoader<Player>(strategy) {

    private val x = getIntProperty("homeX", 0)
    private val y = getIntProperty("homeY", 0)
    private val plane = getIntProperty("homePlane", 0)
    private val tile = Tile(x, y, plane)

    fun loadPlayer(name: String): Player {
        val player = super.load(name) ?: Player(id = -1, tile = tile)
        val interfaceIO = PlayerInterfaceIO(player, bus, openEncoder, updateEncoder, animationEncoder, closeEncoder, playerHeadEncoder, npcHeadEncoder, textEncoder, visibleEncoder, spriteEncoder, itemEncoder)
        player.interfaces = InterfaceManager(interfaceIO, interfaces, player.gameFrame)
        player.interfaceOptions = InterfaceOptions(player, interfaces, definitions)
        player.experience.addListener { skill, _, experience ->
            val level = player.levels.get(skill)
            levelEncoder.encode(player, skill.ordinal, level, experience.toInt())
        }
        player.interactTarget = RectangleTargetStrategy(collisions, player)
        player.followTarget = FollowTargetStrategy(player)
        player.options = PlayerOptions(player, get())
        return player
    }
}

val playerLoaderModule = module {
    single { PlayerLoader(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}