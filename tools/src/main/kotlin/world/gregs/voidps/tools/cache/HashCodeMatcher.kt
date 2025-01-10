package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.Index
import world.gregs.voidps.tools.property
import java.io.File

object HashCodeMatcher {


    private var counter = 0
    private val matches = mutableMapOf<Int, MutableSet<String>>()
    private lateinit var known: Map<Int, String?>
    private val output = File("./temp/hashes/found-scripts.tsv")

    @JvmStatic
    fun main(args: Array<String>) {
//        dumpHashes()
        output.delete()
        val keywords = Keywords("./tools/src/main/resources/keywords.txt")
        val index = Index.SPRITES
        known = File("./temp/hashes/hashes-modified.tsv").readLines()
            .filter {
                val parts = it.split("\t")
                parts[0].toInt() == index
            }
            .associate {
                val parts = it.split("\t")
                parts[3].toInt() to parts.getOrNull(4)
            }

        for ((key, value) in known) {
            if (!value.isNullOrBlank() && value.hashCode() != key) {
                println("Invalid $key $value - ${value.hashCode()}")
            }
        }

        findInterfaces(keywords, depth = -1)
        findSprites(keywords, depth = -1)
        findScripts(keywords, depth = 2)
    }

    fun add(string: String): Boolean {
        val hash = string.hashCode()
        if (!known.containsKey(hash)) {
            return false
        }
        if (known[hash] == null && matches.getOrPut(hash) { mutableSetOf() }.add(string)) {
            println("Found $hash -\t$string")
            counter++
            if (counter.rem(10_000) == 0) {
                writeAll()
            }
        }
        return true
    }

    private fun numbers(string: String, separator: Char? = null, prefix: String? = null, suffix: String? = null, maxSequence: Int = 10) {
        for (i in 0..500) {
            val added = add("${prefix ?: ""}$string${separator ?: ""}${i}${suffix ?: ""}")
            if (i < maxSequence) {
                add("${prefix ?: ""}$string${separator ?: ""}${i}${suffix ?: ""}")
            } else if (!added) {
                add("${prefix ?: ""}${string}${separator ?: ""}0${i}${suffix ?: ""}")
                break
            }
        }
    }

    private fun check(string: String, separators: List<Char> = emptyList(), prefix: String? = null, suffix: String? = null, maxSequence: Int = 1) {
        add("${prefix ?: ""}${string}${suffix ?: ""}")
        numbers(string, prefix = prefix, suffix = suffix, maxSequence = maxSequence)
        for (separator in separators) {
            numbers(string, separator, prefix, suffix, maxSequence)
        }
    }

    private fun findInterfaces(keywords: Set<String>, depth: Int) {
        val underscore = listOf('_')
        val space = listOf(' ')
        if (depth == 1) {
            println("Searching individual keywords")
            for (a in keywords) {
                check(a)
            }
        }
        if (depth == 2) {
            println("Searching double keywords")
            val underscored = keywords.filter { !it.contains(" ") }
            val spaced = keywords.filter { !it.contains("_") }
            for (a in underscored) {
                for (b in underscored) {
                    check("${a}_${b}", underscore)
                    check("${a}${b}")
                }
            }
            for (a in spaced) {
                for (b in spaced) {
                    check("$a $b", underscore)
                    check("${a}${b}")
                }
            }
        }

        if (depth == 3) {
            println("Searching triple keywords")
            for (a in keywords) {
                for (b in keywords) {
                    for (c in keywords) {
                        if (!a.contains(" ") && !b.contains(" ") && !c.contains(" ")) {
                            check("${a}_${b}_${c}", underscore)
                            check("${a}${b}_${c}", underscore)
                            check("${a}_${b}${c}", underscore)
                        }
                        check("${a}${b}${c}", underscore)
                        check("${a}${b}${c}", space)
                        if (!a.contains("_") && !b.contains("_") && !c.contains("_")) {
                            check("$a $b $c", space)
                            check("$a$b $c", space)
                            check("$a $b$c", space)
                        }
                    }
                }
            }
        }
        writeAll()
    }

    private fun findScripts(keywordSet: Set<String>, depth: Int) {
        val keywords = keywordSet.filter { !it.contains(" ") }
        val underscore = listOf('_')
        if (depth == 1) {
            println("Searching individual keywords")
            for (a in keywords) {
                check(a, prefix = "[proc,", suffix = "]", maxSequence = 1)
                check(a, prefix = "[clientscript,", suffix = "]", maxSequence = 1)
            }
        }
        if (depth == 2) {
            println("Searching double keywords")
            for (a in keywords) {
                for (b in keywords) {
                    if (!a.contains(" ") && !b.contains(" ")) {
                        check("${a}_${b}", underscore, prefix = "[proc,", suffix = "]", maxSequence = 1)
                        check("${a}_${b}", underscore, prefix = "[clientscript,", suffix = "]", maxSequence = 1)
                    }
                    check("${a}${b}", prefix = "[proc,", suffix = "]", maxSequence = 1)
                    check("${a}${b}", prefix = "[clientscript,", suffix = "]", maxSequence = 1)
                }
            }
        }

        if (depth == 3) {
            println("Searching triple keywords")
            for (a in keywords) {
                for (b in keywords) {
                    for (c in keywords) {
                        if (!a.contains(" ") && !b.contains(" ") && !c.contains(" ")) {
                            check("${a}_${b}_${c}", underscore, prefix = "[proc,", suffix = "]", maxSequence = 1)
                            check("${a}${b}_${c}", underscore, prefix = "[proc,", suffix = "]", maxSequence = 1)
                            check("${a}_${b}${c}", underscore, prefix = "[proc,", suffix = "]", maxSequence = 1)
                            check("${a}_${b}_${c}", underscore, prefix = "[clientscript,", suffix = "]", maxSequence = 1)
                            check("${a}${b}_${c}", underscore, prefix = "[clientscript,", suffix = "]", maxSequence = 1)
                            check("${a}_${b}${c}", underscore, prefix = "[clientscript,", suffix = "]", maxSequence = 1)
                        }
                        check("${a}${b}${c}", underscore, prefix = "[proc,", suffix = "]", maxSequence = 1)
                        check("${a}${b}${c}", underscore, prefix = "[clientscript,", suffix = "]", maxSequence = 1)
                    }
                }
            }
        }
        writeAll()
    }

    private fun findSprites(keywordSet: Set<String>, depth: Int) {
        val keywords = keywordSet.filter { !it.contains(" ") }
        val comma = listOf(',')
        if (depth == 1) {
            println("Searching individual keywords")
            for (a in keywords) {
                check(a, comma, maxSequence = 1)
            }
        }
        if (depth == 2) {
            println("Searching double keywords")
            for (a in keywords) {
                for (b in keywords) {
                    check("${a}_${b}", comma, maxSequence = 1)
                    check("${a}${b}", comma, maxSequence = 1)
                }
            }
        }

        if (depth == 3) {
            println("Searching triple keywords")
            for (a in keywords) {
                for (b in keywords) {
                    for (c in keywords) {
                        check("${a}_${b}_${c}", comma, maxSequence = 1)
                        check("${a}${b}_${c}", comma, maxSequence = 1)
                        check("${a}_${b}${c}", comma, maxSequence = 1)
                        check("${a}${b}${c}", comma, maxSequence = 1)
                    }
                }
            }
        }
        writeAll()
    }

    private fun writeAll() {
        output.writeText(matches.toList().joinToString("\n") { "${it.first} -\t${it.second.joinToString("\t")}" })
    }

    private fun dumpHashes() {
        val lib = CacheLibrary.create(property("storage.cache.path"))
        val hashes = File("./temp/hashes/hashes.txt")
        hashes.delete()
        for (index in lib.indices()) {
            for (archive in index.archives()) {
                if (archive.hashName != 0) {
                    hashes.appendText("${archive.hashName}, ${index.id}, ${archive.id}, ${-1}\n")
                }
                for (file in archive.files()) {
                    if (file.hashName != 0) {
                        hashes.appendText("${file.hashName}, ${index.id}, ${archive.id}, ${file.id}\n")
                    }
                }
            }
        }
    }

    private fun brute(prefix: String, target: Int, chars: List<Char>, limit: Int, depth: Int = 0) {
        if (depth > limit) {
            return
        }
        for (a in chars) {
            val str = "$prefix${a}"
            if (str.hashCode() == target) {
                println("Found: $str")
            }
            brute(str, target, chars, limit, depth + 1)
        }
    }
}