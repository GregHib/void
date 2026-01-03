package content.area.misthalin.varrock.palace

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.quest
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory

class CaptainRovin : Script {

    init {
        npcOperate("Talk-to", "captain_rovin") {
            npc<Angry>("What are you doing up here? Only the palace guards are allowed up here.")
            choice {
                palaceGuard()
                whatAboutKing()
                if (quest("demon_slayer") != "unstarted") {
                    itsImportant()
                }
            }
        }
    }

    fun ChoiceOption.palaceGuard(): Unit = option<Quiz>("I am one of the palace guards.") {
        npc<Angry>("No, you're not! I know all the palace guards.")
        choice {
            newRecruit()
            plasticSurgery()
        }
    }

    fun ChoiceOption.whatAboutKing(): Unit = option<Quiz>("What about the King? Surely you'd let him up here.") {
        npc<Neutral>("Well, yes, I suppose we'd let him up. He doesn't generally want to come up here, but if he did want to, he could.")
        npc<Angry>("Anyway, you're not the King either. So get out of my sight.")
    }

    fun ChoiceOption.itsImportant() = option<Neutral>("Yes, I know, but this is important.") {
        npc<Neutral>("Ok, I'm listening. Tell me what's so important.")
        choice {
            if (!questCompleted("demon_slayer")) {
                theresADemon()
            }
            forgot()
            aleDelivery()
        }
    }

    fun ChoiceOption.newRecruit(): Unit = option<Neutral>("I'm a new recruit.") {
        npc<Neutral>("I interview all the new recruits. I'd know if you were one of them.")
        player<Idle>("That blows that story out of the water then.")
        npc<Angry>("Get out of my sight.")
    }

    fun ChoiceOption.plasticSurgery(): Unit = option<Neutral>("I've had extensive plastic surgery.") {
        npc<Neutral>("What sort of surgery is that? I've never heard of it. Besides, you look reasonably healthy.")
        npc<Angry>("Why is this relevant anyway? You still shouldn't be here.")
    }

    fun ChoiceOption.theresADemon(): Unit = option<Neutral>("There's a demon who wants to invade the city.") {
        if (ownsItem("silverlight_key_captain_rovin")) {
            haveYouNotKilledIt()
        } else {
            isItPowerful()
        }
    }

    fun ChoiceOption.forgot(): Unit = option<Sad>("Erm I forgot.") {
        npc<Angry>("Well it can't be that important then.")
        player<Neutral>("How do you know?")
        npc<Angry>("Just go away.")
    }

    fun ChoiceOption.aleDelivery(): Unit = option<Idle>("The castle has just received its ale delivery.") {
        npc<Neutral>("Now that is important. However I'm the wrong person to speak to about it. Go talk to the kitchen staff.")
    }

    suspend fun Player.haveYouNotKilledIt() {
        npc<Neutral>("Yes, you said before, haven't you killed it yet?")
        player<Neutral>("I'm going to use the powerful sword Silverlight, which I believe you have one of the keys for?")
        if (holdsItem("silverlight_key_captain_rovin")) {
            npc<Angry>("I already gave you my key. Check your pockets.")
        } else {
            npc<Neutral>("I already gave you my key. Maybe you left it somewhere. Have you checked your bank account?")
        }
    }

    suspend fun Player.isItPowerful() {
        npc<Quiz>("Is it a powerful demon?")
        choice {
            notReallyPowerful()
            yesVeryPowerful()
        }
    }

    fun ChoiceOption.notReallyPowerful(): Unit = option<Neutral>("Not really.") {
        npc<Happy>("Well, I'm sure the palace guards can deal with it, then. Thanks for the information.")
    }

    fun ChoiceOption.yesVeryPowerful(): Unit = option<Sad>("Yes, very.") {
        npc<Scared>("As good as the palace guards are, I don't know if they're up to taking on a very powerful demon.")
        choice {
            palaceGuardsAreRubbish()
            illFightIt()
        }
    }

    fun ChoiceOption.palaceGuardsAreRubbish(): Unit = option<Laugh>("Yeah, the palace guards are rubbish!") {
        npc<Laugh>("Yeah, they're--")
        npc<Angry>("Wait! How dare you insult the palace guards? Get out of my sight!")
    }

    fun ChoiceOption.illFightIt(): Unit = option<Neutral>("It's not them who are going to fight the demon, it's me.") {
        npc<Shock>("What, all by yourself? How are you going to do that?")
        player<Neutral>("I'm going to use the powerful sword Silverlight, which I believe you have one of the keys for?")
        npc<Quiz>("Yes, I do. But why should I give it to you?")
        choice {
            arisSaidSo()
            demonWillDestroyCity()
            prysinSaidSo()
        }
    }

    fun ChoiceOption.arisSaidSo(): Unit = option<Neutral>("Fortune-teller Aris said I was destined to kill the demon.") {
        npc<Angry>("A fortune-teller? Destiny? I don't believe in that stuff. I got where I am today by hard work, not by destiny! Why should I care what that mad old fortune-teller says?")
        choice {
            demonWillDestroyCity()
            prysinSaidSo()
        }
    }

    fun ChoiceOption.demonWillDestroyCity(): Unit = option<Scared>("Otherwise the demon will destroy the city!") {
        npc<Angry>("You can't fool me! How do I know you haven't just made that story up to get my key?")
        choice {
            arisSaidSo()
            prysinSaidSo()
        }
    }

    fun ChoiceOption.prysinSaidSo(): Unit = option<Neutral>("Sir Prysin said you would give me the key.") {
        npc<Angry>("Oh, he did, did he? Well I don't report to Sir Prysin, I report directly to the king!")
        npc<Angry>("I didn't work my way up through the ranks of the palace guards so I could take orders from an ill-bred moron who only has his job because his great- grandfather was a hero with a silly name!")
        choice {
            whyDidHeGiveKeyToYou()
            arisSaidSo()
            demonWillDestroyCity()
        }
    }

    fun ChoiceOption.whyDidHeGiveKeyToYou(): Unit = option<Quiz>("Why did he give you one of the keys then?") {
        npc<Angry>("Only because the king ordered him to! The king couldn't get Sir Prysin to part with his precious ancestral sword, but he made him lock it up so he couldn't lose it.")
        npc<Quiz>("I got one key and I think some wizard got another. Now what happened to the third one?")
        player<Laugh>("Sir Prysin dropped it down a drain!")
        npc<Laugh>("Ha ha ha! The idiot!")
        npc<Laugh>("Okay, I'll give you the key, just so that it's you that kills the demon and not Sir Prysin!")
        if (inventory.add("silverlight_key_captain_rovin")) {
            item("silverlight_key_captain_rovin", 400, "Captain Rovin hands you a key.")
        }
    }
}
