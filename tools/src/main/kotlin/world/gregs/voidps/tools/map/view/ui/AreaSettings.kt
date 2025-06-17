package world.gregs.voidps.tools.map.view.ui

import java.awt.Dimension
import javax.swing.*

class AreaSettings : JPanel() {
    val name = JTextField("")
    val tagsList = DefaultListModel<String>()

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        val label = JLabel("Name")
        val pane = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(this@AreaSettings.name)
            add(Box.createRigidArea(Dimension(10, 0)))
            alignmentX = LEFT_ALIGNMENT
        }
        label.labelFor = name
        add(label)
        add(Box.createRigidArea(Dimension(0, 5)))
        add(pane)
        add(Box.createRigidArea(Dimension(0, 5)))
        add(MutableListPane("Tags", tagsList))
    }
}
