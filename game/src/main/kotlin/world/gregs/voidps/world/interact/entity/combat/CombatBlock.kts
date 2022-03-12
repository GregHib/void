import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.sound.playSound

on<CombatHit> { character: Character ->
    val name = (character as? NPC)?.def?.getOrNull("race") ?: "player"
    if (source is Player) {
        source.playSound("${name}_hit", delay = 40)
    }
    character.setAnimation("${name}_hit")
}