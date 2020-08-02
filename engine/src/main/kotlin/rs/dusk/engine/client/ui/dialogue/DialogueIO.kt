package rs.dusk.engine.client.ui.dialogue

interface DialogueIO {
    fun sendChat(builder: DialogueBuilder)
    fun sendStatement(builder: DialogueBuilder)
    fun sendChoice(any: DialogueBuilder)
    fun sendStringEntry(text: String, clickToContinue: Boolean)
    fun sendIntEntry(text: String)
    fun sendItemDestroy(text: String, item: Int)
}