## Build Commands

```bash
./gradlew build -x test        # initial setup (cache files needed for tests)
./gradlew test                 # all tests
./gradlew test --tests "content.skill.firemaking.FiremakingTest"
./gradlew :game:test           # single module
./gradlew spotlessApply        # format (required before committing)
./gradlew shadowJar            # distributable JAR
./gradlew run                  # start server (working dir = root)
./gradlew bundleDistribution   # JAR + data + startup scripts
```

Tests require cache files in `data/cache/`. CI runs all engine and content tests on all PRs.