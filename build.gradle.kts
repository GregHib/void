plugins {
    id("com.diffplug.spotless")
}

spotless {
    flexmark {
        target("*.md", "docs/**/*.md", "config/**/*.md", "tools/*.md", "web/site/*.md", ".claude/*.md", ".github/**/*.md")
        targetExclude("**/build/**", "**/node_modules/**")
        flexmark()
    }
}
