package world.gregs.voidps.world.interact.entity.player.combat.magic.spell.book.ancient

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.fightStyle
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.proj.shoot

val definitions: SpellDefinitions by inject()

combatSwing(spell = "miasmic_*", style = "magic") { player ->
    val spell = player.spell
    player.setAnimation("${spell}_cast")
    player.setGraphic("${spell}_cast")
    player.shoot(spell, target)
    player.hit(target)
    delay = 5
}

fun meleeOrRanged(type: String) = type == "range" || type == "melee"

combatSwing { player ->
    if (delay != null && delay!! > 0 && player.hasClock("miasmic") && meleeOrRanged(player.fightStyle)) {
        delay = delay!! * 2
    }
}

characterCombatAttack(spell = "miasmic_*", type = "magic") {
    if (damage <= 0) {
        return@characterCombatAttack
    }
    val seconds: Int = definitions.get(spell)["effect_seconds"]
    target.start("miasmic", seconds, epochSeconds())
}