package rs.dusk.tools.map.view.ui

import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultListModel
import javax.swing.JPanel

class LinkSettings : JPanel() {
    val actionsList = DefaultListModel<String>()
    val requirementsList = DefaultListModel<String>()
    val coords = CoordinatesPane("Coordinates")
    val delta = CoordinatesPane("Delta movement")

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        add(coords)
        add(delta)

        add(MutableListPane("Actions", actionsList))
        add(Box.createRigidArea(Dimension(0, 5)))
        add(MutableListPane("Requirements", requirementsList))
    }
}