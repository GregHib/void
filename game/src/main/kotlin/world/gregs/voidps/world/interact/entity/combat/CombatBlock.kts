import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatAttack
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.random.Random

on<CombatAttack> { character: Character ->
    val sound = calculateHitSound(character, target)
    if (target is Player) {
        target.playSound(sound, delay = delay)
    }
    if (character is Player) {
        character.playSound(sound, delay = delay)
    }
    character.setAnimation(hitAnimation(character))
}

fun calculateHitSound(source: Character, target: Character): String {
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

fun hitAnimation(character: Character): String {
    return when (character) {
        is Player -> "player"
        is NPC -> {
            val race: String? = character.def.getOrNull("race")
            if (race != null) {
                return "${race}_hit"
            }
            character.def["hit_anim"]
        }
        else -> ""
    }
}