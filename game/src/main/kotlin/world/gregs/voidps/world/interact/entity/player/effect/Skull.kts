import world.gregs.voidps.engine.entity.EffectStart
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.appearance
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.event.on

on<EffectStart>({ effect == "skull" }) { player: Player ->
    player.appearance.skull = player["skull", 0]
    player.flagAppearance()
}

on<EffectStop>({ effect == "skull" }) { player: Player ->
    player.appearance.skull = -1
    player.clear("skull")
    player.flagAppearance()
}