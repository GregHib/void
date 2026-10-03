plugins {
    id("com.diffplug.spotless")
}

spotless {
    flexmark {
        target("**/*.md")
        targetExclude("**/build/**", "**/out/**", "temp/**", ".claude/worktrees/**", "**/node_modules/**")
        flexmark()
    }
}
