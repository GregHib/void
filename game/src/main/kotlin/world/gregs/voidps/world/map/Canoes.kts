package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.statement

on<ObjectOption>({ obj.id == "canoe_station" && option == "Chop-down" }) { player: Player ->
    player.dialogue {
        if (!player.has(Skill.Woodcutting, 12, false)) {
            statement("You must have at least level 12 woodcutting to start making canoes.")
        }
    }
}