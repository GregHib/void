import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.mode.Rest
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

val alreadyResting = "You are already resting."

on<InterfaceOption>({ id == "energy_orb" && option == "Rest" }) { player: Player ->
    if (player["movement", "walk"] == "rest") {
        player.message(alreadyResting)
    } else {
        player.mode = Rest(player, -1)
    }
}

on<NPCClick>({ option == "Listen-to" }) { player: Player ->
    if (player["movement", "walk"] == "music") {
        player.message(alreadyResting)
        cancel()
    }
}

on<NPCOption>({ def["song", -1] != -1 && option == "Listen-to" }) { player: Player ->
    player.mode = Rest(player, def["song"])
}
