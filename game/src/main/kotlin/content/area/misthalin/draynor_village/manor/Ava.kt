package content.area.misthalin.draynor_village.manor

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Ava : Script {

    init {
        npcOperate("Talk-to", "ava_2") {
            if (!questCompleted("animal_magnetism")) {
                return@npcOperate
            }
            npc<Confused>("Hello again; I'm busy with my newest research, so can't gossip too much. Are you after information, an upgrade, another device, or would you like to see my goods for sale?")
            choice {
                option<Neutral>("I'd like information, please.") {
                    information()
                }
                option<Neutral>("I seem to need a new device.") {
                    newDevice()
                }
                option<Neutral>("I'd like to upgrade my device, please.") {
                    upgrade()
                }
                option<Neutral>("I'd like to see your stuff for sale, please.") {
                    npc<Neutral>("Here's my shop.")
                    openShop("avas_odds_and_ends")
                }
                option<Neutral>("I'll just head off, I think.") {
                    npc<Neutral>("Thanks for the help with my bed; see you again.")
                }
            }
        }

        npcOperate("Buy device", "ava_2") {
            if (!questCompleted("animal_magnetism")) {
                return@npcOperate
            }
            choice("Which device would you like?") {
                option("The attractor") {
                    newDevice()
                }
                if (levels.getMax(Skill.Ranged) >= 50) {
                    option("The accumulator") {
                        upgrade()
                    }
                }
            }
        }
    }

    private suspend fun Player.information() {
        npc<Neutral>("Just a few bits of information before you run away to persecute rock crabs or cows.")
        npc<Neutral>("First, your new device won't work if you are wearing certain metallic chest armours.")
        npc<Neutral>("The magnetic attraction required for operation would cause feedback, so the device does not allow such incompatible item equippage.")
        npc<Neutral>("Secondly, although the device is calibrated to attract only arrow heads, there is a chance that other magnetically active items will be attracted.")
        npc<Neutral>("The arrow recovery function of the devices is slightly different and relies upon a harmonic bond between your arrows and the chicken's magnet. You'll thus only recover arrows which you have fired rather than those fired by nearby folks.")
        npc<Neutral>("Thirdly, there is an upgraded version available when your Ranged level is 50 or greater.")
        npc<Neutral>("You'll also need to move about if you want to collect arrows since the gathering of long-lost arrowheads can't work otherwise.")
        npc<Neutral>("Finally, the device will only work when worn. It automatically deactivates in other circumstances.")
        npc<Neutral>("I was worried about what would happen if you were to place it in a bank in its active state, so I've made sure it only works when it's on your back.")
    }

    private suspend fun Player.newDevice() {
        player<Neutral>("I need another arrow-attracting device, please.")
        npc<Neutral>("Well, luckily for you, they have a habit of returning to me when people lose them, So I have some spares. They are homing chickens, it seems.")
        npc<Neutral>("I'll need 999gp, however, for the expenses involved in restoring the poor creature to health, or unhealth, or whatever.")
        player<Quiz>("Why 999?")
        npc<Neutral>("Well, it just sounds less expensive than 1000. Do you want that replacement or not?")
        choice {
            option<Happy>("Sounds good to me.") {
                inventory.transaction {
                    remove("coins", 999)
                    add("avas_attractor")
                }
                when (inventory.transaction.error) {
                    TransactionError.None -> npc<Happy>("Here's your device; take good care of your chicken.")
                    is TransactionError.Full -> player<Sad>("Sorry, I don't have enough inventory space.")
                    else -> npc<Sad>("You seem not to have enough cash; you could always sell some of your gear, though.")
                }
            }
            option<Neutral>("I'd prefer not to, actually.") {
                npc<Neutral>("I've better things to do than be irritated by you.")
            }
        }
    }

    private suspend fun Player.upgrade() {
        if (levels.getMax(Skill.Ranged) < 50) {
            npc<Neutral>("I'm afraid you aren't yet skilled enough for the upgraded version. You need a Range level of 50 or greater.")
            return
        }
        if (!inventory.contains("avas_attractor")) {
            npc<Neutral>("You don't have a device in your bags that I can upgrade, I'm afraid.")
            return
        }
        npc<Happy>("You are ready to upgrade. I'll take 75 steel arrows and the old device, if that's all fine with you?")
        choice {
            option<Happy>("Sounds good to me.") {
                if (!inventory.contains("steel_arrow", 75)) {
                    npc<Sad>("I need 75 steel arrows for the upgrade process, I'm afraid.")
                    return@option
                }
                inventory.transaction {
                    remove("coins", 999)
                    remove("steel_arrow", 75)
                    remove("avas_attractor")
                    add("avas_accumulator")
                }
                when (inventory.transaction.error) {
                    TransactionError.None -> npc<Happy>("Here's your upgraded device; take good care of it.")
                    else -> npc<Sad>("You seem not to have enough cash; you could always sell some of your gear, though.")
                }
            }
            option<Neutral>("I'd prefer not to, actually.") {
                npc<Neutral>("I've better things to do than be irritated by you.")
            }
        }
    }
}
