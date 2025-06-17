package world.gregs.voidps.tools.map.view.interact

import world.gregs.voidps.tools.map.view.draw.WorldMap
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener

class ResizeListener(private val map: WorldMap) : ComponentListener {

    override fun componentResized(e: ComponentEvent?) {
        map.updateView()
    }

    override fun componentMoved(e: ComponentEvent?) {
    }

    override fun componentShown(e: ComponentEvent?) {
    }

    override fun componentHidden(e: ComponentEvent?) {
    }
}
