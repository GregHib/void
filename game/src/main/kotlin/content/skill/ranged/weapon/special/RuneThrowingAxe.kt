package content.skill.ranged.weapon.special

import content.area.wilderness.inMultiCombat
import content.entity.combat.Target
import content.entity.combat.hit.characterCombatDamage
import content.entity.combat.hit.hit
import content.entity.player.combat.special.SpecialAttack
import content.entity.player.combat.special.specialAttack
import content.entity.proj.shoot
import content.skill.ranged.ammo
import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.spiral

@Script
class RuneThrowingAxe {

    val players: Players by inject()
    val npcs: NPCs by inject()
    val lineOfSight: LineValidator by inject()

    init {
        specialAttack("chainhit") { player ->
            val ammo = player.ammo
            player["chain_hits"] = mutableSetOf(target.index)
            player.anim("rune_throwing_axe_special")
            player.gfx("${ammo}_special_throw")
            val time = player.shoot(id = "${ammo}_special", target = target)
            player.hit(target, delay = time)
        }

        characterCombatDamage("rune_throwing_axe", "range") { target ->
            if (source !is Player || !target.inMultiCombat || !special) {
                return@characterCombatDamage
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
                        return@characterCombatDamage
                    }
                    chain.add(character.index)
                    val time = target.shoot(id = "rune_throwing_axe_special", target = character)
                    source.hit(character, weapon, type, special = true, delay = time)
                    return@characterCombatDamage
                }
            }
        }
    }
}
