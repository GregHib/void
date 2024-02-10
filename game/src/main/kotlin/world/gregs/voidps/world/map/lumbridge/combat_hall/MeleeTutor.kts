package world.gregs.voidps.world.map.lumbridge.combat_hall

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.world.activity.bank.ownsItem
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*
import world.gregs.voidps.world.interact.entity.player.display.Tab

npcOperate("Talk-to", "harlan") {
    npc<Unsure>("Greetings adventurer, I am the Melee combat tutor. Is there anything I can do for you?")
    menu()
}

suspend fun CharacterContext.menu(followUp: String = "") {
    if (followUp.isNotEmpty()) {
        npc<Unsure>(followUp)
    }
    choice {
        meleeCombat()
        weaponTypes()
        skillcapes()
        training()
        option("Goodbye.")
    }
}

suspend fun PlayerChoice.meleeCombat(): Unit = option<Unsure>("Tell me about melee combat.") {
    npc<Talking>("Well adventurer, the first thing you will need is a sword and a shield appropriate for your level.")
    // look down talking, look up eyebrow raised then quiet
    player["tab"] = Tab.WornEquipment.name
    npc<Talking>("Make sure to equip your sword and shield. Click on them in your inventory, they will disappear from your inventory and move to your worn items. You can see your worn items in the worn items tab here.")
    player["tab"] = Tab.CombatStyles.name
    npc<Talking>("When you are wielding your sword you will then be able to see the correct options in the combat interface.")
    npc<Talking>("There are four different melee styles. Accurate, aggressive, defensive and controlled. Not all weapons will have all four styles though.")
    player<Unsure>("Interesting, what does each style do?")
    npc<Talking>("Well I am glad you asked. The accurate style will give you experience points in your Attack skill, you will also find you will deal damage more frequently as a result of being, well, more accurate.")
    npc<Talking>("Next we have the aggressive style. This style will give you experience points in your Strength skill. When using this style you will notice that your attacks will hit a little harder.")
    npc<Talking>("Now for the defensive style, this style will give you experience points in your Defensive skill. When using this style you will notice that you get hit less often.")
    npc<Talking>("Finally, we have the controlled style. This style will give you the same amount of experience as the other styles would but shared across all three of the combat skills.")
    npc<Talking>("If you were using the training sword for example, there are four different attack types. Stab, lunge, slash and block.")
    npc<Talking>("Each type uses one of the attack styles. Stab uses accurate, lunge and slash use aggressive and block uses defensive.")
    npc<Talking>("To find out which style an attack type uses, hover your mouse cursor over the style button.")
    npc<Unsure>("Is there anything else you would like to know?")
    choice {
        option("What if I wanted to fight something a bit more... human.") {
            player<Talking>("What if I wanted to fight something a bit more... human.")
            npc<Cheerful>("Well adventurer, there are a few places you might be able to do this.")
            npc<Talking>("You could try your luck at castle wars. Here, two teams fight each other to defend their respective flags. To win the game you will need to get the other team's flag and return it to your flag stand.")
            player<Cheerful>("Capture the flag, sounds like a lot of fun.")
            npc<Talking>("If you are in a clan, you should gather some clan members and try out clan wars. There you can see which clan is better than the other by fighting each other in an arena.")
            npc<Talking>("Both activities are safe minigame, which means if you die you will not loose any of your items. You can get to them by using the teleport option in your minigames tab.")
            npc<Suspicious>("There is also the wilderness. The wilderness is north of Varrock and you can fight other players there. But bare in mind if you die to another player in the wilderness you will lose your stuff.")
            npc<Suspicious>("But this also means that if you kill another player you will be able to take their stuff too.")
            npc<Suspicious>("Only go into the wilderness with items you are willing to lose and pay attention to the wilderness level you are in. The higher the level you go, more player will be able to attack you.")
            npc<Suspicious>("You can find which player can attack you by checking your combat level.")
            npc<Suspicious>("Minus the wilderness level from your combat level to find the lowest level that you can attack, then add the wilderness level to your combat level to find the highest level that you can attack.")
            menu("Is there anything else you would like to know?")
        }
        weaponTypes()
        skillcapes()
        training()
        option("Goodbye.")
    }
}

suspend fun PlayerChoice.weaponTypes(): Unit = option<Talking>("Tell me about different weapon types I can use.") {
    npc<Cheerful>("Well let me see now...There are stabbing type weapons such as daggers, then you have swords which are slashing, maces that have great crushing abilities, battle axes which are powerful.")
    npc<Cheerful>("There are also spears. Spears can be good for Defence and many forms of Attack.")
    npc<Talking>("It depends a lot on how you want to fight. Experiment and find out what is best for you. Never be scared to try out a new weapon; you never know, you might like it!")
    npc<Talking>("While I tried all of them for a while, I settled on this rather good sword.")
    npc<Talking>("You might also find that different weapon types are more accurate against different monsters.")
    menu("Is there anything else you would like to know?")
}

suspend fun PlayerChoice.skillcapes(): Unit = option<Talking>("Tell me about skillcapes.") {
    if (player.levels.getMax(Skill.Defence) < Level.MAX_LEVEL) {
        npc<Talking>("Of course. Skillcapes are a symbol of achievement. Only people who have mastered a skill and reached level 99 can get their hands on them and gain the benefits they carry.")
        npc<Talking>("The Cape of Defence will act as ring of life, saving you from combat if your hitpoints become low.")
        menu("Is there anything else you would like to know?")
        return@option
    }
    npc<Talking>("Ah, but I can see you're already a master in the fine art of Defence. Perhaps you have come to me to purchase a Skillcape of Defence, and thus join the elite few who have mastered this exacting skill?")
    npc<Talking>("In recognition of your defensive abilities, when you have it equipped it will act as ring of life, saving you from combat if your hitpoints become low.")
    choice {
        option("May I buy a Skillcape of Defence, please?") {
            buySkillcape()
        }
        option("Can I ask about something else?") {
            choice {
                option("Skillcape") {
                    buySkillcape()
                }
                option("Hood") {
                    player<Unsure>("May I have another hood for my cape, please?")
                    npc<Talking>("Most certainly, and free of charge!")
                    item("defence_hood", 400, "The tutor hands you another hood for your skillcape.")
                    player.inventory.add("defence_hood")
                }
            }
        }
    }
}

suspend fun CharacterContext.buySkillcape() {
    player<Unsure>("May I buy a Skillcape of Defence, please?")
    npc<Talking>("You wish to join the elite defenders of this world? I'm afraid such things do not come cheaply - in fact they cost 99000 coins, to be precise!")
    choice {
        option<Unsure>("99000 coins? That's much too expensive.") {
            npc<Talking>("Not at all; there are many other adventurers who would love the opportunity to purchase such a prestigious item! You can find me here if you change your mind.")
        }
        option("I think I have the money right here, actually.") {
            player<Cheerful>("I think I have the money right here, actually.")
            player.inventory.transaction {
                remove("coins", 99000)
                add("defence_hood")
                val trimmed = Skill.entries.any { it != Skill.Defence && player.levels.getMax(it) >= Level.MAX_LEVEL }
                add("defence_skillcape${if (trimmed) "_t" else ""}")
            }
            when (player.inventory.transaction.error) {
                TransactionError.None -> npc<Cheerful>("Excellent! Wear that cape with pride my friend.")
                is TransactionError.Deficient -> {
                    player<Upset>("But, unfortunately, I was mistaken.")
                    npc<Talking>("Well, come back and see me when you do.")
                }
                is TransactionError.Full, is TransactionError.Invalid -> {
                    npc<Upset>("Unfortunately all Skillcapes are only available with a free hood, it's part of a skill promotion deal; buy one get one free, you know. So you'll need to free up some inventory space before I can sell you one.")
                }
            }
        }
    }
}

suspend fun PlayerChoice.training(): Unit = option<Talking>("I'd like a training sword and shield.") {
    if (player.ownsItem("training_sword") || player.ownsItem("training_shield")) {
        npc<Unsure>("You already have a training sword and shield. Save some for the other adventurers.")
        menu("Is there anything else I can help you with?")
        return@option
    }

    if (player.inventory.spaces < 2) {
        npc<Upset>("You don't have enough space for me to give you a training sword, nor a shield.")
        menu("Is there anything else I can help you with?")
        return@option
    }

    item("training_sword", 800, "Harlan gives you a Training sword.")
    player.inventory.add("training_sword")
    item("training_shield", 800, "Harlan gives you a Training shield.")
    player.inventory.add("training_shield")
    npc<Talking>("There you go, use it well.")
    menu("Is there anything else I can help you with?")
}
