package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendVisibility
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
on<Command>({ prefix == "canoe" }) { player: Player ->
    player.open("canoe")
}

on<InterfaceOpened>({ id == "canoe" }) { player: Player ->
    if (player.levels.get(Skill.Woodcutting) > 26) {
        player.interfaces.sendVisibility(id, "visible_dugout", true)
        player.interfaces.sendVisibility(id, "invisible_dugout", false)
    }
    if (player.levels.get(Skill.Woodcutting) > 41) {
        player.interfaces.sendVisibility(id, "visible_stable_dugout", true)
        player.interfaces.sendVisibility(id, "invisible_stable_dugout", false)
    }
    if (player.levels.get(Skill.Woodcutting) > 56) {
        player.interfaces.sendVisibility(id, "visible_waka", true)
        player.interfaces.sendVisibility(id, "invisible_waka", false)
    }
}

on<InterfaceOpened>({ id == "canoe_stations_map" }) { player: Player ->
}

on<InterfaceOpened>({ id == "canoe_travel" }) { player: Player ->
}