SHELL := bash

.PHONY: help bootstrap check-cache cache-bootstrap \
	build build-db run run-db clean \
	gradle-build gradle-build-db gradle-run gradle-run-db gradle-jar gradle-jar-db gradle-test gradle-spotless gradle-clean \
	kt-bootstrap kt-bootstrap-kotlin kt-bootstrap-cs kt-deps kt-script-metadata kt-build kt-build-db kt-run kt-run-db kt-clean \
	kt kt-db vendor-deps vendor-verify

GRADLEW := ./gradlew

# Requested JDK major version. Used to bootstrap a repo-local Temurin JDK under ./.jdk/.
JDK ?= 21
BOOTSTRAP_JAVA_HOME ?= $(CURDIR)/.jdk/temurin$(JDK)

JAVA_BOOTSTRAP := $(BOOTSTRAP_JAVA_HOME)/bin/java

# Prefer the bootstrapped JDK if present; otherwise fall back to system java.
JAVA ?= java
ifneq ($(wildcard $(JAVA_BOOTSTRAP)),)
JAVA := $(JAVA_BOOTSTRAP)
export JAVA_HOME := $(BOOTSTRAP_JAVA_HOME)
export PATH := $(BOOTSTRAP_JAVA_HOME)/bin:$(PATH)
endif

KOTLIN_VERSION ?= 2.2.20
KOTLIN_HOME ?= $(CURDIR)/.kotlin/kotlin-compiler-$(KOTLIN_VERSION)
CS ?= $(CURDIR)/.cs/cs

KT_BUILD_ROOT ?= build/make-kotlinc
KT_COORDS ?= $(KT_BUILD_ROOT)/deps.coords
KT_MAVEN_CP ?= $(KT_BUILD_ROOT)/maven.classpath
KT_COURSIER_CACHE ?= $(KT_BUILD_ROOT)/coursier-cache
KT_KOTLINC_JAVA_OPTS ?= -Xmx6g -Xms512m
KT_KOTLINC_FLAGS ?=
KT_SCRIPTS_TXT ?= $(KT_BUILD_ROOT)/generated/scripts.txt
NINJA ?= ninja
VENDOR_DIR ?= vendor
VENDOR_JARS ?= $(VENDOR_DIR)/jars
VENDOR_MAVEN_CP ?= $(VENDOR_DIR)/maven.classpath
VENDOR_COORDS ?= $(VENDOR_DIR)/deps.coords
VENDOR_SHA256 ?= $(VENDOR_DIR)/SHA256SUMS
VENDOR_INCLUDE_DB ?= 0
# Offline mode:
# - KT_OFFLINE=auto (default): use vendored deps if present, otherwise download via coursier.
# - KT_OFFLINE=1: require vendored deps (no downloads).
# - KT_OFFLINE=0: always download via coursier.
KT_OFFLINE ?= auto

VENDOR_PRESENT := $(if $(wildcard $(VENDOR_MAVEN_CP)),1,0)
ifeq ($(KT_OFFLINE),1)
KT_USE_VENDOR := 1
else ifeq ($(KT_OFFLINE),0)
KT_USE_VENDOR := 0
else ifeq ($(VENDOR_PRESENT),1)
KT_USE_VENDOR := 1
else
KT_USE_VENDOR := 0
endif

ifeq ($(KT_USE_VENDOR),1)
KT_MAVEN_CP_EFFECTIVE := $(VENDOR_MAVEN_CP)
else
KT_MAVEN_CP_EFFECTIVE := $(KT_MAVEN_CP)
endif

help:
	@echo "Targets:"
	@echo "  make bootstrap   # Download repo-local Temurin JDK (./.jdk/temurin\$$JDK)"
	@echo "  make cache-bootstrap # Ensure ./data/cache is populated (CACHE_ZIP/CACHE_DIR/CACHE_URL)"
	@echo "  make build       # Build server (incremental, ninja+kotlinc)"
	@echo "  make run         # Run server (incremental, ninja+kotlinc)"
	@echo "  make build-db    # Build server with DB module (ninja+kotlinc)"
	@echo "  make run-db      # Run server with DB module (ninja+kotlinc)"
	@echo "  make clean       # Clean ninja+kotlinc outputs"
	@echo ""
	@echo "Gradle (optional):"
	@echo "  make gradle-build    # Build fat jar (no DB module)"
	@echo "  make gradle-run      # Run via Gradle (no DB module)"
	@echo "  make gradle-jar      # Build fat jar then run it (no DB module)"
	@echo "  make gradle-build-db # Build fat jar (includes DB module)"
	@echo "  make gradle-run-db   # Run via Gradle (includes DB module)"
	@echo "  make gradle-jar-db   # Build fat jar then run it (includes DB module)"
	@echo "  make gradle-test     # Run tests"
	@echo "  make gradle-spotless # Apply formatting"
	@echo "  make gradle-clean    # Gradle clean"
	@echo ""
	@echo "Ninja + kotlinc (no Gradle):"
	@echo "  make kt-bootstrap     # Ensure JDK + Kotlin compiler"
	@echo "  make kt-bootstrap-cs  # Download repo-local coursier (for fetching deps)"
	@echo "  make kt-deps          # Download Maven deps using coursier"
	@echo "  make kt-build         # Compile modules (incremental, via ninja)"
	@echo "  make kt-run           # Run server (Main) using built classpath"
	@echo "  make kt-build-db      # Same as kt-build, includes :database"
	@echo "  make kt-run-db        # Run server with :database on classpath"
	@echo "  make kt              # One-shot: bootstrap + deps + build + run"
	@echo "  make kt-db           # One-shot: bootstrap + deps + build + run (DB)"
	@echo "  make vendor-deps      # Vendor resolved Maven jars into ./vendor/ for offline builds"
	@echo ""
	@echo "Vars:"
	@echo "  JDK=21                   (requested major version; default 21)"
	@echo "  BOOTSTRAP_JAVA_HOME=...  (where the repo-local JDK is installed; default ./.jdk/temurin\$$JDK)"
	@echo "  KOTLIN_VERSION=2.2.20    (Kotlin compiler version; default from gradle/libs.versions.toml)"
	@echo "  KT_BUILD_ROOT=build/...  (make+kotlinc output directory)"
	@echo "  KT_KOTLINC_JAVA_OPTS=... (memory for kotlinc JVM; default -Xmx6g -Xms512m)"
	@echo "  KT_KOTLINC_FLAGS=...     (extra kotlinc flags; e.g. -nowarn)"
	@echo "  KT_OFFLINE=auto|0|1      (deps mode; default auto: prefer vendor/ if present)"
	@echo "  NINJA=ninja              (ninja binary)"

bootstrap:
	@if [ -x "$(JAVA_BOOTSTRAP)" ]; then \
		echo "Temurin JDK $(JDK) already present at $(BOOTSTRAP_JAVA_HOME)"; \
	elif [ "$(KT_OFFLINE)" = "1" ]; then \
		echo "Offline mode requested but missing $(JAVA_BOOTSTRAP)."; \
		echo "Run: make bootstrap  (once, online)"; \
		exit 2; \
	else \
		echo "Bootstrapping Temurin JDK $(JDK) into $(BOOTSTRAP_JAVA_HOME)"; \
		bash tools/bootstrap-jdk.sh "$(JDK)"; \
	fi

check-cache:
	@if [ -s "data/cache/main_file_cache.dat2" ]; then \
		echo "Cache OK: data/cache/main_file_cache.dat2"; \
	else \
		echo "Cache missing: data/cache/main_file_cache.dat2"; \
		exit 2; \
	fi

cache-bootstrap:
	@bash tools/bootstrap-cache.sh

vendor-deps: kt-deps
	@mkdir -p "$(KT_BUILD_ROOT)"
	@if [ "$(VENDOR_INCLUDE_DB)" = "1" ]; then \
		python3 tools/resolve-maven-deps.py --include-db >"$(KT_BUILD_ROOT)/deps.vendor.coords"; \
		COURSIER_CACHE="$(KT_COURSIER_CACHE)" python3 tools/fetch-maven-classpath.py --cs "$(CS)" --coords "$(KT_BUILD_ROOT)/deps.vendor.coords" >"$(KT_BUILD_ROOT)/maven.vendor.classpath"; \
		python3 tools/vendor-deps.py --classpath-file "$(KT_BUILD_ROOT)/maven.vendor.classpath" --out-dir "$(VENDOR_JARS)" --out-classpath "$(VENDOR_MAVEN_CP)"; \
		cp -f "$(KT_BUILD_ROOT)/deps.vendor.coords" "$(VENDOR_COORDS)"; \
	else \
		python3 tools/vendor-deps.py --classpath-file "$(KT_MAVEN_CP)" --out-dir "$(VENDOR_JARS)" --out-classpath "$(VENDOR_MAVEN_CP)"; \
		cp -f "$(KT_COORDS)" "$(VENDOR_COORDS)"; \
	fi
	@python3 tools/vendor-manifest.py --vendor-classpath "$(VENDOR_MAVEN_CP)" --out "$(VENDOR_SHA256)"

vendor-verify:
	@sha256sum -c --quiet "$(VENDOR_SHA256)" && echo "Vendored jars OK ($(VENDOR_SHA256))"

#
# Default workflow: ninja+kotlinc
#
build: kt-build

build-db: kt-build-db

run: kt-run

run-db: kt-run-db

clean: kt-clean

#
# Gradle workflow: explicitly prefixed
#
gradle-build: bootstrap
	@JAVA_HOME="$(BOOTSTRAP_JAVA_HOME)" PATH="$(BOOTSTRAP_JAVA_HOME)/bin:$$PATH" $(GRADLEW) :game:shadowJar

gradle-build-db: bootstrap
	@JAVA_HOME="$(BOOTSTRAP_JAVA_HOME)" PATH="$(BOOTSTRAP_JAVA_HOME)/bin:$$PATH" $(GRADLEW) -PincludeDb :game:shadowJar

gradle-run: bootstrap cache-bootstrap check-cache
	@JAVA_HOME="$(BOOTSTRAP_JAVA_HOME)" PATH="$(BOOTSTRAP_JAVA_HOME)/bin:$$PATH" $(GRADLEW) :game:run

gradle-run-db: bootstrap cache-bootstrap check-cache
	@JAVA_HOME="$(BOOTSTRAP_JAVA_HOME)" PATH="$(BOOTSTRAP_JAVA_HOME)/bin:$$PATH" $(GRADLEW) -PincludeDb :game:run

gradle-jar: cache-bootstrap check-cache gradle-build
	@JAR="$$(ls -1 game/build/libs/void-server*.jar 2>/dev/null | head -n1)"; \
	if [ -z "$$JAR" ]; then echo "Jar not found. Did the build fail?"; exit 1; fi; \
	echo "Running $$JAR"; \
	exec "$(JAVA_BOOTSTRAP)" -jar "$$JAR"

gradle-jar-db: cache-bootstrap check-cache gradle-build-db
	@JAR="$$(ls -1 game/build/libs/void-server-db*.jar 2>/dev/null | head -n1)"; \
	if [ -z "$$JAR" ]; then echo "Jar not found. Did the build fail?"; exit 1; fi; \
	echo "Running $$JAR"; \
	exec "$(JAVA_BOOTSTRAP)" -jar "$$JAR"

gradle-test:
	$(GRADLEW) test

gradle-spotless:
	$(GRADLEW) spotlessApply

gradle-clean:
	$(GRADLEW) clean

kt-bootstrap: bootstrap kt-bootstrap-kotlin

kt-bootstrap-kotlin:
	@if [ -x "$(KOTLIN_HOME)/bin/kotlinc" ]; then \
		echo "Kotlin compiler already present at $(KOTLIN_HOME)"; \
	else \
		if [ "$(KT_OFFLINE)" = "1" ]; then \
			echo "Offline mode requested but missing Kotlin compiler at $(KOTLIN_HOME)."; \
			echo "Install Kotlin compiler or run: make kt-bootstrap-kotlin  (once, online)"; \
			exit 2; \
		fi; \
		bash tools/bootstrap-kotlin.sh "$(KOTLIN_VERSION)"; \
	fi

kt-bootstrap-cs:
	@if [ -x "$(CS)" ]; then \
		echo "coursier already present at $(CS)"; \
	else \
		if [ "$(KT_OFFLINE)" = "1" ]; then \
			echo "Offline mode requested but missing coursier at $(CS)."; \
			echo "Install coursier or run: make kt-bootstrap-cs  (once, online)"; \
			exit 2; \
		fi; \
		bash tools/bootstrap-coursier.sh; \
	fi

kt-script-metadata:
	@mkdir -p "$(KT_BUILD_ROOT)/generated"
	@python3 tools/generate-scripts-txt.py --output "$(KT_SCRIPTS_TXT)" >/dev/null

kt-deps: kt-bootstrap kt-bootstrap-cs
	@mkdir -p "$(KT_BUILD_ROOT)"
	@python3 tools/resolve-maven-deps.py >"$(KT_COORDS)"
	@COURSIER_CACHE="$(KT_COURSIER_CACHE)" python3 tools/fetch-maven-classpath.py --cs "$(CS)" --coords "$(KT_COORDS)" >"$(KT_MAVEN_CP)"
	@echo "Wrote $(KT_MAVEN_CP)"

kt-maven-cp: kt-bootstrap
	@if [ "$(KT_USE_VENDOR)" = "1" ]; then \
		if [ -s "$(VENDOR_MAVEN_CP)" ]; then \
			if [ "$(KT_OFFLINE)" = "1" ]; then \
				echo "Offline deps OK: $(VENDOR_MAVEN_CP)"; \
			else \
				echo "Using vendored deps: $(VENDOR_MAVEN_CP)"; \
			fi; \
		else \
			if [ "$(KT_OFFLINE)" = "1" ]; then \
				echo "Offline mode requested but missing $(VENDOR_MAVEN_CP)."; \
				echo "Run: make vendor-deps  (once, online)"; \
			else \
				echo "Vendored deps not found at $(VENDOR_MAVEN_CP); falling back to downloads."; \
				$(MAKE) kt-deps; \
				exit 0; \
			fi; \
			exit 2; \
		fi; \
	else \
		$(MAKE) kt-deps; \
	fi

kt-run: cache-bootstrap check-cache kt-build
	@JAVA_HOME="$(BOOTSTRAP_JAVA_HOME)" PATH="$(BOOTSTRAP_JAVA_HOME)/bin:$$PATH" BUILD_ROOT="$(KT_BUILD_ROOT)" JAVA="$(JAVA_BOOTSTRAP)" bash tools/kotlinc-run.sh

kt-run-db: cache-bootstrap check-cache kt-build-db
	@JAVA_HOME="$(BOOTSTRAP_JAVA_HOME)" PATH="$(BOOTSTRAP_JAVA_HOME)/bin:$$PATH" BUILD_ROOT="$(KT_BUILD_ROOT)" JAVA="$(JAVA_BOOTSTRAP)" bash tools/kotlinc-run.sh

kt-clean:
	rm -rf "$(KT_BUILD_ROOT)"

# One-shot convenience aliases
kt: kt-run

kt-db: kt-run-db

kt-build: kt-maven-cp kt-script-metadata
	@python3 tools/gen-ninja.py --build-root "$(KT_BUILD_ROOT)" --kotlin-home "$(KOTLIN_HOME)" --maven-cp "$(KT_MAVEN_CP_EFFECTIVE)" --scripts-txt "$(KT_SCRIPTS_TXT)" --kotlinc-java-opts "$(KT_KOTLINC_JAVA_OPTS)" --kotlinc-flags "$(KT_KOTLINC_FLAGS)" >/dev/null
	@JAVA_HOME="$(BOOTSTRAP_JAVA_HOME)" PATH="$(BOOTSTRAP_JAVA_HOME)/bin:$$PATH" "$(NINJA)" -f "$(KT_BUILD_ROOT)/build.ninja" game
	@KOTLIN_HOME="$(KOTLIN_HOME)" BUILD_ROOT="$(KT_BUILD_ROOT)" MAVEN_CP_FILE="$(KT_MAVEN_CP_EFFECTIVE)" bash tools/write-run-classpath.sh

kt-build-db: kt-maven-cp kt-script-metadata
	@python3 tools/gen-ninja.py --build-root "$(KT_BUILD_ROOT)" --kotlin-home "$(KOTLIN_HOME)" --maven-cp "$(KT_MAVEN_CP_EFFECTIVE)" --scripts-txt "$(KT_SCRIPTS_TXT)" --kotlinc-java-opts "$(KT_KOTLINC_JAVA_OPTS)" --kotlinc-flags "$(KT_KOTLINC_FLAGS)" --include-db >/dev/null
	@JAVA_HOME="$(BOOTSTRAP_JAVA_HOME)" PATH="$(BOOTSTRAP_JAVA_HOME)/bin:$$PATH" "$(NINJA)" -f "$(KT_BUILD_ROOT)/build.ninja" game
	@KOTLIN_HOME="$(KOTLIN_HOME)" BUILD_ROOT="$(KT_BUILD_ROOT)" MAVEN_CP_FILE="$(KT_MAVEN_CP_EFFECTIVE)" INCLUDE_DB=1 bash tools/write-run-classpath.sh
