package content.skill.melee.weapon.special

import content.area.wilderness.inMultiCombat
import content.entity.combat.Target
import content.entity.combat.hit.Damage
import content.entity.combat.hit.hit
import content.entity.player.combat.special.SpecialAttack
import content.skill.melee.weapon.weapon
import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.combat.CombatDamage
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.type.random

class KorasiSword : Script, SpecialAttack {

    val players: Players by inject()
    val npcs: NPCs by inject()
    val lineOfSight: LineValidator by inject()

    init {
        specialAttack("disrupt") { target, id ->
            set("korasi_chain", mutableSetOf(target.index))
            anim("${id}_special")
            gfx("${id}_special")
            areaSound("godwars_saradomin_magic_impact", tile, 10)
            areaSound("godwars_godsword_special_attack", tile, 5)
            val maxHit = Damage.maximum(this, target, "melee", weapon)
            val hit = random.nextInt(maxHit / 2, (maxHit * 1.5).toInt())
            hit(target, damage = hit, offensiveType = "magic", delay = 0)
        }

        combatDamage(handler = ::damage)
        npcCombatDamage(handler = ::damage)
    }

    fun damage(target: Character, it: CombatDamage) {
        val (source, type, damage, weapon, _, special) = it
        if (it.weapon.id != "korasis_sword") {
            return
        }
        if (!special) {
            return
        }
        areaSound("godwars_saradomin_magic_impact", target.tile, 10)
        target.gfx("disrupt_impact")
        if (!target.inMultiCombat) {
            return
        }
        val chain: MutableSet<Int> = source["korasi_chain", mutableSetOf()]
        if (chain.size >= 3) {
            return
        }
        val characters = if (target is Player) players else npcs
        for (tile in target.tile.spiral(4)) {
            for (character in characters[tile]) {
                if (character == target || chain.contains(character.index) || !Target.attackable(source, character)) {
                    continue
                }
                if (!lineOfSight.hasLineOfSight(target, character)) {
                    continue
                }
                chain.add(character.index)
                val hit = damage / when (chain.size) {
                    2 -> 2
                    3 -> 4
                    else -> return
                }
                source.hit(character, damage = hit, weapon = weapon, offensiveType = type, special = true)
                return
            }
        }
    }
}
