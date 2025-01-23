package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.combat.hit.Damage
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.sound.areaSound

val players: Players by inject()
val npcs: NPCs by inject()
val lineOfSight: LineValidator by inject()

specialAttack("disrupt") { player ->
    player["korasi_chain"] = mutableSetOf(target.index)
    player.anim("${id}_special")
    player.gfx("${id}_special")
    areaSound("godwars_saradomin_magic_impact", player.tile, 10)
    areaSound("godwars_godsword_special_attack", player.tile, 5)
    val maxHit = Damage.maximum(player, target, "melee", player.weapon)
    val hit = random.nextInt(maxHit / 2, (maxHit * 1.5).toInt())
    player.hit(target, damage = hit, type = "magic", delay = 0)
}

characterCombatHit("korasis_sword") { target ->
    if (!special) {
        return@characterCombatHit
    }
    areaSound("godwars_saradomin_magic_impact", target.tile, 10)
    target.gfx("disrupt_hit")
    if (!target.inMultiCombat) {
        return@characterCombatHit
    }
    val chain: MutableSet<Int> = source["korasi_chain", mutableSetOf()]
    if (chain.size >= 3) {
        return@characterCombatHit
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
                else -> return@characterCombatHit
            }
            source.hit(character, damage = hit, weapon = weapon, type = type, special = true)
            return@characterCombatHit
        }
    }
}