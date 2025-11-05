package content.area.morytania.slayer_tower

import content.entity.combat.hit.hit
import content.entity.combat.npcCombatSwing
import content.entity.player.equip.Equipment
import content.entity.proj.shoot
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Banshee : Script {

    init {
        npcCombatSwing("banshee") { npc ->
            if (target !is Player) {
                return@npcCombatSwing
            }
            if (!Equipment.isEarmuffs(target.equipped(EquipSlot.Hat).id)) {
                target.sound("banshee_attack_earmuffs")
                target.anim("ears_bleed")
                npc.anim("banshee_attack_scream")
                npc.shoot("banshee_scream", target, 0, 18, 23, 23)
                target.levels.drain(Skill.Attack, multiplier = 0.2)
                target.levels.drain(Skill.Strength, multiplier = 0.2)
                target.levels.drain(Skill.Defence, multiplier = 0.2)
                target.levels.drain(Skill.Ranged, multiplier = 0.2)
                target.levels.drain(Skill.Magic, multiplier = 0.2)
                target.levels.drain(Skill.Prayer, multiplier = 0.1)
                target.levels.drain(Skill.Agility, multiplier = 0.1)
            } else {
                target.sound("banshee_attack")
            }
            npc.hit(target, offensiveType = "melee", defensiveType = "magic")
        }
    }
}
