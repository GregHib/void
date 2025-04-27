package world.gregs.voidps.tools.convert

object NPCSpawnConverter {

    @JvmStatic
    fun main(args: Array<String>) {
        val string = """
{{LocTableHead|league=yes}}
{{LocLine
|name = Bat
|location = [[Great Kourend]]
|levels = 6
|members = Yes
|mapID = -1
|plane = 0
|x:1644,y:3741|x:1718,y:3824|x:1543,y:3768|x:1544,y:3772|x:1655,y:3880|x:1532,y:3727|x:1480,y:3559|x:1709,y:3742|x:1678,y:3859|x:1691,y:3855|x:1701,y:3862|x:1732,y:3840|x:1737,y:3851|x:1721,y:3855|x:1709,y:3709|x:1710,y:3706|x:1714,y:3703|x:1571,y:3748|x:1575,y:3769|x:1576,y:3751|x:1578,y:3753|x:1578,y:3771|x:1667,y:3735|x:1750,y:3897|x:1714,y:3888|x:1724,y:3890|x:1495,y:3835|x:1499,y:3810|x:1499,y:3831|x:1501,y:3816|x:1502,y:3833|x:1506,y:3834|x:1507,y:3816|x:1508,y:3810|x:1670,y:3797|x:1603,y:3844|x:1615,y:3841|x:1675,y:3877|x:1768,y:3858|x:1624,y:3853|x:1646,y:3765|x:1546,y:3799|x:1551,y:3802|x:1552,y:3791|x:1755,y:3876|x:1482,y:3570|x:1490,y:3557
|mtype = pin
|leagueRegion = Kourend
}}
{{LocLine
|name = Bat
|location = [[Slayer Tower]] Entrance
|levels = 6
|members = Yes
|mapID = -1
|plane = 0
|x:3418,y:3522|x:3425,y:3525|x:3429,y:3522|x:3432,y:3527|x:3434,y:3521|x:3445,y:3526|x:3411,y:3536
|mtype = pin
|leagueRegion = Morytania
}}
{{LocLine
|name = Bat
|location = [[Haunted Woods]]
|levels = 6
|members = Yes
|mapID = -1
|plane = 0
|x:3567,y:3481|x:3599,y:3486|x:3611,y:3499|x:3613,y:3482|x:3628,y:3473|x:3636,y:3466|x:3598,y:3509|x:3565,y:3503|x:3607,y:3462|x:3588,y:3472|x:3580,y:3491|x:3627,y:3511|x:3639,y:3510
|mtype = pin
|leagueRegion = Morytania
}}
{{LocLine
|name = Bat
|location = [[Silvarea]]
|levels = 6
|members = Yes
|mapID = -1
|plane = 0
|x:3341,y:3478|x:3343,y:3484|x:3345,y:3484|x:3350,y:3493|x:3353,y:3494|x:3361,y:3484|x:3364,y:3489|x:3370,y:3497|x:3379,y:3478|x:3379,y:3484|x:3387,y:3483|x:3388,y:3494|x:3389,y:3483
|mtype = pin
|leagueRegion = Misthalin
}}
{{LocLine
|name = Bat
|location = [[Sisterhood Sanctuary]]
|levels = 6
|members = Yes
|mapID = -1
|plane = 0
|x:3727,y:9755|x:3729,y:9763
|mtype = pin
|leagueRegion = Morytania
}}
{{LocLine
|name = Bat
|location = North-west of [[Mistrock]]
|levels = 6
|members = Yes
|mapID = -1
|plane = 0
|x:1333,y:2891|x:1335,y:2894|x:1336,y:2890|x:1343,y:2888
|mtype = pin
|leagueRegion = Varlamore
}}
{{LocLine
|name = Bat
|location = [[Abandoned Mine]] Level 1
|levels = 6
|members = Yes
|mapID = -1
|plane = 0
|x:3409,y:9637|x:3410,y:9640|x:3411,y:9644|x:3414,y:9643|x:3418,y:9642|x:3419,y:9646|x:3420,y:9631|x:3421,y:9629|x:3423,y:9625|x:3433,y:9638|x:3434,y:9636|x:3436,y:9636
|mtype = pin
|leagueRegion = Morytania
}}
{{LocLine
|name = Bat
|location = [[Abandoned Mine]] Level 4
|levels = 6
|members = Yes
|mapID = -1
|plane = 0
|x:2782,y:4491|x:2783,y:4487|x:2792,y:4488|x:2759,y:4498|x:2761,y:4495|x:2762,y:4500
|mtype = pin
|leagueRegion = Morytania
}}
{{LocLine
|name = Bat
|location = [[Shadow Dungeon]]
|levels = 6
|members = Yes
|mapID = -1
|plane = 0
|x:2735,y:5105|x:2742,y:5097|x:2735,y:5073|x:2744,y:5065|x:2699,y:5083|x:2693,y:5102|x:2707,y:5106|x:2721,y:5062
|mtype = pin
|leagueRegion = Kandarin
}}
{{LocTableBottom}}
        """.trimIndent()
        val lines = string.lines()
        for (i in lines.indices) {
            val line = lines[i]
            if (line.startsWith("{{LocLine")) {
                process(i, lines)
            }
        }
    }

    fun process(index: Int, lines: List<String>) {
        var i = index + 1
        var name = ""
        var members = false
        var level = 0
        while (i < lines.size) {
            val line = lines[i]
            if (!line.startsWith("|")) {
                break
            }
            if (line.startsWith("|x:") || line.startsWith("|npcid:")) {
                for (entry in line.replace("}}", "").split("|").drop(1)) {
                    val parts = entry.split(",", ":")
                    if (parts.contains("type=maplink")) {
                        continue
                    }
                    val index = parts.indexOf("x")
                    val x = parts[index + 1].toInt()
                    val y = parts[index + 3].toInt()
                    val id = if (parts.contains("npcid")) {
                        parts[parts.indexOf("npcid") + 1].toInt()
                    } else {
                        name.lowercase()
                    }
                    println("  { id = \"$id\", x = $x, y = $y${if (level != 0) ", level = $level" else ""}${if (members) ", members = true" else ""} },")
                }
                i++
                continue
            }
            val parts = line.removePrefix("|").split("=").map { it.trim() }
            when (parts[0]) {
                "location", "loc" -> println("  # ${parts[1].replace("[", "").replace("]", "")}")
                "name" -> name = parts[1]
                "members", "mem" -> members = parts[1].equals("yes", true) || parts[1].equals("true", true)
                "plane" -> level = parts[1].toInt()
            }
            i++
        }
    }
}