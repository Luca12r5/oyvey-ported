package dev.visor.browser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BrowserPolicyTest {
	@Test
	void httpsPagesLoadAsIs() {
		BrowserPolicy.Decision d = BrowserPolicy.check("https://minecraft.wiki/w/Creeper");
		assertEquals(BrowserPolicy.Kind.ALLOW, d.kind());
		assertEquals("https://minecraft.wiki/w/Creeper", d.url());
	}

	@Test
	void httpIsUpgraded() {
		BrowserPolicy.Decision d = BrowserPolicy.check("http://example.org/a?b=1");
		assertEquals(BrowserPolicy.Kind.REDIRECT, d.kind());
		assertEquals("https://example.org/a?b=1", d.url());
	}

	@Test
	void localAndInternalPagesAreBlocked() {
		assertEquals(BrowserPolicy.Kind.BLOCK, BrowserPolicy.check("file:///C:/Users/x/secret.txt").kind());
		assertEquals(BrowserPolicy.Kind.BLOCK, BrowserPolicy.check("chrome://settings").kind());
		assertEquals(BrowserPolicy.Kind.BLOCK, BrowserPolicy.check("javascript:alert(1)").kind());
		assertEquals(BrowserPolicy.Kind.BLOCK, BrowserPolicy.check("data:text/html,<b>x</b>").kind());
		assertEquals(BrowserPolicy.Kind.ALLOW, BrowserPolicy.check("about:blank").kind());
	}

	@Test
	void safeSearchIsForced() {
		assertEquals("https://www.google.com/search?q=lego&safe=active", BrowserPolicy.check("https://www.google.com/search?q=lego").url());
		assertEquals("https://www.google.de/search?q=x&safe=active", BrowserPolicy.check("https://www.google.de/search?q=x&safe=off").url());
		assertEquals("https://www.google.com/?safe=active", BrowserPolicy.check("https://www.google.com/").url());
		assertEquals("https://www.bing.com/search?q=a&adlt=strict", BrowserPolicy.check("https://www.bing.com/search?q=a&adlt=off").url());
		assertEquals("https://duckduckgo.com/?q=a&kp=1", BrowserPolicy.check("https://duckduckgo.com/?q=a").url());
		assertEquals(BrowserPolicy.Kind.ALLOW, BrowserPolicy.check("https://www.google.com/search?q=lego&safe=active").kind());
		// Other Google services are left alone.
		assertEquals(BrowserPolicy.Kind.ALLOW, BrowserPolicy.check("https://www.google.com/maps").kind());
	}

	@Test
	void fragmentsSurviveRewrites() {
		assertEquals("https://www.google.com/search?q=a&safe=active#top", BrowserPolicy.check("https://www.google.com/search?q=a#top").url());
	}

	@Test
	void youtubeHosts() {
		assertTrue(BrowserPolicy.youtube("https://www.youtube.com/watch?v=1"));
		assertTrue(BrowserPolicy.youtube("https://m.youtube.com/"));
		assertFalse(BrowserPolicy.youtube("https://notyoutube.com/"));
	}
}
