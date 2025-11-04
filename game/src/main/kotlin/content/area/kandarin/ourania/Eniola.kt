package content.area.kandarin.ourania

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.social.trade.lend.Loan.getSecondsRemaining
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.continueDialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.StringSuspension

class Eniola : Script {

    val runes = listOf(
        "air_rune",
        "mind_rune",
        "water_rune",
        "earth_rune",
        "fire_rune",
        "body_rune",
        "cosmic_rune",
        "chaos_rune",
        "astral_rune",
        "law_rune",
        "death_rune",
        "blood_rune",
        "nature_rune",
        "soul_rune",
    )

    init {
        npcOperate("Talk-to", "eniola") {
            npc<Quiz>("Well met, fellow adventurer! How can I help you?")
            val loanReturned = getSecondsRemaining(this, "lend_timeout") < 0
            val collection = false
            if (loanReturned) {
                npc<Talk>("Before we go any further, I should inform you that an item you lent out has been returned to you.")
            } else if (collection) {
                npc<Talk>("Before we go any further, I should inform you that you have items ready for collection from the Grand Exchange.")
            }
            choice {
                option<Quiz>("Who are you?") {
                    npc<Happy>("How frightfully rude of me, my dear chap. My name is Eniola and I work for that most excellent enterprise, the Bank of Gielinor.")
                    choice {
                        option<Quiz>("If you work for the bank, what are you doing here?") {
                            npc<Happy>("My presence here is the start of a new enterprise of travelling banks.")
                            npc<Talk>("I, and others like me, will provide you with the convenience of having banking facilities where they will be of optimum use to you.")
                            player<Uncertain>("So...what are you doing here?")
                            npc<Talk>("The Z.M.I., that is, the Zamorakian Magical Institute, requested my services upon discovery of this altar. We at the Bank of Gielinor are a neutral party and are willing to offer our services regardless of affiliation.")
                            npc<Neutral>("So that is why I am here.")
                            player<Quiz>("Can I access my bank account by speaking to you?")
                            npc<Neutral>("Of course, dear sir.")
                            npc<Upset>("However, I must inform you that because the Z.M.I. are paying for my services, they require anyone not part of the Institute to pay an access fee to open their bank account.")
                            npc<Happy>("But, as our goal as travelling bankers is to make our customers' lives more convenient, we have accomodated to your needs. We know you will be busy creating runes and do not wish to carry money with you. The")
                            npc<Happy>("charge to open your account is the small amount of twenty of one type of rune. The type of rune is up to you.")
                            npc<Quiz>("Would you like to pay the price of twenty runes to open your bank account?")
                            choice {
                                option<Talk>("Yes please.") {
                                    open("ourania_bank_charge")
                                }
                                option("Let me open my account and then I'll give you the runes.") {
                                    player<Quiz>("I don't have the runes on me. Let me open my account and then I'll give them to you.")
                                    npc<Happy>("It's not that I don't trust you, old chap, but as the old adage goes: 'Payment comes before friends.'")
                                }
                                option("No way! I'm not paying to withdraw my own money.") {
                                    player<Angry>("That's preposterous! I'm not paying runes to withdraw my own stuff.")
                                    npc<Happy>("I'm sorry to hear that, sir. If you should reconsider, because I believe this service offers excellent value for money, do not hesitate to contact me.")
                                }
                            }
                        }
                        accessBank()
                        pinSettings()
                        collectionBox()
                    }
                }
                accessBank()
                pinSettings()
                collectionBox()
            }
        }

        npcOperate("Bank", "eniola") {
            openBank()
        }

        npcOperate("Collect", "eniola") {
            openCollection()
        }

        interfaceOpen("ourania_bank_charge") { id ->
            for (rune in runes) {
                interfaces.sendVisibility(id, "${rune}_hide", !inventory.contains(rune, 20))
            }
            interfaces.sendText("ourania_bank_charge", "text", "Choose a highlighted rune to make your payment.")
        }

        continueDialogue("ourania_bank_charge", "*_rune") { player ->
            (player.dialogueSuspension as? StringSuspension)?.resume(component)
        }

        interfaceOption(id = "ourania_bank_charge:*_rune") {
            if (inventory.remove(it.component, 20)) {
                val id = get("ourania_interface", "bank")
                open(id)
            } else {
                queue("not_enough_runes") {
                    npc<Upset>("I'm afraid you don't have the necessary runes with you at this time, so I can't allow you to access your account. Please bring twenty runes of one type and you can open your account.")
                }
            }
        }
    }

    fun ChoiceOption.accessBank() {
        option("I'd like to access my bank account, please.") {
            openBank()
        }
    }

    fun ChoiceOption.collectionBox() {
        option("I'd like to see my collection box.") {
            openCollection()
        }
    }

    fun ChoiceOption.pinSettings() {
        option("I'd like to check my PIN settings.") {
        }
    }

    suspend fun Player.openCollection() {
        if (runePayment()) {
            open("collection_box")
        }
    }

    suspend fun Player.openBank() {
        if (runePayment()) {
            open("bank")
        }
    }

    suspend fun Player.runePayment(): Boolean {
        open("ourania_bank_charge")
        val rune = StringSuspension.get(this)
        close("ourania_bank_charge")

        if (!inventory.remove(rune, 20)) {
            npc<Upset>("I'm afraid you don't have the necessary runes with you at this time, so I can't allow you to access your account. Please bring twenty runes of one type and you can open your account.")
            return false
        }
        return true
    }
}
