package world.gregs.voidps.tools.map.view.ui

import java.awt.Dimension
import javax.swing.*

class CoordinatesPane(title: String) : JPanel() {
    val xCoord = JTextField("")
    val yCoord = JTextField("")
    val zCoord = JTextField("")

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val label = JLabel(title)
        val pane = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(xCoord)
            add(Box.createRigidArea(Dimension(10, 0)))
            add(yCoord)
            add(Box.createRigidArea(Dimension(10, 0)))
            add(zCoord)
            alignmentX = LEFT_ALIGNMENT
        }
        label.labelFor = pane
        add(label)
        add(Box.createRigidArea(Dimension(0, 5)))
        add(pane)
        add(Box.createRigidArea(Dimension(0, 5)))
    }
}
