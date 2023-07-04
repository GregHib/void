package world.gregs.voidps.world.activity.combat.consume.food

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.consume.Consume
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy

on<Consume>({ item.id.startsWith("summer_pie") }) { player: Player ->
    player.runEnergy += (player.runEnergy / 100) * 10
    player.levels.boost(Skill.Agility, 5)
}