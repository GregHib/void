import world.gregs.voidps.engine.entity.StartEffect
import world.gregs.voidps.engine.entity.StopEffect
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.engine.event.on

on<StartEffect>({ effect == "hidden" }) { player: Player ->
    player.appearance.hidden = true
    player.flagAppearance()
}

on<StopEffect>({ effect == "hidden" }) { player: Player ->
    player.appearance.hidden = false
    player.flagAppearance()
}