package content.bot.behaviour.condition

import content.entity.player.bank.bank
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory

data class BotOwnsItem(val id: String, val min: Int? = null, val max: Int? = null) : Condition(110) {
    override fun keys() = setOf("item:$id")
    override fun events() = setOf("inv:worn_equipment", "inv:inventory", "inv:bank")
    override fun check(player: Player): Boolean {
        val count = player.inventory.count(id) + player.bank.count(id) + player.equipment.count(id)
        return inRange(count, min, max)
    }
}
