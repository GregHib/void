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

        add(JMutableList("Actions", actionsList))
        add(Box.createRigidArea(Dimension(0, 5)))
        add(JMutableList("Requirements", requirementsList))
    }

    internal class JMutableList(title: String, private val model: DefaultListModel<String>) : JPanel() {
        private val list = JList<String>(model)

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
            add(JScrollPane(list).apply {
                preferredSize = Dimension(250, 80)
                alignmentX = LEFT_ALIGNMENT
            })
            add(JPanel().apply {
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
            })
        }

    }
}