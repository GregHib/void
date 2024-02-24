package world.gregs.voidps.engine.data

import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.variable.PlayerVariables
import world.gregs.voidps.engine.inv.Inventories
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.DependentOnItem
import world.gregs.voidps.engine.data.definition.*
import world.gregs.voidps.engine.data.yaml.PlayerYamlReaderConfig
import world.gregs.voidps.engine.data.yaml.PlayerYamlWriterConfig
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerOptions
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.equip.BodyParts
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.network.visual.PlayerVisuals
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.yaml.Yaml
import java.io.File

class PlayerAccounts(
    private val store: EventHandlerStore,
    private val interfaceDefinitions: InterfaceDefinitions,
    private val inventoryDefinitions: InventoryDefinitions,
    private val itemDefinitions: ItemDefinitions,
    private val accountDefinitions: AccountDefinitions,
    private val yaml: Yaml,
    private val path: String,
    private val collisionStrategyProvider: CollisionStrategyProvider,
    private val variableDefinitions: VariableDefinitions,
    private val homeTile: Tile,
    experienceRate: Double
) : Runnable {

    private val validItems = ValidItemRestriction(itemDefinitions)
    private val writeConfig = PlayerYamlWriterConfig()
    private val readerConfig = PlayerYamlReaderConfig(itemDefinitions, experienceRate)
    private val saveQueue = mutableMapOf<String, PlayerSave>()

    private fun path(name: String) = "$path${name}.json"

    override fun run() {
        for ((name, player) in saveQueue) {
            save(name, player)
        }
        saveQueue.clear()
    }

    fun queueSave(player: Player) {
        saveQueue[player.accountName] = player.copy()
    }

    fun saving(name: String) = saveQueue.containsKey(name)

    private fun save(name: String, player: PlayerSave) {
        yaml.save(path(name), player, writeConfig)
    }

    @Suppress("UNCHECKED_CAST")
    fun getOrElse(name: String, index: Int, block: () -> Player): Player {
        val file = File(path(name))
        val player = if (file.exists()) {
            val map: Map<String, Any> = Yaml().load(file.path, readerConfig)
            Player(
                accountName = map["accountName"] as String,
                passwordHash = map["passwordHash"] as String,
                tile = map["tile"] as Tile,
                experience = map["experience"] as Experience,
                levels = map["levels"] as Levels,
                body = BodyParts(map["male"] as Boolean, map["looks"] as IntArray, map["colours"] as IntArray),
                variables = map["variables"] as MutableMap<String, Any>,
                inventories = Inventories((map["inventories"] as MutableMap<String, List<Item>>).mapValues { (_, value: List<Item>) ->
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
        player.interfaces = Interfaces(player.events, player.client, interfaceDefinitions)
        player.interfaceOptions = InterfaceOptions(player, interfaceDefinitions, inventoryDefinitions)
        player.options = PlayerOptions(player)
        (player.variables as PlayerVariables).definitions = variableDefinitions
        player.inventories.definitions = inventoryDefinitions
        player.inventories.itemDefinitions = itemDefinitions
        player.inventories.validItemRule = validItems
        player.inventories.normalStack = DependentOnItem(itemDefinitions)
        player.inventories.events = player.events
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