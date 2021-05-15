import world.gregs.voidps.engine.entity.StopEffect
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.colourOverlay
import world.gregs.voidps.engine.event.on

on<StopEffect>({ effect == "colour_overlay" }) { player: Player ->
    stop(player)
}

on<StopEffect>({ effect == "colour_overlay" }) { npc: NPC ->
    stop(npc)
}

fun stop(character: Character) {
    character.colourOverlay.apply {
        delay = 0
        duration = 0
        colour = 0
    }
}