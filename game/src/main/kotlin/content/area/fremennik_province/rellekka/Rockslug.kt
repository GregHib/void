package content.area.fremennik_province.rellekka

import content.entity.combat.attacker
import content.entity.combat.attackers
import content.entity.combat.hit.damage
import content.entity.combat.underAttack
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue

class Rockslug : Script {
    init {
        itemOnNPCOperate("bag_of_salt", "rockslug*") { (target) ->
            arriveDelay()
            if (target.underAttack && target.attacker != this) {
                message("Someone else is fighting that.")
                return@itemOnNPCOperate
            }
            salt(this, target)
        }
        npcLevelChanged(Skill.Constitution, "rockslug*", ::killingBlow)
    }

    fun killingBlow(npc: NPC, skill: Skill, from: Int, to: Int) {
        if (to > 60) {
            return
        }
        for (attacker in npc.attackers) {
            attacker.mode = EmptyMode
            if (attacker is Player && attacker["killing_blow", false]) {
                salt(attacker, npc)
                break
            }
        }
        npc.mode = EmptyMode
    }

    fun salt(player: Player, target: NPC) {
        if (!player.inventory.remove("bag_of_salt")) {
            player.message("You need a bag of salt to kill the rockslug.") // TODO proper message
            return
        }
        val hitpoints = target.levels.get(Skill.Constitution)
        player.face(target)
        player.anim("slayer_salt_sprinkle")
        player.gfx("slayer_salt_sprinkle")
        if (hitpoints >= 60) {
            player.message("The rockslug isn't weak enough to be affected by the salt.")
            return
        }
        target.damage(hitpoints, source = player)
        player.message("The rockslug shrivels up and dies.", ChatType.Filter)
    }
}