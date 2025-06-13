package content.area.karamja.brimhaven

import content.entity.combat.hit.hit
import content.entity.combat.npcCombatSwing
import content.entity.sound.sound
import kotlinx.coroutines.delay
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.type.random

npcCombatSwing("red_dragon*") { npc ->
    val withinMelee = CharacterTargetStrategy(npc).reached(target)
    if (!withinMelee) {
        delay(1)
    }
    val useFire = random.nextInt(4) == 0 // 1 in 4 chance to breathe fire
    if (useFire) {
        npc.anim("colour_dragon_breath")
        npc.gfx("dragon_breath_shoot")
        npc.hit(target, type = "dragonfire", special = true)
        target.sound("dragon_breath")
    } else {
        npc.anim("colour_dragon_attack")
        npc.hit(target, type = "melee")
        target.sound("dragon_attack")
    }
}
