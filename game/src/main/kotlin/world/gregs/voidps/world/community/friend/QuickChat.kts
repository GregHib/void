import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.PrivateQuickMessage
import world.gregs.voidps.engine.entity.character.player.chat.PublicQuickMessage
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.combatLevel
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.encode.privateQuickChatFrom
import world.gregs.voidps.network.encode.privateQuickChatTo
import world.gregs.voidps.network.encode.publicQuickChat

val players: Players by inject()

on<PrivateQuickMessage> { player: Player ->
    val target = players.get(friend)
    if (target == null) {
        player.message("Unable to send message - player unavailable.")
        return@on
    }
    val data = generateData(player, file, data)
    player.client?.privateQuickChatTo(target.name, file, data)
    target.client?.privateQuickChatFrom(player.name, player.rights.ordinal, file, data)
}

on<PublicQuickMessage> { player: Player ->
    val data = generateData(player, file, data)
    player.viewport.players.current.forEach {
        it.client?.publicQuickChat(player.index, 0x8000, player.rights.ordinal, file, data)
    }
}

fun generateData(player: Player, file: Int, data: ByteArray): ByteArray {
    println("$file - ${data.contentToString()}")
    //TODO proper system for handling these (prob should be writing to a packet not setting a ByteArray manually
    when (file) {
        952 -> return byteArrayOf(player.combatLevel.toByte()) //SK2 combat level
        547 -> return int(2)//GA7 quest points
        218 -> return data // GEG5 assist me in skill
        702 -> return byteArrayOf(player.levels.getMax(Skill.Runecrafting).toByte())//GEG6 runecrafting level
        1 -> return byteArrayOf(player.levels.getMax(Skill.Agility).toByte())//SA2 agility level
        615 -> return byteArrayOf((player.levels.getMax(Skill.Agility) - 1).toByte(), (player.levels.getMax(Skill.Agility)).toByte(), *int(12345678))//SA8 diff between agility levels
        8 -> return byteArrayOf(player.levels.getMax(Skill.Attack).toByte())//SZ2 attack level
        616 -> {}//SA8 diff between attack levels
        70 -> return byteArrayOf((player.levels.getMax(Skill.Constitution) / 10).toByte())//SL2 constitution level
        626 -> {}//SL6 dif const level
        965 -> {//SL8 Life points
            val value = player.levels.get(Skill.Constitution)
            return int(value)
        }
        13 -> return byteArrayOf(player.levels.getMax(Skill.Construction).toByte())//SC2 construction level
        617 -> {}//SC4 dif const
        16 -> return byteArrayOf(player.levels.getMax(Skill.Cooking).toByte())//SV2 cooking
        618 -> {}//SV8 dif cooking
        23 -> return byteArrayOf(player.levels.getMax(Skill.Crafting).toByte())//SX2 craft
        619 -> {}//SX8 dif craft
        30 -> return byteArrayOf(player.levels.getMax(Skill.Defence).toByte())//SD2 def
        620 -> {}//SD6 dif def
        990 -> return byteArrayOf(player.levels.getMax(Skill.Dungeoneering).toByte())//SF2 dung
        988 -> {}//SF3 dif dung
        34 -> return byteArrayOf(player.levels.getMax(Skill.Farming).toByte())//SQ2 farm
        621 -> {}//SQ8 dif farm
        41 -> return byteArrayOf(player.levels.getMax(Skill.Firemaking).toByte())//SP2 firemaking
        622 -> {}//SP7 dif firemaking
        47 -> return byteArrayOf(player.levels.getMax(Skill.Fishing).toByte())//SW2 fishing
        623 -> {}//SW5 fishing
        55 -> return byteArrayOf(player.levels.getMax(Skill.Fletching).toByte())//SE2 fletching
        624 -> {}//SE8 dif fletching
        62 -> return byteArrayOf(player.levels.getMax(Skill.Herblore).toByte())//SH2 herblore
        625 -> {}//SH8 dif herblore
        74 -> return byteArrayOf(player.levels.getMax(Skill.Hunter).toByte())//SU2 hunter
        627 -> {}//SU6 dif hunter
        135 -> return byteArrayOf(player.levels.getMax(Skill.Magic).toByte())//SM2 magic
        639 -> {}//SM8 dif magic
        127 -> return byteArrayOf(player.levels.getMax(Skill.Mining).toByte())//SI2 mining
        638 -> {}//SI8 dif mining
        120 -> return byteArrayOf(player.levels.getMax(Skill.Prayer).toByte())//SY2 prayer
        637 -> {}//SY5 dif prayer
        116 -> return byteArrayOf(player.levels.getMax(Skill.Ranged).toByte())//SR2 range
        636 -> {}//SR5 dif range
        111 -> return byteArrayOf(player.levels.getMax(Skill.Runecrafting).toByte())//SN2 runecrafting
        635 -> {}//SN6 dif runecrafting
        103 -> return byteArrayOf(player.levels.getMax(Skill.Slayer).toByte())//SS2 slayer
        634 -> {}//SS8 dif slayer
        96 -> return byteArrayOf(player.levels.getMax(Skill.Smithing).toByte())//SB2 smithing
        631 -> {}//SB8 dif smithing
        92 -> return byteArrayOf(player.levels.getMax(Skill.Strength).toByte())//SG2 strength
        630 -> {}//SG6 dif strength
        85 -> return byteArrayOf(player.levels.getMax(Skill.Summoning).toByte())//SO2 summoning
        629 -> {}//SO8 dif summoning
        79 -> return byteArrayOf(player.levels.getMax(Skill.Thieving).toByte())//ST2 thieving
        628 -> {}//ST7 dif thieving
        142 -> return byteArrayOf(player.levels.getMax(Skill.Woodcutting).toByte())//SJ2 woodcutting
        640 -> {}//SJ7 dif woodcutting
        906 -> {}//ECOGS2 blue team's avatar level
        907 -> {}//ECOGS3 red team's avatar level
        908 -> {}//ECOGS4 blue team's avatar health
        909 -> {}//ECOGS5 red team's avatar health
        957 -> {}//ECH5 champion challenges complete
        963 -> {}//ECWS1 average clan combat level
        356 -> {}//ECFC4 fist of guthix rating
        361 -> {}//ECFD5 fist of guthix charges
        604 -> {}//ECSLS3 stealing creation my levels: fishing, hunter, woodcutting, mining
        605 -> {}//ECSLS4 stealing creation my levels: smithing crafting fletching
        606 -> {}//ECSLS5 stealing creation my levels: construction herblore cooking runecrafting
        608 -> {}//ECSLS7 stealing creation my levels: attack strength ranged magic
        609 -> {}//ECSLS8 stealing creation my levels: defence prayer constitution summoning
        611 -> {}//ECSLP2 stealing creation points
        850 -> {}//EFMCM1 mobilising armies rank
        941 -> {}//EFMCM2 mobilising armies investment credits
        942 -> {}//EFMCM3 mobilising armies reward credits
        961 -> {}//EFDF3 familiarisation raw shards collected
        821 -> {}//EFDP2 penguins found this week
        528 -> {}//CS2 Clan chat rank
    }
    return data
}

fun int(value: Int) = byteArrayOf((value shr 24).toByte(), (value shr 16).toByte(), (value shr 8).toByte(), value.toByte())