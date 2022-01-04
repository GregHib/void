package world.gregs.voidps.world.map.lumbridge.combat_hall

import world.gregs.voidps.engine.client.ui.dialogue.DialogueContext
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.toTicks
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.bank.has
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import java.util.concurrent.TimeUnit

on<NPCOption>({ npc.def.name == "Ranged instructor" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        npc("unsure", """
            Hey there adventurer, I am the Ranged combat tutor.
            Is there anything you would like to know?
        """)
        menu()
    }
}

suspend fun DialogueContext.menu(followUp: String = "") {
    if (followUp.isNotEmpty()) {
        npc("unsure", followUp)
    }
    val choice = choice("""
        How can I train my Ranged?
        How do I create a bow and arrows?
        I'd like some arrows and a training bow.
        Goodbye.
    """)
    when (choice) {
        1 -> rangedTraining()
        2 -> arrowMaking()
        3 -> claimBow()
    }
}

suspend fun DialogueContext.rangedTraining() {
    player("talking", "How can I train my Ranged?")
    npc("cheerful", """
        To start with you'll need a bow and arrows, you were
        given a Shortbow and some arrows when you arrived
        here from Tutorial island.
    """)
    npc("cheerful", """
        Alternatively, you can claim a training bow and some
        arrows from me.
    """)
    npc("cheerful", """
        Mikasi, the Magic Combat tutor and I both give out
        items every 30 minutes, however you must choose
        whether you want runes or ranged equipment.
    """)
    /*npc("cheerful", """
        To claim the Training bow and arrows, right-click on
        me and choose Claim, to claim runes right-click on the
        Magic Combat tutor and select Claim.
    """)*/
    npc("cheerful", """
        Not all bows can use every type of arrow, most bows
        have a limit. You can find out your bows limit by
        checking the Ranged skill guide.
    """)
    npc("amazed", """
        If you do decide to use the Training bow, you will only
        be able to use the Training arrows with it. Remember
        to pick up your arrows, re-use them and come back
        when you need more.
    """)
    npc("cheerful", """
        Once you have your bow and arrows, equip them by
        selecting their Wield option in your inventory.
    """)
    npc("cheerful", """
        You can change the way you attack by going to the
        combat options tab. There are three attack styles for
        bows. Those styles are Accurate, Rapid and Longrange.
    """)
    npc("amazed", """
        Accurate increases your bows attack accuracy. Rapid
        will increase your attack speed with the bow. Longrange
        will let you attack your enemies from a greater
        distance.
    """)
    npc("talking", """
        If you are ever in the market for a new bow or some
        arrows, you should head on over to Lowe's Archery
        Emporium in Varrock.
    """)
    menu("Is there anything else you want to know?")
}

suspend fun DialogueContext.arrowMaking() {
    player("unsure", "How do I create a bow and arrows?")
    npc("cheerful", """
        Ahh the art of fletching. Fletching is used to create
        your own bow and arrows.
    """)
    npc("amazed", """
        It's quite simple really. You'll need an axe to cut some
        logs from trees and a knife. Knives can be found in
        and around the Lumbridge castle and in the Varrock
        General store upstairs.
    """)
    npc("cheerful", """
        Use your knife on the logs. This will bring up a menu
        listing items you can fletch.
    """)
    npc("cheerful", """
        For arrows you will need to smith some arrow heads
        and kill some chickens for feathers.
    """)
    npc("cheerful", """
        Add the feathers to your Arrow shafts to make Headless
        arrows, then use your chosen arrow heads on the
        Headless arrows to make your arrows.
    """)
    npc("cheerful", """
        Now for making bows. When accessing the fletching
        menu, instead of choosing Arrows shafts, you can make
        an unstrung bow instead.
    """)
    npc("cheerful", """
        To complete the bow you will need to get your hands
        on a Bow string.
    """)
    npc("amazed", """
        First you will need to get some flax from a flax field.
        There's one south of Seers' Village. Gather flax, then
        spin it on a spinning wheel, there's one in Seers' Village
        too.
    """)
    npc("cheerful", """
        This makes bow strings which you can then use on the
        unstrung bows to make a working bow!
    """)
    player("cheerful", """
        Brilliant. If I forget anything I'll come talk to you
        again.
    """)
    menu("Is there anything else you want to know?")
}

suspend fun DialogueContext.claimBow() {
    if (player.hasEffect("claimed_tutor_consumables")) {
        npc("amazed", """
            I work with the Magic tutor to give out consumable
            items that you may need for combat such as arrows
            and runes. However we have had some cheeky people
            try to take both!
        """)
        npc("cheerful", """
            So, every half an hour, you may come back and claim
            either arrows OR runes, but not both. Come back in a
            while for arrows, or simply make your own.
        """)
        return
    }
    if (player.has("training_bow", banked = true) || player.has("training_arrows", banked = true)) {
        hasEquipment()
        if (player.has("training_arrows", banked = true)) {
            return
        }
    }
    if (player.inventory.isFull()) {
        npc("upset", """
            If you had enough space in your inventory I'd give
            you a training bow, come back when you do.
        """)
        player.inventoryFull()
        return
    }
    if (player.inventory.spaces < 2) {
        npc("upset", """
            If you had enough space in your inventory I'd give
            you some arrows, come back when you do.
        """)
        player.inventoryFull()
        return
    }
    item("Nemarti gives you a Training shortbow.", "training_bow", 400)
    player.inventory.add("training_bow")
    item("""
        Mikasi gives you 25 arrows. They can only
        be used with the Training shortbow.
    """, "training_arrows", 400)
    player.inventory.add("training_arrows", 25)
    player.start("claimed_tutor_consumables", ticks = TimeUnit.MINUTES.toTicks(30), persist = true)
}

suspend fun DialogueContext.hasEquipment() {
    var banked = false
    if (player.bank.contains("training_arrows")) {
        npc("cheerful", "You have some training arrows in your bank.")
        banked = true
    }
    if (player.bank.contains("training_bow")) {
        npc("cheerful", "You have a training bow in your bank.")
        banked = true
    }
    if (banked) {
        item("""
            You have some arrows in your bank. Climb the stairs in
            Lumbridge Castle until you see this icon on your
            minimap. There you will find a bank.
        """, "bank_icon", 1200)
        return
    }
    if (player.inventory.contains("training_arrows")) {
        npc("cheerful", "You already have some training arrows.")
    }
    if (player.inventory.contains("training_bow")) {
        npc("cheerful", "You already have a training bow.")
    }
}
