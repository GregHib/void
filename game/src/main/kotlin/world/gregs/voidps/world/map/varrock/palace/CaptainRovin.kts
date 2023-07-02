package world.gregs.voidps.world.map.varrock.palace

import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.contain.add
import world.gregs.voidps.engine.contain.hasItem
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.bank.hasBanked
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

on<NPCOption>({ operate && npc.id == "captain_rovin" && option == "Talk-to" }) { player: Player ->
    npc<Furious>("""
        What are you doing up here? Only the palace guards
        are allowed up here.
    """)
    if (player["demon_slayer", "unstarted"] == "key_hunt") {
        demonSlayerChoice()
    } else {
        regularChoice()
    }
}

suspend fun NPCOption.palaceGuard() {
    player<Unsure>("I am one of the palace guards.")
    npc<Furious>("No, you're not! I know all the palace guards.")
    val choice = choice("""
        I'm a new recruit.
        I've had extensive plastic surgery.
    """)
    when (choice) {
        1 -> newRecruit()
        2 -> plasticSurgery()
    }
}

suspend fun NPCOption.whatAboutKing() {
    player<Unsure>("What about the King? Surely you'd let him up here.")
    npc<Talk>("""
        Well, yes, I suppose we'd let him up. He doesn't
        generally want to come up here, but if he did want to,
        he could.
    """)
    npc<Furious>("""
        Anyway, you're not the King either. So get out of my
        sight.
    """)
}

suspend fun NPCOption.itsImportant() {
    player<Talk>("Yes, I know, but this is important.")
    npc<Talk>("Ok, I'm listening. Tell me what's so important.")
    if (player["demon_slayer", "unstarted"] == "completed") {
        importantDefaultChoice()
    } else {
        importantChoice()
    }
}

suspend fun NPCOption.newRecruit() {
    player<Talk>("I'm a new recruit.")
    npc<Talk>("""
        I interview all the new recruits. I'd know if you were
        one of them.
    """)
    player<Talking>("That blows that story out of the water then.")
    npc<Furious>("Get out of my sight.")
}

suspend fun NPCOption.plasticSurgery() {
    player<Talk>("I've had extensive plastic surgery.")
    npc<Talk>("""
        What sort of surgery is that? I've never heard of it.
        Besides, you look reasonably healthy.
    """)
    npc<Furious>("""
        Why is this relevant anyway? You still shouldn't be
        here.
    """)
}

suspend fun NPCOption.theresADemon() {
    player<Talk>("There's a demon who wants to invade the city.")
    if (player.hasBanked("silverlight_key_captain_rovin")) {
        haventYouKilledIt()
    } else {
        isItPowerful()
    }
}

suspend fun NPCOption.forgot() {
    player<Upset>("Erm I forgot.")
    npc<Furious>("Well it can't be that important then.")
    player<Talk>("How do you know?")
    npc<Furious>("Just go away.")
}

suspend fun NPCOption.aleDelivery() {
    player<Talking>("The castle has just received its ale delivery.")
    npc<Talk>("""
        Now that is important. However I'm the wrong person
        to speak to about it. Go talk to the kitchen staff.
    """)
}

suspend fun NPCOption.haventYouKilledIt() {
    npc<Talk>("Yes, you said before, haven't you killed it yet?")
    player<Talk>("""
        I'm going to use the powerful sword Silverlight, which I
        believe you have one of the keys for?
    """)
    if (player.hasItem("silverlight_key_captain_rovin")) {
        npc<Furious>("I already gave you my key. Check your pockets.")
    } else {
        npc<Talk>("""
            I already gave you my key. Maybe you left it
            somewhere. Have you checked your bank account?
        """)
    }
}

suspend fun NPCOption.isItPowerful() {
    npc<Unsure>("Is it a powerful demon?")
    val choice = choice("""
        Not really.
        Yes, very.
    """)
    when (choice) {
        1 -> notReallyPowerful()
        2 -> yesVeryPowerful()
    }
}

suspend fun NPCOption.notReallyPowerful() {
    player<Talk>("Not really.")
    npc<Cheerful>("""
        Well, I'm sure the palace guards can deal with it, then.
        Thanks for the information.
    """)
}

suspend fun NPCOption.yesVeryPowerful() {
    player<Upset>("Yes, very.")
    npc<Afraid>("""
        As good as the palace guards are, I don't know if
        they're up to taking on a very powerful demon.
    """)
    val choice = choice("""
        Yeah, the palace guards are rubbish!
        It's not them who are going to fight the demon, it's me.
    """)
    when (choice) {
        1 -> palaceGuardsAreRubbish()
        2 -> illFightIt()
    }
}

suspend fun NPCOption.palaceGuardsAreRubbish() {
    player<Laugh>("Yeah, the palace guards are rubbish!")
    npc<Laugh>("Yeah, they're--")
    npc<Furious>("""
        Wait! How dare you insult the palace guards? Get out
        of my sight!
    """)
}

suspend fun NPCOption.illFightIt() {
    player<Talk>("It's not them who are going to fight the demon, it's me.")
    npc<Surprised>("What, all by yourself? How are you going to do that?")
    player<Talk>("""
        I'm going to use the powerful sword Silverlight, which I
        believe you have one of the keys for?
    """)
    npc<Unsure>("Yes, I do. But why should I give it to you?")
    val choice = choice("""
        Fortune-teller Aris said I was destined to kill the demon.
        Otherwise the demon will destroy the city!
        Sir Prysin said you would give me the key.
    """)
    when (choice) {
        1 -> arisSaidSo()
        2 -> demonWillDestroyCity()
        3 -> prysinSaidSo()
    }
}

suspend fun NPCOption.arisSaidSo() {
    player<Talk>("Fortune-teller Aris said I was destined to kill the demon.")
    npc<Furious>("""
        A fortune-teller? Destiny? I don't believe in that stuff.
        I got where I am today by hard work, not by destiny!
        Why should I care what that mad old fortune-teller
        says?
    """)
    val choice = choice("""
        Otherwise the demon will destroy the city!
        Sir Prysin said you would give me the key.
    """)
    when (choice) {
        1 -> demonWillDestroyCity()
        2 -> prysinSaidSo()
    }
}

suspend fun NPCOption.demonWillDestroyCity() {
    player<Afraid>("Otherwise the demon will destroy the city!")
    npc<Furious>("""
        You can't fool me! How do I know you haven't just
        made that story up to get my key?
    """)
    val choice = choice("""
        Fortune-teller Aris said I was destined to kill the demon.
        Sir Prysin said you would give me the key
    """)
    when (choice) {
        1 -> arisSaidSo()
        2 -> prysinSaidSo()
    }
}

suspend fun NPCOption.prysinSaidSo() {
    player<Talk>("Sir Prysin said you would give me the key.")
    npc<Furious>("""
        Oh, he did, did he? Well I don't report to Sir Prysin, I
        report directly to the king!
    """)
    npc<Furious>("""
        I didn't work my way up through the ranks of the
        palace guards so I could take orders from an ill-bred
        moron who only has his job because his great-
        grandfather was a hero with a silly name!
    """)
    val choice = choice("""
        Why did he give you one of the keys then?
        Fortune-teller Aris said I was destined to kill the demon.
        Otherwise the demon will destroy the city!
    """)
    when (choice) {
        1 -> whyDidHeGiveKeyToYou()
        2 -> arisSaidSo()
        3 -> demonWillDestroyCity()
    }
}

suspend fun NPCOption.whyDidHeGiveKeyToYou() {
    player<Unsure>("Why did he give you one of the keys then?")
    npc<Furious>("""
        Only because the king ordered him to! The king
        couldn't get Sir Prysin to part with his precious
        ancestral sword, but he made him lock it up so he
        couldn't lose it.
    """)
    npc<Unsure>("""
        I got one key and I think some wizard got another.
        Now what happened to the third one?
    """)
    player<Laugh>("Sir Prysin dropped it down a drain!")
    npc<Laugh>("Ha ha ha! The idiot!")
    npc<Laugh>("""
        Okay, I'll give you the key, just so that it's you that
        kills the demon and not Sir Prysin!
    """)
    if (player.inventory.add("silverlight_key_captain_rovin")) {
        item("Captain Rovin hands you a key.", "silverlight_key_captain_rovin", 400)
    }
}

suspend fun NPCOption.demonSlayerChoice() {
    val choice = choice("""
        I am one of the palace guards.
        What about the King?
        Yes I know, but this is important.
    """)
    when (choice) {
        1 -> palaceGuard()
        2 -> whatAboutKing()
        3 -> itsImportant()
    }
}

suspend fun NPCOption.regularChoice() {
    val choice = choice("""
        I am one of the palace guards.
        What about the King?
    """)
    when (choice) {
        1 -> palaceGuard()
        2 -> whatAboutKing()
    }
}

suspend fun NPCOption.importantChoice() {
    val choice = choice("""
        There's a demon who wants to invade this city.
        Erm I forgot.
        The castle has just received its ale delivery.
    """)
    when (choice) {
        1 -> theresADemon()
        2 -> forgot()
        3 -> aleDelivery()
    }
}

suspend fun NPCOption.importantDefaultChoice() {
    val choice = choice("""
        Erm I forgot.
        The castle has just received its ale delivery.
    """)
    when (choice) {
        1 -> forgot()
        2 -> aleDelivery()
    }
}