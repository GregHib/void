package content.area.kandarin.ourania

import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.Id
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.hunt.huntNPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

@Script
class CaveLizard : Api {

    @Id("cave_lizard")
    override fun spawn(npc: NPC) {
        npc.softTimers.start("aggressive_hunt_mode_switch")
    }

    @Timer("aggressive_hunt_mode_switch")
    override fun start(npc: NPC, timer: String, restart: Boolean): Int = random.nextInt(6, 12)

    @Timer("aggressive_hunt_mode_switch")
    override fun tick(npc: NPC, timer: String): Int {
        npc.huntMode = if (random.nextBoolean()) "aggressive" else "aggressive_npcs"
        return super.tick(npc, timer)
    }

    init {
        huntNPC("cave_lizard", "zamorak_*", "aggressive_npcs") { npc ->
            npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
        }

        huntPlayer("cave_lizard", "aggressive") { npc ->
            npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
        }
    }
}
