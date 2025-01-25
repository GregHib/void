package content.skill.prayer.active

import world.gregs.voidps.engine.data.definition.PrayerDefinitions
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import content.skill.prayer.Prayer
import content.skill.prayer.prayerStart
import content.skill.prayer.prayerStop


val definitions: PrayerDefinitions by inject()

prayerStart { player ->
    val definition = definitions.getOrNull(prayer) ?: return@prayerStart
    for ((bonus, value) in definition.bonuses) {
        player["base_${bonus}_bonus"] = player["base_${bonus}_bonus", 1.0] + value / 100.0
    }
}

prayerStop { player ->
    val definition = definitions.getOrNull(prayer) ?: return@prayerStop
    for ((bonus, value) in definition.bonuses) {
        player["base_${bonus}_bonus"] = player["base_${bonus}_bonus", 1.0] - value / 100.0
    }
}

characterCombatAttack { character ->
    if (!Prayer.usingDeflectPrayer(character, target, type)) {
        return@characterCombatAttack
    }
    val damage = target["protected_damage", 0]
    if (damage > 0) {
        target.anim("deflect", delay)
        target.gfx("deflect_$type", delay)
        if (random.nextDouble() >= 0.4) {
            target.hit(target = character, type = "deflect", delay = delay, damage = (damage * 0.10).toInt())
        }
    }
}