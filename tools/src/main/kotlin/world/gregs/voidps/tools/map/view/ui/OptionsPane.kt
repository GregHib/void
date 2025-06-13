package world.gregs.voidps.tools.map.view.ui

import world.gregs.voidps.tools.map.view.draw.MapView
import world.gregs.voidps.type.Region
import javax.swing.*

class OptionsPane(private val view: MapView) : JPanel() {
    private val region = JLabel("16383")
    private val tileX = JTextField("16383")
    private val tileY = JTextField("16383")
    private val tileLevel = JTextField("4")

    fun updatePosition(mapX: Int, mapY: Int, level: Int) {
        tileX.text = mapX.toString()
        tileY.text = mapY.toString()
        tileLevel.text = level.toString()
        region.text = Region.id(mapX / 64, mapY / 64).toString()
    }

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        isOpaque = false
        alignmentX = LEFT_ALIGNMENT

        val region = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(region)
        }
        add(region)

        val coordinates = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(tileX)
            add(tileY)
            add(tileLevel)
        }
        add(coordinates)

        val levelControls = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(
                JButton("+").apply {
                    addActionListener {
                        view.updateLevel((view.level + 1).coerceIn(0, 4))
                    }
                },
            )
            add(
                JButton("-").apply {
                    addActionListener {
                        view.updateLevel((view.level - 1).coerceIn(0, 4))
                        parent.transferFocus()
                    }
                },
            )
        }
        add(levelControls)
    }
}
