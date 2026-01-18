package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import world.gregs.config.Config
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.area.Cuboid
import world.gregs.voidps.type.area.Polygon
import world.gregs.voidps.type.area.Rectangle

object Areas {

    private var named: Map<String, AreaDefinition> = Object2ObjectOpenHashMap()
    private var tagged: Map<String, Set<AreaDefinition>> = Object2ObjectOpenHashMap()
    private var areas: Map<Int, Set<AreaDefinition>> = Int2ObjectOpenHashMap()

    val names: Set<String>
        get() = named.keys

    fun getAll() = named.values

    fun getOrNull(name: String): AreaDefinition? = named[name]

    operator fun get(name: String): Area = named[name]?.area ?: AreaDefinition.EMPTY.area

    fun get(zone: Zone): Set<AreaDefinition> = areas[zone.id] ?: emptySet()

    fun tagged(tag: String): Set<AreaDefinition> = tagged[tag] ?: emptySet()

    fun load(paths: List<String>): Areas {
        timedLoad("map area") {
            val named = Object2ObjectOpenHashMap<String, AreaDefinition>()
            val tagged = Object2ObjectOpenHashMap<String, MutableSet<AreaDefinition>>()
            val areas = Int2ObjectOpenHashMap<MutableSet<AreaDefinition>>()
            for (path in paths) {
                Config.fileReader(path) {
                    val x = IntArrayList()
                    val y = IntArrayList()
                    while (nextSection()) {
                        val name = section()
                        x.clear()
                        y.clear()
                        var level: Int? = null
                        val tags = ObjectOpenHashSet<String>()
                        val extras = Object2ObjectOpenHashMap<String, Any>(0, Hash.VERY_FAST_LOAD_FACTOR)
                        while (nextPair()) {
                            when (val key = key()) {
                                "x" -> while (nextElement()) {
                                    x.add(int())
                                }
                                "y" -> while (nextElement()) {
                                    y.add(int())
                                }
                                "level" -> level = int()
                                "tags" -> while (nextElement()) {
                                    tags.add(string())
                                }
                                else -> extras[key] = value()
                            }
                        }
                        val area: Area = if (x.size <= 2) {
                            if (level == null) {
                                Rectangle(x.min(), y.min(), x.max(), y.max())
                            } else {
                                Cuboid(x.min(), y.min(), x.max(), y.max(), level, level)
                            }
                        } else {
                            Polygon(x.toIntArray(), y.toIntArray(), level ?: 0, level ?: 3)
                        }
                        val definition = if (extras.isEmpty()) {
                            AreaDefinition(name = name, area = area, tags = tags, stringId = name)
                        } else {
                            AreaDefinition(name = name, area = area, tags = tags, stringId = name, extras = extras)
                        }
                        named[name] = definition
                        for (tag in tags) {
                            tagged.getOrPut(tag) { ObjectOpenHashSet(2) }.add(definition)
                        }
                        if (level != null) {
                            for (zone in area.toZones(level)) {
                                areas.getOrPut(zone.id) { ObjectOpenHashSet(2) }.add(definition)
                            }
                        } else {
                            for (lvl in 0..3) {
                                for (zone in area.toZones(lvl)) {
                                    areas.getOrPut(zone.id) { ObjectOpenHashSet(2) }.add(definition)
                                }
                            }
                        }
                    }
                }
            }
            this.named = named
            this.areas = areas
            this.tagged = tagged
            named.size
        }
        return this
    }

    internal fun set(named: Map<String, AreaDefinition>, tagged: Map<String, Set<AreaDefinition>>, areas: Map<Int, Set<AreaDefinition>>) {
        this.named = named
        this.tagged = tagged
        this.areas = areas
    }

    fun clear() {
        named = Object2ObjectOpenHashMap()
        tagged = Object2ObjectOpenHashMap()
        areas = Int2ObjectOpenHashMap()
    }
}
