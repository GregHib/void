package content.quest.free.vampire_slayer

import content.entity.combat.killer
import content.quest.quest
import content.quest.questComplete
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Death.OnDeath
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

class CountDraynor : Script {
    init {
        npcTimerStart("vampire_slayer_count") { 2 }

        npcTimerTick("vampire_slayer_count") { regenerate() }

        npcCanDie("count_draynor_normal") { canDie() }

        npcDeath("count_draynor_normal") { death -> completeQuest(death) }

        npcDespawn("count_draynor_normal") { clearOwner() }

        playerDespawn { removeCount() }

        playerDeath { removeCount() }
    }

    private fun NPC.regenerate(): Int {
        val owner = Players.findByAccount(get("owner", ""))
        if (owner == null || owner.dead || owner.quest("vampire_slayer") != "armed" || owner.tile.level != tile.level || owner.tile.distanceTo(tile) > 16) {
            NPCs.remove(this)
            return Timer.CANCEL
        }
        // Garlic suppresses regeneration; the existing rate is unchanged by this refactor.
        if (random.nextInt(7) == 0) {
            levels.set(Skill.Constitution, (levels.get(Skill.Constitution) + if (owner.carriesItem("garlic")) 10 else 100).coerceAtMost(350))
        }
        return Timer.CONTINUE
    }

    private fun NPC.canDie(): Boolean {
        val owner = Players.findByAccount(get("owner", ""))
        if (owner != null && owner.quest("vampire_slayer") == "armed" && owner.hasVampireTools()) {
            return true
        }
        levels.set(Skill.Constitution, 350)
        owner?.message("The vampyre regenerates. You need a stake and hammer to finish him off.")
        return false
    }

    private fun NPC.completeQuest(death: OnDeath) {
        death.respawn = false
        death.dropItems = false
        val player = killer as? Player ?: return
        if (player.accountName != get<String>("owner") || player.quest("vampire_slayer") != "armed" || !player.hasVampireTools()) {
            return
        }
        player.reward()
    }

    private fun Player.reward() {
        if (!consumeStake()) {
            return
        }
        set("vampire_slayer", "completed")
        inc("quest_points", 3)
        exp(Skill.Attack, 4825.0)
        clear("vampire_slayer_count")
        message("You drive the stake through Count Draynor's heart.")
        message("Congratulations, you've completed a quest: <col=081190>Vampire Slayer</col>.")
        queue("vampire_slayer_reward", 8) {
            jingle("quest_complete_1")
            questComplete("Vampire Slayer", "3 Quest Points", "4,825 Attack XP.", item = "stake")
        }
    }

    private fun NPC.clearOwner() {
        val owner = Players.findByAccount(get("owner", ""))
        if (owner?.get<NPC>("vampire_slayer_count") === this) {
            owner.clear("vampire_slayer_count")
        }
    }

    private fun Player.removeCount() {
        NPCs.remove(get<NPC>("vampire_slayer_count"))
        clear("vampire_slayer_count")
    }

    private fun Player.consumeStake(): Boolean {
        if (inventory.remove("stake")) {
            return true
        }
        return equipment.remove("stake")
    }

    companion object {
        internal fun Player.hasVampireTools(): Boolean = carriesItem("stake") && (carriesItem("stake_hammer") || carriesItem("hammer"))
    }
}
