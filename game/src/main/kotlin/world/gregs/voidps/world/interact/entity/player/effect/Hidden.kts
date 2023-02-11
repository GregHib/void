package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.entity.EffectStart
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.event.on

on<EffectStart>({ effect == "hidden" }) { player: Player ->
    player.appearance.hidden = true
    player.flagAppearance()
}

on<EffectStop>({ effect == "hidden" }) { player: Player ->
    player.appearance.hidden = false
    player.flagAppearance()
}