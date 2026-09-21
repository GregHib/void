package content.area.asgarnia.falador

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Unamused
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class MiningGuildDwarf : Script {

    init {
        npcOperate("Talk-to", "dwarf_falador,dwarf_falador_2") {
            npc<Neutral>("Welcome to the mining guild. Can I help you with anything?")
            menu()
        }
    }

    private suspend fun Player.menu(nextGuildQuestion: Boolean? = null) {
        choice("Select an option") {
            if (nextGuildQuestion == null || nextGuildQuestion) {
                option<Quiz>("What have you got in the guild?") {
                    npc<Happy>("All sorts of things!<br>There's plenty of coal rocks along with some iron,<br>mithril and adamantite as well.")
                    npc<Happy>("There's no better mining site anywhere!")
                    menu(nextGuildQuestion = false)
                    return@option
                }
            }
            if (nextGuildQuestion == null || !nextGuildQuestion) {
                option<Quiz>("What do you dwarves do with the ore you mine?") {
                    npc<Happy>("What do you think? We smelt it into bars, smith the metal to make armour and weapons, then we exchange them for goods and services.")
                    player<Unamused>("I don't see many dwarves<br>selling armour or weapons here.")
                    npc<Neutral>("No, this is only a mining outpost. We dwarves don't<br>much like to settle in human cities. Most of the ore is<br>carted off to Keldagrim, the great dwarven city.<br>They've got a special blast furnace up there - it makes")
                    npc<Neutral>("smelting the ore so much easier. There are plenty of dwarven traders working in Keldagrim. Anyway, can I<br>help you with anything else?")
                    menu(nextGuildQuestion = true)
                    return@option
                }
            }
            option("No thanks, I'm fine.")
        }
    }
}
