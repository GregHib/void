package world.gregs.voidps.world.map.lumbridge.combat_hall

import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.activity.bank.ownsItem
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*
import java.util.concurrent.TimeUnit

on<NPCOption>({ operate && def.name == "Ranged instructor" && option == "Talk-to" }) { player: Player ->
    npc<Unsure>("Hey there adventurer, I am the Ranged combat tutor. Is there anything you would like to know?")
    menu()
}

suspend fun CharacterContext.menu(followUp: String = "") {
    if (followUp.isNotEmpty()) {
        npc<Unsure>(followUp)
    }
    choice {
        rangedTraining()
        arrowMaking()
        option("I'd like some arrows and a training bow.") {
            claimBow()
        }
        option("Goodbye.")
    }
}

suspend fun PlayerChoice.rangedTraining(): Unit = option<Talking>("How can I train my Ranged?") {
    npc<Cheerful>("To start with you'll need a bow and arrows, you were given a Shortbow and some arrows when you arrived here from Tutorial island.")
    npc<Cheerful>("Alternatively, you can claim a training bow and some arrows from me.")
    npc<Cheerful>("Mikasi, the Magic Combat tutor and I both give out items every 30 minutes, however you must choose whether you want runes or ranged equipment.")
    /*npc<Cheerful>("        To claim the Training bow and arrows, right-click on
        me and choose Claim, to claim runes right-click on the
        Magic Combat tutor and select Claim.
   ")*/
    npc<Cheerful>("Not all bows can use every type of arrow, most bows have a limit. You can find out your bows limit by checking the Ranged skill guide.")
    npc<Amazed>("If you do decide to use the Training bow, you will only be able to use the Training arrows with it. Remember to pick up your arrows, re-use them and come back when you need more.")
    npc<Cheerful>("Once you have your bow and arrows, equip them by selecting their Wield option in your inventory.")
    npc<Cheerful>("You can change the way you attack by going to the combat options tab. There are three attack styles for bows. Those styles are Accurate, Rapid and Longrange.")
    npc<Amazed>("Accurate increases your bows attack accuracy. Rapid will increase your attack speed with the bow. Longrange will let you attack your enemies from a greater distance.")
    npc<Talking>("If you are ever in the market for a new bow or some arrows, you should head on over to Lowe's Archery Emporium in Varrock.")
    menu("Is there anything else you want to know?")
}

suspend fun PlayerChoice.arrowMaking(): Unit = option<Unsure>("How do I create a bow and arrows?") {
    npc<Cheerful>("Ahh the art of fletching. Fletching is used to create your own bow and arrows.")
    npc<Amazed>("It's quite simple really. You'll need an axe to cut some logs from trees and a knife. Knives can be found in and around the Lumbridge castle and in the Varrock General store upstairs.")
    npc<Cheerful>("Use your knife on the logs. This will bring up a menu listing items you can fletch.")
    npc<Cheerful>("For arrows you will need to smith some arrow heads and kill some chickens for feathers.")
    npc<Cheerful>("Add the feathers to your Arrow shafts to make Headless arrows, then use your chosen arrow heads on the Headless arrows to make your arrows.")
    npc<Cheerful>("Now for making bows. When accessing the fletching menu, instead of choosing Arrows shafts, you can make an unstrung bow instead.")
    npc<Cheerful>("To complete the bow you will need to get your hands on a Bow string.")
    npc<Amazed>("First you will need to get some flax from a flax field. There's one south of Seers' Village. Gather flax, then spin it on a spinning wheel, there's one in Seers' Village too.")
    npc<Cheerful>("This makes bow strings which you can then use on the unstrung bows to make a working bow!")
    player<Cheerful>("Brilliant. If I forget anything I'll come talk to you again.")
    menu("Is there anything else you want to know?")
}

suspend fun CharacterContext.claimBow() {
    if (player.remaining("claimed_tutor_consumables", epochSeconds()) > 0) {
        npc<Amazed>("I work with the Magic tutor to give out consumable items that you may need for combat such as arrows and runes. However we have had some cheeky people try to take both!")
        npc<Cheerful>("So, every half an hour, you may come back and claim either arrows OR runes, but not both. Come back in a while for arrows, or simply make your own.")
        return
    }
    if (player.ownsItem("training_bow") || player.ownsItem("training_arrows")) {
        hasEquipment()
        if (player.ownsItem("training_arrows")) {
            return
        }
    }
    if (!player.ownsItem("training_bow")) {
        if (player.inventory.isFull()) {
            npc<Upset>("If you had enough space in your inventory I'd give you a training bow, come back when you do.")
            player.inventoryFull()
            return
        }
    }
    if (player.inventory.spaces < 2) {
        npc<Upset>("If you had enough space in your inventory I'd give you some arrows, come back when you do.")
        player.inventoryFull()
        return
    }
    if (!player.ownsItem("training_bow")) {
        item("training_bow", 400, "Nemarti gives you a Training shortbow.")
        player.inventory.add("training_bow")
    }
    item("training_arrows", 400, "Mikasi gives you 25 arrows. They can only be used with the Training shortbow.")
    player.inventory.add("training_arrows", 25)
    player.start("claimed_tutor_consumables", TimeUnit.MINUTES.toSeconds(30).toInt(), epochSeconds())
}

suspend fun CharacterContext.hasEquipment() {
    var banked = false
    if (player.bank.contains("training_arrows")) {
        npc<Cheerful>("You have some training arrows in your bank.")
        banked = true
    }
    if (player.bank.contains("training_bow")) {
        npc<Cheerful>("You have a training bow in your bank.")
        banked = true
    }
    if (banked) {
        item("bank_icon", 1200, "You have some arrows in your bank. Climb the stairs in Lumbridge Castle until you see this icon on your minimap. There you will find a bank.")
        return
    }
    if (player.inventory.contains("training_arrows") || player.equipment.contains("training_arrows")) {
        npc<Cheerful>("You already have some training arrows.")
    }
    if (player.inventory.contains("training_bow") || player.equipment.contains("training_bow")) {
        npc<Cheerful>("You already have a training bow.")
    }
}
