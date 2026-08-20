package content.area.misthalin.edgeville.monastery

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax

suspend fun Player.joinMonasteryOrder(): Boolean {
    if (!hasMax(Skill.Prayer, 31)) {
        npc<Neutral>("abbot_langley", "No. I am sorry, but I feel you are not devout enough.")
        message("You need a prayer level of 31 to join the order.")
        return false
    }
    npc<Happy>("abbot_langley", "Ok, I see you are someone suitable for our order. You may join.")
    set("edgeville_monastery_order_member", true)
    return true
}
