package world.gregs.voidps.world.map.lumbridge.combat_hall

import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.bank.ownsItem
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.PlayerChoice
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.player.display.Tab
import java.util.concurrent.TimeUnit

on<NPCOption>({ operate && def.name == "Magic instructor" && option == "Talk-to" }) { player: Player ->
    npc<Unsure>("Hello there adventurer, I am the Magic combat tutor. Would you like to learn about magic combat, or perhaps how to make runes?")
    menu()
}

suspend fun CharacterContext.menu(followUp: String = "") {
    if (followUp.isNotEmpty()) {
        npc<Unsure>(followUp)
    }
    choice {
        magicCombat()
        runeMaking()
        claimRunes()
        option("Goodbye.")
    }
}

suspend fun PlayerChoice.magicCombat(): Unit = option<Talking>("Tell me about magic combat please.") {
    npc<Cheerful>("Of course ${player.name}! As a rule of thumb, if you cast the highest spell of which you're capable, you'll get the best experience possible.")
    npc<Cheerful>("Wearing metal armour and ranged armour can seriously impair your magical abilities. Make sure you wear some robes to maximise your capabilities.")
    npc<Cheerful>("Superheat Item and the Alchemy spells are good ways to level magic if you are not interested in the combat aspect of magic.")
    npc<Suspicious>("There's always the Magic Training Arena. You can find it north of the Duel Arena in Al Kharid. You will be able to earn some special rewards there by practicing your magic there.")
    npc<Cheerful>("I see you already have access to the ancient magicks. Well done, these will aid you greatly.")
    menu("Is there anything else you would like to know?")
}

suspend fun PlayerChoice.runeMaking(): Unit = option<Unsure>("How do I make runes?") {
    npc<Cheerful>("There are a couple of things you will need to make runes, rune essence and a talisman to enter the temple ruins.")
    if (player.experience.get(Skill.Runecrafting) > 0.0) {
        npc<Amazed>("To get rune essence you will need to gather them in the essence mine. You can get to the mine by talking to Aubury who owns the runes shop in south east Varrock.")
        npc<Cheerful>("I see you have some experience already in Runecrafting. Perhaps you should try crafting some runes which you can then use in magic.")
        player["tab"] = Tab.Stats.name
        npc<Cheerful>("Check the skill guide to see which runes you can craft.")
    } else {
        npc<Cheerful>("To get rune essence you will need to gather them somehow. You should talk to the Duke of Lumbridge, he may be able to help you with that. Alternatively, other players may sell you the essence.")
        npc<Cheerful>("As you're fairly new to runecrafting you should start with air runes and mind runes.")
    }
    npc<Cheerful>("You will need a talisman for the rune you would like to create. You can right-click on it and select the Locate option. This will tell you the rough location of the altar.")
    npc<Cheerful>("When you find the ruined altar, use the talisman on it to be transported to a temple where you can craft your runes.")
    npc<Cheerful>("Clicking on the temple's altar will imbue your rune essence with the altar's magical property.")
    npc<Cheerful>("If you want to save yourself an inventory space, you could always try binding the talisman to a tiara.")
    npc<Cheerful>("To make one, take a tiara and talisman to the ruins and use the tiara on the temple altar. This will bind the talisman to your tiara.")
    menu("Is there anything else you would like to know?")
}

suspend fun PlayerChoice.claimRunes(): Unit = option("I'd like some air and mind runes.") {
    if (player.remaining("claimed_tutor_consumables", epochSeconds()) > 0) {
        npc<Amazed>("I work with the Ranged Combat tutor to give out consumable items that you may need for combat such as arrows and runes. However we have had some cheeky people try to take both!")
        npc<Cheerful>("So, every half an hour, you may come back and claim either arrows OR runes, but not both. Come back in a while for runes, or simply make your own.")
        return@option
    }
    if (player.ownsItem("mind_rune") || player.ownsItem("air_rune")) {
        hasRunes()
        return@option
    }
    if (player.inventory.isFull()) {
        npc<Upset>("If you had enough space in your inventory I'd give you some mind runes, come back when you do.")
        player.inventoryFull()
        return@option
    }
    if (player.inventory.spaces < 2) {
        npc<Upset>("If you had enough space in your inventory I'd give you some air runes, come back when you do.")
        player.inventoryFull()
        return@option
    }
    item("air_rune", 400, "Mikasi gives you 30 air runes.")
    player.inventory.add("air_rune", 30)
    player.start("claimed_tutor_consumables", TimeUnit.MINUTES.toSeconds(30).toInt(), epochSeconds())
    item("mind_rune", 400, "Mikasi gives you 30 mind runes.")
    player.inventory.add("mind_rune", 30)
}

suspend fun CharacterContext.hasRunes() {
    var banked = false
    if (player.bank.contains("mind_rune")) {
        npc<Cheerful>("You have some mind runes in your bank.")
        banked = true
    }
    if (player.bank.contains("air_rune")) {
        npc<Cheerful>("You have some air runes in your bank.")
        banked = true
    }
    if (banked) {
        item("bank_icon", 1200, "You have some runes in your bank. Climb the stairs in Lumbridge Castle until you see this icon on your minimap. There you will find a bank.")
        return
    }
    if (player.inventory.contains("mind_rune")) {
        npc<Cheerful>("You already have some mind runes.")
    }
    if (player.inventory.contains("air_rune")) {
        npc<Cheerful>("You already have some air runes.")
    }
}
