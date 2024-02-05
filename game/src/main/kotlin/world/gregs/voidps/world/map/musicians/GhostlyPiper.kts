package world.gregs.voidps.world.map.musicians

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate({ target.id == "ghostly_piper" && option == "Talk-to" }) { player: Player ->
    if (player.equipped(EquipSlot.Amulet).id != "ghostspeak_amulet") {
        npc<Happy>("Woo, wooo. Woooo.")
        player.message("The ghost seems barely aware of your existence,")
        player.message("but you sense that resting here might recharge you for battle!")
        return@npcOperate
    }
    choice()
}

suspend fun CharacterContext.choice() {
    choice {
        option<Unsure>("Who are you?") {
            npc<Cheerful>("I play the pipes, to rouse the brave warriors of Saradomin for the fight!")
            player<Unsure>("Which fight?")
            npc<Cheerful>("Why, the great battles with the forces of Zamorak, of course!")
            player<Unsure>("I see. How long have you been standing here then?")
            npc<Cheerful>("Well, it is all a bit fuzzy. I remember standing at the front of the massed forces of Saradomin, and playing the Call to Arms, but after that I can't quite recall.")

            player<Talk>("I think you've been here for quite some time. You do know you're a gh-")
            player<Talking>("No, never mind, you look happy enough here, and your music is quite rousing. I might rest here a while.")
            choice()
        }
        option<Unsure>("That's all for now") {
            npc<Cheerful>("Be strong and fight the good fight, my friend!")
        }
    }
}