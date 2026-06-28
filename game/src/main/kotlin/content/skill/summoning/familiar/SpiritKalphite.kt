package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random

class SpiritKalphite : Script {
    init {
        npcOperate("Interact", "spirit_kalphite_familiar") {
            if (equipped(EquipSlot.Weapon).id.startsWith("keris") || inventory.items.any { it.id.startsWith("keris") }) {
                npc<Neutral>("How dare you!")
                player<Happy>("How dare I what?")
                npc<Neutral>("That weapon offends us!")
                player<Happy>("How dare you!")
                player<Happy>("What weapon?")
                player<Happy>("That weapon offends us!")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("This activity is not optimal for us.")
                    player<Happy>("Well, you'll just have to put up with it for now.")
                    npc<Neutral>("We would not have to 'put up' with this in the hive.")
                }
                1 -> {
                    npc<Neutral>("We are growing infuriated. What is our goal?")
                    player<Happy>("Well, I haven't quite decided yet.")
                    npc<Neutral>("There is no indecision in the hive.")
                    player<Happy>("Or a sense of humour or patience, it seems.")
                }
                2 -> {
                    npc<Neutral>("We find this to be wasteful of our time.")
                    player<Happy>("Maybe I find you wasteful...")
                    npc<Neutral>("We would not face this form of abuse in the hive.")
                }
                3 -> {
                    npc<Neutral>("We grow tired of your antics, biped.")
                    player<Happy>("What antics? I'm just getting on with my day.")
                    npc<Neutral>("In an inefficient way. In the hive, you would be replaced.")
                    player<Happy>("In the hive this, in the hive that...")
                }
            }
        }
    }
}
