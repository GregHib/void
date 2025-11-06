package content.area.karamja.brimhaven

import content.entity.combat.hit.hit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.random

class ColourDragon : Script {
    init {
        npcCombatSwing("blue_dragon", handler = ::swing)
        npcCombatSwing("black_dragon", handler = ::swing)
        npcCombatSwing("green_dragon", handler = ::swing)
        npcCombatSwing("red_dragon", handler = ::swing)
    }

    fun swing(npc: NPC, target: Character) {
        val useFire = random.nextInt(4) == 0 // 1 in 4 chance to breathe fire
        if (useFire) {
            npc.anim("colour_dragon_breath")
            npc.gfx("dragon_breath_shoot")
            npc.hit(target, offensiveType = "dragonfire", special = true)
            target.sound("dragon_breath")
        } else {
            npc.anim("colour_dragon_attack")
            npc.hit(target, offensiveType = "melee")
            target.sound("dragon_attack")
        }
    }
}
