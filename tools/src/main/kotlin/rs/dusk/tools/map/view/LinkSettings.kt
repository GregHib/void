package rs.dusk.tools.map.view

import java.awt.Dimension
import javax.swing.*
import javax.swing.BoxLayout

class LinkSettings : JPanel() {
    val actionsList = DefaultListModel<String>()
    val requirementsList = DefaultListModel<String>()
    val xCoord = JTextField("")
    val yCoord = JTextField("")
    val zCoord = JTextField("")

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        val label = JLabel("Coordinates")
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
        pane.alignmentX = LEFT_ALIGNMENT
        add(pane)

        add(MutableListPane("Actions", actionsList))
        add(Box.createRigidArea(Dimension(0, 5)))
        add(MutableListPane("Requirements", requirementsList))
    }
}