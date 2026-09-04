package content.area.karamja.musa_point

import content.entity.obj.ship.boatTravel
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class CustomsOfficer : Script {

    init {
        npcOperate("Talk-to", "customs_officer_brimhaven") { (officer) ->
            npc<Neutral>("Can I help you?")
            choice {
                option<Quiz>("Can I journey on this ship?") {
                    npc<Neutral>("You need to be searched before you can board.")
                    searchMenu(officer, askedWhy = false)
                }
                option("Does Karamja have unusual customs then?") {
                    player<Quiz>("Does Karamja have any unusual customs then?")
                    npc<Neutral>("I'm not that sort of customs officer.")
                }
            }
        }

        npcOperate("Pay-Fare", "customs_officer_brimhaven") { (officer) ->
            if (!questCompleted("pirates_treasure")) {
                return@npcOperate message("You may only use the Pay-fare option after completing Pirate's Treasure.")
            }
            if (confiscateRum()) {
                return@npcOperate
            }
            if (!inventory.remove("coins", 30)) {
                message("You do not have enough money for that.")
                return@npcOperate
            }
            travel(officer)
        }
    }

    private suspend fun Player.searchMenu(officer: NPC, askedWhy: Boolean) {
        choice {
            if (!askedWhy) {
                option<Quiz>("Why?") {
                    npc<Neutral>("Because Asgarnia has banned the import of intoxicating spirits.")
                    searchMenu(officer, askedWhy = true)
                }
            }
            option<Happy>("Search away, I have nothing to hide.") {
                search(officer)
            }
            option<Shock>("You're not putting your hands on my things!") {
                npc<Bored>("You're not getting on this ship then.")
            }
            if (charmed) {
                option("[Charm] You don't need to look through my things.") {
                    player<Neutral>("You don't need to look through my things.")
                    npc<Neutral>("Oh, I think I do. If you don't want to let me search your possessions, then you're not coming aboard this ship.")
                }
            }
        }
    }

    private suspend fun Player.search(officer: NPC) {
        if (inventory.contains("karamjan_rum")) {
            npc<Neutral>("Aha, trying to smuggle rum are we?")
            if (!charmed) {
                confiscateRum()
                return
            }
            choice {
                option("Umm... it's for personal use?") {
                    confiscateRum()
                }
                option("[Charm] This is not the Karamja rum you are looking for.") {
                    player<Neutral>("This is not the Karamja rum you are looking for.")
                    npc<Neutral>("This is not the Karamja rum we are looking for.")
                    player<Laugh>("Well that was easy!")
                    loseRum()
                }
            }
            return
        }
        npc<Neutral>("Well you've got some odd stuff, but it's all legal. Now you need to pay a boarding charge of 30 coins.")
        choice {
            option("Ok.") {
                if (!inventory.remove("coins", 30)) {
                    player<Sad>("Oh dear, I don't seem to have enough money.")
                    return@option
                }
                travel(officer)
            }
            option<Neutral>("Oh, I'll not bother then.")
            if (charmed) {
                option("[Charm] Or perhaps you could let me travel for free?") {
                    player<Neutral>("Or perhaps you could let me travel for free?")
                    npc<Neutral>("Yes, perhaps I could. Okay, jump aboard then.")
                    travel(officer)
                }
            }
        }
    }

    private suspend fun Player.confiscateRum(): Boolean {
        if (!inventory.contains("karamjan_rum")) {
            return false
        }
        player<Shifty>("Umm... it's for personal use?")
        if (!inventory.remove("karamjan_rum", inventory.count("karamjan_rum"))) {
            return true
        }
        message("The customs officer confiscates your rum.")
        message("You will need to find some way to smuggle it off the island...")
        return true
    }

    private suspend fun Player.loseRum() {
        message("You dance for joy...")
        anim("emote_dance")
        delay(4)
        if (!inventory.remove("karamjan_rum", inventory.count("karamjan_rum"))) {
            return
        }
        message("...and accidentally drop the rum.")
        player<Sad>("Drat...")
        message("You will need to try again.")
    }

    private val Player.charmed: Boolean
        get() = equipped(EquipSlot.Ring).id == "ring_of_charos_a"

    private suspend fun Player.travel(officer: NPC) {
        message("You pay 30 coins and board the ship.")
        if (officer.tile in Areas["brimhaven"]) {
            boatTravel("brimhaven_to_ardougne", 5, Tile(2683, 3268, 1))
            statement("The ship arrives at Ardougne.")
        } else {
            boatTravel("karamja_to_port_sarim", 7, Tile(3032, 3217, 1))
            statement("The ship arrives at Port Sarim.")
        }
    }
}
