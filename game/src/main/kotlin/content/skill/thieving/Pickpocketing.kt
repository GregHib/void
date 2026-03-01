package content.skill.thieving

import com.github.michaelbull.logging.InlineLogger
import content.entity.effect.stun
import content.skill.slayer.categories
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.toIntRange
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.success
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.Transaction
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class Pickpocketing(val combatDefinitions: CombatDefinitions, val dropTables: DropTables) : Script {

    val logger = InlineLogger()

    init {
        npcApproach("Pickpocket") { (target) ->
            approach(target)
        }

        npcApproach("Steal-from") { (target) ->
            approach(target)
        }
    }

    private suspend fun Player.approach(target: NPC) {
        approachRange(2)
        if (hasClock("food_delay") || hasClock("action_delay")) { // Should action_delay and food_delay be the same??
            return
        }
        if (hasClock("under_attack")) {
            message("You can't pickpocket during combat.")
            return
        }
        val type = EnumDefinitions.stringOrNull("pickpocket_type", target.id) ?: return
        val level = EnumDefinitions.int("pickpocket_level", type)
        if (!has(Skill.Thieving, level)) {
            return
        }
        var chances = EnumDefinitions.string("pickpocket_chance", type).toIntRange()
        if (equipped(EquipSlot.Hands).id == "gloves_of_silence" && equipment.discharge(this, EquipSlot.Hands.index)) {
            chances = (chances.first + (chances.first / 20)).coerceAtMost(255)..(chances.last + (chances.last / 20)).coerceAtMost(255)
        }
        val success = success(levels.get(Skill.Thieving), chances)
        val table = EnumDefinitions.stringOrNull("pickpocket_table", type)
        val drops = getLoot(target, table) ?: emptyList()
        if (success && !canLoot(this, drops)) {
            return
        }
        val name = target.def.name
        message("You attempt to pick the $name's pocket.", ChatType.Filter)
        anim("pick_pocket")
        delay(2)
        if (success) {
            inventory.transaction {
                addLoot(drops)
            }
            message("You pick the $name's pocket.", ChatType.Filter)
            val xp = EnumDefinitions.int("pickpocket_xp", type) / 10.0
            exp(Skill.Thieving, xp)
        } else {
            target.face(this)
            target.say("What do you think you're doing?")
            target.anim(combatDefinitions.get(target["combat_def", target.id]).defendAnim)
            message("You fail to pick the $name's pocket.", ChatType.Filter)
            val ticks = EnumDefinitions.int("pickpocket_stun_ticks", type)
            val damage = EnumDefinitions.string("pickpocket_damage", type).toIntRange()
            target.stun(this, ticks, damage.random(random))
            delay(2)
        }
    }

    fun getLoot(target: NPC, table: String?): List<ItemDrop>? {
        if (table == null) {
            return null
        }
        var id = dropTables.get("${table}_pickpocket")
        if (id != null) {
            return id.roll()
        }
        id = dropTables.get("${target.id}_pickpocket")
        if (id != null) {
            return id.roll()
        }
        for (category in target.categories) {
            id = dropTables.get("${category}_pickpocket")
            if (id != null) {
                return id.roll()
            }
        }
        return null
    }

    fun canLoot(player: Player, drops: List<ItemDrop>): Boolean {
        val transaction = player.inventory.transaction
        transaction.start()
        transaction.addLoot(drops)
        transaction.revert()
        when (transaction.error) {
            is TransactionError.Full -> player.inventoryFull()
            TransactionError.None -> return true
            else -> logger.warn { "Unable to add pickpocket loot $player $drops" }
        }
        return false
    }

    fun Transaction.addLoot(drops: List<ItemDrop>) {
        for (drop in drops) {
            val item = drop.toItem()
            if (item.isEmpty()) {
                continue
            }
            add(item.id, item.amount)
        }
    }
}
