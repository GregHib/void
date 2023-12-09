package world.gregs.voidps.world.interact.entity.player.combat.melee.defence

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.skill.slayer.race
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.sound.playSound

on<CombatAttack>(priority = Priority.LOWER) { player: Player ->
    player.playSound(calculateHitSound(target), delay)
}

on<CombatAttack>({ target is Player }, Priority.LOWER) { _: Character ->
    val player = target as Player
    player.playSound(calculateHitSound(target), delay)
}

fun calculateHitSound(target: Character): String {
    if (target is NPC) {
        return "${target.race}_hit"
    }

    if (target is Player) {
        return if (target.male) {
            "male_hit_${random.nextInt(0, 3)}"
        } else {
            "female_hit_${random.nextInt(0, 1)}"
        }
    }
    return "human_hit"
}