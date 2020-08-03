package rs.dusk.engine.client.ui.dialogue

interface DialogueIO {
    fun sendChat(builder: DialogueBuilder)
    fun sendStatement(builder: DialogueBuilder)
    fun sendChoice(builder: DialogueBuilder)
}