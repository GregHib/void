package content.skill.thieving

import com.github.michaelbull.logging.InlineLogger
import content.entity.effect.stun
import content.entity.npc.combat.NPCAttack
import content.skill.slayer.categories
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.data.Pocket
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcApproach
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.success
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.Transaction
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.event.Script
@Script
class Pickpocketing {

    val animationDefinitions: AnimationDefinitions by inject()
    val dropTables: DropTables by inject()
    val logger = InlineLogger()
    
    init {
        npcApproach("Pickpocket") {
            approachRange(2)
            if (player.hasClock("food_delay") || player.hasClock("action_delay")) { // Should action_delay and food_delay be the same??
                return@npcApproach
            }
            if (player.hasClock("in_combat")) {
                player.message("You can't pickpocket during combat.")
                return@npcApproach
            }
            val pocket: Pocket = target.def.getOrNull("pickpocket") ?: return@npcApproach
            if (!player.has(Skill.Thieving, pocket.level)) {
                return@npcApproach
            }
            val success = success(player.levels.get(Skill.Thieving), pocket.chance)
            val drops = getLoot(target) ?: emptyList()
            if (success && !canLoot(player, drops)) {
                return@npcApproach
            }
            val name = target.def.name
            player.message("You attempt to pick the $name's pocket.", ChatType.Filter)
            player.anim("pick_pocket")
            delay(2)
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
                target.stun(player, pocket.stunTicks, pocket.stunHit)
                delay(2)
            }
        }

    }

    fun getLoot(target: NPC): List<ItemDrop>? {
        var table = dropTables.get("${target.id}_pickpocket")
        if (table == null) {
            for (category in target.categories) {
                table = dropTables.get("${category}_pickpocket")
                if (table != null) {
                    break
                }
            }
        }
        return table?.role()
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
