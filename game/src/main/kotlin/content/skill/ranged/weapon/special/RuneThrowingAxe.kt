package content.skill.ranged.weapon.special

import content.area.wilderness.inMultiCombat
import content.entity.combat.Target
import content.entity.combat.hit.hit
import content.entity.player.combat.special.SpecialAttack
import content.entity.proj.shoot
import content.skill.ranged.ammo
import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatDamage
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.map.spiral

class RuneThrowingAxe(
    val players: Players,
    val npcs: NPCs,
    val lineOfSight: LineValidator,
) : Script {

    init {
        specialAttack("chainhit") { target, _ ->
            val ammo = ammo
            set("chain_hits", mutableSetOf(target.index))
            anim("rune_throwing_axe_special")
            gfx("${ammo}_special_throw")
            val time = shoot(id = "${ammo}_special", target = target)
            hit(target, delay = time)
        }

        combatDamage("range", ::damage)
        npcCombatDamage(style = "range", handler = ::damage)
    }

    fun damage(target: Character, it: CombatDamage) {
        if (it.weapon.id != "rune_throwing_axe") {
            return
        }
        val (source, type, _, weapon, _, special) = it
        if (source !is Player || !target.inMultiCombat || !special) {
            return
        }
        val chain: MutableSet<Int> = source.getOrPut("chain_hits") { mutableSetOf() }
        val characters = if (target is Player) players else npcs
        for (tile in target.tile.spiral(4)) {
            for (character in characters[tile]) {
                if (character == target || chain.contains(character.index) || !Target.attackable(source, character)) {
                    continue
                }
                if (!lineOfSight.hasLineOfSight(target, character)) {
                    continue
                }
                if (!SpecialAttack.drain(source)) {
                    source.clear("chain_hits")
                    return
                }
                chain.add(character.index)
                val time = target.shoot(id = "rune_throwing_axe_special", target = character)
                source.hit(character, weapon, type, special = true, delay = time)
                return
            }
        }
    }
}
