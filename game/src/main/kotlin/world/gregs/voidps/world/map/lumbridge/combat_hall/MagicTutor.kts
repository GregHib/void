package world.gregs.voidps.world.map.lumbridge.combat_hall

import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.add
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.toTicks
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.player.display.Tab
import java.util.concurrent.TimeUnit

on<NPCOption>({ def.name == "Magic instructor" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        npc("unsure", """
            Hello there adventurer, I am the Magic combat tutor.
            Would you like to learn about magic combat, or perhaps
            how to make runes?
        """)
        menu()
    }
}

suspend fun DialogueContext.menu(followUp: String = "") {
    if (followUp.isNotEmpty()) {
        npc("unsure", followUp)
    }
    val choice = choice("""
        Tell me about magic combat please.
        How do I make runes?
        I'd like some air and mind runes.
        Goodbye.
    """)
    when (choice) {
        1 -> magicCombat()
        2 -> runeMaking()
        3 -> claimRunes()
    }
}

suspend fun DialogueContext.magicCombat() {
    player("talking", "Tell me about magic combat please.")
    npc("cheerful", """
        Of course ${player.name}! As a rule of thumb, if you cast the
        highest spell of which you're capable, you'll get the best
        experience possible.
    """)
    npc("cheerful", """
        Wearing metal armour and ranged armour can
        seriously impair your magical abilities. Make sure you
        wear some robes to maximise your capabilities.
    """)
    npc("cheerful", """
        Superheat Item and the Alchemy spells are good ways
        to level magic if you are not interested in the combat
        aspect of magic.
    """)
    npc("suspicious", """
        There's always the Magic Training Arena. You can
        find it north of the Duel Arena in Al Kharid. You will
        be able to earn some special rewards there by practicing
        your magic there.
    """)
    npc("cheerful", """
        I see you already have access to the ancient magicks.
        Well done, these will aid you greatly.
    """)
    menu("Is there anything else you would like to know?")
}

suspend fun DialogueContext.runeMaking() {
    player("unsure", "How do I make runes?")
    npc("cheerful", """
        There are a couple of things you will need to make
        runes, rune essence and a talisman to enter the temple
        ruins.
    """)
    if (player.experience.get(Skill.Runecrafting) > 0.0) {
        npc("amazed", """
            To get rune essence you will need to gather them in
            the essence mine. You can get to the mine by talking
            to Aubury who owns the runes shop in south east
            Varrock.
        """)
        npc("cheerful", """
            I see you have some experience already in
            Runecrafting. Perhaps you should try crafting some
            runes which you can then use in magic.
        """)
        player.setVar("tab", Tab.Stats.name)
        npc("cheerful", "Check the skill guide to see which runes you can craft.")
    } else {
        npc("cheerful", """
            To get rune essence you will need to gather them
            somehow. You should talk to the Duke of Lumbridge, he
            may be able to help you with that. Alternatively, other
            players may sell you the essence.
        """)
        npc("cheerful", """
            As you're fairly new to runecrafting you should start
            with air runes and mind runes.
        """)
    }
    npc("cheerful", """
        You will need a talisman for the rune you would like to
        create. You can right-click on it and select the Locate
        option. This will tell you the rough location of the altar.
    """)
    npc("cheerful", """
        When you find the ruined altar, use the talisman on it
        to be transported to a temple where you can craft your
        runes.
    """)
    npc("cheerful", """
        Clicking on the temple's altar will imbue your rune
        essence with the altar's magical property.
    """)
    npc("cheerful", """
        If you want to save yourself an inventory space, you
        could always try binding the talisman to a tiara.
    """)
    npc("cheerful", """
        To make one, take a tiara and talisman to the ruins
        and use the tiara on the temple altar. This will bind the
        talisman to your tiara.
    """)
    menu("Is there anything else you would like to know?")
}

suspend fun DialogueContext.claimRunes() {
    if (player.hasEffect("claimed_tutor_consumables")) {
        npc("amazed", """
            I work with the Ranged Combat tutor to give out
            consumable items that you may need for combat such
            as arrows and runes. However we have had some
            cheeky people try to take both!
        """)
        npc("cheerful", """
            So, every half an hour, you may come back and claim
            either arrows OR runes, but not both. Come back in a
            while for runes, or simply make your own.
        """)
        return
    }
    if (player.hasBanked("mind_rune", banked = true) || player.hasBanked("air_rune", banked = true)) {
        hasRunes()
        return
    }
    if (player.inventory.isFull()) {
        npc("upset", """
            If you had enough space in your inventory I'd give
            you some mind runes, come back when you do.
        """)
        player.inventoryFull()
        return
    }
    if (player.inventory.spaces < 2) {
        npc("upset", """
            If you had enough space in your inventory I'd give
            you some air runes, come back when you do.
        """)
        player.inventoryFull()
        return
    }
    item("Mikasi gives you 30 air runes.", "air_rune", 400)
    player.inventory.add("air_rune", 30)
    item("Mikasi gives you 30 mind runes.", "mind_rune", 400)
    player.inventory.add("mind_rune", 30)
    player.start("claimed_tutor_consumables", ticks = TimeUnit.MINUTES.toTicks(30), persist = true)
}

suspend fun DialogueContext.hasRunes() {
    var banked = false
    if (player.bank.contains("mind_rune")) {
        npc("cheerful", "You have some mind runes in your bank.")
        banked = true
    }
    if (player.bank.contains("air_rune")) {
        npc("cheerful", "You have some air runes in your bank.")
        banked = true
    }
    if (banked) {
        item("""
            You have some runes in your bank. Climb the stairs in
            Lumbridge Castle until you see this icon on your
            minimap. There you will find a bank.
        """, "bank_icon", 1200)
        return
    }
    if (player.inventory.contains("mind_rune")) {
        npc("cheerful", "You already have some mind runes.")
    }
    if (player.inventory.contains("air_rune")) {
        npc("cheerful", "You already have some air runes.")
    }
}
