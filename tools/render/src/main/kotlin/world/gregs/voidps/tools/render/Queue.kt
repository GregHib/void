package world.gregs.voidps.tools.render

/* Class107 */

internal class Queue {
    var aSecondaryNode_1647: SecondaryNode = SecondaryNode()

    fun method1008(): SecondaryNode? {
        val class348_sub42 = (this.aSecondaryNode_1647.aSecondaryNode_7063)
        if (class348_sub42 === this.aSecondaryNode_1647) return null
        class348_sub42!!.unlink2()
        return class348_sub42
    }

    fun add(secondaryNode: SecondaryNode) {
        if (secondaryNode.aSecondaryNode_7060 != null) secondaryNode.unlink2()
        secondaryNode.aSecondaryNode_7063 = this.aSecondaryNode_1647
        secondaryNode.aSecondaryNode_7060 = (this.aSecondaryNode_1647.aSecondaryNode_7060)
        secondaryNode.aSecondaryNode_7060!!.aSecondaryNode_7063 = secondaryNode
        secondaryNode.aSecondaryNode_7063!!.aSecondaryNode_7060 = secondaryNode
    }

    init {
        this.aSecondaryNode_1647.aSecondaryNode_7060 = this.aSecondaryNode_1647
        this.aSecondaryNode_1647.aSecondaryNode_7063 = this.aSecondaryNode_1647
    }
}
