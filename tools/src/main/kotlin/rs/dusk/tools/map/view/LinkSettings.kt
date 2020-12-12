package rs.dusk.tools.map.view

import java.awt.Dimension
import javax.swing.*
import javax.swing.BoxLayout

class LinkSettings : JPanel() {
    val bidirectional = JCheckBox("Bidirectional")
    val interaction = JTextField("")
    val requirementsList = DefaultListModel<String>()

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        add(bidirectional)

        var label = JLabel("Interaction")
        label.labelFor = interaction
        add(label)
        add(Box.createRigidArea(Dimension(0, 5)))
        interaction.alignmentX = LEFT_ALIGNMENT
        add(interaction)

        label = JLabel("Requirements")
        val pane = pane()
        label.labelFor = pane
        add(label)
        add(pane)

        add(Box.createRigidArea(Dimension(0, 5)))
        val requirements = JList<String>(requirementsList)
        val listScroller = JScrollPane(requirements)
        listScroller.preferredSize = Dimension(250, 80)
        listScroller.alignmentX = LEFT_ALIGNMENT
        add(listScroller)

        add(JPanel().apply {
            alignmentX = LEFT_ALIGNMENT
            layout = BoxLayout(this, BoxLayout.LINE_AXIS)
            border = BorderFactory.createEmptyBorder(5, 10, 5, 0)
            add(Box.createHorizontalGlue())
            val clear = JButton("Clear")
            add(clear)
            clear.addActionListener {
                requirementsList.removeAllElements()
            }
            add(Box.createRigidArea(Dimension(10, 0)))
            val remove = JButton("Remove")
            add(remove)
            remove.addActionListener {
                requirements.selectedValuesList.forEach {
                    requirementsList.removeElement(it)
                }
            }
        })

        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    }

    private fun pane() = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        val requirement = JTextField("")
        add(requirement)
        add(Box.createRigidArea(Dimension(10, 0)))
        val add = JButton("Add")
        add(add)
        add.addActionListener {
            if (requirement.text.isNotBlank()) {
                requirementsList.addElement(requirement.text)
                requirement.text = ""
            }
        }
        alignmentX = LEFT_ALIGNMENT
    }
}