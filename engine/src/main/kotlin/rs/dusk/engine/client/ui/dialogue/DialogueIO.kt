package rs.dusk.engine.client.ui.dialogue

interface DialogueIO {
    fun sendChat(builder: DialogueBuilder): Boolean
}