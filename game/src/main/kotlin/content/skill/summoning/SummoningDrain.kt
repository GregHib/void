package content.skill.summoning

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.timer.Timer

/**
 * Drains the player's summoning points over their familiar's lifetime. Over the whole life the
 * total drained equals `level required - summon cost + 1`, spread evenly across the familiar's
 * `summoning_time_minutes` (100 ticks per minute), mirroring the live game. When points hit zero
 * the familiar is dismissed early.
 *
 * Mirrors [content.skill.prayer.active.PrayerDrain]: a per-tick timer with an integer accumulator,
 * draining a point each time the accumulator crosses the familiar's lifetime in ticks.
 */
class SummoningDrain : Script {

    init {
        timerStart("summoning_drain") { 1 }
        timerTick("summoning_drain", ::drain)
        timerStop("summoning_drain") {
            clear("summoning_drain_counter")
        }
    }

    fun drain(player: Player): Int {
        val familiar = player.follower ?: return Timer.CANCEL
        val maxTicks = familiar.def["summoning_time_minutes", 0] * 100
        if (maxTicks <= 0) {
            return Timer.CONTINUE
        }
        val pouchId = EnumDefinitions.get("summoning_familiar_ids").getKey(familiar.def.id)
        val levelRequired = EnumDefinitions.get("summoning_pouch_levels").int(pouchId)
        val summonCost = ItemDefinitions.get(pouchId)["summon_points", 0]
        val drainTotal = levelRequired - summonCost + 1
        if (drainTotal <= 0) {
            return Timer.CONTINUE
        }

        var counter = player["summoning_drain_counter", 0] + drainTotal
        while (counter >= maxTicks) {
            player.levels.drain(Skill.Summoning, 1)
            counter -= maxTicks
            if (player.levels.get(Skill.Summoning) == 0) {
                player.clear("summoning_drain_counter")
                player.message("You have run out of summoning points and your familiar vanishes.")
                player.dismissFamiliar()
                return Timer.CANCEL
            }
        }
        player["summoning_drain_counter"] = counter
        return Timer.CONTINUE
    }
}
