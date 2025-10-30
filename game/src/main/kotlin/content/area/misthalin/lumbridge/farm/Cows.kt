package content.area.misthalin.lumbridge.farm

import content.entity.effect.stun
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

class Cows : Script {

    init {
        npcSpawn("cow_*") {
            softTimers.start("eat_grass")
        }

        npcTimerStart("eat_grass") {
            mode = EmptyMode
            random.nextInt(50, 200)
        }

        npcTimerTick("eat_grass") {
            if (mode == EmptyMode) {
                say("Moo")
                anim("cow_eat_grass")
            }
            Timer.CONTINUE
        }

        itemOnNPCOperate("*", "cow*") {
            player.message("The cow doesn't want that.")
        }

        objectOperate("Steal-cowbell", "dairy_cow") {
            if (!has(Skill.Thieving, 15)) {
                return@objectOperate
            }
            if (!questCompleted("cold_war")) {
                statement("You need to have started the Cold War quest to attempt this.")
                return@objectOperate
            }
            if (!Level.success(levels.get(Skill.Thieving), 128, 200)) {
                message("The cow kicks you and stuns you.")
                stun(this, 8, 10)
                return@objectOperate
            }
            inventory.add("cowbells")
        }
    }
}
