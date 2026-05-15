package content.quest

import content.entity.player.modal.Tab
import content.quest.Cutscene.Companion.tabs
import content.skill.magic.spell.spellBook
import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.engine.client.Minimap
import world.gregs.voidps.engine.client.clearMinimap
import world.gregs.voidps.engine.client.minimap
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.clear
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.engine.queue.longQueue
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
    val instance: Region,
    val offset: Delta,
) {

    constructor(player: Player, name: String, region: Region? = null, levels: Int = 4) : this(player, name, player.smallInstance(region, levels), player.instanceOffset())

    var block: (suspend () -> Unit)? = null

    init {
        hideTabs()
    }

    fun onEnd(destroyInstance: Boolean = true, block: suspend () -> Unit) {
        player.longQueue("${name}_cutscene_end", 1) {
            end(destroyInstance)
        }
        this@Cutscene.block = block
    }

    fun tile(x: Int, y: Int, level: Int = 0): Tile = Tile(x + offset.x, y + offset.y, level + offset.level)

    fun convert(tile: Tile): Tile = tile.add(offset)

    fun original(tile: Tile): Tile = tile.minus(offset)

    private var end = false

    suspend fun end(destroyInstance: Boolean = true, invokeEnd: Boolean = true) {
        if (!end) {
            end = true
            if (invokeEnd) {
                block?.invoke()
            }
            if (destroyInstance) {
                player.clearInstance()
            }
            player.open("fade_in")
            showTabs()
            player.queue.clear("${name}_cutscene_end")
        }
    }

    fun showTabs() {
        player.openTabs()
        player.clearMinimap()
    }

    fun hideTabs() {
        player.closeTabs()
        player.minimap(Minimap.HideMap)
    }

    companion object {
        val tabs = listOf(
            "combat_styles",
            "task_system",
            "stats",
            "quest_journals",
            "inventory",
            "worn_equipment",
            "prayer_list",
            "emotes",
            "notes",
        )
    }
}

fun Player.smallInstance(region: Region? = null, levels: Int = 4): Region {
    val instance = Instances.small()
    if (region != null) {
        get<DynamicZones>().copy(region, instance, levels)
        set("instance_offset", instance.offset(region).id)
    }
    set("instance", instance.id)
    return instance
}

fun Player.largeInstance(): Region {
    val instance = Instances.large()
    set("instance", instance.id)
    return instance
}

/**
 * Delta between original and instance
 * Add to convert to instance
 * Minus to convert to original
 */
fun Player.instanceOffset(): Delta {
    val id: Long = get("instance_offset") ?: return Delta.EMPTY
    return Delta(id)
}

fun Player.setInstanceLogout(tile: Tile) {
    set("instance_logout", tile.id)
}

fun Player.exitInstance() {
    val tile = instanceOrigin()
    if (clearInstance()) {
        tele(tile)
    }
}

fun Player.instanceOrigin(): Tile = instanceLogout() ?: tile.minus(instanceOffset())

fun Player.instanceLogout(): Tile? {
    val logout: Int = get("instance_logout") ?: return null
    return Tile(logout)
}

fun Player.instance(): Region? {
    val id: Int = get("instance") ?: return null
    return Region(id)
}

fun Player.clearInstance(): Boolean {
    val id: Int = remove("instance") ?: return false
    clear("instance_offset")
    val region = Region(id)
    Instances.free(region)
    get<DynamicZones>().clear(region)
    val regionLevel = Region(id).toLevel(0)
    NPCs.clear(regionLevel)
    for (zone in regionLevel.toCuboid().toZones()) {
        GameObjects.clear(zone)
        Collisions.clear(zone)
    }
    return true
}

fun Player.openTabs(vararg others: Tab) {
    for (tab in tabs) {
        open(tab)
    }
    for (other in others) {
        open(other.name.toSnakeCase())
    }
    open(get("spell_book", "modern_spellbook"))
}

fun Player.closeTabs(vararg others: Tab) {
    for (tab in tabs) {
        close(tab)
    }
    set("spell_book", spellBook)
    for (other in others) {
        close(other.name.toSnakeCase())
    }
    close(spellBook)
}

fun Player.startCutscene(name: String, region: Region = Region.EMPTY): Cutscene = Cutscene(this, name, region)
fun Player.startCutscene(name: String, region: Region, offset: Delta): Cutscene = Cutscene(this, name, region, offset)
