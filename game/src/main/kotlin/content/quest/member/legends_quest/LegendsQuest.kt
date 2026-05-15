package content.quest.member.legends_quest

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class LegendsQuest : Script {
    val logger = InlineLogger()

    init {
        itemOption("Enchant-Vials", "binding_book") {
            item("binding_book", "You prepare an incantation from the page...")
            if (levels.get(Skill.Prayer) < 10) {
                statement("You need a Prayer level of at least 10 to cast this enchantment.")
                return@itemOption
            }
            if (levels.get(Skill.Magic) < 10) {
                statement("You need a Magic level of at least 10 to cast this enchantment.")
                return@itemOption
            }
            val vials = inventory.count("vial")
            if (vials <= 0) {
                statement("However, you don't have the right components to cast this spell.")
                return@itemOption
            }
            choice {
                option("Enchant 1 vial") {
                    enchant(1)
                }
                if (vials >= 5) {
                    option("Enchant 5 vials") {
                        enchant(5)
                    }
                }
                if (vials >= 10) {
                    option("Enchant 10 vials") {
                        enchant(10)
                    }
                }
            }
        }
    }

    private suspend fun Player.enchant(count: Int) {
        delay(3)
        anim("charge")
        delay(1)
        inventory.transaction {
            repeat(count) {
                replace("vial", "enchanted_vial")
            }
        }
        when (inventory.transaction.error) {
            TransactionError.None -> {
                levels.drain(Skill.Prayer, 5 * count)
                levels.drain(Skill.Magic, 5 * count)
                exp(Skill.Magic, 5.0 * count)
                if (count == 1) {
                    message("You enchant a vial!")
                } else {
                    message("You enchant some vials!")
                }
            }
            else -> logger.warn { "Error enchanting vials: ${inventory.transaction.error}" }
        }
    }
}
