package world.gregs.voidps.tools

import world.gregs.config.Config
import world.gregs.config.ConfigWriter
import world.gregs.config.Serializable
import java.io.File

@Serializable
data class Project(
    val name: String,
    val owners: List<User>,
    val votes: Int,
    val array: IntArray,
    var variables: Map<String, Int>,
    val social: Social,
    var mapMap: Map<String, Map<String, Int>>
)

@Serializable
data class Social(val friends: Map<String, Rank>)

@Serializable
data class User(val name: String)

enum class Rank {
    NOONE,
    FRIEND,
    MAJOR
}

fun main() {
    val data = Project(
        name = "kotlinx.serialization",
        owners = listOf(User("kotlin"), User("jetbrains")),
        votes = 9000,
        array = intArrayOf(1, 2, 3, 4),
        variables = mapOf("onety" to 1, "twoty" to 2),
        social = Social(mapOf("friend" to Rank.FRIEND, "friend2" to Rank.MAJOR)),
        mapMap = mapOf("one" to mapOf("1" to 1, "1-2" to 1), "two" to mapOf("2" to 2))
    )
    Config.fileWriter(File("./test.toml")) {
        ProjectCodec.write( this, data)
    }
    val string = """
        name = "kotlinx.serialization"
        owners = [{name = "kotlin"}, {name = "jetbrains"}]
        votes = 9000
        array = [1, 2, 3, 4]

        [variables]
        onety = 1
        twoty = 2

        [social]
        friends = {friend = "rank", friend2 = "major"}

        [mapMap]
        one = {1 = 1, 1-2 = 1}
        two = {2 = 2}
    """.trimIndent()
    Config.stringReader(string) {
        while (nextSection()) {
            val section = section()
            println(section)
            while (nextPair()) {
                val key = key()
                val value = value()
                println("${key} = ${value}")
            }
            println(section)
        }
    }
}