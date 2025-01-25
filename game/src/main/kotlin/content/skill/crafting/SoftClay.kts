package content.skill.crafting

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.onEvent
import world.gregs.voidps.world.activity.skill.ItemUsedOnItem

onEvent<Player, ItemUsedOnItem>("item_used_on_item", Skill.Crafting) { player ->
    if (def.add.any { it.id == "soft_clay" }) {
        player.message("You now have some soft, workable clay.", ChatType.Filter)
    }
}