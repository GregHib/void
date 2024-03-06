package world.gregs.voidps.world.interact.entity.player.combat.range.weapon

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.player.combat.melee.multiTargetHit
import world.gregs.voidps.world.interact.entity.sound.playSound

fun isChinchompa(item: Item) = item.id.endsWith("chinchompa")

characterCombatHit("*chinchompa", "range") { character ->
    source as Player
    source.playSound("chinchompa_explode", delay = 40)
    character.setGraphic("chinchompa_hit")
}

multiTargetHit({ isChinchompa(weapon) }, { if (it is Player) 9 else 11 })