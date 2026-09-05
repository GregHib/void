# Void - RuneScape 634 server
# Game protocol port (NOT http) — access with void-client on localhost:43594

PORT      := 43594
PID_FILE  := .server.pid
LOG_FILE  := server.log

# Override on the command line, e.g. `make start START_TIMEOUT=900`.
# Default 10 min covers a clean :game:compileKotlin from cold (~4 min build
# + engine startup) without silencing real failures.
START_TIMEOUT ?= 600

.PHONY: start stop restart status logs help clean

help:
	@echo "Void server commands:"
	@echo "  make start    - build and run server in background"
	@echo "  make stop     - stop the server"
	@echo "  make status   - show if server is running"
	@echo "  make restart  - stop and start again"
	@echo "  make logs     - follow server output (Ctrl+C to exit)"
	@echo "  make clean    - remove build outputs and stale gradle caches"
	@echo ""
	@echo "Variables:"
	@echo "  START_TIMEOUT - seconds to wait for the port (default $(START_TIMEOUT))"

start:
	@if [ ! -f "data/cache/main_file_cache.dat2" ]; then \
		echo "✘ Game cache missing: data/cache/main_file_cache.dat2"; \
		echo "  1. Download cache.zip: https://mega.nz/folder/ZMN2AQaZ#4rJgfzbVW0_mWsr1oPLh1A"; \
		echo "  2. Extract contents into ./data/cache/ (so main_file_cache.dat2 exists)"; \
		echo "  Then run 'make start' again."; \
		exit 1; \
	fi
	@if [ -f "$(PID_FILE)" ] && kill -0 $$(cat $(PID_FILE)) 2>/dev/null; then \
		echo "Server already running (pid $$(cat $(PID_FILE))). Use 'make restart' or 'make stop'."; \
		exit 1; \
	fi
	@echo "Starting Void server in background..."
	@: >$(LOG_FILE)
	@nohup ./gradlew :game:run --console=plain</dev/null >$(LOG_FILE) 2>&1 & echo $$! > $(PID_FILE)
	@pid=$$(cat $(PID_FILE)); \
	echo "PID: $$pid | log: $(LOG_FILE)"; \
	echo "Waiting for port $(PORT) (timeout $(START_TIMEOUT)s)... (Ctrl+C safe, server keeps running)"; \
	steps=$$(( $(START_TIMEOUT) / 2 )); \
	for i in $$(seq 1 $$steps); do \
		if lsof -nP -iTCP:$(PORT) -sTCP:LISTEN >/dev/null 2>&1; then \
			echo ""; \
			echo "✔ Server is UP"; \
			echo "  Address : localhost:$(PORT)  (game protocol — connect with void-client, not a browser)"; \
			echo "  Logs    : make logs"; \
			exit 0; \
		fi; \
		if ! kill -0 $$pid 2>/dev/null; then \
			echo ""; \
			echo "✘ Process died. Last log lines:"; \
			tail -n 30 $(LOG_FILE); \
			rm -f $(PID_FILE); \
			exit 1; \
		fi; \
		printf "."; \
		sleep 2; \
	done; \
	echo ""; \
	echo "⚠ Still not listening after $(START_TIMEOUT)s. Check: make logs"

stop:
	@stopped=0; \
	if [ -f "$(PID_FILE)" ]; then \
		pid=$$(cat $(PID_FILE)); \
		cmd=$$(ps -p $$pid -o comm= 2>/dev/null); \
		case "$$cmd" in \
			java|*/java|bash|*/bash|sh|*/sh) \
				echo "Stopping server (pid $$pid)..."; \
				kill $$pid 2>/dev/null; \
				for i in $$(seq 1 15); do \
					kill -0 $$pid 2>/dev/null || break; \
					sleep 1; \
				done; \
				if kill -0 $$pid 2>/dev/null; then \
					echo "Force killing..."; \
					kill -9 $$pid 2>/dev/null; \
				fi; \
				echo "Stopped."; \
				stopped=1;; \
			"") \
				echo "Process $$pid not running (stale pid file)."; \
				stopped=1;; \
			*) \
				echo "PID $$pid is no longer ours (now '$$cmd'). Leaving it alone.";; \
		esac; \
		rm -f $(PID_FILE); \
	else \
		echo "No pid file. Will fall back to port-based shutdown."; \
	fi
	@# Belt-and-braces: clear any leftover listener on the game port, but only
	@# if it's owned by a java/gradle process (don't kill a stranger's server).
	@leftovers=$$(lsof -ti tcp:$(PORT) 2>/dev/null); \
	if [ -n "$$leftovers" ]; then \
		for pid in $$leftovers; do \
			cmd=$$(ps -p $$pid -o comm= 2>/dev/null); \
			case "$$cmd" in \
				java|*/java|bash|*/bash|sh|*/sh) echo "Killing leftover $$pid on port $(PORT)..."; kill $$pid 2>/dev/null; stopped=1;; \
				*) echo "Port $(PORT) held by '$$cmd' (pid $$pid) — leaving alone.";; \
			esac; \
		done; \
	fi; \
	exit 0

restart: stop start

status:
	@running=0; \
	if [ -f "$(PID_FILE)" ] && kill -0 $$(cat $(PID_FILE)) 2>/dev/null; then \
		echo "Status: RUNNING (pid $$(cat $(PID_FILE)))"; \
		running=1; \
	else \
		if [ -f "$(PID_FILE)" ]; then \
			echo "Status: STOPPED (stale pid file)"; \
		else \
			echo "Status: STOPPED"; \
		fi; \
	fi
	@if lsof -nP -iTCP:$(PORT) -sTCP:LISTEN >/dev/null 2>&1; then \
		echo "Port $(PORT): LISTENING ✔"; \
	elif [ $$running -eq 0 ]; then \
		echo "Port $(PORT): not listening"; \
	fi
	@if [ -f "$(LOG_FILE)" ]; then \
		echo "--- last 5 log lines ---"; \
		tail -n 5 $(LOG_FILE); \
	fi

logs:
	@tail -n 50 -f $(LOG_FILE)

clean:
	@echo "Removing build outputs and stale Kotlin incremental caches..."
	@rm -rf */build .gradle
	@# Kotlin's incremental caches get poisoned when the daemon dies mid-write
	@# (e.g. "Storage for [.../class-attributes.tab] is already registered").
	@# The next build recreates them automatically.
	@./gradlew --stop 2>/dev/null || true
	@echo "Cleaned. Next 'make start' will rebuild from cold."
