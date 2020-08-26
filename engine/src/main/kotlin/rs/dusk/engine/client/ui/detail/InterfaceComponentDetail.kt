package rs.dusk.engine.client.ui.detail

data class InterfaceComponentDetail(
    val id: Int,
    val name: String,
    var parent: Int = -1
)