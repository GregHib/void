package world.gregs.voidps.engine.entity.obj

import org.koin.mp.KoinPlatformTools
import world.gregs.voidps.cache.definition.data.ObjectDefinition
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.get
import world.gregs.voidps.network.login.protocol.encode.zone.ObjectAnimation
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Tile

/**
 * Interactive Object
 */
@JvmInline
value class GameObject(internal val packed: Long) : Entity {

    constructor(id: Int, x: Int, y: Int, level: Int, shape: Int, rotation: Int) : this(pack(id, x, y, level, shape, rotation))

    val id: String
        get() = def.stringId
    override var tile: Tile
        get() = Tile(x, y, level)
        set(value) {}
    val width: Int
        get() = if (rotation and 0x1 == 1) def.sizeY else def.sizeX
    val height: Int
        get() = if (rotation and 0x1 == 1) def.sizeX else def.sizeY
    val def: ObjectDefinition
        get() = ObjectDefinitions.get(intId)

    val intId: Int
        get() = id(packed)
    val x: Int
        get() = x(packed)
    val y: Int
        get() = y(packed)
    val level: Int
        get() = level(packed)
    val shape: Int
        get() = shape(packed)
    val rotation: Int
        get() = rotation(packed)

    fun anim(id: String) = get<ZoneBatchUpdates>()
        .add(tile.zone, ObjectAnimation(tile.id, get<AnimationDefinitions>().get(id).id, shape, rotation))

    fun nearestTo(tile: Tile) = Tile(
        x = Distance.getNearest(x, width, tile.x),
        y = Distance.getNearest(y, height, tile.y),
        level = level,
    )

    override fun toString(): String = if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        "GameObject(intId=$intId, tile=$tile, shape=$shape, rotation=$rotation)"
    } else {
        "GameObject(id=$id, intId=$intId, tile=$tile, shape=$shape, rotation=$rotation)"
    }

    fun def(player: Player): ObjectDefinition = ObjectDefinitions.resolve(def, player)

    companion object {
        operator fun invoke(id: Int, tile: Tile, shape: Int, rotation: Int): GameObject = GameObject(id, tile.x, tile.y, tile.level, shape, rotation)

        internal fun pack(id: Int, x: Int, y: Int, level: Int, shape: Int, rotation: Int): Long = pack(id.toLong(), x.toLong(), y.toLong(), level.toLong(), shape.toLong(), rotation.toLong())

        private fun pack(id: Long, x: Long, y: Long, level: Long, shape: Long, rotation: Long): Long = y or (x shl 14) or (level shl 28) or (rotation shl 30) or (shape shl 32) or (id shl 37)

        fun id(packed: Long): Int = (packed shr 37 and 0x1ffff).toInt()
        fun x(packed: Long): Int = (packed shr 14 and 0x3fff).toInt()
        fun y(packed: Long): Int = (packed and 0x3fff).toInt()
        fun level(packed: Long): Int = (packed shr 28 and 0x3).toInt()
        fun shape(packed: Long): Int = (packed shr 32 and 0x1f).toInt()
        fun rotation(packed: Long): Int = (packed shr 30 and 0x3).toInt()
    }
}
