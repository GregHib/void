package world.gregs.voidps.web.site

import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser
import world.gregs.voidps.web.site.components.Icons

/** One heading found in a rendered doc, used to build the "on this page" side nav. */
data class MarkdownHeading(val level: Int, val id: String, val text: String)

data class MarkdownDocument(val html: String, val headings: List<MarkdownHeading>)

/** Jekyll-style `title`/`description` overrides read from a doc's front matter, if present. */
data class FrontMatter(val title: String?, val description: String?)

private val frontMatterBlock = Regex("^---\\n(.*?)\\n---\\s*\\n?", RegexOption.DOT_MATCHES_ALL)

/**
 * Strips a leading `---\nkey: value\n---` front-matter block (as used by Jekyll/GitHub Pages)
 * from [markdown] and returns it alongside the remaining body, so `title`/`description` can be
 * read without them leaking into the rendered page as a stray paragraph.
 */
fun extractFrontMatter(markdown: String): Pair<FrontMatter, String> {
    val source = markdown.replace("\r\n", "\n").replace('\r', '\n')
    val match = frontMatterBlock.find(source) ?: return FrontMatter(null, null) to source
    val fields = match.groupValues[1].lines().mapNotNull { line ->
        val i = line.indexOf(':')
        if (i <= 0) null else line.substring(0, i).trim() to line.substring(i + 1).trim().trim('"', '\'')
    }.toMap()
    val body = source.substring(match.range.last + 1)
    return FrontMatter(fields["title"]?.ifBlank { null }, fields["description"]?.ifBlank { null }) to body
}

private val headingTypes = mapOf(
    MarkdownElementTypes.ATX_1 to 1,
    MarkdownElementTypes.ATX_2 to 2,
    MarkdownElementTypes.ATX_3 to 3,
    MarkdownElementTypes.ATX_4 to 4,
    MarkdownElementTypes.ATX_5 to 5,
    MarkdownElementTypes.ATX_6 to 6,
)

/**
 * Renders Markdown source into an HTML fragment using GitHub-flavoured Markdown (tables,
 * strikethrough, autolinks, `> [!NOTE]` alerts). Headings get a slugified `id` so the fragment
 * can be dropped into a page alongside a generated table of contents ([MarkdownDocument.headings]).
 * Fenced code blocks are syntax-highlighted (see [Highlight.kt]) and adjacent fences are grouped
 * into a switchable tab set (see [CodeTabs]).
 */
fun renderMarkdown(markdown: String): MarkdownDocument {
    // The GFM table/blank-line detection only recognises `\n`; CRLF source files (common on
    // Windows checkouts) would otherwise silently fall back to one giant paragraph.
    val source = markdown.replace("\r\n", "\n").replace('\r', '\n')
    val flavour = GFMFlavourDescriptor()
    val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(source)

    val headings = mutableListOf<MarkdownHeading>()
    val slugs = mutableSetOf<String>()

    fun slugFor(node: ASTNode): String {
        val text = headingText(node, source)
        val base = text.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifEmpty { "section" }
        var slug = base
        var suffix = 2
        while (!slugs.add(slug)) {
            slug = "$base-${suffix++}"
        }
        return slug
    }

    val attributesCustomizer = { node: ASTNode, _: CharSequence?, attributes: Iterable<CharSequence?> ->
        val level = headingTypes[node.type]
        if (level != null) {
            val id = slugFor(node)
            headings += MarkdownHeading(level, id, headingText(node, source))
            attributes + "id=\"$id\""
        } else {
            attributes
        }
    }

    val linkMap = LinkMap.buildLinkMap(tree, source)
    val providers: Map<org.intellij.markdown.IElementType, GeneratingProvider> =
        flavour.createHtmlGeneratingProviders(linkMap, null) +
            mapOf(
                GFMElementTypes.ALERT to AlertGeneratingProvider(),
                MarkdownElementTypes.CODE_FENCE to CodeFenceGeneratingProvider(CodeTabs.analyze(tree, source)),
            )

    val html = HtmlGenerator(source, tree, providers, false)
        .generateHtml(HtmlGenerator.DefaultTagRenderer(attributesCustomizer, false))
        .removeSurrounding("<body>", "</body>")

    return MarkdownDocument(groupImageRuns(html), headings)
}

/**
 * Raw `<img>` HTML pasted directly into a doc (e.g. GitHub-style screenshot dumps) is emitted by
 * the parser as a top-level HTML block, not wrapped in a `<p>` — so consecutive images have no
 * shared container to lay out with. Wraps runs of two or more back-to-back `<img>` tags in an
 * `.image-row` div so CSS can flex them side by side and wrap onto a new row once they no longer
 * fit, instead of always stacking one per line.
 */
private val imageRun = Regex("(?:<img\\b[^>]*>\\s*){2,}")

private fun groupImageRuns(html: String): String =
    imageRun.replace(html) { match -> "<div class=\"image-row\">${match.value.trim()}</div>" }

private val headingImage = Regex("!\\[([^]]*)]\\([^)]*\\)")
private val headingLink = Regex("\\[([^]]*)]\\([^)]*\\)")
private val headingBoldItalic = Regex("(\\*\\*\\*|___)(.+?)\\1|(\\*\\*|__)(.+?)\\3|([*_])(.+?)\\5")
private val headingCode = Regex("`([^`]*)`")

/**
 * Strips the leading `#`s and renders common inline Markdown (links, images, emphasis, code
 * spans) down to plain text, so the "on this page" nav and heading `id`s don't end up with raw
 * `[Combat](combat-scripts)` syntax baked into them.
 */
private fun headingText(node: ASTNode, source: String): String {
    var text = node.getTextInNode(source).toString()
        .trimStart('#')
        .trim()
    text = headingImage.replace(text) { it.groupValues[1] }
    text = headingLink.replace(text) { it.groupValues[1] }
    text = headingCode.replace(text) { it.groupValues[1] }
    while (headingBoldItalic.containsMatchIn(text)) {
        text = headingBoldItalic.replace(text) { match ->
            match.groupValues[2].ifEmpty { match.groupValues[4].ifEmpty { match.groupValues[6] } }
        }
    }
    return text
}

/** Renders GitHub-style `> [!NOTE]`/`[!TIP]`/`[!WARNING]`/`[!IMPORTANT]`/`[!CAUTION]` callouts. */
private class AlertGeneratingProvider : GeneratingProvider {

    private val icons = mapOf(
        "note" to Icons.INFO,
        "tip" to Icons.LIGHTBULB,
        "important" to Icons.MEGAPHONE,
        "warning" to Icons.ALERT_TRIANGLE,
        "caution" to Icons.ALERT_TRIANGLE,
    )

    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        val kind = node.children.find { it.type == GFMTokenTypes.ALERT_TITLE }
            ?.getTextInNode(text)?.toString()
            ?.removePrefix("[!")?.removeSuffix("]")
            ?.lowercase()
            ?.takeIf { it in icons }
            ?: "note"
        val label = kind.replaceFirstChar { it.uppercase() }

        visitor.consumeHtml("<div class=\"markdown-alert markdown-alert-$kind\">")
        visitor.consumeHtml(
            "<p class=\"markdown-alert-title\">" + iconSpan(icons.getValue(kind)) + label + "</p>",
        )
        val skip = setOf(GFMTokenTypes.ALERT_TITLE, MarkdownTokenTypes.BLOCK_QUOTE, MarkdownTokenTypes.EOL, MarkdownTokenTypes.WHITE_SPACE)
        for (child in node.children) {
            if (child.type in skip) {
                continue
            }
            visitor.visitNode(child)
        }
        visitor.consumeHtml("</div>")
    }

    private fun iconSpan(path: String): String =
        """<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" """ +
            """stroke-linecap="square" aria-hidden="true">$path</svg>"""
}

/** Highlights fenced code blocks and wires up [CodeTabs] groups for consecutive fences. */
private class CodeFenceGeneratingProvider(private val slots: Map<Int, CodeTabs.Slot>) : GeneratingProvider {

    override fun processNode(visitor: HtmlGenerator.HtmlGeneratingVisitor, text: String, node: ASTNode) {
        val language = node.children.find { it.type == MarkdownTokenTypes.FENCE_LANG }
            ?.getTextInNode(text)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val code = node.children
            .filter { it.type == MarkdownTokenTypes.CODE_FENCE_CONTENT }
            .joinToString("\n") { it.getTextInNode(text) }
        val block = codeBlockHtml(language, highlightHtml(language, code))

        val slot = slots[node.startOffset]
        if (slot == null) {
            visitor.consumeHtml(block)
            return
        }

        if (slot.index == 0) {
            visitor.consumeHtml("<div class=\"code-tabs\">")
            visitor.consumeHtml("<div class=\"code-tabs-bar\" role=\"tablist\">")
            for ((index, tabLanguage) in slot.languages.withIndex()) {
                val active = if (index == 0) " active" else ""
                visitor.consumeHtml(
                    "<button type=\"button\" class=\"code-tab-btn$active\" data-lang=\"${tabLanguage.lowercase()}\" " +
                        "onclick=\"voidSwitchCodeTab(this)\">${escapeHtml(tabLanguage)}</button>",
                )
            }
            visitor.consumeHtml("</div>")
        }

        val hidden = if (slot.index == 0) "" else " hidden"
        visitor.consumeHtml("<div class=\"code-tab-panel\" data-lang=\"${(language ?: "").lowercase()}\"$hidden>")
        visitor.consumeHtml(block)
        visitor.consumeHtml("</div>")

        if (slot.index == slot.size - 1) {
            visitor.consumeHtml("</div>")
        }
    }

    private fun codeBlockHtml(language: String?, highlighted: String): String {
        val cls = if (language != null) " class=\"language-${language.lowercase()}\"" else ""
        return "<div class=\"code-block\">" +
            "<button type=\"button\" class=\"code-copy-btn\" onclick=\"voidCopyCode(this)\">Copy</button>" +
            "<pre><code$cls>$highlighted</code></pre>" +
            "</div>"
    }
}

/**
 * Groups adjacent fenced code blocks (separated only by the blank-line `EOL` the parser leaves
 * between top-level blocks) so they can render as a tab set, e.g. the same config shown as both
 * Groml and JSON. Tabs are switched by *language*: clicking "JSON" in one group flips every other
 * group on the page that also has a JSON panel (see `voidSwitchCodeTab` in `void.js`).
 */
private object CodeTabs {

    data class Slot(val index: Int, val size: Int, val languages: List<String>)

    fun analyze(root: ASTNode, source: String): Map<Int, Slot> {
        val slots = mutableMapOf<Int, Slot>()
        collectGroups(root, source, slots)
        return slots
    }

    private fun fenceLanguage(fence: ASTNode, source: String): String =
        fence.children.find { it.type == MarkdownTokenTypes.FENCE_LANG }
            ?.getTextInNode(source)?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: "text"

    private fun collectGroups(node: ASTNode, source: String, slots: MutableMap<Int, Slot>) {
        val children = node.children
        var i = 0
        while (i < children.size) {
            val child = children[i]
            if (child.type == MarkdownElementTypes.CODE_FENCE) {
                val group = mutableListOf(child)
                var j = i + 1
                while (j < children.size) {
                    val next = children[j]
                    when (next.type) {
                        MarkdownTokenTypes.EOL -> j++
                        // Only fold the next fence into this tab group if it's a *different*
                        // language — two same-language fences in a row are separate snippets
                        // (e.g. before/after), not alternate views of the same one, and should
                        // stay stacked rather than being merged into a switchable tab set.
                        MarkdownElementTypes.CODE_FENCE -> {
                            if (fenceLanguage(next, source) == fenceLanguage(group.last(), source)) {
                                break
                            }
                            group += next
                            j++
                        }
                        else -> break
                    }
                }
                if (group.size >= 2) {
                    val languages = group.map { fenceLanguage(it, source) }
                    for ((index, fence) in group.withIndex()) {
                        slots[fence.startOffset] = Slot(index, group.size, languages)
                    }
                }
                i = j
            } else {
                collectGroups(child, source, slots)
                i++
            }
        }
    }
}
