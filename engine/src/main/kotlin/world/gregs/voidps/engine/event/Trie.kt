package world.gregs.voidps.engine.event

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.yaml.Yaml
import kotlin.system.measureTimeMillis
class CharTrie {
    class CharTrieNode(
        val char: Char? = null,
        val children: MutableMap<Char, CharTrieNode> = mutableMapOf(),
        var isEndOfWord: Boolean = false
    )

    private val root = CharTrieNode()

    fun insert(word: String) {
        var current = root
        for (char in word) {
            current = current.children.getOrPut(char) { CharTrieNode(char) }
        }
        current.isEndOfWord = true
    }

    fun search(id: String): List<String> {
        val results = mutableListOf<String>()
        searchHelper(id, root, "", results)
        return results
    }

    private fun searchHelper(
        id: String,
        node: CharTrieNode,
        currentPath: String,
        results: MutableList<String>,
        index: Int = 0
    ) {
        if (index == id.length) {
            if (node.isEndOfWord) {
                results.add(currentPath)
            }
            return
        }

        val currentChar = id[index]

        // Handling wildcard '*' in the trie
        node.children['*']?.let { wildcardNode ->
            for (i in index until id.length) {
                searchHelper(id, wildcardNode, currentPath + id.substring(index, i + 1), results, i + 1)
            }
            // Also consider '*' matching zero characters
            searchHelper(id, wildcardNode, currentPath, results, index + 1)
        }

        // Handling wildcard '#' in the trie for digits
        if (currentChar.isDigit()) {
            node.children['#']?.let { digitWildcardNode ->
                searchHelper(id, digitWildcardNode, currentPath + currentChar, results, index + 1)
            }
        }

        // Regular character matching
        node.children[currentChar]?.let { nextNode ->
            searchHelper(id, nextNode, currentPath + currentChar, results, index + 1)
        }
    }
}
class CharTrieWildcardSearch {
    internal class CharTrieNode {
        val children = mutableMapOf<Char, CharTrieNode>()
        var isEndOfWord = false
    }

    private val root = CharTrieNode()

    fun insert(word: String) {
        var current = root
        for (char in word) {
            current = current.children.getOrPut(char) { CharTrieNode() }
        }
        current.isEndOfWord = true
    }

    fun search(word: String): List<String> {
        val results = mutableListOf<String>()
        searchHelper(word, 0, root, "", results)
        return results
    }

    private fun searchHelper(
        word: String,
        index: Int,
        node: CharTrieNode,
        currentWord: String,
        results: MutableList<String>
    ) {
        // If the end of the word is reached, check if current node marks the end of a word
        if (index == word.length) {
            if (node.isEndOfWord) {
                results.add(currentWord)
            }
            // If '*' is the last character, we need to add all words below this node
            for (child in node.children.values) {
                exploreAll(child, currentWord, results)
            }
            return
        }

        val char = word[index]
        when (char) {
            '*' -> {
                // Case for matching zero characters: move to the next character in the word
                if (index + 1 < word.length) {
                    searchHelper(word, index + 1, node, currentWord, results)
                } else if (node.isEndOfWord) {
                    // If '*' is the last character and current node is a word end
                    results.add(currentWord)
                }

                // Case for matching one or more characters: stay on the current character
                for ((key, child) in node.children) {
                    searchHelper(word, index, child, currentWord + key, results)
                }
            }
            '#' -> {
                for ((key, child) in node.children) {
                    if (key in '0'..'9') {
                        searchHelper(word, index + 1, child, currentWord + key, results)
                    }
                }
            }
            else -> {
                node.children[char]?.let { nextNode ->
                    searchHelper(word, index + 1, nextNode, currentWord + char, results)
                }
            }
        }
    }

    private fun exploreAll(node: CharTrieNode, currentWord: String, results: MutableList<String>) {
        if (node.isEndOfWord) {
            results.add(currentWord)
        }
        for ((char, child) in node.children) {
            exploreAll(child, currentWord + char, results)
        }
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val root = CharTrieWildcardSearch()
//            root.insert("he*")
//            root.insert("w#rld")
//            root.insert("world_#")
//            root.insert("*_2")
//            root.insert("*o*_2")
//            root.insert("*")
//            println(root.search("hello")) // [hello, hello]
//            println(root.search("hello_2")) // [hello_2, hello_2, hello_2]
//            println(root.search("help")) // [help, help]
//            println(root.search("world")) // [world]
//            println(root.search("world_1")) // [world_1, world_1]
//            println(root.search("world_2")) // [world_2, world_2, world_2, orld_2, world_2]
            val cache = CacheDelegate("./data/cache")
            val definitions = ObjectDefinitions(ObjectDecoder(member = true, lowDetail = false).load(cache)).load(Yaml(), "./data/definitions/objects.yml")
            for(key in definitions.ids.keys) {
                root.insert(key)
            }
            val wildcard = "spinning_wheel*"
            println(measureTimeMillis {
                definitions.ids.keys.filter { wildcardEquals(wildcard, it) }
            })
            println(measureTimeMillis {
                root.search(wildcard)
            })
        }
    }
}