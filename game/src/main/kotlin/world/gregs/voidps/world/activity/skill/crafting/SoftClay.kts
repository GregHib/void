import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.skill.ItemOnItem

on<ItemOnItem>({ def.skill == Skill.Crafting && def.add.any { it.id == "soft_clay" } }) { player: Player ->
    player.message("You now have some soft, workable clay.", ChatType.Filter)
}