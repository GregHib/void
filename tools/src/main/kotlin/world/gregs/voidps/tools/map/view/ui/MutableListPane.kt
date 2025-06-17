package world.gregs.voidps.tools.map.view.ui

import java.awt.Dimension
import javax.swing.*

class MutableListPane(title: String, private val model: DefaultListModel<String>) : JPanel() {
    private val list = JList(model)

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        val label = JLabel(title)
        val pane = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            val requirement = JTextField("")
            add(requirement)
            add(Box.createRigidArea(Dimension(10, 0)))
            val add = JButton("Add")
            add(add)
            add.addActionListener {
                if (requirement.text.isNotBlank()) {
                    model.addElement(requirement.text)
                    requirement.text = ""
                }
            }
            alignmentX = LEFT_ALIGNMENT
        }
        label.labelFor = pane
        add(label)
        add(pane)
        add(Box.createRigidArea(Dimension(0, 5)))
        add(
            JScrollPane(list).apply {
                preferredSize = Dimension(250, 80)
                alignmentX = LEFT_ALIGNMENT
            },
        )
        add(
            JPanel().apply {
                alignmentX = LEFT_ALIGNMENT
                layout = BoxLayout(this, BoxLayout.LINE_AXIS)
                border = BorderFactory.createEmptyBorder(5, 10, 5, 0)
                add(Box.createHorizontalGlue())
                val clear = JButton("Clear")
                add(clear)
                clear.addActionListener {
                    model.removeAllElements()
                }
                add(Box.createRigidArea(Dimension(10, 0)))
                val remove = JButton("Remove")
                add(remove)
                remove.addActionListener {
                    list.selectedValuesList.forEach {
                        model.removeElement(it)
                    }
                }
            },
        )
    }
}
