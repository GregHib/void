package world.gregs.voidps.world.map.lumbridge.combat_hall

import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.bank.has
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.player.display.Tab

on<NPCOption>({ npc.def.name == "Melee instructor" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        npc("unsure", """
           Greetings adventurer, I am the Melee combat tutor. Is
           there anything I can do for you?
        """)
        menu()
    }
}

suspend fun DialogueContext.menu(followUp: String = "") {
    if (followUp.isNotEmpty()) {
        npc("unsure", followUp)
    }
    val choice = choice("""
        Tell me about melee combat.
        Tell me about different weapon types I can use.
        Tell me about skillcapes.
        I'd like a training sword and shield.
        Goodbye.
    """)
    chosen(choice)
}

suspend fun DialogueContext.chosen(choice: Int) {
    when (choice) {
        1 -> meleeCombat()
        2 -> weaponTypes()
        3 -> skillcapes()
        4 -> training()
    }
}

suspend fun DialogueContext.meleeCombat() {
    player("unsure", "Tell me about melee combat.")// raised eyebrow then quiet
    npc("talking", """
        Well adventurer, the first thing you will need is a
        sword and a shield appropriate for your level.
    """)
    // look down talking, look up eyebrow raised then quiet
    player.setVar("tab", Tab.WornEquipment.name)
    npc("talking", """
        Make sure to equip your sword and shield. Click on
        them in your inventory, they will disappear from your
        inventory and move to your worn items. You can see
        your worn items in the worn items tab here.
    """)
    player.setVar("tab", Tab.CombatStyles.name)
    npc("talking", """
        When you are wielding your sword you will then be
        able to see the correct options in the combat interface.
    """)
    npc("talking", """
        There are four different melee styles. Accurate,
        aggressive, defensive and controlled. Not all weapons will
        have all four styles though.
    """)
    player("unsure", "Interesting, what does each style do?")
    npc("talking", """
        Well I am glad you asked. The accurate style will give
        you experience points in your Attack skill, you will also
        find you will deal damage more frequently as a result
        of being, well, more accurate.
    """)
    npc("talking", """
        Next we have the aggressive style. This style will give
        you experience points in your Strength skill. When
        using this style you will notice that your attacks will hit
        a little harder.
    """)
    npc("talking", """
        Now for the defensive style, this style will give you
        experience points in your Defensive skill. When using
        this style you will notice that you get hit less often.
    """)
    npc("talking", """
        Finally, we have the controlled style. This style will give
        you the same amount of experience as the other styles
        would but shared across all three of the combat skills.
    """)
    npc("talking", """
        If you were using the training sword for example, there
        are four different attack types. Stab, lunge, slash and
        block.
    """)
    npc("talking", """
        Each type uses one of the attack styles. Stab uses
        accurate, lunge and slash use aggressive and block uses
        defensive.
    """)
    npc("talking", """
        To find out which style an attack type uses, hover your
        mouse cursor over the style button.
    """)
    npc("unsure", "Is there anything else you would like to know?")
    val choice = choice("""
        What if I wanted to fight something a bit more... human.
        Tell me about different weapon types I can use.
        Tell me about skillcapes.
        I'd like a training sword and shield.
        Goodbye.
    """)
    if (choice == 1) {
        player("talking", """
            What if I wanted to fight something a bit more...
            human.
        """)
        npc("cheerful", """
            Well adventurer, there are a few places you might be
            able to do this.
        """)
        npc("talking", """
            You could try your luck at castle wars. Here, two
            teams fight each other to defend their respective flags.
            To win the game you will need to get the other team's
            flag and return it to your flag stand.
        """)
        player("cheerful", "Capture the flag, sounds like a lot of fun.")
        npc("talking", """
            If you are in a clan, you should gather some clan
            members and try out clan wars. There you can see
            which clan is better than the other by fighting each
            other in an arena.
        """)
        npc("talking", """
            Both activities are safe minigame, which means if you
            die you will not loose any of your items. You can get
            to them by using the teleport option in your minigames
            tab.
        """)
        npc("suspicious", """
            There is also the wilderness. The wilderness is north of
            Varrock and you can fight other players there. But
            bare in mind if you die to another player in the
            wilderness you will lose your stuff.
        """)
        npc("suspicious", """
            But this also means that if you kill another player you
            will be able to take their stuff too.
        """)
        npc("suspicious", """
            Only go into the wilderness with items you are willing
            to lose and pay attention to the wilderness level you are
            in. The higher the level you go, more player will be able
            to attack you.
        """)
        npc("suspicious", """
            You can find which player can attack you by checking
            your combat level.
        """)
        npc("suspicious", """
            Minus the wilderness level from your combat level to
            find the lowest level that you can attack, then add the
            wilderness level to your combat level to find the highest
            level that you can attack.
        """)
        menu("Is there anything else you would like to know?")
    } else {
        chosen(choice)
    }
}

suspend fun DialogueContext.weaponTypes() {
    player("talking", "Tell me about different weapon types I can use.")
    npc("cheerful", """
        Well let me see now...There are stabbing type weapons
        such as daggers, then you have swords which are
        slashing, maces that have great crushing abilities, battle
        axes which are powerful.
    """)
    npc("cheerful", """
        There are also spears. Spears can be good for Defence
        and many forms of Attack.
    """)
    npc("talking", """
        It depends a lot on how you want to fight. Experiment
        and find out what is best for you. Never be scared to
        try out a new weapon; you never know, you might like
        it!
    """)
    npc("talking", """
        While I tried all of them for a while, I settled on this
        rather good sword.
    """)
    npc("talking", """
        You might also find that different weapon types are
        more accurate against different monsters.
    """)
    menu("Is there anything else you would like to know?")
}

suspend fun DialogueContext.skillcapes() {
    player("talking", "Tell me about skillcapes.")
    if (player.levels.getMax(Skill.Defence) >= Level.MAX_LEVEL) {
        npc("talking", """
            Ah, but I can see you're already a master in the fine
            art of Defence. Perhaps you have come to me to
            purchase a Skillcape of Defence, and thus join the elite
            few who have mastered this exacting skill?
        """)
        npc("talking", """
            In recognition of your defensive abilities, when you have
            it equipped it will act as ring of life, saving you from
            combat if your hitpoints become low.
        """)
        var choice = choice("""
            May I buy a Skillcape of Defence, please?
            Can I ask about something else?
        """)
        if (choice == 1) {
            buySkillcape()
        } else {
            choice = choice("""
                Skillcape
                Hood
            """)
            if (choice == 1) {
                buySkillcape()
            } else {
                player("unsure", "May I have another hood for my cape, please?")
                npc("talking", "Most certainly, and free of charge!")
                item("The tutor hands you another hood for your skillcape.", "defence_hood", 400)
                player.inventory.add("defence_hood")
            }
        }
    } else {
        npc("talking", """
            Of course. Skillcapes are a symbol of achievement. Only
            people who have mastered a skill and reached level 99
            can get their hands on them and gain the benefits they
            carry.
        """)
        npc("talking", """
            The Cape of Defence will act as ring of life, saving you
            from combat if your hitpoints become low.
        """)
        menu("Is there anything else you would like to know?")
    }
}

suspend fun DialogueContext.buySkillcape() {
    player("unsure", "May I buy a Skillcape of Defence, please?")
    npc("talking", """
        You wish to join the elite defenders of this world? I'm
        afraid such things do not come cheaply - in fact they
        cost 99000 coins, to be precise!
    """)
    val choice = choice("""
        99000 coins? That's much too expensive.
        I think I have the money right here, actually.
    """)
    if (choice == 1) {
        player("unsure", "99000 coins? That's much too expensive.")
        npc("talking", """
            Not at all; there are many other adventurers who
            would love the opportunity to purchase such a
            prestigious item! You can find me here if you change
            your mind.
        """)
    } else {
        player("cheerful", "I think I have the money right here, actually.")
        if (player.inventory.spaces < 2) {
            npc("upset", """
                Unfortunately all Skillcapes are only available with a free
                hood, it's part of a skill promotion deal; buy one get one
                free, you know. So you'll need to free up some
                inventory space before I can sell you one.
            """)
            return
        }
        if (player.inventory.remove("coins", 99000)) {
            npc("cheerful", "Excellent! Wear that cape with pride my friend.")
            player.inventory.add("defence_hood")
            val trimmed = Skill.values().any { it != Skill.Defence && player.levels.getMax(it) >= Level.MAX_LEVEL }
            player.inventory.add("defence_skillcape${if (trimmed) "_t" else ""}")
        } else {
            player("upset", "But, unfortunately, I was mistaken.")
            npc("talking", "Well, come back and see me when you do.")
        }
    }
}

suspend fun DialogueContext.training() {
    player("talking", "I'd like a training sword and shield.")
    if (player.has("training_sword", banked = true) || player.has("training_shield", banked = true)) {
        npc("unsure", """
            You already have a training sword and shield. Save
            some for the other adventurers.
        """)
        menu("Is there anything else I can help you with?")
        return
    }

    if (player.inventory.spaces < 2) {
        npc("upset", """
            You don't have enough space for me to give you a
            training sword, nor a shield.
        """)
        menu("Is there anything else I can help you with?")
        return
    }

    item("Harlan gives you a Training sword.", "training_sword", 800)
    player.inventory.add("training_sword")
    item("Harlan gives you a Training shield.", "training_shield", 800)
    player.inventory.add("training_shield")
    npc("talking", "There you go, use it well.")
    menu("Is there anything else I can help you with?")
}
