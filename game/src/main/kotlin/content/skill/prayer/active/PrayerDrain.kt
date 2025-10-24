package content.skill.prayer.active

import content.entity.sound.sound
import content.skill.prayer.PrayerConfigs
import content.skill.prayer.getActivePrayerVarKey
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.data.definition.PrayerDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.Timer

@Script
class PrayerDrain : Api {

    val definitions: PrayerDefinitions by inject()
    val variableDefinitions: VariableDefinitions by inject()

    override fun start(player: Player, timer: String, restart: Boolean) = 1

    override fun tick(player: Player, timer: String): Int {
        val equipmentBonus = player["prayer", 0]
        var prayerDrainCounter = player["prayer_drain_counter", 0]

        prayerDrainCounter += getTotalDrainEffect(player)
        val prayerDrainResistance = 60 + (equipmentBonus * 2)
        while (prayerDrainCounter > prayerDrainResistance) {
            player.levels.drain(Skill.Prayer, 1)
            prayerDrainCounter -= prayerDrainResistance
            if (player.levels.get(Skill.Prayer) == 0) {
                player.clear("prayer_drain_counter")
                player.sound("prayer_drain")
                player.message("You have run out of Prayer points; you can recharge at an altar.")
                return Timer.CANCEL
            }
        }
        player["prayer_drain_counter"] = prayerDrainCounter
        return Timer.CONTINUE
    }

    override fun stop(player: Player, timer: String, logout: Boolean) {
        player.clear(player.getActivePrayerVarKey())
        player[PrayerConfigs.USING_QUICK_PRAYERS] = false
    }

    fun getTotalDrainEffect(player: Player): Int {
        val listKey = player.getActivePrayerVarKey()
        val variable = variableDefinitions.get(listKey)
        val values = (variable?.values as BitwiseValues).values
        var total = 0
        for (prayer in values) {
            if (player.containsVarbit(listKey, prayer)) {
                val definition = definitions.get(prayer as String)
                total += definition.drain
            }
        }
        return total
    }
}
