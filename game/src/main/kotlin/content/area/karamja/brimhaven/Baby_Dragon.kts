package content.area.karamja.brimhaven

import content.entity.combat.CombatSwing
import content.entity.combat.hit.hit
import content.entity.combat.npcCombatSwing
import content.entity.sound.sound
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC

val handler: suspend CombatSwing.(NPC) -> Unit = { npc ->
    val withinMelee = CharacterTargetStrategy(npc).reached(target)
    if (withinMelee) {
        npc.anim("baby_dragon_attack")
        npc.hit(target, type = "melee")
        target.sound("dragon_attack")
    }
}
npcCombatSwing("baby_red_dragon", handler = handler)
npcCombatSwing("baby_red_dragon_1", handler = handler)
npcCombatSwing("baby_red_dragon_2", handler = handler)