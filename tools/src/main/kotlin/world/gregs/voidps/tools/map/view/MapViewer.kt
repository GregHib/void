package world.gregs.voidps.tools.map.view

import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.LafManager.getPreferredThemeStyle
import content.bot.interact.navigation.graph.NavigationGraph
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.update.batch.ZoneBatchUpdates
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.MapDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.CollisionDecoder
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.GameObjectCollisionAdd
import world.gregs.voidps.engine.map.collision.GameObjectCollisionRemove
import world.gregs.voidps.tools.map.view.draw.MapView
import world.gregs.voidps.tools.property
import world.gregs.yaml.Yaml
import java.awt.EventQueue
import javax.swing.JFrame

/**
 * Make sure to run WorldMapDumper.kt first
 */
class MapViewer {

    init {
        EventQueue.invokeLater {
            LafManager.install(LafManager.themeForPreferredStyle(getPreferredThemeStyle()))
            val frame = JFrame("Map viewer")
            frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            val cache = CacheDelegate("./data/cache/")
            val decoder = ObjectDecoder(member = false, lowDetail = false).load(cache)
            val defs = ObjectDefinitions(decoder).load("./data/definitions/objects.toml")
            val areas = AreaDefinitions().load("./data/map/areas.toml")
            val nav = NavigationGraph(defs, areas).load("./data/map/nav-graph.toml")
            val collisions = Collisions()
            if (DISPLAY_AREA_COLLISIONS || DISPLAY_ALL_COLLISIONS) {
                val objectDefinitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).load(cache))
                    .load(property("definitions.objects"))
                val objects = GameObjects(GameObjectCollisionAdd(collisions), GameObjectCollisionRemove(collisions), ZoneBatchUpdates(), objectDefinitions)
                MapDefinitions(CollisionDecoder(collisions), objectDefinitions, objects, cache).loadCache()
            }
            frame.add(MapView(nav, collisions, "./data/map/areas.toml"))
            frame.pack()
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
        }
    }

    companion object {
        const val FILTER_VIEWPORT = true
        const val DISPLAY_ZONES = false
        const val DISPLAY_AREA_COLLISIONS = false
        const val DISPLAY_ALL_COLLISIONS = false

        @JvmStatic
        fun main(args: Array<String>) {
            MapViewer()
        }
    }
}