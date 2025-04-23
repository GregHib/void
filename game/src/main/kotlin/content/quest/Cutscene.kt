package content.quest

import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.clear
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.engine.queue.LogoutBehaviour
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile

/**
 * Creates a dynamic region instance, handling deletion on completion or disconnection.
 * Provides helper functions to reference the relative [tile] coordinates.
 */
class Cutscene(
    private val player: Player,
    val name: String,
    region: Region,
) {
    val instance: Region = Instances.small()
    val offset: Delta
    var block: (suspend SuspendableContext<Player>.() -> Unit)? = null

    init {
        get<DynamicZones>().copy(region, instance)
        offset = instance.offset(region)
        hideTabs()
    }

    fun onEnd(block: suspend SuspendableContext<Player>.() -> Unit) {
        player.queue("${name}_cutscene_end", 1, LogoutBehaviour.Accelerate) {
            block.invoke(this)
            end(this)
        }
        this@Cutscene.block = block
    }

    fun tile(x: Int, y: Int, level: Int = 0): Tile {
        return Tile(x + offset.x, y + offset.y, level + offset.level)
    }

    fun convert(tile: Tile): Tile {
        return tile.add(offset)
    }

    fun original(tile: Tile): Tile {
        return tile.minus(offset)
    }

    private var end = false

    suspend fun end(context: SuspendableContext<Player>) {
        if (!end) {
            end = true
            destroy()
            block?.invoke(context)
            player.open("fade_in")
            showTabs()
        }
    }

    fun destroy() {
        Instances.free(instance)
        get<DynamicZones>().clear(instance)
        val regionLevel = instance.toLevel(0)
        get<NPCs>().clear(regionLevel)
        val objects = get<GameObjects>()
        val collisions = get<Collisions>()
        for (zone in regionLevel.toCuboid().toZones()) {
            objects.clear(zone)
            collisions.clear(zone)
        }
    }

    fun showTabs() {
        tabs.forEach {
            player.open(it)
        }
        player.clearMinimap()
    }

    fun hideTabs() {
        tabs.forEach {
            player.close(it)
        }
        player.minimap(Minimap.HideMap)
    }

    companion object {
        private val tabs = listOf(
            "combat_styles",
            "task_system",
            "stats",
            "quest_journals",
            "inventory",
            "worn_equipment",
            "prayer_list",
            "modern_spellbook",
            "emotes",
            "notes"
        )
    }
}

fun Context<Player>.startCutscene(name: String, region: Region): Cutscene {
    return Cutscene(player, name, region)
}