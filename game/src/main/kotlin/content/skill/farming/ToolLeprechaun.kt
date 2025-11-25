package content.skill.farming

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.bank.noted
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

class ToolLeprechaun : Script {

    val logger = InlineLogger()

    init {
        npcOperate("Talk-to", "tool_leprechaun*") {
            npc<Happy>("Ah, 'tis a foine day to be sure! Were yez wantin' me to store yer tools, or maybe ye might be wantin' yer stuff back from me?")
            choice("What would you like to say?") {
                yesPlease()
                option<Talk>("What can you store?") {
                    npc<Happy>("We'll hold onto yer rake, yer seed dibber, yer spade, yer secateurs, yer waterin' can and yer trowel - but mind it's not one of them fancy trowels only archaeologists use!")
                    npc<RollEyes>("We'll take a few buckets off yer hands too, and even yer compost, supercompost an' ultracompost! Also plant cure vials.")
                    npc<Happy>("Aside from that, if ye hands me yer farming produce, I can mebbe change it into banknotes for ye.")
                    npc<Quiz>("So... do ye want to be using the store?")
                    choice("What would you like to say?") {
                        yesPlease()
                        whatDoYouDo()
                        noThanks()
                    }
                }
                whatDoYouDo()
                noThanks()
            }
        }

        npcOperate("Exchange", "tool_leprechaun*") {
            open("farming_equipment_store")
        }

        itemOnNPCOperate("*", "tool_leprechaun*") {
            val item = it.item
            val noted = item.noted
            if (noted == null) {
                npc<Talk>("Nay, there's no such thing as a banknote for that.")
                return@itemOnNPCOperate
            }
            if (!item.def["compostable", false]) {
                npc<Talk>("Nay, I've got no banknotes to exchange for that item.")
                return@itemOnNPCOperate
            }
            var removed = 0
            inventory.transaction {
                removed = removeToLimit(item.id, 28)
                add(noted.id, removed)
            }
            when (inventory.transaction.error) {
                TransactionError.None -> {
                    AuditLog.event(this, "noted", item.id, removed)
                    statement("The leprechaun exchanges your items for banknotes.")
                }
                else -> logger.warn { "Issue exchanging noted item $item" }
            }
        }
    }

    private fun ChoiceOption.yesPlease() {
        option("Yes please.") {
            open("farming_equipment_store")
        }
    }

    private fun ChoiceOption.noThanks() {
        option<Talk>("No thanks, I'll keep hold of my stuff.") {
            npc<Chuckle>("Ye must be dafter than ye look if ye likes luggin' yer tools everywhere ye goes!")
        }
    }

    private fun ChoiceOption.whatDoYouDo() {
        option("What do you do with the tools you're storing?") {
            player<Quiz>("What do you do with the tools you're storing? They can't possibly all fit in your pockets!")
        }
    }
}
