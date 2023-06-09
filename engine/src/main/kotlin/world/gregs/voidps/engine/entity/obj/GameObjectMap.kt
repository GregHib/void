package world.gregs.voidps.engine.entity.obj

/**
 * Stores [GameObject]s by zone + group
 */
interface GameObjectMap {

    operator fun get(obj: GameObject): Int

    operator fun get(x: Int, y: Int, level: Int, group: Int): Int

    operator fun set(zone: Int, tile: Int, mask: Int)

    operator fun set(x: Int, y: Int, level: Int, group: Int, mask: Int)

    fun add(obj: GameObject, mask: Int)

    fun remove(obj: GameObject, mask: Int)

    fun deallocateZone(zoneX: Int, zoneY: Int, level: Int)

    fun clear()
}