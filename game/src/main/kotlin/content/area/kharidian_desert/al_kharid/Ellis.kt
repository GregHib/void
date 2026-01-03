package content.area.kharidian_desert.al_kharid

import content.entity.player.dialogue.Disheartened
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.intEntry
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Tanning
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class Ellis : Script {

    val itemDefs: ItemDefinitions by inject()

    init {
        npcOperate("Talk-to", "ellis,tanner") {
            npc<Neutral>("Greetings friend. I am a manufacturer of leather.")
            if (inventory.items.none { it.id == "cowhide" || it.id.startsWith("snake_hide") || it.id.endsWith("dragonhide") }) {
                leather()
                return@npcOperate
            }
            npc<Neutral>("I see you have bought me some hides. Would you like me to tan them for you?")
            choice {
                option("Yes please.") {
                    player<Neutral>("Yes please.")
                    open("tanner")
                }
                option("No thanks.") {
                    player<Disheartened>("No thanks.")
                    npc<Neutral>("Very well, ${if (male) "sir" else "madam"}, as you wish.")
                }
            }
        }

        npcOperate("Trade", "ellis,tanner") {
            open("tanner")
        }

        interfaceOption(id = "tanner:*") {
            val amount = when (it.option.lowercase()) {
                "tan ${Colours.ORANGE.toTag()}1" -> 1
                "tan ${Colours.ORANGE.toTag()}5" -> 5
                "tan ${Colours.ORANGE.toTag()}10" -> 10
                "tan ${Colours.ORANGE.toTag()}all" -> inventory.count(it.component.removeSuffix("_1"))
                "tan ${Colours.ORANGE.toTag()}X" -> intEntry("Enter amount:").also { int ->
                    set("last_bank_amount", int)
                }
                else -> return@interfaceOption
            }
            tan(this, it.component, amount)
        }

        interfaceClosed("tanner") {
            sendScript("clear_dialogues")
        }
    }

    suspend fun Player.leather() {
        choice("What would you like to say?") {
            option<Quiz>("Can I buy some leather then?") {
                npc<Neutral>("I make leather from animal hides. Bring me some cowhides and one gold coin per hide, and I'll tan them into soft leather for you.")
            }
            option<Neutral>("Leather is rather weak stuff.") {
                npc<Neutral>("Normal leather may be quite weak, but it's very heap - I make it from cowhides for only 1 gp per hide - and it's so easy to craft that anyone can work with it.")
                npc<Neutral>("Alternatively you could try hard leather. It's not so easy to craft, but I only charge 3 gp per cowhide to prepare it, and it makes much sturdier armour.")
                npc<Happy>("I can also tan snake hides and dragonhides, suitable for crafting into the highest quality armour for rangers.")
                player<Neutral>("Thanks, I'll bear it in mind.")
            }
        }
    }

    fun tan(player: Player, type: String, amount: Int) {
        val item = type.removeSuffix("_1")
        if (!player.holdsItem(item)) {
            player.message("You don't have any ${item.toLowerSpaceCase()} to tan.")
            return
        }
        player.softTimers.start("tanning")
        val tanning: Tanning = itemDefs.get(item)["tanning"]
        val (leather, cost) = tanning.prices[if (type.endsWith("_1")) 1 else 0]
        var tanned = 0
        var noHides = false
        for (i in 0 until amount) {
            if (!player.inventory.transaction {
                    replace(item, leather)
                    if (failed) {
                        noHides = true
                    }
                    remove("coins", cost)
                }
            ) {
                break
            }
            tanned++
        }
        player.softTimers.stop("tanning")
        if (tanned == 1) {
            player.message("The tanner tans your ${item.toLowerSpaceCase()}.")
        } else if (tanned > 0) {
            player.message("The tanner tans $tanned ${item.toLowerSpaceCase().plural(tanned)} for you.")
        }
        if (noHides) {
            player.message("You have run out of ${item.plural().toLowerSpaceCase()}.")
        } else if (tanned < amount) {
            player.message("You haven't got enough coins to pay for ${if (tanned == 0) "" else "more "}${leather.toLowerSpaceCase()}.")
        }
    }
}
