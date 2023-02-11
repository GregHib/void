package world.gregs.voidps.world.interact.entity.player.effect

import world.gregs.voidps.engine.entity.EffectStart
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.inWilderness
import world.gregs.voidps.world.interact.entity.player.effect.skull

on<CombatSwing>({ it.inWilderness && target is Player && !it.get<List<Character>>("attackers").contains(target) }) { player: Player ->
    player.skull()
}

on<EffectStart>({ effect == "skull" }) { player: Player ->
    player.appearance.skull = player["skull", 0]
    player.flagAppearance()
}

on<EffectStop>({ effect == "skull" }) { player: Player ->
    player.appearance.skull = -1
    player.clear("skull")
    player.flagAppearance()
}