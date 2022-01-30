package world.gregs.voidps.engine.data

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.sendInterfaceItemUpdate
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.Death
import world.gregs.voidps.engine.entity.character.contain.ContainerUpdate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOptions
import world.gregs.voidps.engine.entity.character.player.skill.CurrentLevelChanged
import world.gregs.voidps.engine.entity.character.player.skill.GrantExp
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.contains
import world.gregs.voidps.engine.entity.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.definition.ContainerDefinitions
import world.gregs.voidps.engine.entity.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.path.strat.FollowTargetStrategy
import world.gregs.voidps.engine.path.strat.RectangleTargetStrategy
import world.gregs.voidps.engine.utility.getIntProperty
import world.gregs.voidps.network.encode.skillLevel

class PlayerFactory(
    private val store: EventHandlerStore,
    private val interfaces: InterfaceDefinitions,
    private val collisions: Collisions,
    private val containerDefs: ContainerDefinitions,
    private val itemDefs: ItemDefinitions,
    private val accountDefinitions: AccountDefinitions,
    private val fileStorage: FileStorage,
    private val path: String
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
        player.appearance.displayName = player.name
        player.start()
        if (player.contains("new_player")) {
            accountDefinitions.add(player)
        }
        player.events.on<Player, ContainerUpdate> {
            player.sendInterfaceItemUpdate(
                key = containerDefs.get(container).id,
                updates = updates.map { Triple(it.index, itemDefs.getOrNull(it.item.id)?.id ?: -1, it.item.amount) },
                secondary = secondary
            )
        }
        player.events.on<Player, GrantExp> {
            val level = player.levels.get(skill)
            player.client?.skillLevel(skill.ordinal, if (skill == Skill.Constitution) level / 10 else level, to.toInt())
        }
        player.events.on<Player, CurrentLevelChanged> {
            val exp = player.experience.get(skill)
            player.client?.skillLevel(skill.ordinal, if (skill == Skill.Constitution) to / 10 else to, exp.toInt())
            if (skill == Skill.Constitution) {
                player.setVar("life_points", player.levels.get(Skill.Constitution))
                if (to <= 0) {
                    player.events.emit(Death)
                }
            }
        }
        player.interactTarget = RectangleTargetStrategy(collisions, player, allowUnder = false)
        player.followTarget = FollowTargetStrategy(player)
    }

}

val playerLoaderModule = module {
    single { PlayerFactory(get(), get(), get(), get(), get(), get(), get(named("jsonStorage")), getProperty("savePath")) }
}