package world.gregs.voidps.engine.data

import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.client.variable.contains
import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.contain.Containers
import world.gregs.voidps.engine.contain.equipment
import world.gregs.voidps.engine.contain.restrict.ValidItemRestriction
import world.gregs.voidps.engine.contain.stack.DependentOnItem
import world.gregs.voidps.engine.data.definition.extra.*
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.equip.BodyParts
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration
import world.gregs.yaml.write.YamlWriterConfiguration
import java.io.File

@Suppress("UNCHECKED_CAST")
class PlayerFactory(
    private val store: EventHandlerStore,
    private val interfaceDefinitions: InterfaceDefinitions,
    private val containerDefinitions: ContainerDefinitions,
    private val itemDefinitions: ItemDefinitions,
    private val accountDefinitions: AccountDefinitions,
    private val yaml: Yaml,
    private val path: String,
    private val collisionStrategyProvider: CollisionStrategyProvider,
    private val variableDefinitions: VariableDefinitions,
    private val homeTile: Tile
) : Runnable {

    private val validItems = ValidItemRestriction(itemDefinitions)

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
        yaml.save(path(name), player, playerWriter)
    }

    private val playerWriter = object : YamlWriterConfiguration(quoteStrings = true, forceExplicit = true, quoteKeys = true, formatExplicitMap = true) {
        override fun write(value: Any?, indent: Int, parentMap: String?): Any? {
            return if (value is Item) {
                if (value.isEmpty()) {
                    emptyMap()
                } else {
                    val map = mutableMapOf<String, Any>("id" to value.id)
                    if (value.amount != 0) {
                        map["amount"] = value.amount
                    }
                    map
                }
            } else if (value is Player) {
                mapOf(
                    "accountName" to value.accountName,
                    "passwordHash" to value.passwordHash,
                    "tile" to mapOf(
                        "x" to value.tile.x,
                        "y" to value.tile.y,
                        "plane" to value.tile.plane,
                    ),
                    "experience" to mapOf(
                        "experience" to value.experience.experience,
                        "blocked" to value.experience.blocked
                    ),
                    "levels" to value.levels.levels,
                    "male" to value.male,
                    "looks" to value.body.looks,
                    "colours" to value.body.colours,
                    "variables" to value.variables.data,
                    "containers" to value.containers.containers,
                    "friends" to value.friends,
                    "ignores" to value.ignores
                )
            } else {
                super.write(value, indent, parentMap)
            }
        }
    }
    private val playerReader = object : YamlReaderConfiguration() {
        override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
            if (value is Map<*, *> && value.containsKey("id")) {
                val id = value["id"] as String
                val item = Item(id, value["amount"] as? Int ?: 0, itemDefinitions.get(id))
                super.add(list, item, parentMap)
            } else if (value is Map<*, *> && value.isEmpty()) {
                super.add(list, Item.EMPTY, parentMap)
            } else {
                super.add(list, when (parentMap) {
                    "blocked" -> Skill.valueOf(value as String)
                    "friends" -> ClanRank.valueOf(value as String)
                    else -> value
                }, parentMap)
            }
        }

        override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
            if (parentMap == "tile") {
                super.set(map, key, Tile.fromMap(value as Map<String, Any>), indent, parentMap)
            } else if (key == "experience" && value is Map<*, *>) {
                value as Map<String, Any>
                val exp = Experience(
                    experience = (value["experience"] as List<Double>).toDoubleArray(),
                    blocked = (value["blocked"] as List<Skill>).toMutableSet()
                )
                super.set(map, key, exp, indent, parentMap)
            } else if (key == "levels") {
                value as List<Int>
                super.set(map, key, Levels(value.toIntArray()), indent, parentMap)
            } else if (key == "looks" || key == "colours") {
                value as List<Int>
                super.set(map, key, value.toIntArray(), indent, parentMap)
            } else {
                super.set(map, key, value, indent, parentMap)
            }
        }
    }

    fun getOrElse(name: String, index: Int, block: () -> Player): Player {
        val file = File(path(name))
        val player = if (file.exists()) {
            val map: Map<String, Any> = yaml.load(file.path, playerReader)
            Player(
                accountName = map["accountName"] as String,
                passwordHash = map["passwordHash"] as String,
                tile = map["tile"] as Tile,
                experience = map["experience"] as Experience,
                levels = map["levels"] as Levels,
                body = BodyParts(map["male"] as Boolean, map["looks"] as IntArray, map["colours"] as IntArray),
                variables = map["variables"] as MutableMap<String, Any>,
                containers = Containers((map["containers"] as MutableMap<String, List<Item>>).mapValues { (_, value: List<Item>) ->
                    value.toTypedArray()
                }.toMutableMap()),
                friends = map["friends"] as MutableMap<String, ClanRank>,
                ignores = map["ignores"] as MutableList<String>
            )
        } else {
            block()
        }
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
        player.interfaces = Interfaces(player.events, player.client, interfaceDefinitions, player.gameFrame)
        player.interfaceOptions = InterfaceOptions(player, interfaceDefinitions, containerDefinitions)
        player.options = PlayerOptions(player)
        (player.variables as PlayerVariables).definitions = variableDefinitions
        player.containers.definitions = containerDefinitions
        player.containers.itemDefinitions = itemDefinitions
        player.containers.validItemRule = validItems
        player.containers.normalStack = DependentOnItem(itemDefinitions)
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