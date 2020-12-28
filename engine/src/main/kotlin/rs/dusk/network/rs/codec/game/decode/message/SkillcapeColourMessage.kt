package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

data class SkillcapeColourMessage(val colour: Int) : Message {
    companion object : MessageCompanion<SkillcapeColourMessage>()
}