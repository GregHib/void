package rs.dusk.engine.client.ui.dialogue

interface DialogueIO {
    fun sendChat(builder: DialogueBuilder)
    fun sendStatement(builder: DialogueBuilder)
    fun sendChoice(builder: DialogueBuilder)
    fun sendStringEntry(text: String)
    fun sendIntEntry(text: String)
    fun sendItemDestroy(text: String, item: Int)
    fun sendItemBox(text: String, model: Int, zoom: Int, sprite: Int?)
}