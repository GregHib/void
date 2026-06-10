package content.area.misthalin.lumbridge.blood_pact

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.player.inv.item.addOrDrop
import content.entity.world.music.unlockTrack
import content.quest.quest
import content.quest.questComplete
import content.quest.refreshQuestJournal
import content.skill.melee.weapon.Weapon
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.longQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Xenia : Script {
    init {
        npcOperate("Talk-to", "xenia*") { (target) ->
            when (quest("blood_pact")) {
                "unstarted" -> {
                    npc<Neutral>("I'm glad you've come by. I need some help.")
                    choiceBase()
                }
                "started" -> npc<Neutral>("We've got no time to lose. You head down the stairs, and I'll follow.")
                "watched_cutscene" -> {
                    npc<Neutral>("There's a guard in the room ahead. Together we should be able to take him out.")
                    optionsAfterEntering()
                }
                "xenia_wounded" -> {
                    if (!hasPlayerWeaponType("melee")) {
                        addOrDrop("bronze_dagger")
                        npc<LookDown>("You'll need a weapon. Equip this bronze dagger, then talk to me again.")
                        return@npcOperate
                    }
                    npc<LookDown>("Ah...")
                    npc<LookDown>("It looks like I'm too old for this after all. You'll have to do the rest without me.")
                    npc<LookDown>("The first cultist is using a ranged weapon, so you should attack him with your melee weapon.")
                    npc<LookDown>("I'll follow you, but I'll stay out of combat. Return to me if you're wounded. I have some food to share.")
                    foodChat()
                    optionsAfterEntering()
                }
                "kayle" -> {
                    val kayleStatus = get<String>("blood_pact_kayle")
                    foodChat()
                    if (kayleStatus == "defeated") {
                        npc<LookDown>("The first cultist is defeated, but not dead. I'll leave it up to you to how to deal with him.")
                    }
                    optionsBeforeFirstFight()
                }
                "caitlin", "winch_activated" -> {
                    val caitlinStatus = get<String>("blood_pact_caitlin")
                    foodChat()
                    when (caitlinStatus) {
                        "alive" -> {
                            if (equipped(EquipSlot.Weapon).id == "kayles_sling") {
                                npc<LookDown>("You're holding the sling right. You should be able to attack the second cultist without any trouble.")
                            } else if (Weapon.type(this, equipped(EquipSlot.Weapon)) == "range") {
                                npc<LookDown>("I see you've brought your own ranged weapon. I'll assume you know how to use it!")
                            } else if (inventory.contains("kayles_sling")) {
                                npc<LookDown>("You'll need to equip the sling before you can attack the second cultist.")
                            } else {
                                npc<LookDown>("You'll need to pick up the sling and equip it before you can attack the second cultist.")
                            }
                        }
                        "defeated" -> npc<LookDown>("The second cultist is defeated, but not dead. I'll leave it up to you how to deal with her.")
                    }
                    optionsBeforeSecondFight()
                }
                "reese" -> {
                    val reeseStatus = get<String>("blood_pact_reese")
                    foodChat()
                    when (reeseStatus) {
                        "alive" -> {
                            if (equipped(EquipSlot.Weapon).id == "caitlins_staff") {
                                npc<LookDown>("You're holding the staff. You should be able to attack the last cultist without any trouble.")
                            } else if (Weapon.type(this, equipped(EquipSlot.Weapon)) == "magic") {
                                npc<LookDown>("I see you've brought your own magic weapon. I'll assume you know how to use it!")
                            } else if (inventory.contains("caitlins_staff")) {
                                npc<LookDown>("You'll need to equip the staff before you can attack the last cultist.")
                            } else {
                                npc<LookDown>("You'll need to pick up the staff and equip it before you can attack the last cultist.")
                            }
                        }
                        "killed" -> npc<LookDown>("You've defeated the last cultist. Now you need to untie their prisoner.")
                        "defeated" -> npc<LookDown>("If you've defeated the last cultist, you'll need to decide how to deal with him.")
                        else -> npc<LookDown>("The last cultist is a swordsman. Magic is the best thing to use against melee fighters.")
                    }
                    optionsBeforeThirdFight()
                }
                "untied_ilona" -> askAnything()
                "completed" -> {
                    if (get("blood_pact_ilona_departed", false)) {
                        askAnything()
                        return@npcOperate
                    }
                    npc<Happy>("Hello again, adventurer.")
                    choiceAfterQuest()
                }
            }
        }
    }

    private suspend fun Player.askAnything() {
        completeBloodPact()
        npc<Neutral>("Is there anything you want to ask before you go to seek out new adventures?")
        finalDialogBloodPact()
    }

    fun Player.hasPlayerWeaponType(weaponType: String): Boolean {
        val equippedWeapon = equipped(EquipSlot.Weapon)
        if (equippedWeapon.isNotEmpty() && Weapon.type(this, equippedWeapon) == weaponType) {
            return true
        }
        return inventory.items.any { !it.isEmpty() && Weapon.type(this, it) == weaponType }
    }

    suspend fun Player.choiceBase() {
        choice {
            whatDoYouNeed()
            whoAreYou()
            howDoYouKnow()
            option<Neutral>("Sorry, I've got to go.")
        }
    }

    suspend fun Player.foodChat() {
        val playerHealthPercentage = levels.get(Skill.Constitution).toDouble() / levels.getMax(Skill.Constitution)
        if (inventory.items.any { it.def.contains("heals") && !it.def.contains("excess") }) {
            if (playerHealthPercentage < 1.0 && playerHealthPercentage > 0.75) {
                npc<LookDown>("You're lightly wounded. You should eat some of the food you're carrying.")
            } else if (playerHealthPercentage <= 0.75) {
                npc<LookDown>("You're badly wounded! You should eat some of the food you're carrying.")
            }
        } else {
            if (playerHealthPercentage < 1.0 && playerHealthPercentage > 0.75) {
                npc<LookDown>("You're lightly wounded. Here, have some food...")
                statement("Xenia gives you a piece of cooked meat. Eat food to heal yourself.")
                addOrDrop("cooked_meat")
            } else if (playerHealthPercentage <= 0.75) {
                npc<LookDown>("You're badly wounded! Eat some food, quickly...")
                statement("Xenia gives you 4 pieces of cooked meat. Eat food to heal yourself.")
                repeat(4) {
                    addOrDrop("cooked_meat")
                }
            }
        }
    }

    fun ChoiceOption.leaving(): Unit = option<Neutral>("I think I'll go now.") {
        npc<Neutral>("Farewell, adventurer.")
    }

    fun ChoiceOption.whatDoYouNeed(): Unit = option<Neutral>("What do you need help with?") {
        npc<Neutral>("Some cultists of Zamorak have gone into the catacombs with a prisoner. I don't know what they're planning, but I'm pretty sure it's not a tea party.")
        npc<Neutral>("There are three of them, and I'm not as young as I was the last time I was here. I don't want to go down there without backup.")
        questAccept()
    }

    suspend fun Player.questAccept() {
        choice {
            acceptQuest()
            moreInfos()
            whoAreYou()
            howDoYouKnow()
        }
    }

    fun ChoiceOption.acceptQuest(): Unit = option<Neutral>("I'll help you.") {
        if (startQuest("blood_pact")) {
            set("blood_pact", "started")
            // Reset per-cultist progress so a replay doesn't inherit stale "killed"/"spared" state
            set("blood_pact_kayle", "alive")
            set("blood_pact_caitlin", "alive")
            set("blood_pact_reese", "alive")
            clear("blood_pact_kayle_tile")
            clear("blood_pact_caitlin_tile")
            clear("blood_pact_reese_tile")
            clear("blood_pact_reese_door")
            refreshQuestJournal()
            npc<Happy>("I knew you would!")
            npc<Neutral>("We've got no time to lose. You head down the stairs, and I'll follow.")
        } else {
            player<No>("Not Right Now.")
        }
    }

    fun ChoiceOption.moreInfos(): Unit = option<Neutral>("I need to know more before I help you.") {
        npc<Neutral>("Very wise. I got into a lot of trouble in my youth by rushing in without knowing a situation.")
        moreInfoChoices()
    }

    suspend fun Player.moreInfoChoices() {
        choice {
            option<Neutral>("Tell me more about these cultists.") {
                npc<Neutral>("Lumbridge is a Saradominist town, but there will always be some people drawn to worship Zamorak. They must have found some ritual that they think will give them power over other people.")
                moreInfoChoices()
            }

            option<Neutral>("Who did they kidnap?") {
                npc<Neutral>("A young woman named Ilona. She had just left Lumbridge to apprentice at the Wizards' Tower.")
                npc<Neutral>("They grabbed her on the road. Without training she didn't have a chance.")
                moreInfoChoices()
            }

            option<Neutral>("What's down there?") {
                npc<Neutral>("The catacombs of Lumbridge Church. The dead of Lumbridge have been buried there since...well, for about forty years now.")
                moreInfoChoices()
            }

            option<Neutral>("Is there a reward if I help you?") {
                npc<Neutral>("The cultists all have weapons, and you'll be able to keep them if we succeed. This adventure will also help to train your combat skills.")
                moreInfoChoices()
            }

            option<Neutral>("Enough questions.") {
                npc<Neutral>("So, will you help me, adventurer?")
                questAccept()
            }
        }
    }

    fun ChoiceOption.whoAreYou(): Unit = option<Confused>("Who are you?") {
        npc<Neutral>("My name's Xenia. I'm an adventurer.")
        npc<Neutral>("I'm one of the old guard, I suppose. I helped found the Champions' Guild, and I've done a fair few quests in my time.")
        npc<Neutral>("Now I'm starting to get a bit old for action, which is why I need your help.")
        choiceBase()
    }

    fun ChoiceOption.howDoYouKnow(): Unit = option<Confused>("How did you know who I am?") {
        npc<Neutral>("Oh, I have my ways. I get the feeling that you're one to watch; you could be quite the hero some day.")
        choiceBase()
    }

    suspend fun Player.choiceAfterQuest() {
        choice {
            choiceQuestDetail()
            if (checkForLostWeapons(this@choiceAfterQuest)) {
                lostWeapon()
            }
            leaving()
        }
    }

    fun ChoiceOption.lostWeapon(): Unit = option<Neutral>("I've lost some of the cultists' weapons.") {
        npc<Neutral>("Yes, one of my contacts in the Champion's Guild found them and returned them to me.")
        giveWeapons(this)
    }

    fun ChoiceOption.choiceQuestDetail(): Unit = option<Neutral>("I've got a question about my adventure in the catacombs...") {
        afterQuestDetail()
    }

    fun ChoiceOption.notWounded(): Unit = option<Neutral>("You weren't really wounded, were you?") {
        npc<Neutral>("Very perceptive, adventurer. I was wounded, but not as badly as I looked. I took the opportunity to see how you would fare.")
        woundedDetails()
    }

    fun ChoiceOption.whatNow(): Unit = option<Neutral>("What will happen in the catacombs now?") {
        npc<Neutral>(" Reese managed to complete the ritual with his own death. He's opened the staircase to the nest of undead creatures in the lower level of the catacombs. Without a necromancer to control them, the creatures won't leave the tomb. I'll warn Father Aereck not to let people go down there. You're an adventurer, though. If you want to, you can venture into the tomb and fight the creatures.")
        afterQuestDetail()
    }

    fun ChoiceOption.whatBloodPact(): Unit = option<Neutral>("What is a blood pact?") {
        npc<Neutral>(" It's something Zamorakian cults do sometimes; a way of swearing loyalty to their leader. A blood pact doesn't have real magical power, but that kind of thing can have great power over a person if they believe strongly enough.")
        afterQuestDetail()
    }

    fun ChoiceOption.whoDragith(): Unit = option<Neutral>("Who was Dragith Nurn?") {
        npc<Neutral>("Dragith Nurn was a wizard. He studied at the Wizards' Tower, but he also studied the dar ark, necromancy, on his own. He had a secret magical workshop beneath Lumbridge. He would steal bodies from the graveyard and perform experiments on them. Necromancy was like an addiction for him. When I met him he was very troubled; very conflicted. I convinced him to put an end to it all. He couldn't destroy all the undead he had created - not permanently - so he trapped them all in the lower level of his workshop and sealed it off. He converted the upper level into these catacombs. Everyone thinks Dragith Nurn is buried here in the tomb, but he isn't. He built the tomb to hide the entrance to the lower level. Dragith Nurn is still down there. He knew that when he died he would rise again as a monster, so he sealed himself in with his creatures.")
        afterQuestDetail()
    }

    fun ChoiceOption.womansLife(): Unit = option<Neutral>("You risked that woman's life for the sake of a test?") {
        npc<Neutral>(" I was prepared to step in and rescue her if you failed, but I won't always be that ready. That's why I had to do this. The world needs heroes. I was a hero, once, but I'm not getting any younger. I need to make sure the news generation has its own heroes.")
        woundedDetails()
    }

    fun ChoiceOption.playerLife(): Unit = option<Neutral>("You risked my life for the sake of a test?") {
        npc<Neutral>("You're a born adventurer. I can practically smell it on you. People like you have a habit of coming back from things that would kill an ordinary person.")
        woundedDetails()
    }

    fun ChoiceOption.howDidIDo(): Unit = option<Neutral>("So how did I do?") {
        npc<Neutral>("Very well indeed. You're a hero. You're exactly the sort of person the world needs. I'm glad I met you.")
        woundedDetails()
    }

    suspend fun Player.optionsAfterEntering() {
        choice {
            option<Neutral>("What's the plan of attack?") {
                npc<Neutral>("It looks like the cultist has a sling. The best way to deal with someone with a ranged weapon is to get close to them and attack with melee.")
                optionsAfterEntering()
            }
            option<Neutral>("What's a blood pact?") {
                npc<Neutral>("It's something Zamorakian cults do sometimes; a way of swearing loyalty to their leader.")
                npc<Neutral>("A blood pact doesn't have real magical power, but that kind of thing can have great power over a person if they believe strongly enough.")
                optionsAfterEntering()
            }
            option<Neutral>("Let's get on with this.") { }
        }
    }

    suspend fun Player.optionsBeforeFirstFight() {
        choice {
            option<LookDown>("Tell me more about melee combat.") {
                npc<LookDown>("There's not much to tell. Just run up and attack. You don't even need a weapon - you can use your fists. Melee combat is strong against rangers. Avoid magic users, though.")
                optionsBeforeFirstFight()
            }
            option<LookDown>("What's a blood pact?") {
                npc<LookDown>("It's something Zamorakian cults do sometimes; a way of swearing loyalty to their leader.")
                npc<LookDown>("A blood pact doesn't have real magical power, but that kind of thing can have great power over a person if they believe strongly enough.")
                optionsBeforeFirstFight()
            }
            option<LookDown>("Are you going to be alright?") {
                npc<LookDown>("Don't worry about me. I've survived worse wounds than this. I'm going to hang back from combat, but I'll be here to give you advice if you need it. I'm sure you can beat these cultists on your own.")
                optionsBeforeFirstFight()
            }
            option<LookDown>("I can handle this.") { }
        }
    }

    suspend fun Player.optionsBeforeSecondFight() {
        choice {
            option<LookDown>("Tell me more about ranged combat.") {
                npc<LookDown>("In order to use ranged combat, you'll need to wield a ranged weapon. For most weapons you'll also need ammunition, but if you use a sling you'll always be able to fire low power stones. After that it's easy; just attack your enemy. Ranged combat is good against magic users. It's not so good against melee fighters, since projectiles have trouble getting through heavy armour.")
                optionsBeforeSecondFight()
            }
            option<LookDown>("What's a blood pact?") {
                npc<LookDown>("It's something Zamorakian cults do sometimes; a way of swearing loyalty to their leader.")
                npc<LookDown>("A blood pact doesn't have real magical power, but that kind of thing can have great power over a person if they believe strongly enough.")
                optionsBeforeSecondFight()
            }
            option<LookDown>("Are you going to be alright?") {
                npc<LookDown>("Don't worry about me. I've survived worse wounds than this. I'm going to hang back from combat, but I'll be here to give you advice if you need it. I'm sure you can beat these cultists on your own.")
                optionsBeforeSecondFight()
            }
            option<LookDown>("I can handle this.") { }
        }
    }

    suspend fun Player.optionsBeforeThirdFight() {
        choice {
            option<LookDown>("Tell me more about magic combat.") {
                npc<LookDown>("Magic is based on runes. The runes contain magical power, and you can cast spells by combining them in specific ways. It's like cooking: the runes are ingredients, and a spell is a recipe.")
                npc<LookDown>("Some magical staves act as an infinite supply of a certain type of rune. Magic is very useful against melee fighters. Watch out for rangers, though; arrows go straight through mage robes.")
                optionsBeforeThirdFight()
            }
            option<LookDown>("What's a blood pact?") {
                npc<LookDown>("It's something Zamorakian cults do sometimes; a way of swearing loyalty to their leader.")
                npc<LookDown>("A blood pact doesn't have real magical power, but that kind of thing can have great power over a person if they believe strongly enough.")
                optionsBeforeThirdFight()
            }
            option<LookDown>("Are you going to be alright?") {
                npc<LookDown>("Don't worry about me. I've survived worse wounds than this. I'm going to hang back from combat, but I'll be here to give you advice if you need it. I'm sure you can beat these cultists on your own.")
                optionsBeforeThirdFight()
            }
            option<LookDown>("I can handle this.") { }
        }
    }

    suspend fun Player.backToQuestions() {
        choice {
            whatNow()
            whatBloodPact()
            whoDragith()
            leaving()
        }
    }

    suspend fun Player.woundedDetails() {
        choice {
            womansLife()
            playerLife()
            howDidIDo()
            option<Neutral>("Back to my other questions...") { backToQuestions() }
        }
    }

    suspend fun Player.afterQuestDetail() {
        choice {
            notWounded()
            whatNow()
            whatBloodPact()
            whoDragith()
            leaving()
        }
    }

    suspend fun Player.finalDialogBloodPact() {
        choice {
            option("I'm ready for my reward.") {
                npc<Neutral>("Farewell, adventurer.")
            }
            option("What should I do now?") {
                npc<Neutral>("The cultists' ritual opened up the lower level of the catacombs, which is swarming with undead creatures. If you want to practice your combat skills, you could go down there.")
                npc<Neutral>("Alternatively, if you explore the world I'm sure you'll find other quests you can do.")
                finalDialogBloodPact()
            }
            option("You weren't really wounded, were you?") {
                npc<Neutral>("Very perceptive, adventurer.")
                npc<Neutral>("I was wounded, but not as badly as I looked. I took the opportunity to see how you would fare.")
                werentWoundedOptions()
            }
            option("What will happen in the catacombs now?") {
                npc<Neutral>("Reese managed to complete the ritual with his own death. He's opened the staircase to the nest of undead creatures in the lower level of the catacombs, which is swarming with undead.")
                npc<Neutral>("Without a necromancer to control them, the creatures won't leave the tomb. I'll warn Father Aereck not to let people go down there.")
                npc<Neutral>("You're an adventurer, though. If you want to, you can venture into the tomb and fight the creatures.")
                finalDialogBloodPact()
            }
        }
    }

    fun Player.completeBloodPact() {
        longQueue("quest_complete") {
            clear("blood_pact_ilona_departed")
            set("blood_pact", "completed")
            inc("quest_points", 1)
            jingle("quest_complete_1")
            unlockTrack("catacomb")
            unlockTrack("cursed_you_are")
            exp(Skill.Attack, 100.0)
            exp(Skill.Strength, 100.0)
            exp(Skill.Defence, 100.0)
            exp(Skill.Ranged, 100.0)
            exp(Skill.Magic, 100.0)
            message("Congratulations, you've completed a quest: <navy>The Blood Pact")
            refreshQuestJournal()
            questComplete(
                "The Blood Pact",
                "1 Quest Point",
                "Kayle's sling, Caitlin's staff",
                "and Reese's sword",
                "100 Attack, Strength,",
                "Defence, Ranged and Magic",
                "XP",
                "Access to the Lumbridge",
                "Catacombs dungeon",
                item = "reeses_sword",
            )
        }
    }

    suspend fun Player.werentWoundedOptions() {
        choice {
            option("You risked that woman's life for the sake of a test?") {
                npc<Neutral>("I was prepared to step in and rescue her if you failed, but I won't always be that ready. That's why I had to do this. The world needs heroes. I was a hero, once, but I'm not getting any younger. I need to make sure the new generation has its own heroes.")
                werentWoundedOptions()
            }
            option("You risked my life for the sake of a test?") {
                npc<Neutral>("You're a born adventurer. I can practically smell it on you. People like you have a habit of coming back from things that would kill an ordinary person.")
                werentWoundedOptions()
            }
            option("So how did I do?") {
                npc<Neutral>("Very well indeed. You're a hero. You're exactly the sort of person the world needs. I'm glad I met you.")
                werentWoundedOptions()
            }
            option("Back to my other questions...") {
                finalDialogBloodPact()
            }
        }
    }

    companion object {
        fun checkForLostWeapons(player: Player): Boolean {
            val weapons = arrayOf("reeses_sword", "kayles_sling", "caitlins_staff")
            for (weapon in weapons) {
                if (!player.ownsItem(weapon)) {
                    return true
                }
            }
            return false
        }

        suspend fun giveWeapons(player: Player) {
            if (!player.ownsItem("kayles_sling") && player.inventory.add("kayles_sling")) {
                player.statement("Xenia gives you Kayle's sling.")
            }
            if (!player.ownsItem("caitlins_staff") && player.inventory.add("caitlins_staff")) {
                player.statement("Xenia gives you Caitlin's staff.")
            }
            if (!player.ownsItem("reeses_sword") && player.inventory.add("reeses_sword")) {
                player.statement("Xenia gives you Reese's sword.")
            }
        }
    }
}
