package content.area.kharidian_desert

import content.entity.effect.stun
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.success
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory

class DesertPhoenix : Script {

    init {
        npcOperate("Grab-feather", "desert_phoenix*") { (target) ->
            grabFeather(target)
        }
    }

    private suspend fun Player.grabFeather(target: NPC) {
        if (inventory.contains("phoenix_feather") || inventory.contains("phoenix_quill_pen")) {
            message("You already have a phoenix tail-feather.")
            return
        }
        // food_delay and action_delay stay separate.
        if (hasClock("food_delay") || hasClock("action_delay")) {
            return
        }
        if (hasClock("under_attack")) {
            message("You can't pickpocket during combat.")
            return
        }
        if (!has(Skill.Thieving, 25)) {
            message("You need to be a level 25 thief to grab the phoenix's tail-feather.")
            return
        }
        val success = success(levels.get(Skill.Thieving), IntRange(100, 240))
        message("You attempt to grab the phoenix's tail-feather.", ChatType.Filter)
        delay(1)
        if (success) {
            anim("pick_pocket")
            sound("pick")
            addOrDrop("phoenix_feather")
            message("You grab a tail-feather.", ChatType.Filter)
            exp(Skill.Thieving, 26.0)
            return
        }
        target.say("Squawk!")
        message("You fail to grab the feather.", ChatType.Filter)
        delay(1)
        target.face(this)
        // 6811 and 245 are the dump's firebird_update_attack and stunned_thieving,
        // already named phoenix_attack and stun_long by existing content.
        target.anim("phoenix_attack")
        gfx("stun_long")
        sound("stunned")
        // TODO Defend animation
        target.stun(this, 8, 10)
    }
}
