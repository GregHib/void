package content.area.kandarin.seers_village

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.type.equals

objectOperate("Walk-across", "log_balance") {
    if (!player.has(Skill.Agility, 20)) {
        player.message("You need at least 20 Agility to do that.") // TODO proper message
        return@objectOperate
    }
    player.message("You walk carefully across the slippery log...", ChatType.Filter)
    player.renderEmote("rope_balance")
    player.walkOverDelay(target.tile)
    if (target.tile.equals(2602, 3477)) {
        player.walkOverDelay(target.tile.copy(2598))
    } else if (target.tile.equals(2599, 3477)) {
        player.walkOverDelay(target.tile.copy(2603))
    }
    player.clearRenderEmote()
    player.exp(Skill.Agility, 8.5)
    player.message("... and make it safely to the other side.", ChatType.Filter)
}