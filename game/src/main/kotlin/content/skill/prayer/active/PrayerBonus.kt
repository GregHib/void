package content.skill.prayer.active

import content.entity.combat.hit.hit
import content.skill.prayer.Prayer
import content.skill.prayer.PrayerApi
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.PrayerDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.type.random

class PrayerBonus(val definitions: PrayerDefinitions) :
    Script,
    PrayerApi {

    init {
        prayerStart { prayer ->
            val definition = definitions.getOrNull(prayer) ?: return@prayerStart
            for ((bonus, value) in definition.bonuses) {
                set("base_${bonus}_bonus", get("base_${bonus}_bonus", 1.0) + value / 100.0)
            }
        }

        prayerStop { prayer ->
            val definition = definitions.getOrNull(prayer) ?: return@prayerStop
            for ((bonus, value) in definition.bonuses) {
                set("base_${bonus}_bonus", get("base_${bonus}_bonus", 1.0) - value / 100.0)
            }
        }

        combatAttack(handler = ::attack)
        npcCombatAttack(handler = ::attack)
    }

    fun attack(source: Character, attack: world.gregs.voidps.engine.entity.character.mode.combat.CombatAttack) {
        val (target, _, type) = attack
        val delay = attack.delay
        if (!Prayer.usingDeflectPrayer(source, target, type)) {
            return
        }
        val damage = target["protected_damage", 0]
        if (damage > 0) {
            target.anim("deflect", delay)
            target.gfx("deflect_$type", delay)
            if (random.nextDouble() >= 0.4) {
                target.hit(target = source, offensiveType = "deflect", delay = delay, damage = (damage * 0.10).toInt())
            }
        }
    }
}
