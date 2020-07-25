package rs.dusk.engine.client.ui

data class InterfaceData(
    val fixedParent: Int? = null,
    val resizableParent: Int? = null,
    val fixedIndex: Int? = null,
    val resizableIndex: Int? = null
) {

    fun getIndex(resize: Boolean) = if(resize) resizableIndex else fixedIndex

    fun getParent(resize: Boolean) = if(resize) resizableParent else fixedParent

}