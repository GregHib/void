package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOptions
import world.gregs.voidps.engine.client.ui.Interfaces
import world.gregs.voidps.engine.client.ui.open
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
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.zone.RegionLoad
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.client.ConnectionQueue
import world.gregs.voidps.network.login.protocol.encode.logout
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class AccountManager(
    private val interfaceDefinitions: InterfaceDefinitions,
    private val inventoryDefinitions: InventoryDefinitions,
    private val itemDefinitions: ItemDefinitions,
    private val accountDefinitions: AccountDefinitions,
    private val collisionStrategyProvider: CollisionStrategyProvider,
    private val variableDefinitions: VariableDefinitions,
    private val saveQueue: SaveQueue,
    private val connectionQueue: ConnectionQueue,
    private val players: Players,
    private val areaDefinitions: AreaDefinitions
) {
    private val validItems = ValidItemRestriction(itemDefinitions)
    private val homeTile: Tile
        get() = Tile(Settings["world.home.x", 0], Settings["world.home.y", 0], Settings["world.home.level", 0])

    fun create(name: String, passwordHash: String): Player {
        return Player(tile = homeTile, accountName = name, passwordHash = passwordHash).apply {
            this["creation"] = System.currentTimeMillis()
            this["new_player"] = true
        }
    }

    fun setup(player: Player): Boolean {
        player.index = players.index() ?: return false
        player.visuals.hits.self = player.index
        player.interfaces = Interfaces(player, player.client, interfaceDefinitions)
        player.interfaceOptions = InterfaceOptions(player, interfaceDefinitions, inventoryDefinitions)
        player.options = PlayerOptions(player)
        (player.variables as PlayerVariables).definitions = variableDefinitions
        player.inventories.definitions = inventoryDefinitions
        player.inventories.itemDefinitions = itemDefinitions
        player.inventories.validItemRule = validItems
        player.inventories.normalStack = ItemDependentStack(itemDefinitions)
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
        return true
    }

    fun spawn(player: Player, client: Client? = null, displayMode: Int = 0) {
        player.interfaces.displayMode = displayMode
        if (client != null) {
            player.viewport = Viewport()
            player.client = client
            player.interfaces.client = client
            (player.variables as PlayerVariables).client = client
            client.onDisconnecting {
                logout(player, false)
            }
        }
        player.emit(RegionLoad)
        player.open(player.interfaces.gameFrame)
        player.emit(Spawn)
        for (def in areaDefinitions.get(player.tile.zone)) {
            if (player.tile in def.area) {
                player.emit(AreaEntered(player, def.name, def.tags, def.area))
            }
        }
    }

    fun logout(player: Player, safely: Boolean) {
        if (player["logged_out", false]) {
            return
        }
        if (player.contains("delay")) {
            player.message("You need to wait a few moments before you can log out.")
            return
        }
        player["logged_out"] = true
        if (safely) {
            player.client?.logout()
            player.strongQueue("logout", onCancel = null) {
                // Make sure nothing else starts
            }
        }
        player.client?.disconnect()
        connectionQueue.disconnect {
            World.queue("logout", 1) {
                players.remove(player)
                players.removeIndex(player)
            }
            for (def in areaDefinitions.get(player.tile.zone)) {
                if (player.tile in def.area) {
                    player.emit(AreaExited(player, def.name, def.tags, def.area))
                }
            }
            player.emit(Despawn)
            player.queue.logout()
            player.softTimers.stopAll()
            player.timers.stopAll()
            saveQueue.save(player)
        }
    }
}