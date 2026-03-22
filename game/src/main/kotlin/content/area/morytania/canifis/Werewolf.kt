package content.area.morytania.canifis

import content.entity.effect.transform
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class Werewolf : Script {
    init {
        val werewolves = "alexis,boris,eduard,galina,georgy,imre,irina,joseph,ksenia,lev,liliya,milla,nikita,nikolai,sofiya,svetlana,vera,yadviga,yuri,zoja"
        npcOperate("Talk-to", werewolves) {
            if (equipped(EquipSlot.Ring).id.startsWith("ring_of_charos")) {
                when (random.nextInt(10)) {
                    0 -> npc<Neutral>("Fancy going up to the castle for a bit of a snack?")
                    1 -> npc<Neutral>("Seen any humans around here? I'm v-e-r-y hungry.")
                    2 -> npc<Neutral>("I bet you have wonderful paws.")
                    3 -> npc<Neutral>("A very miserable day, altogether... enjoy it while it lasts.")
                    4 -> npc<Neutral>("You look to me like someone with a healthy taste for blood.")
                    5 -> npc<Neutral>("If you catch anyone promise me you'll share.")
                    6 -> npc<Neutral>("Give me a moment, I have a bit of someone stuck in my teeth...")
                    7 -> npc<Neutral>("I haven't smelt you around here before...")
                    8 -> npc<Neutral>("Good day to you, my friend.")
                    else -> npc<Neutral>("You smell familiar...")
                }
                return@npcOperate
            }
            when (random.nextInt(10)) {
                0 -> npc<Angry>("Leave me alone.")
                1 -> npc<Angry>("If I were as ugly as you I would not dare to show my face in public!")
                2 -> npc<Angry>("I have no interest in talking to a pathetic meat bag like yourself.")
                3 -> npc<Angry>("Don't talk to me again if you value your life!")
                4 -> npc<Angry>("I don't have anything to give you so leave me alone, mendicant.")
                5 -> npc<Angry>("Out of my way, punk.")
                6 -> npc<Angry>("Get lost!")
                7 -> npc<Angry>("Have you no manners?")
                8 -> npc<Angry>("I don't have time for this right now.")
                else -> {
                    npc<Angry>("Hmm... you smell strange...")
                    player<Quiz>("Strange how?")
                    npc<Angry>("Like a human!")
                    player<Shock>("Oh! Er... I just ate one is why!")
                }
            }
        }

        npcCombatDamage(werewolves) { (source) ->
            if (transform != "") {
                return@npcCombatDamage
            }
            areaSound("lycanthropy", tile, radius = 5)
            source.mode = EmptyMode
            mode = PauseMode
            softQueue("werewolf_transform", 1) {
                transform("werewolf")
                if (source is Player) {
                    interactPlayer(source, "Attack")
                }
            }
        }
    }
}
