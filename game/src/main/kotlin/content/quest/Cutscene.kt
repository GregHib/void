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
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.engine.queue.longQueue
import world.gregs.voidps.engine.queue.queue
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
        player.walkTrigger {
            player.queue.clear("${name}_cutscene_end")
            player.queue("${name}_cutscene_end") {
                end(destroyInstance)
            }
        }
        player.longQueue("${name}_cutscene_end", Int.MAX_VALUE) {
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

fun Player.smallInstance(region: Region? = null, levels: Int = 4, logout: Boolean = true, timeout: Int = 0): Region {
    val instance = Instances.small(timeout)
    if (region != null) {
        get<DynamicZones>().copy(region, instance, levels)
        set("instance_offset", instance.offset(region).id)
    }
    set("instance_logout", logout)
    joinInstance(instance)
    return instance
}

fun Player.largeInstance(logout: Boolean = true, timeout: Int = 0): Region {
    val instance = Instances.large(timeout)
    set("instance_logout", logout)
    joinInstance(instance)
    return instance
}

/**
 * Links the player to an allocated [instance] so it can be looked up (e.g. after logging back in)
 */
fun Player.joinInstance(instance: Region) {
    set("instance", instance.id)
    set("instance_key", Instances.key(instance) ?: return)
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
    set("instance_logout_tile", tile.id)
}

fun Player.exitInstance() {
    val tile = instanceOrigin()
    if (clearInstance()) {
        tele(tile)
    }
}

fun Player.instanceOrigin(): Tile = instanceLogout() ?: tile.minus(instanceOffset())

fun Player.instanceLogout(): Tile? {
    val logout: Int = get("instance_logout_tile") ?: return null
    return Tile(logout)
}

/**
 * The instance the player belongs to, or null if it has since been freed
 */
fun Player.instance(): Region? {
    val id: Int = get("instance") ?: return null
    val region = Region(id)
    if (!Instances.reserved(region)) {
        return region
    }
    return if (Instances.valid(region, get<Long>("instance_key"))) region else null
}

/**
 * Unlinks the player from their instance but remembers it as [name], so it can be re-entered
 * with [rejoinInstance] until it's freed.
 */
fun Player.leaveInstance(name: String): Boolean {
    val id: Int = get("instance") ?: return false
    val key: Long? = get("instance_key")
    if (key != null) {
        set("${name}_instance", id)
        set("${name}_instance_key", key)
        set("${name}_instance_offset", instanceOffset().id)
    }
    return clearInstance()
}

/**
 * Re-links the player to the instance previously left with [leaveInstance], if it hasn't been freed.
 */
fun Player.rejoinInstance(name: String): Region? {
    val id: Int = remove("${name}_instance") ?: return null
    val key: Long? = remove("${name}_instance_key")
    val offset: Long? = remove("${name}_instance_offset")
    val region = Region(id)
    if (!Instances.valid(region, key)) {
        return null
    }
    joinInstance(region)
    if (offset != null) {
        set("instance_offset", offset)
    }
    return region
}

/**
 * Unlinks the player from their instance, which is freed automatically once empty
 */
fun Player.clearInstance(): Boolean {
    remove<Int>("instance") ?: return false
    clear("instance_key")
    clear("instance_offset")
    // Only meaningful while inside the instance; leaving it set sends the next instance exit
    // (and any death drop) back to an exit tile that has nothing to do with where the player is.
    clear("instance_logout_tile")
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

/**
 * Starts a cutscene in a private copy of a [width] by [height] block of chunks, the south west
 * corner of which is the chunk [base] sits in. For scenes that only need a room or two rather than
 * a whole region.
 */
fun Player.startCutscene(name: String, base: Tile, width: Int, height: Int, levels: Int = 4): Cutscene {
    val instance = smallInstance()
    return Cutscene(this, name, instance, copyChunks(instance, base, width, height, levels))
}

/**
 * Copies a [width] by [height] block of chunks, starting at the chunk [base] sits in, into
 * [instance], and returns the offset from the original chunks to the copy.
 */
fun Player.copyChunks(instance: Region, base: Tile, width: Int, height: Int, levels: Int = 4): Delta {
    val offset = instance.tile.delta(base.zone.tile)
    get<DynamicZones>().copy(base.zone, instance.tile.zone, width, height, levels)
    set("instance_offset", offset.id)
    return offset
}

fun Player.startCutscene(name: String, region: Region, offset: Delta): Cutscene = Cutscene(this, name, region, offset)
