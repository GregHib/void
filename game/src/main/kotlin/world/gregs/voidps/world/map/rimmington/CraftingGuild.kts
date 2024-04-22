package world.gregs.voidps.world.map.rimmington

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.obj.door.Door

val logger = InlineLogger()

objectOperate("Open", "guild_door_2_closed") {
    if (player.tile.y == 3288) {
        Door.enter(player, target)
        return@objectOperate
    }
    if (!player.has(Skill.Crafting, 40)) {
        npc<Talking>("master_crafter", "Sorry, only experienced crafters are allowed in here. You must be level 40 or above to enter.")
        return@objectOperate
    }
    if (!player.equipment.contains("brown_apron")) {
        npc<Talking>("master_crafter", "Where's your brown apron? You can't come in here unless you're wearing one.")
        return@objectOperate
    }
    Door.enter(player, target)
    npc<Cheerful>("master_crafter", "Welcome to the Guild of Master Craftsmen.")
}

npcOperate("Talk-to", "master_crafter") {
    npc<Cheerful>("Hello, and welcome to the Crafting Guild. Accomplished crafters from all over the land come here to use our top notch workshops.")
    if (player.hasMax(Skill.Crafting, 99)) {
        player<Unsure>("Are you the person I need to talk to about buying a Skillcape of Crafting")
        npc<Cheerful>("I certainly am, and I can see that you are definitely talented enough to own one! Unfortunately, being such a prestigious item, they are appropriately expensive. I'm afraid I must ask you for 99000 gold.")
        choice {
            option("99000 gold! Are you mad?") {
                npc<Talking>("Not at all; there are many other adventurers who would love the opportunity to purchase such a prestigious item! You can find me here if you change your mind.")
            }
            option<Talking>("That's fine.") {
                player.inventory.transaction {
                    remove("coins", 99000)
                    add("crafting_cape")
                    add("crafting_hood")
                }
                when (player.inventory.transaction.error) {
                    is TransactionError.Deficient -> {
                        player<Upset>("But, unfortunately, I don't have enough money with me.")
                        npc<Talking>("Well, come back and see me when you do.")
                    }
                    is TransactionError.Full -> npc<Unsure>("Unfortunately all Skillcapes are only available with a free hood, it's part of a skill promotion deal; buy one get one free, you know. So you'll need to free up some inventory space before I can sell you one.")
                    TransactionError.None -> npc<Cheerful>("Excellent! Wear that cape with pride my friend.")
                    else -> logger.debug { "Error buying crafting skillcape." }
                }
            }
        }
    } else {
        player<Unsure>("Hey, what is that cape you're wearing? I don't recognise it.")
        npc<Cheerful>("This? This is a Skillcape of Crafting. It is a symbol of my ability and standing here in the Crafting Guild. If you should ever achieve level 99 Crafting come and talk to me and we'll see if we can sort you out with one.")
    }
}

npcOperate("Talk-to", "master_crafter_2") {
    npc<Cheerful>("Hello, and welcome to the Crafting Guild. Accomplished crafters from all over the land come here to use our top notch workshops.")
}

npcOperate("Talk-to", "master_crafter_3") {
    npc<Uncertain>("Yeah?")
    player<Cheerful>("Hello.")
    npc<Uncertain>("Whassup?")
    player<Unsure>("So... are you here to give crafting tips?")
    npc<Uncertain>("Dude, do I look like I wanna talk to you?")
    player<Talking>("I suppose not.")
    npc<Cheerful>("Right on!")
}