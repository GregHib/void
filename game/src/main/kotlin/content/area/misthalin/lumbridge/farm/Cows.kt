package content.area.misthalin.lumbridge.farm

import content.entity.effect.stun
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

@Script
class Cows : Api {

    @Timer("eat_grass")
    override fun start(npc: NPC, timer: String, restart: Boolean): Int {
        npc.mode = EmptyMode
        return random.nextInt(50, 200)
    }

    @Timer("eat_grass")
    override fun tick(npc: NPC, timer: String): Int {
        if (npc.mode == EmptyMode) {
            npc.say("Moo")
            npc.anim("cow_eat_grass")
        }
        return Timer.CONTINUE
    }

    init {
        npcSpawn("cow_*") { npc ->
            npc.softTimers.start("eat_grass")
        }

        itemOnNPCOperate("*", "cow*") {
            player.message("The cow doesn't want that.")
        }

        objectOperate("Steal-cowbell", "dairy_cow") {
            if (!player.has(Skill.Thieving, 15)) {
                return@objectOperate
            }
            if (!player.questCompleted("cold_war")) {
                statement("You need to have started the Cold War quest to attempt this.")
                return@objectOperate
            }
            if (!Level.success(player.levels.get(Skill.Thieving), 128, 200)) {
                player.message("The cow kicks you and stuns you.")
                player.stun(player, 8, 10)
                return@objectOperate
            }
            player.inventory.add("cowbells")
        }
    }
}
