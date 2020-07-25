package rs.dusk.engine.client.ui

import rs.dusk.engine.client.ui.InterfaceId.AncientSpellbook
import rs.dusk.engine.client.ui.InterfaceId.AreaStatusIcon
import rs.dusk.engine.client.ui.InterfaceId.CastleWarsScore
import rs.dusk.engine.client.ui.InterfaceId.CastleWarsStatusOverlay
import rs.dusk.engine.client.ui.InterfaceId.Chat1
import rs.dusk.engine.client.ui.InterfaceId.Chat2
import rs.dusk.engine.client.ui.InterfaceId.Chat3
import rs.dusk.engine.client.ui.InterfaceId.Chat4
import rs.dusk.engine.client.ui.InterfaceId.ChatBackground
import rs.dusk.engine.client.ui.InterfaceId.ChatBoth
import rs.dusk.engine.client.ui.InterfaceId.ChatBox
import rs.dusk.engine.client.ui.InterfaceId.ChatNp1
import rs.dusk.engine.client.ui.InterfaceId.ChatNp2
import rs.dusk.engine.client.ui.InterfaceId.ChatNp3
import rs.dusk.engine.client.ui.InterfaceId.ChatNp4
import rs.dusk.engine.client.ui.InterfaceId.ClanChat
import rs.dusk.engine.client.ui.InterfaceId.CombatStyles
import rs.dusk.engine.client.ui.InterfaceId.ConfirmDestroy
import rs.dusk.engine.client.ui.InterfaceId.ContainerContinue
import rs.dusk.engine.client.ui.InterfaceId.DoubleChat1
import rs.dusk.engine.client.ui.InterfaceId.DoubleChat2
import rs.dusk.engine.client.ui.InterfaceId.DoubleChat3
import rs.dusk.engine.client.ui.InterfaceId.DoubleChat4
import rs.dusk.engine.client.ui.InterfaceId.DoubleObjBox
import rs.dusk.engine.client.ui.InterfaceId.DungeoneeringSpellbook
import rs.dusk.engine.client.ui.InterfaceId.Emotes
import rs.dusk.engine.client.ui.InterfaceId.EnergyOrb
import rs.dusk.engine.client.ui.InterfaceId.FilterButtons
import rs.dusk.engine.client.ui.InterfaceId.FixedGameframe
import rs.dusk.engine.client.ui.InterfaceId.FriendsChat
import rs.dusk.engine.client.ui.InterfaceId.FriendsList
import rs.dusk.engine.client.ui.InterfaceId.GardenQuiz
import rs.dusk.engine.client.ui.InterfaceId.HealthOrb
import rs.dusk.engine.client.ui.InterfaceId.Inventory
import rs.dusk.engine.client.ui.InterfaceId.LevelUpDialog
import rs.dusk.engine.client.ui.InterfaceId.Logout
import rs.dusk.engine.client.ui.InterfaceId.LunarSpellbook
import rs.dusk.engine.client.ui.InterfaceId.MacroMimeEmotes
import rs.dusk.engine.client.ui.InterfaceId.MacroQuizShow
import rs.dusk.engine.client.ui.InterfaceId.MakeAmount
import rs.dusk.engine.client.ui.InterfaceId.Message1
import rs.dusk.engine.client.ui.InterfaceId.Message2
import rs.dusk.engine.client.ui.InterfaceId.Message3
import rs.dusk.engine.client.ui.InterfaceId.Message4
import rs.dusk.engine.client.ui.InterfaceId.Message5
import rs.dusk.engine.client.ui.InterfaceId.MessageNp1
import rs.dusk.engine.client.ui.InterfaceId.MessageNp2
import rs.dusk.engine.client.ui.InterfaceId.MessageNp3
import rs.dusk.engine.client.ui.InterfaceId.MessageNp4
import rs.dusk.engine.client.ui.InterfaceId.MessageNp5
import rs.dusk.engine.client.ui.InterfaceId.MobilisingArmies1
import rs.dusk.engine.client.ui.InterfaceId.ModernSpellbook
import rs.dusk.engine.client.ui.InterfaceId.Multi2
import rs.dusk.engine.client.ui.InterfaceId.Multi2Chat
import rs.dusk.engine.client.ui.InterfaceId.Multi2Mes
import rs.dusk.engine.client.ui.InterfaceId.Multi3
import rs.dusk.engine.client.ui.InterfaceId.Multi3Chat
import rs.dusk.engine.client.ui.InterfaceId.Multi3OffCentre
import rs.dusk.engine.client.ui.InterfaceId.Multi4
import rs.dusk.engine.client.ui.InterfaceId.Multi4Chat
import rs.dusk.engine.client.ui.InterfaceId.Multi4Offscreen
import rs.dusk.engine.client.ui.InterfaceId.Multi5
import rs.dusk.engine.client.ui.InterfaceId.Multi5Chat
import rs.dusk.engine.client.ui.InterfaceId.MultiVar2
import rs.dusk.engine.client.ui.InterfaceId.MultiVar2Wide
import rs.dusk.engine.client.ui.InterfaceId.MultiVar3
import rs.dusk.engine.client.ui.InterfaceId.MultiVar4
import rs.dusk.engine.client.ui.InterfaceId.MultiVar5
import rs.dusk.engine.client.ui.InterfaceId.MusicPlayer
import rs.dusk.engine.client.ui.InterfaceId.Notes
import rs.dusk.engine.client.ui.InterfaceId.NpcChat1
import rs.dusk.engine.client.ui.InterfaceId.NpcChat2
import rs.dusk.engine.client.ui.InterfaceId.NpcChat3
import rs.dusk.engine.client.ui.InterfaceId.NpcChat4
import rs.dusk.engine.client.ui.InterfaceId.NpcChatNp1
import rs.dusk.engine.client.ui.InterfaceId.NpcChatNp1u
import rs.dusk.engine.client.ui.InterfaceId.NpcChatNp2
import rs.dusk.engine.client.ui.InterfaceId.NpcChatNp2u
import rs.dusk.engine.client.ui.InterfaceId.NpcChatNp3
import rs.dusk.engine.client.ui.InterfaceId.NpcChatNp3u
import rs.dusk.engine.client.ui.InterfaceId.NpcChatNp4
import rs.dusk.engine.client.ui.InterfaceId.NpcChatNp4u
import rs.dusk.engine.client.ui.InterfaceId.ObjBox
import rs.dusk.engine.client.ui.InterfaceId.ObjDialog
import rs.dusk.engine.client.ui.InterfaceId.OpenUrl
import rs.dusk.engine.client.ui.InterfaceId.Options
import rs.dusk.engine.client.ui.InterfaceId.PickAKitten
import rs.dusk.engine.client.ui.InterfaceId.PickAPuppy
import rs.dusk.engine.client.ui.InterfaceId.PohHangman
import rs.dusk.engine.client.ui.InterfaceId.PohHangmanGerman
import rs.dusk.engine.client.ui.InterfaceId.PrayerList
import rs.dusk.engine.client.ui.InterfaceId.PrayerOrb
import rs.dusk.engine.client.ui.InterfaceId.PriceCheckBoxTitle
import rs.dusk.engine.client.ui.InterfaceId.PrivateChat
import rs.dusk.engine.client.ui.InterfaceId.QuestJournals
import rs.dusk.engine.client.ui.InterfaceId.ResizableGameframe
import rs.dusk.engine.client.ui.InterfaceId.Select2Models
import rs.dusk.engine.client.ui.InterfaceId.SkillCreation
import rs.dusk.engine.client.ui.InterfaceId.SkillCreationAmount
import rs.dusk.engine.client.ui.InterfaceId.SmeltType
import rs.dusk.engine.client.ui.InterfaceId.Stats
import rs.dusk.engine.client.ui.InterfaceId.SummoningOrb
import rs.dusk.engine.client.ui.InterfaceId.TaskSystem
import rs.dusk.engine.client.ui.InterfaceId.TextBoxChat
import rs.dusk.engine.client.ui.InterfaceId.TextBoxContinue
import rs.dusk.engine.client.ui.InterfaceId.TextBoxContinue2
import rs.dusk.engine.client.ui.InterfaceId.TextBoxContinueResizable
import rs.dusk.engine.client.ui.InterfaceId.TextBoxModel
import rs.dusk.engine.client.ui.InterfaceId.TextBoxModelSprite
import rs.dusk.engine.client.ui.InterfaceId.TradeSide
import rs.dusk.engine.client.ui.InterfaceId.TutorialText
import rs.dusk.engine.client.ui.InterfaceId.TutorialText2
import rs.dusk.engine.client.ui.InterfaceId.WorldMap
import rs.dusk.engine.client.ui.InterfaceId.WornEquipment


/**
 * List of interface parents and child indices
 * @param resizeableParent The parent interface that [ids] should be display on (-1 for gameframe)
 * @param fixedIndex The component index of the parent [ids] should be displayed on when fixed gameframe
 * @param resizableIndex The component index of the parent [ids] should be displayed on when resizable gameframe
 * @param ids List of interfaces which use this [Window]
 */
enum class Window(val fixedParent: Int, val resizeableParent: Int, val fixedIndex: Int, val resizableIndex: Int, vararg val ids: Int) {
    //Chat box
    CHAT_BACKGROUND(ChatBox,ChatBox, 9, 9, ChatBackground),
    CHAT_BOX(FixedGameframe, ResizableGameframe, 192, 73, ChatBox),
    CHAT_SETTINGS(FixedGameframe, ResizableGameframe, 68, 19, FilterButtons),
    PRIVATE_CHAT(FixedGameframe, ResizableGameframe, 17, 72, PrivateChat),

    //Minimap
    ENERGY_ORB(FixedGameframe, ResizableGameframe, 186, 179, EnergyOrb),
    HEALTH_ORB(FixedGameframe, ResizableGameframe, 183, 177, HealthOrb),
    PRAYER_ORB(FixedGameframe, ResizableGameframe, 185, 178, PrayerOrb),
    SUMMONING_ORB(FixedGameframe, ResizableGameframe, 188, 180, SummoningOrb),

    //Tab slots
    CLAN_CHAT(FixedGameframe, ResizableGameframe, 215, 101, ClanChat),
    COMBAT_STYLES(FixedGameframe, ResizableGameframe, 204, 90, CombatStyles),
    EMOTES(FixedGameframe, ResizableGameframe, 217, 103, Emotes),
    FRIENDS_CHAT(FixedGameframe, ResizableGameframe, 214, 100, FriendsChat),
    FRIENDS_LIST(FixedGameframe, ResizableGameframe, 213, 99, FriendsList),
    INVENTORY(FixedGameframe, ResizableGameframe, 208, 94, Inventory, TradeSide),
    LOGOUT(FixedGameframe, ResizableGameframe, 222, 108, Logout),
    SPELLBOOK(FixedGameframe, ResizableGameframe, 211, 97, ModernSpellbook, LunarSpellbook, AncientSpellbook, DungeoneeringSpellbook),
    MUSIC_PLAYER(FixedGameframe, ResizableGameframe, 218, 104, MusicPlayer),
    NOTES(FixedGameframe, ResizableGameframe, 219, 105, Notes),
    OPTIONS(FixedGameframe, ResizableGameframe, 216, 102, Options),
    PRAYER_LIST(FixedGameframe, ResizableGameframe, 210, 96, PrayerList),
    QUEST_JOURNALS(FixedGameframe, ResizableGameframe, 207, 93, QuestJournals),
    STATS(FixedGameframe, ResizableGameframe, 206, 92, Stats),
    TASK_SYSTEM(FixedGameframe, ResizableGameframe, 205, 91, TaskSystem),
    WORN_EQUIPMENT(FixedGameframe, ResizableGameframe, 209, 95, WornEquipment),

    //Main screen
    DIALOGUE_BOX(ChatBox,ChatBox, 13, 13, Multi4Offscreen, TextBoxModelSprite, CastleWarsScore, CastleWarsStatusOverlay, NpcChatNp4u, NpcChatNp3u, NpcChatNp1u, NpcChatNp2u, ConfirmDestroy, GardenQuiz, ChatBoth, Select2Models, TextBoxModel, MacroMimeEmotes, MacroQuizShow, ContainerContinue, PriceCheckBoxTitle, TutorialText, ObjDialog, PohHangman, TutorialText2, OpenUrl, Multi3OffCentre, PohHangmanGerman, Multi2Mes, PickAPuppy, MultiVar2Wide, PickAKitten, LevelUpDialog, TextBoxContinue, TextBoxContinueResizable, MobilisingArmies1, SkillCreation, SmeltType, MakeAmount, TextBoxChat, TextBoxContinue2, DoubleChat3, DoubleChat2, DoubleChat1, DoubleChat4, DoubleObjBox, ObjBox, Chat1, Chat2, Chat3, Chat4, ChatNp1, ChatNp2, ChatNp3, ChatNp4, Message1, Message2, Message3, Message4, Message5, MessageNp1, MessageNp2, MessageNp3, MessageNp4, MessageNp5, Multi2, Multi2Chat, Multi3, Multi3Chat, Multi4, Multi4Chat, Multi5, Multi5Chat, MultiVar2, MultiVar3, MultiVar4, MultiVar5, NpcChat1, NpcChat2, NpcChat3, NpcChat4, NpcChatNp1, NpcChatNp2, NpcChatNp3, NpcChatNp4),
    AREA(FixedGameframe, ResizableGameframe, 15, 15, AreaStatusIcon),
    OVERLAY(FixedGameframe, ResizableGameframe, 9, 12),
    MAIN_SCREEN(FixedGameframe, ResizableGameframe, 9, 12),
    FULL_SCREEN(0,0, 0, 0, ResizableGameframe, FixedGameframe, WorldMap),

    SKILL_CREATION(SkillCreation,SkillCreation, 4, 4, SkillCreationAmount)
    ;


    companion object {
        val windows = values().flatMap { window -> window.ids.map { id -> id to window } }
        @JvmStatic
        fun main(args: Array<String>) {
            val names = InterfaceId::class.java.fields.toList().map { it.get(InterfaceId) to it.name }.toMap()

            fun getName(id: Int) : String? {
                val name = names[id]
                return name
            }
            windows.forEach { (id, window) ->
                val builder = StringBuilder()
                builder.append("- id: ").append(id).appendln()
                val name = getName(id)
                builder.append("  name: ").append(name).appendln()
                if(window.fixedParent == window.resizeableParent) {
                    val p = getName(window.resizeableParent)
                    if (p != null) {
                        builder.append("  parent: ").append(p).appendln()
                    }
                } else {
                    val fp = getName(window.fixedParent)
                    if (fp != "FixedGameframe") {
                        builder.append("  fixedParent: ").append(fp).appendln()
                    }
                    val rp = getName(window.resizeableParent)
                    if (rp != "ResizableGameframe") {
                        builder.append("  resizeParent: ").append(rp).appendln()
                    }
                }

                if(window.fixedIndex == window.resizableIndex) {
                    builder.append("  index: ").append(window.fixedIndex).appendln()
                } else {
                    builder.append("  fixedIndex: ").append(window.fixedIndex).appendln()
                    builder.append("  resizeIndex: ").append(window.resizableIndex).appendln()
                }
                println(builder.toString())
            }
        }
    }
}