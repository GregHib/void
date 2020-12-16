package rs.dusk.tools.map.view

import java.awt.Dimension
import javax.swing.*
import javax.swing.BoxLayout

class NodeSettings : JPanel() {
    val xCoord = JTextField("")
    val yCoord = JTextField("")
    val zCoord = JTextField("")

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)

        val label = JLabel("Coordinates")
        val pane = pane()
        label.labelFor = pane
        add(label)
        add(Box.createRigidArea(Dimension(0, 5)))
        pane.alignmentX = LEFT_ALIGNMENT
        add(pane)

        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    }

    private fun pane() = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        add(xCoord)
        add(Box.createRigidArea(Dimension(10, 0)))
        add(yCoord)
        add(Box.createRigidArea(Dimension(10, 0)))
        add(zCoord)
        alignmentX = LEFT_ALIGNMENT
    }
}