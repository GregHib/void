package content.area.kandarin.yanille

import content.entity.player.equip.DragonfireShield
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.combat.CombatDamage
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.random

class SalarinTheTwisted : Script {

    init {
        npcCombatDamage("salarin_the_twisted", handler = ::blockedDamage)
    }

    private fun blockedDamage(target: NPC, damage: CombatDamage) {
        if (damage.damage == 0 && random.nextInt(6) == 0) {
            target.say("Your pitiful attacks cannot hurt me!")
        }
    }

    companion object {
        private val fixedSpellDamage = mapOf(
            "wind_rush" to 90,
            "wind_strike" to 90,
            "water_strike" to 100,
            "earth_strike" to 110,
            "fire_strike" to 120,
        )

        fun damage(damage: Int, type: String, weapon: Item, spell: String): Int = when {
            type == "dragonfire" && weapon.id == DragonfireShield.CHARGED -> damage
            type == "magic" && weapon.id == "korasis_sword" -> damage
            type == "deflect" -> damage
            type != "magic" -> 0
            spell == "magic_dart" -> damage
            spell == "wind_rush" -> fixedSpellDamage.getValue(spell)
            spell !in fixedSpellDamage -> 0
            damage < 0 -> damage
            else -> fixedSpellDamage.getValue(spell)
        }
    }
}
