package world.gregs.voidps.world.interact.entity.player.combat.range.special

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.SpecialAttack
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot

val players: Players by inject()
val npcs: NPCs by inject()
val lineOfSight: LineValidator by inject()

specialAttack("chainhit") { player ->
    val ammo = player.ammo
    player["chain_hits"] = mutableSetOf(target.index)
    player.setAnimation("rune_throwing_axe_special")
    player.setGraphic("${ammo}_special_throw")
    val time = player.shoot(id = "${ammo}_special", target = target)
    player.hit(target, delay = time)
}

characterCombatHit("rune_throwing_axe", "range") { target ->
    if (source !is Player || !target.inMultiCombat || !special) {
        return@characterCombatHit
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
                return@characterCombatHit
            }
            chain.add(character.index)
            val time = target.shoot(id = "rune_throwing_axe_special", target = character)
            source.hit(character, weapon, type, special = true, delay = time)
            return@characterCombatHit
        }
    }
}