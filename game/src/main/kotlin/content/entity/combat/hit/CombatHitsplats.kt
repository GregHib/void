package content.entity.combat.hit

import content.entity.combat.damageDealers
import content.skill.melee.weapon.Weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.flagHits
import world.gregs.voidps.engine.entity.character.mode.combat.CombatDamage
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.visual.update.HitSplat
import kotlin.collections.set
import kotlin.math.floor

class CombatHitsplats : Script {

    val definitions: SpellDefinitions by inject()

    init {
        combatDamage(handler = ::hit)
        npcCombatDamage(handler = ::hit)
    }

    fun hit(target: Character, it: CombatDamage) {
        var (source, type, damage, _, spell, _) = it
        if (damage < 0) {
            target.hit(
                source = source,
                amount = 0,
                mark = HitSplat.Mark.Missed,
            )
        } else if (type == "healed") {
            target.hit(
                source = source,
                amount = damage,
                mark = HitSplat.Mark.Healed,
            )
            target.levels.restore(Skill.Constitution, damage)
        } else {
            if (type == "magic" && definitions.get(spell).maxHit == -1) {
                return
            }
            var soak = 0
            if (!source["insta_kill", false] && Settings["combat.damageSoak", true] && damage > 200) {
                val percent = target["absorb_$type", 10] / 100.0
                soak = floor((damage - 200) * percent).toInt()
                damage -= soak
            }
            if (Settings["combat.showSoak", true] || soak <= 0) {
                soak = -1
            }
            val dealers = target.damageDealers
            dealers[source] = dealers.getOrDefault(source, 0) + damage
            val maxHit = source["max_hit", 0]
            val mark = Weapon.mark(type)
            val critical = mark.id < 3 && damage > 10 && maxHit > 0 && damage > (maxHit * 0.9)
            target.hit(
                source = source,
                amount = damage,
                mark = mark,
                critical = critical,
                soak = soak,
            )
            target.levels.drain(Skill.Constitution, damage)
        }
    }

    fun Character.hit(source: Character, amount: Int, mark: HitSplat.Mark, delay: Int = 0, critical: Boolean = false, soak: Int = -1) {
        val after = (levels.get(Skill.Constitution) - amount).coerceAtLeast(0)
        val percentage = levels.getPercent(Skill.Constitution, after, 255.0).toInt()
        visuals.hits.add(HitSplat(amount, mark, percentage, delay, critical, if (source is NPC) -source.index else source.index, soak))
        flagHits()
    }
}
