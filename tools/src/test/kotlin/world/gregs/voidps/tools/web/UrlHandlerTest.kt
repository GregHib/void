package world.gregs.voidps.tools.web

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Suppress("HttpUrlsUsage")
internal class UrlHandlerTest {

    @Test
    fun `Remove domain`() {
        assertEquals("", UrlHandler.removeDomain("http://example.com/", "example.com"))
        assertEquals("", UrlHandler.removeDomain("https://www.example.com/", "example.com"))
    }

    @Test
    fun `Leave domain path`() {
        assertEquals("1/", UrlHandler.removeDomain("http://example.com/1/", "example.com"))
        assertEquals("blog/", UrlHandler.removeDomain("http://www.example.com/blog/", "example.com"))
    }

    @Test
    fun `Leave subdomain`() {
        assertEquals("o/", UrlHandler.removeDomain("http://o.example.com/", "example.com"))
        assertEquals("custom/", UrlHandler.removeDomain("http://custom.example.com/", "example.com"))
        assertEquals("easy/", UrlHandler.removeDomain("http://easy.example.com/", "example.com"))
        assertEquals("easy/", UrlHandler.removeDomain("http://www.easy.example.com/", "example.com"))
    }

    @Test
    fun `Leave subdomain and path`() {
        assertEquals("easy/blog/", UrlHandler.removeDomain("http://www.easy.example.com/blog/", "example.com"))
    }

    @Test
    fun `Leave query`() {
        assertEquals("blog?page=2", UrlHandler.removeDomain("http://example.com/blog?page=2", "example.com"))
    }

    @Test
    fun `Leave subdomain, path and query`() {
        assertEquals("easy/blog?page=2", UrlHandler.removeDomain("http://www.easy.example.com/blog?page=2", "example.com"))
    }

    @Test
    fun `Convert query to unique page`() {
        assertEquals("http://example.com/blog-page-1", UrlHandler.convertQuery("http://example.com/blog?page=1"))
    }

    @Test
    fun `Convert query on relative url`() {
        assertEquals("test/index-page-1.html", UrlHandler.convertQuery("test/index.html?page=1"))
    }

    @Test
    fun `Convert multiple query parameters`() {
        assertEquals("http://example.com/blog-page-2-index-1", UrlHandler.convertQuery("http://example.com/blog?page=2&index=1"))
    }

    @Test
    fun `Convert query keep extension`() {
        assertEquals("http://example.com/blog-page-2-index-1.html", UrlHandler.convertQuery("http://example.com/blog.html?page=2&index=1"))
    }

    @Test
    @Disabled
    fun `Convert query keep anchors`() {
        assertEquals("http://example.com/blog-page-2.html#top", UrlHandler.convertQuery("http://example.com/blog.html?page=2#top"))
    }

    @Test
    fun `Convert query double question mark`() {
        assertEquals("http://example.com/blog-1.html", UrlHandler.convertQuery("http://example.com/blog.html?2?1"))
    }

    @Test
    fun `Convert query double part`() {
        assertEquals("http://example.com/fake.html%3Fquery=fake/blog-page-2-index-1.html", UrlHandler.convertQuery("http://example.com/fake.html?query=fake/blog.html?page=2&index=1"))
    }

    @Test
    fun `Remove prefix domain`() {
        assertEquals("http://www.real.com/", UrlHandler.removePrefixDomain("http://example.com/http://www.real.com/"))
        assertEquals("https://the.real.com/page.html?key=value", UrlHandler.removePrefixDomain("https://example.com/whatever/123456/https://the.real.com/page.html?key=value"))
    }
}
