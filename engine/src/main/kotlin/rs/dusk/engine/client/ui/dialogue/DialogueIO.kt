package rs.dusk.engine.client.ui.dialogue

interface DialogueIO {
    fun sendChat(builder: DialogueBuilder): Boolean
    fun sendStatement(builder: DialogueBuilder): Boolean
    fun sendChoice(builder: DialogueBuilder): Boolean
}