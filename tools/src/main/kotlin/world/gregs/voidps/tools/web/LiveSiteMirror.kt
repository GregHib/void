package world.gregs.voidps.tools.web

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import world.gregs.voidps.tools.web.SiteMirror.Companion.fileTypes
import world.gregs.voidps.tools.web.SiteMirror.Companion.isTextFormat
import world.gregs.voidps.tools.web.SiteMirror.Companion.supportedFileDownloads
import world.gregs.voidps.tools.web.UrlHandler.convertQuery
import world.gregs.voidps.tools.web.UrlHandler.removeDomain
import world.gregs.voidps.tools.web.UrlHandler.trimAnchor
import world.gregs.voidps.tools.web.UrlHandler.trimQuery
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.zip.GZIPInputStream

private val testRegex = "(?:https?:)?\\\\?/\\\\?/[-a-zA-Z0-9+&@#\\\\/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".toRegex()
private val srcRegex = "(imagesrcset|srcSet|src|href|poster)=\"(/.*?)\"".toRegex()
private val urlRegex = "(url\\(\"?'?)(.*?)'?\"?\\)".toRegex()
private val interchangeRegex = "\\[(/.*?), ([a-z]+)]".toRegex()
private val output = File("./live/rs3/")

/**
 * Scrapes a pre-set list of pages from the live sites
 */
fun main() {
    val scrapper = SiteMirrorLive()
    queueRs3(scrapper)
//    queueOsrs(scrapper)
    while (true) {
        scrapper.next()
    }
}

private fun queueOsrs(scrapper: SiteMirrorLive) {
    scrapper.queue("https://play.runescape.com/", force = true)
    scrapper.queue("https://play.runescape.com/oldschool", force = true)
    scrapper.queue("https://oldschool.runescape.com/", force = true)
    scrapper.queue("https://oldschool.runescape.com/launcher", force = true)
    scrapper.queue("https://oldschool.runescape.com/slu", force = true)
    scrapper.queue("https://oldschool.runescape.com/game?world=301", force = true)
    scrapper.queue("https://www.runescape.com/oldschool/world-map/", force = true)
    scrapper.queue("https://cdn.runescape.com/assets/img/external/oldschool/2023/newsposts/2023-07-28/osrs_world_map_july13_2023.png", force = true)
    scrapper.queue("https://account.jagex.com/en-GB/assisted-login?login_challenge=2313a94a92c04802b95ea4c6d01f8787", force = true)

    scrapper.queue("https://secure.runescape.com/m=news/archive?oldschool=1&year=2024&month=1", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/scurrius--dt2-combat-achievements?oldschool=1", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/defender-of-varrock-varlamore-rewards--more?oldschool=1", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/scurrius-the-rat-king?oldschool=1", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/varlamore-part-one---overview?oldschool=1", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/defender-of-varrock---overview?oldschool=1", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/while-guthix-sleeps---overview?oldschool=1", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/sailing---winter-summit-update?oldschool=1", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/the-future-of-the-official-client---hd--plugin-api?oldschool=1", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/low-level-wilderness-hotspot-deadman-2024--more?oldschool=1", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/winter-summit-2024---overview?oldschool=1", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/old-school-roadmap?oldschool=1", force = true) // News

    scrapper.queue("https://secure.runescape.com/m=news/game-status-information-centre?oldschool=1", force = true) // Status

    scrapper.queue("https://www.jagex.com/en-GB/launcher", force = true) // Launcher

    scrapper.queue("https://secure.runescape.com/m=poll/oldschool/", force = true) // Polls
    scrapper.queue("https://secure.runescape.com/m=poll/oldschool/results?id=1685", force = true) // Poll results
    scrapper.queue("https://secure.runescape.com/m=poll/oldschool/results?id=1684", force = true) // Poll results
    scrapper.queue("https://secure.runescape.com/m=poll/oldschool/results?id=1684", force = true) // Poll charter

    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool/overall", force = true) // Hiscores
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool/hiscorepersonal?user1=Lynx%A0Titan", force = true) // Hiscores user
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool/hiscorepersonal?user1=Gibbed", force = true) // Hiscores user
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool_seasonal/overall", force = true) // Hiscores leagues
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool_tournament/overall", force = true) // Dmm
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool_ironman/group-ironman/?groupSize=2", force = true) // Group ironman
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool_ironman/group-ironman/view-group?name=andyfromvent", force = true) // Group ironman
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool_ironman/group-ironman/view-group?name=broh", force = true) // Group ironman 5
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool_hardcore_ironman/group-ironman/?groupSize=5", force = true) // Group ironman 5
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool_hardcore_ironman/group-ironman/view-group?name=barrow%20bros", force = true) // Group ironman 5
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool_fresh_start/overall", force = true) // Fresh start
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool_skiller/overall", force = true) // Skillers
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool_skiller_defence/overall", force = true) // 1 def

    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/", force = true) // G.E
    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/top100?list=2", force = true) // G.E rises
    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/top100?list=3", force = true) // G.E falls

    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/Old+school+bond/viewitem?obj=13190", force = true) // Bonds
    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/top100?list=1", force = true) // Most value
    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/top100?list=0", force = true) // Most traded
    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/Zulrah%27s+scales/viewitem?obj=12934", force = true) // Featured

    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/results", force = true) // Search
    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/results#main-search", force = true) // Search
    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/Santa+hat/viewitem?obj=1050", force = true) // Search

    scrapper.queue("https://runescape.backstreetmerch.com/", force = true) // Merch
    scrapper.queue("https://oldschool.runescape.wiki/", force = true) // Wiki
    scrapper.queue("https://runelite.net/", force = true) // Runelite

    scrapper.queue("https://www.runescape.com/oldschool/bonds", force = true) // Bonds
    scrapper.queue("https://support.runescape.com/hc/en-gb", force = true) // Support
    scrapper.queue("https://support.runescape.com/hc/en-gb/p/contact", force = true) // Contact
    scrapper.queue("https://secure.runescape.com/m=account-creation/create_account?theme=oldschool", force = true) // Create account
}

private fun queueRs3(scrapper: SiteMirrorLive) {
    scrapper.queue("https://runescape.com/", force = true)
    scrapper.queue("https://runescape.com/splash", force = true)
    scrapper.queue("https://secure.runescape.com/m=forum/forums", force = true) // Forums
    scrapper.queue("https://secure.runescape.com/m=forum/forums?294,295,thd,391,66049913", force = true) // Forum category
    scrapper.queue("https://secure.runescape.com/m=forum/forums?294,295,396,66126612", force = true) // Forum thread

    val user = "Trichromes"
    scrapper.queue("https://apps.runescape.com/runemetrics/app/welcome", force = true) // Runemetrics
    scrapper.queue("https://apps.runescape.com/runemetrics/app/overview/player/$user", force = true) // Runemetrics user
    scrapper.queue("https://apps.runescape.com/runemetrics/app/levels/player/$user", force = true) // Runemetrics levels
    scrapper.queue("https://apps.runescape.com/runemetrics/app/xp-monthly/player/$user/-1", force = true) // Runemetrics xp
    scrapper.queue("https://apps.runescape.com/runemetrics/app/xp-monthly/player/$user/-1", force = true) // Runemetrics xp
    scrapper.queue("https://apps.runescape.com/runemetrics/app/activities/player/$user", force = true) // Runemetrics event log
    scrapper.queue("https://apps.runescape.com/runemetrics/app/quests/player/$user", force = true) // Runemetrics quests

    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/results", force = true) // GE search
    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/", force = true) // GE
    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/catalogue", force = true) // GE catalogue
    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/catalogue?cat=32", force = true) // GE catalogue
    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/catalogue?cat=16&page=1", force = true) // GE catalogue
    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/top100?list=2", force = true) // GE price rises
    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/top100?list=3", force = true) // GE price falls
    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/catalogue", force = true) // GE catelogue
    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/top100?list=1", force = true) // GE most valuable
    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/top100?list=0", force = true) // GE most traded
    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/Terrasaur+maul/viewitem?obj=48007", force = true) // Featured
    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/Santa+hat/viewitem?obj=1050", force = true) // GE item
    scrapper.queue("https://secure.runescape.com/m=itemdb_rs/results#main-search", force = true) // GE Search

    scrapper.queue("https://secure.runescape.com/m=hiscore/", force = true) // Hiscores
    scrapper.queue("https://secure.runescape.com/m=hiscore/ranking", force = true) // Hiscores
    scrapper.queue("https://secure.runescape.com/m=hiscore/ranking?category_type=1&table=26", force = true) // Achievements
    scrapper.queue("https://secure.runescape.com/m=hiscore/ranking?category_type=1", force = true) // Achievements
    scrapper.queue("https://services.runescape.com/m=clan-hiscores/ranking", force = true) // Clans
    scrapper.queue("https://services.runescape.com/m=clan-hiscores/landing.ws", force = true) // Clans
    scrapper.queue("https://services.runescape.com/m=clan-home/clan/Maxed", force = true) // Clan info
    scrapper.queue("https://services.runescape.com/m=clan-hiscores/compare.ws?clanName=Maxed", force = true) // Clan stats
    scrapper.queue("https://services.runescape.com/m=clan-hiscores/members.ws?clanName=Maxed", force = true) // Clan mates
    scrapper.queue("https://services.runescape.com/m=temp-hiscores/", force = true) // Seasonal
    scrapper.queue("https://services.runescape.com/m=temp-hiscores/ranking?id=1508716800045&filter=-1&page=1", force = true) // Seasonal topic
    scrapper.queue("https://services.runescape.com/m=temp-hiscores/compare?user1=Sina", force = true) // Seasonal player
    scrapper.queue("https://secure.runescape.com/m=hiscore_seasonal/ranking", force = true) // Seasonal
    scrapper.queue("https://services.runescape.com/m=temp-hiscores/ranking?id=1508716800045&filter=-1&page=1", force = true) // Seasonal topic
    scrapper.queue("https://secure.runescape.com/m=hiscore_seasonal/compare?user1=Mistime", force = true) // Seasonal player
    scrapper.queue("https://secure.runescape.com/m=hiscore_seasonal/compare?category_type=-1&user1=Mistime&user2=Legit", force = true) // Seasonal player
    scrapper.queue("https://secure.runescape.com/m=hiscore_seasonal/ranking?category_type=1&table=26", force = true) // Activities
    scrapper.queue("https://secure.runescape.com/m=hiscore/compare?user1=Blacked+Out&category_type=1", force = true) // Player activities
    scrapper.queue("https://secure.runescape.com/m=hiscore/ranking?category_type=0&table=0&time_filter=1&page=1", force = true)
    scrapper.queue("https://secure.runescape.com/m=hiscore/ranking?category_type=0&table=0&time_filter=2&page=1", force = true)
    scrapper.queue("https://secure.runescape.com/m=hiscore/ranking?category_type=0&table=0&time_filter=2&monthlyArchive=true&date=1704125682789", force = true)
    scrapper.queue("https://secure.runescape.com/m=hiscore/compare?category_type=-1&user1=Raul&user2=Eimis", force = true) // Player
    scrapper.queue("https://secure.runescape.com/m=hiscore/compare?user1=Ozzzyy&category_type=1", force = true) // Player
    scrapper.queue("https://secure.runescape.com/m=hiscore/compare?category_type=1&user1=Saara&user2=Ozzzyy", force = true) // Player
    scrapper.queue("https://services.runescape.com/m=hiscore/compare?user1=Raul", force = true) // Player
    scrapper.queue("https://secure.runescape.com/m=hiscore/compare", force = true) // Player
    scrapper.queue("https://rs.runescape.com/hiscores/bosses/1-zamorak/solo", force = true) // Bosses
    scrapper.queue("https://rs.runescape.com/hiscores/bosses/1-zamorak/2-player", force = true) // Bosses
    scrapper.queue("https://rs.runescape.com/hiscores/bosses/1-zamorak/3-player", force = true) // Bosses
    scrapper.queue("https://rs.runescape.com/hiscores/bosses/1-zamorak/4-player", force = true) // Bosses
    scrapper.queue("https://rs.runescape.com/hiscores/bosses/1-zamorak/5-player", force = true) // Bosses

    scrapper.queue("https://account.runescape.com/en-GB/game/begin-your-adventure", force = true) // account creation
    scrapper.queue("https://www.jagex.com/en-GB/launcher", force = true) // Jagex launcher

    scrapper.queue("https://oldschool.runescape.com/premier-club/", force = true)

    scrapper.queue("https://secure.runescape.com/m=poll/", force = true) // Player power
    scrapper.queue("https://secure.runescape.com/m=poll/archive?id=1674", force = true) // Poll

    scrapper.queue("https://support.runescape.com/hc/en-gb", force = true) // Support
    scrapper.queue("https://support.runescape.com/hc/en-gb/categories/200977391-Your-account", force = true) // Category
    scrapper.queue("https://support.runescape.com/hc/en-gb/articles/360001313349-Go-here-to-install", force = true) // Post
    scrapper.queue("https://help.jagex.com/hc/en-gb/categories/4403516390801-Jagex-Accounts", force = true) // Post

    scrapper.queue("https://secure.runescape.com/m=news/", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/wildywyrm-flash-event-and-valentines---this-week-in-runescape", force = true) // News
    scrapper.queue("https://secure.runescape.com/m=news/archive?oldschool=1", force = true) // osrs news
    scrapper.queue("https://secure.runescape.com/m=news/last-chance-for-dragon-plushies?oldschool=1", force = true) // osrs news post
    scrapper.queue("https://secure.runescape.com/m=news/poll-80-wilderness-changes--more?oldschool=1", force = true) // osrs news post
    scrapper.queue("https://secure.runescape.com/m=news/necromancy-launch-gameplay-trailer", force = true) // news post

    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/", force = true) // GE
    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/top100?list=1&scale=0", force = true) // GE most traded
    scrapper.queue("https://secure.runescape.com/m=itemdb_oldschool/Santa+hat/viewitem?obj=1050", force = true) // GE item

    scrapper.queue("https://secure.runescape.com/m=poll/oldschool/", force = true) // poll
    scrapper.queue("https://secure.runescape.com/m=poll/oldschool/results?id=1616", force = true) // poll results

    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool/overall", force = true) // hiscores
    scrapper.queue("https://secure.runescape.com/m=hiscore_oldschool/hiscorepersonal?user1=Lynx Titan", force = true) // hiscore user

    scrapper.queue("https://runescape.wiki/", force = true) // Wiki
    scrapper.queue("https://www.runescape.com/game-guide/beginners-guide", force = true) // Beginners
    scrapper.queue("https://www.runescape.com/game-guide/combat", force = true) // Combat
    scrapper.queue("https://www.runescape.com/game-guide/skills", force = true) // Skills

    scrapper.queue("https://rs.runescape.com/en-GB/membership", force = true) // Members
    scrapper.queue("https://www.runescape.com/treasure-hunter", force = true) // Treasure hunter
    scrapper.queue("https://www.runescape.com/bonds", force = true) // Bonds
    scrapper.queue("https://www.runescape.com/solomons-store", force = true) // Solomons
    scrapper.queue("https://www.runescape.com/runemetrics", force = true) // Metrics pro
    scrapper.queue("https://www.runescape.com/loyalty", force = true) // Loyalty
    scrapper.queue("https://www.runescape.com/store_locator", force = true) // Store
    scrapper.queue("https://runescape.backstreetmerch.com/", force = true) // Merch
    scrapper.queue("https://www.runescape.com/launcher", force = true) // Launcher
}

class SiteMirrorLive {
    private val all: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private val queue = ConcurrentLinkedQueue<Pair<String, String>>()
    private val validUrlRegex = "https?:\\\\?/\\\\?/(?:[a-zA-Z0-9-.]+?)?(?:runescape.com|jagex.com|ctfassets.net|zdassets.com)".toRegex()

    private val singlePage = false

    private fun shouldSkip(path: String): Boolean {
        if (singlePage && isTextFormat(trimQuery(trimAnchor(path)))) {
            return true
        }
        if ((path.contains("l=") && !path.contains("url=")) ||
            path.contains("set_lang=") ||
            path.contains("de-DE") ||
            path.contains("fr-FR") ||
            path.contains("pt-BR") ||
            path.contains("zh-CN") ||
            path.contains(
                "ja-JP",
            ) ||
            path.contains(
                "ko-KR",
            ) ||
            path.contains("pl-PL")
        ) {
            return true
        }
        if (path.contains("Incapsula")) {
            return true
        }
        if (path.contains("m=forums")) {
            return true
        }
        if (path.contains("m=forum")) {
            return true
        }
        if (path.contains("m=hiscore")) {
            return true
        }
        if (path.contains("m=hiscore_oldschool")) {
            return true
        }
        if (path.contains("/runemetrics/")) {
            return true
        }
        if (path.contains("m=itemdb_rs")) {
            return true
        }
        if (path.contains("m=itemdb_oldschool")) {
            return true
        }
        if (path.contains("m=clan-hiscores")) {
            return true
        }
        if (path.contains("m=temp-hiscores")) {
            return true
        }
        if (path.contains("m=poll")) {
            return true
        }
        if (path.contains("support.runescape.com")) {
            return true
        }
        if (path.contains("payments.dev.jagex.com")) {
            return true
        }
        if (path.contains("payments.stage.jagex.com")) {
            return true
        }
        if (path.contains("wtqa.runescape.com")) {
            return true
        }
        if (path.contains("?world=")) {
            return true
        }
        return false
    }

    private val regex = "(/[^./]+?/\\.\\.)/".toRegex()

    fun queue(archived: String, force: Boolean = false) {
        var archived = URLDecoder.decode(archived, Charsets.UTF_8).replace(regex, "/").replace(" ", "+")
        if (!force && shouldSkip(archived)) {
            return
        }
        if (archived.startsWith("//")) {
            archived = if (!archived.startsWith("//http")) {
                "https:$archived"
            } else {
                archived.removePrefix("//")
            }
        }
        val path = getPath(archived) ?: return
//        println("Queue $archived $path")
        if (!all.contains(path)) {
            queue.add(archived to path)
            all.add(path)
        }
    }

    fun next(): Boolean {
        val (req, path) = queue.poll() ?: return false
        GlobalScope.launch(Dispatchers.Default) {
            grabPage(req, path)
        }
        return true
    }

    private fun getPath(source: String): String? {
        if (validUrlRegex.containsMatchIn(source)) {
            var path: String = convertQuery(
                removeDomain(
                    source
                        .replace("/#/", "/")
                        .replace(".ws", ".html")
                        .replace(" ", "+"),
                    if (source.contains("static.zdassets.com")) {
                        "static.zdassets.com"
                    } else if (source.contains("ctfassets.net")) {
                        "ctfassets.net"
                    } else if (source.contains("jagex.com")) {
                        "jagex.com"
                    } else {
                        "runescape.com"
                    },
                ),
            ).replace(":", "-")

            if (supportedFileDownloads(trimAnchor(path))) {
                for (type in fileTypes) {
                    val index = path.indexOf(type)
                    if (index != -1) {
                        val ending = path.substring(index + type.length)
                        path = "${path.substring(0, index)}${ending.replace("&", "")}$type"
                        break
                    }
                }
                path = path.replace("%2F", "/")
            }
            when {
                path.isBlank() || path == "runescape.com" || path == "oldschool.runescape.com" -> path = "index.html"
                path.endsWith("/") -> path += "index.html"
                !isTextFormat(path) && !supportedFileDownloads(trimAnchor(path)) -> {
                    path += "/index.html"
                }
            }
            return path
        }
        return null
    }

    private fun grabPage(source: String, path: String) {
        println("Grab $source $path")
        if (isTextFormat(trimAnchor(path))) {
            var data = getStream(source) { queue(it, true) }?.readBytes()?.toString(Charsets.UTF_8) ?: return
            val out = File(output, trimAnchor(path))
            if (trimAnchor(path).endsWith("css")) {
                for (match in urlRegex.findAll(data).toList().reversed()) {
                    val type = match.groupValues[match.groupValues.lastIndex - 1]
                    var url = match.groupValues.last()
                    if (url.startsWith("data:")) {
                        continue
                    }
                    if (!url.startsWith("/") && !url.startsWith("http")) {
                        url = "/$url"
                    }
                    if (url.startsWith("/")) {
                        url = if (source.contains("oldschool.runescape.com")) {
                            "${source.substring(0, source.indexOf("oldschool.runescape.com") + 13)}$url"
                        } else {
                            "${source.substring(0, source.indexOf("runescape.com") + 13)}$url"
                        }
                    }
                    if (url.contains("runescape.com") || url.contains("jagex.com") || url.contains("static.zdassets.com") || url.contains("ctfassets.net")) {
                        val other = getPath(trimQuery(trimAnchor(url))) ?: continue
                        queue(trimQuery(trimAnchor(url)))
                        data = data.replaceRange(
                            match.range,
                            "$type${UrlHandler.offset(other, path.count { it == '/' })}${if (type.last() == '\'') {
                                "'"
                            } else if (type.last() == '"') {
                                "\""
                            } else {
                                ""
                            }})",
                        )
                    }
                }
            } else {
                for (match in testRegex.findAll(data).toList().reversed()) {
                    val original = match.groupValues.last()
                    var url = original
                    if (url.startsWith("data:")) {
                        continue
                    }
                    if (url.startsWith("//")) {
                        url = "https:$url"
                    }
                    if (url.contains("runescape.com") || url.contains("jagex.com") || url.contains("static.zdassets.com") || url.contains("ctfassets.net")) {
                        val other = getPath(original) ?: continue
                        url = UrlHandler.offset(other, path.count { it == '/' })
                        queue(original)
                    }

                    if (url != original) {
                        data = data.replaceRange(match.range, url)
                    }
                }
                for (match in interchangeRegex.findAll(data).toList().reversed()) {
                    var url = match.groupValues[match.groupValues.lastIndex - 1]
                    val size = match.groupValues.last()
                    if (url.startsWith("data:")) {
                        continue
                    }
                    if (url.startsWith("/")) {
                        val host = if (source.contains("oldschool.runescape.com")) {
                            source.substring(0, source.indexOf("oldschool.runescape.com") + 13)
                        } else {
                            source.substring(0, source.indexOf("runescape.com") + 13)
                        }
                        url = "${host}${if (host.endsWith("/") || url.startsWith("/")) "" else "/"}$url"
                    }
                    if (url.contains("runescape.com") || url.contains("jagex.com") || url.contains("ctfassets.net")) {
                        val other = getPath(url) ?: continue
                        queue(url)
                        data = data.replaceRange(match.range, "[${UrlHandler.offset(other, path.count { it == '/' })}, $size]")
                    }
                }
                for (match in srcRegex.findAll(data).toList().reversed()) {
                    val type = match.groupValues[match.groupValues.lastIndex - 1]
                    var url = match.groupValues.last()
                    if (url.startsWith("data:")) {
                        continue
                    }

                    if (url.startsWith("/")) {
                        val host = if (source.contains("oldschool.runescape.com")) {
                            source.substring(0, source.indexOf("oldschool.runescape.com") + 13)
                        } else {
                            source.substring(0, source.indexOf("runescape.com") + 13)
                        }
                        url = "${host}${if (host.endsWith("/") || url.startsWith("/")) "" else "/"}$url"
                    }
                    if (url.contains("&amp;")) {
                        url = url.replace("&amp;", "&")
                    }
                    if (url.contains("runescape.com") || url.contains("jagex.com") || url.contains("ctfassets.net")) {
                        if (type.endsWith("srcset", true)) {
                            val urls = url.split(", ")
                            val list = mutableListOf<String>()
                            for (subUrl in urls) {
                                val index = subUrl.indexOf(" ")
                                val size = subUrl.substring(index + 1, subUrl.length)
                                val actualUrl = subUrl.substring(0, index)
                                val other = getPath(actualUrl) ?: continue
                                queue(actualUrl)
                                list.add("${UrlHandler.offset(other, path.count { it == '/' })} $size")
                            }
                            data = data.replaceRange(match.range, "$type=\"${list.joinToString(", ")}\"")
                        } else {
                            val other = getPath(url) ?: continue
                            queue(url)
                            data = data.replaceRange(match.range, "$type=\"${UrlHandler.offset(other, path.count { it == '/' })}\"")
                        }
                    }
                }

                if (data.contains(SiteMirror.AIT)) {
                    getPath(SiteMirror.AIT)?.let { url ->
                        data = data.replace(SiteMirror.AIT, UrlHandler.offset(url, path.count { it == '/' }))
                        queue("https:${SiteMirror.AIT}")
                    }
                }
            }
            out.parentFile.mkdirs()
            out.writeText(data, Charsets.UTF_8)
        } else {
            download(source, path)
        }
    }

    private fun download(source: String, path: String) {
        val data = getStream(source) { queue(it, true) } ?: return
        val out = File(output, trimAnchor(path))
        if (!out.exists()) {
            !out.parentFile.mkdirs()
            out.writeBytes(data.readBytes())
        }
    }

    private fun getStream(source: String, queue: (String) -> Unit): InputStream? {
        try {
            val connection = URL(source).openConnection() as HttpURLConnection
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36")
            val code = connection.responseCode
            if (code != 200) {
                if (code == 302 || code == 301) {
                    val redirect = connection.getHeaderField("Location")
                    return getStream(redirect, queue)
                } else if (code == 503) {
                    queue.invoke(source)
                } else if (code == 400) {
                    queue.invoke(source.replace("../../../../", ""))
                } else if (code != 404 && code != 403) {
                    System.err.println("Error code $code $source")
                }
                return null
            }
            return if (connection.contentEncoding == "gzip") {
                GZIPInputStream(connection.inputStream)
            } else {
                connection.inputStream
            }
        } catch (e: Exception) {
            System.err.println(source)
            e.printStackTrace()
            queue.invoke(source)
            return null
        }
    }
}
