package content.skill.thieving

import com.github.michaelbull.logging.InlineLogger
import content.entity.effect.stun
import content.entity.npc.combat.NPCAttack
import content.skill.slayer.categories
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.data.Pocket
import world.gregs.voidps.engine.entity.character.mode.interact.approachRange
import world.gregs.voidps.engine.entity.character.mode.interact.delay
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
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.discharge
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.Transaction
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

@Script
class Pickpocketing : Api {

    val animationDefinitions: AnimationDefinitions by inject()
    val dropTables: DropTables by inject()
    val logger = InlineLogger()

    init {
        npcApproach("Pickpocket", block = ::approach)
        npcApproach("Steal-from", block = ::approach)
    }

    private suspend fun approach(player: Player, target: NPC) {
        player.approachRange(2)
        if (player.hasClock("food_delay") || player.hasClock("action_delay")) { // Should action_delay and food_delay be the same??
            return
        }
        if (player.hasClock("in_combat")) {
            player.message("You can't pickpocket during combat.")
            return
        }
        val pocket: Pocket = target.def.getOrNull("pickpocket") ?: return
        if (!player.has(Skill.Thieving, pocket.level)) {
            return
        }
        var chances = pocket.chance
        if (player.equipped(EquipSlot.Hands).id == "gloves_of_silence" && player.equipment.discharge(player, EquipSlot.Hands.index)) {
            chances = (chances.first + (chances.first / 20)).coerceAtMost(255)..(chances.last + (chances.last / 20)).coerceAtMost(255)
        }
        val success = success(player.levels.get(Skill.Thieving), chances)
        val drops = getLoot(target, pocket.table) ?: emptyList()
        if (success && !canLoot(player, drops)) {
            return
        }
        val name = target.def.name
        player.message("You attempt to pick the $name's pocket.", ChatType.Filter)
        player.anim("pick_pocket")
        player.delay(2)
        if (success) {
            player.inventory.transaction {
                addLoot(drops)
            }
            player.message("You pick the $name's pocket.", ChatType.Filter)
            player.exp(Skill.Thieving, pocket.xp)
        } else {
            target.face(player)
            target.say(pocket.caughtMessage)
            target.anim(NPCAttack.anim(animationDefinitions, target, "defend"))
            player.message("You fail to pick the $name's pocket.", ChatType.Filter)
            target.stun(player, pocket.stunTicks, pocket.stunHit.random(random))
            player.delay(2)
        }
    }

    fun getLoot(target: NPC, table: String?): List<ItemDrop>? {
        var table = dropTables.get("${table}_pickpocket")
        if (table != null) {
            return table.role()
        }
        table = dropTables.get("${target.id}_pickpocket")
        if (table != null) {
            return table.role()
        }
        for (category in target.categories) {
            table = dropTables.get("${category}_pickpocket")
            if (table != null) {
                return table.role()
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
