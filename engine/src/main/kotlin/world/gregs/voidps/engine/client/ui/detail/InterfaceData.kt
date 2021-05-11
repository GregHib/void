package world.gregs.voidps.engine.client.ui.detail

/**
 * Location of an interface in relation to it's parents and screen mode
 */
data class InterfaceData(
    val fixedParent: String? = null,
    val resizableParent: String? = null,
    val fixedIndex: Int? = null,
    val resizableIndex: Int? = null
) {

    fun getIndex(resize: Boolean) = if(resize) resizableIndex else fixedIndex

    fun getParent(resize: Boolean) = if(resize) resizableParent else fixedParent

}