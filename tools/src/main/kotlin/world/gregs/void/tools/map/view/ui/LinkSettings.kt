package world.gregs.void.tools.map.view.ui

import java.awt.Dimension
import javax.swing.*

class LinkSettings : JPanel() {
    val actionsList = DefaultListModel<String>()
    val requirementsList = DefaultListModel<String>()
    val start = CoordinatesPane("Start tile")
    val end = CoordinatesPane("End tile")

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        add(start)
        add(end)
        start.xCoord.isEnabled = false
        start.yCoord.isEnabled = false
        start.zCoord.isEnabled = false
        end.xCoord.isEnabled = false
        end.yCoord.isEnabled = false
        end.zCoord.isEnabled = false

        add(MutableListPane("Actions", actionsList))
        add(Box.createRigidArea(Dimension(0, 5)))
        add(MutableListPane("Requirements", requirementsList))
    }
}