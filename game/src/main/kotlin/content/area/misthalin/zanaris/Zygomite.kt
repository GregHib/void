package content.area.misthalin.zanaris

import content.entity.combat.attacker
import content.entity.combat.attackers
import content.entity.combat.hit.damage
import content.entity.combat.underAttack
import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.inventory

class Zygomite : Script {
    init {
        npcOperate("Pick", "fungi,fungi_large") { (target) ->
            anim("pick_zygomite")
            sound("pick")
            delay(1)
            target.anim("zygomite_grow")
            sound("zygo_grow")
            delay(2)
            target.transform(if (target.id == "fungi") "zygomite" else "zygomite_large")
            target.interactPlayer(this, "Attack")
        }

        npcLevelChanged(Skill.Constitution, "fungi*", ::killingBlow)

        itemOnNPCOperate("fungicide_spray_*", "zygomite*") { (target, item, index) ->
            arriveDelay()
            if (item.id == "fungicide_spay_0") {
                message("The spray pump is empty! Reload it with another fungicide canister!")
                return@itemOnNPCOperate
            }
            if (target.underAttack && target.attacker != this) {
                message("Someone else is fighting that.")
                return@itemOnNPCOperate
            }
            fungicide(this, target, index)
        }
    }

    fun killingBlow(npc: NPC, skill: Skill, from: Int, to: Int) {
        if (to > 80) {
            return
        }
        for (attacker in npc.attackers) {
            attacker.mode = EmptyMode
            attacker.message("The zygomite is on its last legs! Finish it quickly!")
            if (attacker is Player && attacker["killing_blow", false]) {
                val index = attacker.inventory.items.indexOfFirst { it.id.startsWith("fungicide_spray_") }
                fungicide(attacker, npc, index)
                break
            }
        }
        npc.mode = PauseMode
    }

    fun fungicide(player: Player, target: NPC, index: Int) {
        if (!player.inventory.discharge(player, index)) {
            player.message("You need a fungicide to kill the zygomite.") // TODO proper message
            return
        }
        val hitpoints = target.levels.get(Skill.Constitution)
        player.face(target)
        player.sound("zygomite_spray")
        if (hitpoints >= 80) {
            player.message("The zygomite is not weak enough to succumb to the effects of the fungicide!")
            return
        }
        target.damage(hitpoints, source = player)
        player.message("The zygomite is covered in fungicide. It bubbles away to nothing!", type = ChatType.Filter)
    }
}
