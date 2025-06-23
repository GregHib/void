package content.area.misthalin.lumbridge.combat_hall

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.suspend.SuspendableContext

npcOperate("Talk-to", "harlan") {
    npc<Quiz>("Greetings adventurer, I am the Melee combat tutor. Is there anything I can do for you?")
    menu()
}

suspend fun SuspendableContext<Player>.menu(followUp: String = "") {
    if (followUp.isNotEmpty()) {
        npc<Quiz>(followUp)
    }
    choice {
        meleeCombat()
        weaponTypes()
        skillcapes()
        training()
        option("Goodbye.")
    }
}

suspend fun PlayerChoice.meleeCombat(): Unit = option<Quiz>("Tell me about melee combat.") {
    npc<Neutral>("Well adventurer, the first thing you will need is a sword and a shield appropriate for your level.")
    // look down talking, look up eyebrow raised then quiet
    player.tab(Tab.WornEquipment)
    npc<Neutral>("Make sure to equip your sword and shield. Click on them in your inventory, they will disappear from your inventory and move to your worn items. You can see your worn items in the worn items tab here.")
    player.tab(Tab.CombatStyles)
    npc<Neutral>("When you are wielding your sword you will then be able to see the correct options in the combat interface.")
    npc<Neutral>("There are four different melee styles. Accurate, aggressive, defensive and controlled. Not all weapons will have all four styles though.")
    player<Quiz>("Interesting, what does each style do?")
    npc<Neutral>("Well I am glad you asked. The accurate style will give you experience points in your Attack skill, you will also find you will deal damage more frequently as a result of being, well, more accurate.")
    npc<Neutral>("Next we have the aggressive style. This style will give you experience points in your Strength skill. When using this style you will notice that your attacks will hit a little harder.")
    npc<Neutral>("Now for the defensive style, this style will give you experience points in your Defensive skill. When using this style you will notice that you get hit less often.")
    npc<Neutral>("Finally, we have the controlled style. This style will give you the same amount of experience as the other styles would but shared across all three of the combat skills.")
    npc<Neutral>("If you were using the training sword for example, there are four different attack types. Stab, lunge, slash and block.")
    npc<Neutral>("Each type uses one of the attack styles. Stab uses accurate, lunge and slash use aggressive and block uses defensive.")
    npc<Neutral>("To find out which style an attack type uses, hover your mouse cursor over the style button.")
    npc<Quiz>("Is there anything else you would like to know?")
    choice {
        option("What if I wanted to fight something a bit more... human.") {
            player<Neutral>("What if I wanted to fight something a bit more... human.")
            npc<Happy>("Well adventurer, there are a few places you might be able to do this.")
            npc<Neutral>("You could try your luck at castle wars. Here, two teams fight each other to defend their respective flags. To win the game you will need to get the other team's flag and return it to your flag stand.")
            player<Happy>("Capture the flag, sounds like a lot of fun.")
            npc<Neutral>("If you are in a clan, you should gather some clan members and try out clan wars. There you can see which clan is better than the other by fighting each other in an arena.")
            npc<Neutral>("Both activities are safe minigame, which means if you die you will not loose any of your items. You can get to them by using the teleport option in your minigames tab.")
            npc<Shifty>("There is also the wilderness. The wilderness is north of Varrock and you can fight other players there. But bare in mind if you die to another player in the wilderness you will lose your stuff.")
            npc<Shifty>("But this also means that if you kill another player you will be able to take their stuff too.")
            npc<Shifty>("Only go into the wilderness with items you are willing to lose and pay attention to the wilderness level you are in. The higher the level you go, more player will be able to attack you.")
            npc<Shifty>("You can find which player can attack you by checking your combat level.")
            npc<Shifty>("Minus the wilderness level from your combat level to find the lowest level that you can attack, then add the wilderness level to your combat level to find the highest level that you can attack.")
            menu("Is there anything else you would like to know?")
        }
        weaponTypes()
        skillcapes()
        training()
        option("Goodbye.")
    }
}

suspend fun PlayerChoice.weaponTypes(): Unit = option<Neutral>("Tell me about different weapon types I can use.") {
    npc<Happy>("Well let me see now...There are stabbing type weapons such as daggers, then you have swords which are slashing, maces that have great crushing abilities, battle axes which are powerful.")
    npc<Happy>("There are also spears. Spears can be good for Defence and many forms of Attack.")
    npc<Neutral>("It depends a lot on how you want to fight. Experiment and find out what is best for you. Never be scared to try out a new weapon; you never know, you might like it!")
    npc<Neutral>("While I tried all of them for a while, I settled on this rather good sword.")
    npc<Neutral>("You might also find that different weapon types are more accurate against different monsters.")
    menu("Is there anything else you would like to know?")
}

suspend fun PlayerChoice.skillcapes(): Unit = option<Neutral>("Tell me about skillcapes.") {
    if (player.levels.getMax(Skill.Defence) < Level.MAX_LEVEL) {
        npc<Neutral>("Of course. Skillcapes are a symbol of achievement. Only people who have mastered a skill and reached level 99 can get their hands on them and gain the benefits they carry.")
        npc<Neutral>("The Cape of Defence will act as ring of life, saving you from combat if your hitpoints become low.")
        menu("Is there anything else you would like to know?")
        return@option
    }
    npc<Neutral>("Ah, but I can see you're already a master in the fine art of Defence. Perhaps you have come to me to purchase a Skillcape of Defence, and thus join the elite few who have mastered this exacting skill?")
    npc<Neutral>("In recognition of your defensive abilities, when you have it equipped it will act as ring of life, saving you from combat if your hitpoints become low.")
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
                    player<Quiz>("May I have another hood for my cape, please?")
                    npc<Neutral>("Most certainly, and free of charge!")
                    item("defence_hood", 400, "The tutor hands you another hood for your skillcape.")
                    player.inventory.add("defence_hood")
                }
            }
        }
    }
}

suspend fun SuspendableContext<Player>.buySkillcape() {
    player<Quiz>("May I buy a Skillcape of Defence, please?")
    npc<Neutral>("You wish to join the elite defenders of this world? I'm afraid such things do not come cheaply - in fact they cost 99000 coins, to be precise!")
    choice {
        option<Quiz>("99000 coins? That's much too expensive.") {
            npc<Neutral>("Not at all; there are many other adventurers who would love the opportunity to purchase such a prestigious item! You can find me here if you change your mind.")
        }
        option("I think I have the money right here, actually.") {
            player<Happy>("I think I have the money right here, actually.")
            player.inventory.transaction {
                remove("coins", 99000)
                add("defence_hood")
                val trimmed = Skill.entries.any { it != Skill.Defence && player.levels.getMax(it) >= Level.MAX_LEVEL }
                add("defence_skillcape${if (trimmed) "_t" else ""}")
            }
            when (player.inventory.transaction.error) {
                TransactionError.None -> npc<Happy>("Excellent! Wear that cape with pride my friend.")
                is TransactionError.Deficient -> {
                    player<Upset>("But, unfortunately, I was mistaken.")
                    npc<Neutral>("Well, come back and see me when you do.")
                }
                is TransactionError.Full, is TransactionError.Invalid -> {
                    npc<Upset>("Unfortunately all Skillcapes are only available with a free hood, it's part of a skill promotion deal; buy one get one free, you know. So you'll need to free up some inventory space before I can sell you one.")
                }
            }
        }
    }
}

suspend fun PlayerChoice.training(): Unit = option<Neutral>("I'd like a training sword and shield.") {
    if (player.ownsItem("training_sword") || player.ownsItem("training_shield")) {
        npc<Quiz>("You already have a training sword and shield. Save some for the other adventurers.")
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
    npc<Neutral>("There you go, use it well.")
    menu("Is there anything else I can help you with?")
}
