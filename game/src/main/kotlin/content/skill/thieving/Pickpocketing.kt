package content.skill.thieving

import com.github.michaelbull.logging.InlineLogger
import content.entity.effect.stun
import content.skill.slayer.categories
import content.skill.summoning.familiarBoost
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.success
import world.gregs.voidps.engine.entity.item.drop.DropTable
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
        // food_delay and action_delay stay separate.
        if (hasClock("food_delay") || hasClock("action_delay")) {
            return
        }
        if (hasClock("under_attack")) {
            message("You can't pickpocket during combat.")
            return
        }
        val type = Tables.stringOrNull("pickpocket.${target.id}.type") ?: return
        val pickpocket = Rows.getOrNull("thieving_types.$type") ?: return
        val level = pickpocket.int("level")
        if (!has(Skill.Thieving, level)) {
            return
        }
        var chances = pickpocket.intRange("chance")
        if (equipped(EquipSlot.Hands).id == "gloves_of_silence" && equipment.discharge(this, EquipSlot.Hands.index)) {
            chances = (chances.first + (chances.first / 20)).coerceAtMost(255)..(chances.last + (chances.last / 20)).coerceAtMost(255)
        }
        val success = success(levels.get(Skill.Thieving) + familiarBoost(Skill.Thieving), chances)
        val table = pickpocket.string("table")
        val multiplier = lootMultiplier(pickpocket, level)
        val drops = getLoot(target, table, multiplier) ?: emptyList()
        if (success && !canLoot(this, drops)) {
            return
        }
        val name = target.def.name
        message("You attempt to pick the $name's pocket.", ChatType.Filter)
        anim(
            when (multiplier) {
                4 -> "pickpocket_quad"
                3 -> "pickpocket_triple"
                2 -> "pickpocket_double"
                else -> "pick_pocket"
            },
        )
        when (multiplier) {
            4 -> gfx("pickpocket_quad")
            3 -> gfx("pickpocket_triple")
            2 -> gfx("pickpocket_double")
        }
        delay(2)
        if (success) {
            inventory.transaction {
                addLoot(drops)
            }
            message(
                when (multiplier) {
                    4 -> "Your lighting-fast reactions allow you to steal quadruple loot."
                    3 -> "Your lighting-fast reactions allow you to steal triple loot."
                    2 -> "Your lighting-fast reactions allow you to steal double loot."
                    else -> "You pick the $name's pocket."
                },
                ChatType.Filter,
            )
            val xp = pickpocket.int("xp") / 10.0
            exp(Skill.Thieving, xp)
        } else {
            target.face(this)
            target.say("What do you think you're doing?")
            target.anim(combatDefinitions.get(target["combat_def", target.id]).defendAnim)
            message("You fail to pick the $name's pocket.", ChatType.Filter)
            val ticks = pickpocket.int("stun_ticks")
            val damage = pickpocket.intRange("damage")
            target.stun(this, ticks, damage.random(random))
            delay(2)
        }
    }

    private fun Player.lootMultiplier(pickpocket: RowDefinition, level: Int): Int {
        if (!pickpocket.bool("multiple") || random.nextInt(Settings["thieving.pickpocket.multiChance", 10]) != 0) { // Unknown rates
            return 1
        }
        return when {
            has(Skill.Thieving, level + 30) && has(Skill.Agility, level + 20) -> 4
            has(Skill.Thieving, level + 20) && has(Skill.Agility, level + 10) -> 3
            has(Skill.Thieving, level + 10) && has(Skill.Agility, level) -> 2
            else -> 1
        }
    }

    fun getTable(target: NPC, table: String?): DropTable? {
        var dropTable = dropTables.get("${table}_pickpocket")
        if (dropTable != null) {
            return dropTable
        }
        dropTable = dropTables.get("${target.id}_pickpocket")
        if (dropTable != null) {
            return dropTable
        }
        for (category in target.categories) {
            dropTable = dropTables.get("${category}_pickpocket")
            if (dropTable != null) {
                return dropTable
            }
        }
        return null
    }

    fun getLoot(target: NPC, table: String?, multiplier: Int): List<ItemDrop>? {
        val table = getTable(target, table) ?: return null
        val list = mutableListOf<ItemDrop>()
        for (i in 0 until multiplier) {
            table.roll(list = list)
        }
        return list
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
