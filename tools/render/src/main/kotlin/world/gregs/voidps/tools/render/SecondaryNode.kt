package world.gregs.voidps.tools.render

/** Class348_Sub42 **/
open class SecondaryNode : Node() {
    var aSecondaryNode_7060: SecondaryNode? = null
    var aSecondaryNode_7063: SecondaryNode? = null

    fun unlink2() {
        if (this.aSecondaryNode_7060 != null) {
            this.aSecondaryNode_7060!!.aSecondaryNode_7063 = this.aSecondaryNode_7063
            this.aSecondaryNode_7063!!.aSecondaryNode_7060 = this.aSecondaryNode_7060
            this.aSecondaryNode_7060 = null
            this.aSecondaryNode_7063 = null
        }
    }
}
