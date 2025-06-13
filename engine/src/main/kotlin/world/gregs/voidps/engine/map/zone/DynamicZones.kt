package world.gregs.voidps.engine.map.zone

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import world.gregs.voidps.engine.data.definition.MapDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.clear
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Zone
import java.util.*
import kotlin.collections.set

class DynamicZones(
    private val objects: GameObjects,
    private val collisions: Collisions,
    private val extract: MapDefinitions,
) {
    private val zones: MutableMap<Int, Int> = Int2IntArrayMap()
    private val regions = IntOpenHashSet()

    fun isDynamic(region: Region) = regions.contains(region.id)

    fun getDynamicZone(zone: Zone) = zones[zone.id]

    /**
     * @param from The zone to be copied
     * @param to The zone things will be copied to
     */
    fun copy(from: Zone, to: Zone = from, rotation: Int = 0) {
        zones[to.id] = from.rotatedId(rotation)
        update(from, to, rotation, true)
    }

    /**
     * @param from The region to be copied
     * @param to The region to be replaced
     */
    fun copy(from: Region, to: Region) {
        val targetZones = LinkedList(to.toCuboid().toZones())
        for (zone in from.toCuboid().toZones()) {
            copy(zone, targetZones.poll())
        }
    }

    /**
     * Clear the dynamic [zone] and replace it with the original
     */
    fun clear(zone: Zone) {
        zones.remove(zone.id)
        update(zone, zone, 0, false)
    }

    /**
     * Clear the dynamic [region] and replace it with the original
     */
    fun clear(region: Region) {
        for (zone in region.toCuboid().toZones()) {
            objects.clear(zone)
            collisions.clear(zone)
            extract.loadZone(zone, zone, 0)
            zones.remove(zone.id)
        }
        regions.remove(region.id)
        World.emit(ClearRegion(region))
    }

    private fun update(from: Zone, to: Zone, rotation: Int, set: Boolean) {
        objects.reset(to)
        collisions.clear(to)
        extract.loadZone(from, to, rotation)
        for (region in to.toCuboid(radius = 3).toRegions()) {
            if (set) {
                regions.add(region.id)
            } else if (region.toRectangle().toZones().none { zones.containsKey(it.id) }) {
                regions.remove(region.id)
            }
        }
        World.emit(ReloadZone(to))
    }

    companion object {

        fun Zone.dynamicId() = toZonePosition(x, y, level)

        fun Zone.rotatedId(rotation: Int) = toRotatedZonePosition(
            x,
            y,
            level,
            rotation,
        )

        fun getZone(id: Int) = Zone(x(id), y(id), level(id))

        private fun x(id: Int) = id shr 14 and 0x7ff
        private fun y(id: Int) = id shr 3 and 0x7ff
        private fun level(id: Int) = id shr 28 and 0x7ff

        private fun toZonePosition(zoneX: Int, zoneY: Int, level: Int): Int = zoneY + (zoneX shl 14) + (level shl 28)

        private fun toRotatedZonePosition(zoneX: Int, zoneY: Int, level: Int, rotation: Int): Int = rotation shl 1 or (level shl 24) or (zoneX shl 14) or (zoneY shl 3)
    }
}
