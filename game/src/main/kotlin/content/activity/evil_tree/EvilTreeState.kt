package content.activity.evil_tree

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.type.Tile

/**
 * State of the single evil tree the world currently has, if any.
 */
object EvilTreeState {

    var place: String = ""
    var type: String = "normal"
    var spawnTile: Tile = Tile.EMPTY
    var tree: GameObject = GameObject(0)
    var leprechaun: NPC = NPC()
    var health: Int = 0
    var maxHealth: Int = 0
    var growth: Int = 0
    var grownTick: Long = NOT_GROWN
    var strikes: Int = 0
    var dead: Boolean = false
    var spawnId: Int = 0
    var growthDelay: Int = 0
    var rootDelay: Int = 0
    var deathDelay: Int = 0
    var respawnTicks: Int = 0
    val roots = mutableMapOf<String, Root>()
    val fires = mutableMapOf<String, GameObject>()

    val active: Boolean
        get() = spawnTile != Tile.EMPTY

    val grown: Boolean
        get() = grownTick != NOT_GROWN

    /**
     * A fully grown tree that hasn't been cut down yet.
     */
    val alive: Boolean
        get() = grown && !dead

    val sapling: Boolean
        get() = active && !grown && !dead

    /**
     * Centre of the three by three tree, where the one by one sapling stages sit.
     */
    val centre: Tile
        get() = spawnTile.add(1, 1)

    /**
     * Whether [obj] is the current evil tree, at any stage of its growth.
     */
    fun isTree(obj: GameObject): Boolean = active && (obj.tile == spawnTile || obj.tile == centre)

    fun reset() {
        place = ""
        type = "normal"
        spawnTile = Tile.EMPTY
        tree = GameObject(0)
        leprechaun = NPC()
        health = 0
        maxHealth = 0
        growth = 0
        grownTick = NOT_GROWN
        strikes = 0
        dead = false
        growthDelay = 0
        rootDelay = 0
        deathDelay = 0
        roots.clear()
        fires.clear()
    }

    class Root(var obj: GameObject, var life: Int)

    const val NOT_GROWN = -1L
}

/**
 * Whether the player still has "evil tree magic" left over from an evil tree reward.
 */
val Player.evilTreeMagic: Boolean
    get() = this["evil_tree_buff", 0] > 0
