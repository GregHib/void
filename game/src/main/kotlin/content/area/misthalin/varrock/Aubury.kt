package content.area.misthalin.varrock

import content.entity.npc.shop.openShop
import content.entity.player.bank.bank
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import content.quest.questCompleted
import content.skill.runecrafting.EssenceMine
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Aubury : Script {

    init {
        npcOperate("Talk-to", "aubury") { (target) ->
            if (quest("rune_mysteries") == "research_notes") {
                checkNotes()
                return@npcOperate
            }
            npc<Happy>("Do you want to buy some runes?")
            choice {
                skillcapes()
                openShop()
                if (quest("rune_mysteries") == "research_package") {
                    packageForYou()
                } else if (quest("rune_mysteries") == "package_delivered") {
                    option<Quiz>("Anything useful in that package I gave you?") {
                        npc<Happy>("Well, let's have a look...")
                        researchPackage()
                    }
                }
                noThanks()
                if (questCompleted("rune_mysteries")) {
                    teleport(target)
                }
            }
        }

        npcOperate("Teleport", "aubury") { (target) ->
            EssenceMine.teleport(target, this)
        }
    }

    fun ChoiceOption.openShop(): Unit = option<Happy>("Yes please!") {
        openShop("auburys_rune_shop")
    }

    fun ChoiceOption.noThanks(message: String = "Oh, it's a rune shop. No thank you, then."): Unit = option<Idle>(message) {
        npc<Happy>("Well, if you find someone who does want runes, please send them my way.")
    }

    fun ChoiceOption.teleport(npc: NPC): Unit = option("Can you teleport me to the Rune Essence?") {
        npc<Idle>("Of course. By the way, if you end up making any runes from the essence you mine, I'll happily buy them from you.")
        EssenceMine.teleport(npc, this)
    }

    fun ChoiceOption.packageForYou(): Unit = option<Idle>("I've been sent here with a package for you.") {
        npc<Confused>("A package? From who?")
        player<Idle>("From Sedridor at the Wizards' Tower.")
        npc<Shock>("From Sedridor? But... surely, he can't have? Please, let me have it. It must be extremely important for him to have sent a stranger.")
        if (ownsItem("research_package_rune_mysteries")) {
            set("rune_mysteries", "package_delivered")
            inventory.remove("research_package_rune_mysteries")
            item("research_package_rune_mysteries", 600, "You hand the package to Aubury.")
            npc<Happy>("Now, let's have a look...")
            researchPackage()
        } else {
            player<Confused>("Uh... yeah... about that... I kind of don't have it with me...")
            npc<Shock>("What kind of person says they have a delivery for me, but not with them? Honestly.")
            npc<Idle>("Come back when you have it.")
        }
    }

    suspend fun Player.researchPackage() {
        item("research_package_rune_mysteries", 600, "Aubury goes through the package of research notes.")
        npc<Shock>("This... this is incredible.")
        npc<Happy>("My gratitude to you adventurer for bringing me these research notes. Thanks to you, I think we finally have it.")
        player<Quiz>("You mean the incantation?")
        npc<Happy>("Well when we combine my own research with this latest discovery, I think we might just...")
        npc<Idle>("No, no, I'm getting ahead of myself. The signs are promising, but let's not jump to any conclusions just yet.")
        npc<Quiz>("Here, take these notes back to Sedridor. They should hopefully give him everything he needs.")
        if (inventory.isFull()) {
            item("research_notes_rune_mysteries", 600, "Aubury tries to hand you some research notes, but you don't have enough room to take them.")
            return
        }
        set("rune_mysteries", "research_notes")
        inventory.add("research_notes_rune_mysteries")
        item("research_notes_rune_mysteries", 600, "Aubury hands you some research notes.")
    }

    suspend fun Player.checkNotes() {
        npc<Quiz>("Hello. Did you take those notes back to Sedridor?")
        if (inventory.contains("research_notes_rune_mysteries")) {
            player<Idle>("I'm still working on it.")
            npc<Idle>("Don't take too long. He'll be eager to see if this is indeed the breakthrough we were hoping for.")
            npc<Quiz>("Now, did you want to buy some runes?")
            choice {
                openShop()
                noThanks("No thank you.")
            }
        } else {
            player<Disheartened>("Sorry, but I lost them.")
            npc<Idle>("Well, luckily I have duplicates. It's a good thing they are written in code. I wouldn't want the wrong kind of person to get access to the information contained within.")
            if (inventory.isFull()) {
                item("research_notes_rune_mysteries", 600, "Aubury tries to hand you some research notes, but you don't have enough room to take them.")
                return
            }
            if (bank.contains("research_notes_rune_mysteries")) {
                bank.remove("research_notes_rune_mysteries")
            }
            inventory.add("research_notes_rune_mysteries")
            item("research_notes_rune_mysteries", 600, "Aubury hands you some research notes.")
        }
    }

    fun ChoiceOption.skillcapes(): Unit = option("Can you tell me about your cape?") {
        npc<Happy>("Certainly! Skillcapes are a symbol of achievement. Only people who have mastered a skill and reached level 99 can get their hands on them and gain the benefits they carry.")
        npc<Idle>("The Cape of Runecrafting has been upgraded with each talisman, allowing you to access all Runecrafting altars. Is there anything else I can help you with?")
        choice {
            option<Happy>("I'd like to view your store please.") {
                openShop("runecrafting_skillcape")
            }
            noThanks("No thank you.")
        }
    }
}
