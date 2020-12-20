package rs.dusk.tools.map.view.ui

import rs.dusk.engine.map.region.Region
import rs.dusk.tools.map.view.draw.MapView
import javax.swing.*

class OptionsPane(private val view: MapView) : JPanel() {
    private val region = JLabel("16383")
    private val tileX = JTextField("16383")
    private val tileY = JTextField("16383")
    private val tilePlane = JTextField("4")

    fun updatePosition(mapX: Int, mapY: Int, plane: Int) {
        tileX.text = mapX.toString()
        tileY.text = mapY.toString()
        tilePlane.text = plane.toString()
        region.text = Region.getId(mapX, mapY).toString()
    }

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        isOpaque = false
        alignmentX = LEFT_ALIGNMENT

        val coordinates = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(tileX)
            add(tileY)
            add(tilePlane)
        }
        add(coordinates)

        val planeControls = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(JButton("+").apply {
                addActionListener {
                    view.updatePlane((view.plane + 1).coerceIn(0, 4))
                }
            })
            add(JButton("-").apply {
                addActionListener {
                    view.updatePlane((view.plane - 1).coerceIn(0, 4))
                    parent.transferFocus()
                }
            })
        }
        add(planeControls)
    }
}