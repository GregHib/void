package content.skill.magic.book.lunar

import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound

class MonsterExamine(val combatDefinitions: CombatDefinitions) : Script {

    private val styles = listOf("stab", "slash", "crush", "melee", "range", "magic", "dragonfire")

    init {
        interfaceOption("Close", "monster_stat_spy:close") {
            close("monster_stat_spy")
        }

        onNPCApproach("lunar_spellbook:monster_examine", "*") { (target) ->
            approachRange(8)
            if (hasClock("action_delay")) {
                return@onNPCApproach
            }
            if (!removeSpellItems("monster_examine")) {
                return@onNPCApproach
            }
            start("action_delay", 2)
            face(target.tile)
            anim("lunar_examine")
            gfx("monster_examine")
            sound("stat_spy")
            exp(Skill.Magic, Tables.int("spells.monster_examine.xp") / 10.0)
            open("monster_stat_spy")
            clear("spell")
            val maxHit = maxHit(target)
            interfaces.sendText("monster_stat_spy", "name", target.def.name)
            interfaces.sendText("monster_stat_spy", "line1", "Combat level: ${target.def.combat}")
            interfaces.sendText("monster_stat_spy", "line2", "Hitpoints: ${target.levels.getMax(Skill.Constitution)}")
            interfaces.sendText("monster_stat_spy", "line3", "Max hit: $maxHit")
            interfaces.sendText("monster_stat_spy", "line4", if (target.def["immune_poison", false]) "It is immune to poison." else "It is not immune to poison.")
        }
    }

    private fun maxHit(npc: NPC): Int {
        val definition = combatDefinitions.getOrNull(npc.transformDef["combat_def", npc.id])
        if (definition != null) {
            val max = definition.attacks.values.flatMap { it.targetHits }.maxOfOrNull { npc.def["max_hit_${it.offense}", it.max] } ?: 0
            if (max > 0) {
                return max
            }
        }
        val defined = styles.maxOf { npc.def["max_hit_$it", 0] }
        if (defined > 0) {
            return defined
        }
        // No data defined; estimate with the same melee formula combat uses
        val strengthBonus = npc["strength", 0] + 64
        return 5 + (npc.levels.get(Skill.Strength) * strengthBonus) / 64
    }
}
