package content.area.misthalin.varrock.cooks_guild

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class HeadChef : Script {
    init {
        npcOperate("Talk-to", "head_chef") {
            if (levels.get(Skill.Cooking) == 99) {
                // https://youtu.be/6LuyWmDoJtg?si=_jAvyeri-gTmBEFI&t=66
                npc<Happy>("Hello, welcome to the Cooking Guild. It's always great to have such an accomplished chef visit us. Say, would you be interested in a Skillcape of Cooking? They're only available to master chefs.")
                choice {
                    option("No thanks.")
                    option("Yes please.") {
                        player<Quiz>("Can I buy a Skillcape of Cooking from you?")
                        npc<Neutral>("Most certainly, just as soon as you give me 99000 gold coins.")
                        choice {
                            option("That's much too expensive.")
                            option<Happy>("Sure") {
                                inventory.transaction {
                                    val trimmed = Skill.entries.any { it != Skill.Cooking && levels.getMax(it) >= Level.MAX_LEVEL }
                                    remove("coins", 99000)
                                    add("cooking_cape${if (trimmed) "_t" else ""}")
                                    add("cooking_hood")
                                }
                                when (inventory.transaction.error) {
                                    is TransactionError.Deficient -> player<Sad>("I'm afraid you don't have enough coins on me at the moment.") // TODO proper message
                                    is TransactionError.Full -> npc<Sad>("I'm afraid you don't have enough inventory spaces to take the cape and hood.") // TODO proper message
                                    TransactionError.None -> npc<Happy>("Now you can use the title Master Chef.")
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            } else {
                npc<Happy>("Hello, welcome to the Cooking Guild. Only accomplished chefs and cooks are allowed in here. Feel free to use any of our facilities.")
                choice {
                    option("Nice cape you're wearing!") {
                        player<Neutral>("Nice cape, you're wearing!")
                        npc<Happy>("Thank you! It's my most prized possession, it's a Skillcape of Cooking; it shows that I've achieved level 99 Cooking and am one of the best chefs in the land!")
                        npc<Neutral>("If you ever achieve level 99 cooking you'll get to wear one too and doing so means you'll never burn any food!")
                    }
                    option<Neutral>("Thanks, bye.")
                }
            }
        }
    }
}