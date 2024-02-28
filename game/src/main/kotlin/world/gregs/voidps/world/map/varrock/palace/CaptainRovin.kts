package world.gregs.voidps.world.map.varrock.palace

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.activity.bank.ownsItem
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*

npcOperate("Talk-to", "captain_rovin") {
    npc<Furious>("What are you doing up here? Only the palace guards are allowed up here.")
    choice {
        palaceGuard()
        whatAboutKing()
        itsImportant()
    }
}

suspend fun PlayerChoice.palaceGuard(): Unit = option<Unsure>("I am one of the palace guards.") {
    npc<Furious>("No, you're not! I know all the palace guards.")
    choice {
        newRecruit()
        plasticSurgery()
    }
}

suspend fun PlayerChoice.whatAboutKing(): Unit = option<Unsure>("What about the King? Surely you'd let him up here.") {
    npc<Talk>("Well, yes, I suppose we'd let him up. He doesn't generally want to come up here, but if he did want to, he could.")
    npc<Furious>("Anyway, you're not the King either. So get out of my sight.")
}

suspend fun PlayerChoice.itsImportant(): Unit = option<Talk>(
    "Yes, I know, but this is important.",
    { player.quest("demon_slayer") != "unstarted" }
) {
    npc<Talk>("Ok, I'm listening. Tell me what's so important.")
    choice {
        theresADemon()
        forgot()
        aleDelivery()
    }
}

suspend fun PlayerChoice.newRecruit(): Unit = option<Talk>("I'm a new recruit.") {
    npc<Talk>("I interview all the new recruits. I'd know if you were one of them.")
    player<Talking>("That blows that story out of the water then.")
    npc<Furious>("Get out of my sight.")
}

suspend fun PlayerChoice.plasticSurgery(): Unit = option<Talk>("I've had extensive plastic surgery.") {
    npc<Talk>("What sort of surgery is that? I've never heard of it. Besides, you look reasonably healthy.")
    npc<Furious>("Why is this relevant anyway? You still shouldn't be here.")
}

suspend fun PlayerChoice.theresADemon(): Unit = option<Talk>(
    "There's a demon who wants to invade the city.",
    { player.quest("demon_slayer") != "completed" }
) {
    if (player.ownsItem("silverlight_key_captain_rovin")) {
        haveYouNotKilledIt()
    } else {
        isItPowerful()
    }
}

suspend fun PlayerChoice.forgot(): Unit = option<Upset>("Erm I forgot.") {
    npc<Furious>("Well it can't be that important then.")
    player<Talk>("How do you know?")
    npc<Furious>("Just go away.")
}

suspend fun PlayerChoice.aleDelivery(): Unit = option<Talking>("The castle has just received its ale delivery.") {
    npc<Talk>("Now that is important. However I'm the wrong person to speak to about it. Go talk to the kitchen staff.")
}

suspend fun CharacterContext.haveYouNotKilledIt() {
    npc<Talk>("Yes, you said before, haven't you killed it yet?")
    player<Talk>("I'm going to use the powerful sword Silverlight, which I believe you have one of the keys for?")
    if (player.holdsItem("silverlight_key_captain_rovin")) {
        npc<Furious>("I already gave you my key. Check your pockets.")
    } else {
        npc<Talk>("I already gave you my key. Maybe you left it somewhere. Have you checked your bank account?")
    }
}

suspend fun CharacterContext.isItPowerful() {
    npc<Unsure>("Is it a powerful demon?")
    choice {
        notReallyPowerful()
        yesVeryPowerful()
    }
}

suspend fun PlayerChoice.notReallyPowerful(): Unit = option<Talk>("Not really.") {
    npc<Cheerful>("Well, I'm sure the palace guards can deal with it, then. Thanks for the information.")
}

suspend fun PlayerChoice.yesVeryPowerful(): Unit = option<Upset>("Yes, very.") {
    npc<Afraid>("As good as the palace guards are, I don't know if they're up to taking on a very powerful demon.")
    choice {
        palaceGuardsAreRubbish()
        illFightIt()
    }
}

suspend fun PlayerChoice.palaceGuardsAreRubbish(): Unit = option<Chuckle>("Yeah, the palace guards are rubbish!") {
    npc<Chuckle>("Yeah, they're--")
    npc<Furious>("Wait! How dare you insult the palace guards? Get out of my sight!")
}

suspend fun PlayerChoice.illFightIt(): Unit = option<Talk>("It's not them who are going to fight the demon, it's me.") {
    npc<Surprised>("What, all by yourself? How are you going to do that?")
    player<Talk>("I'm going to use the powerful sword Silverlight, which I believe you have one of the keys for?")
    npc<Unsure>("Yes, I do. But why should I give it to you?")
    choice {
        arisSaidSo()
        demonWillDestroyCity()
        prysinSaidSo()
    }
}

suspend fun PlayerChoice.arisSaidSo(): Unit = option<Talk>("Fortune-teller Aris said I was destined to kill the demon.") {
    npc<Furious>("A fortune-teller? Destiny? I don't believe in that stuff. I got where I am today by hard work, not by destiny! Why should I care what that mad old fortune-teller says?")
    choice {
        demonWillDestroyCity()
        prysinSaidSo()
    }
}

suspend fun PlayerChoice.demonWillDestroyCity(): Unit = option<Afraid>("Otherwise the demon will destroy the city!") {
    npc<Furious>("You can't fool me! How do I know you haven't just made that story up to get my key?")
    choice {
        arisSaidSo()
        prysinSaidSo()
    }
}

suspend fun PlayerChoice.prysinSaidSo(): Unit = option<Talk>("Sir Prysin said you would give me the key.") {
    npc<Furious>("Oh, he did, did he? Well I don't report to Sir Prysin, I report directly to the king!")
    npc<Furious>("I didn't work my way up through the ranks of the palace guards so I could take orders from an ill-bred moron who only has his job because his great- grandfather was a hero with a silly name!")
    choice {
        whyDidHeGiveKeyToYou()
        arisSaidSo()
        demonWillDestroyCity()
    }
}

suspend fun PlayerChoice.whyDidHeGiveKeyToYou(): Unit = option<Unsure>("Why did he give you one of the keys then?") {
    npc<Furious>("Only because the king ordered him to! The king couldn't get Sir Prysin to part with his precious ancestral sword, but he made him lock it up so he couldn't lose it.")
    npc<Unsure>("I got one key and I think some wizard got another. Now what happened to the third one?")
    player<Chuckle>("Sir Prysin dropped it down a drain!")
    npc<Chuckle>("Ha ha ha! The idiot!")
    npc<Chuckle>("Okay, I'll give you the key, just so that it's you that kills the demon and not Sir Prysin!")
    if (player.inventory.add("silverlight_key_captain_rovin")) {
        item("silverlight_key_captain_rovin", 400, "Captain Rovin hands you a key.")
    }
}
