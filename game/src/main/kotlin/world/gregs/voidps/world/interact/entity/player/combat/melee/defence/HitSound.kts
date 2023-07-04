package world.gregs.voidps.world.interact.entity.player.combat.melee.defence

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatAttack
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.random.Random

on<CombatAttack>(priority = Priority.LOWER) { player: Player ->
    player.playSound(calculateHitSound(target), delay)
}

on<CombatAttack>({ target is Player }, Priority.LOWER) { character: Character ->
    val player = target as Player
    player.playSound(calculateHitSound(target), delay)
}

fun calculateHitSound(target: Character): String {
    if (target is NPC) {
        return "${target.def["race", ""]}_hit"
    }

    if (target is Player) {
        return if (target.male) {
            "male_hit_${Random.nextInt(0, 3)}"
        } else {
            "female_hit_${Random.nextInt(0, 1)}"
        }
    }
    return "human_hit"
}