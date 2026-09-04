# Void - RuneScape 634 server
# Game protocol port (NOT http) — access with void-client on localhost:43594

PORT      := 43594
PID_FILE  := .server.pid
LOG_FILE  := server.log

.PHONY: start stop restart status logs help

help:
	@echo "Void server commands:"
	@echo "  make start    - build and run server in background"
	@echo "  make stop     - stop the server"
	@echo "  make status   - show if server is running"
	@echo "  make restart  - stop and start again"
	@echo "  make logs     - follow server output (Ctrl+C to exit)"

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
	@nohup ./gradlew :game:run --console=plain > $(LOG_FILE) 2>&1 & echo $$! > $(PID_FILE)
	@echo "PID: $$(cat $(PID_FILE)) | log: $(LOG_FILE)"
	@echo "Waiting for port $(PORT)... (Ctrl+C safe, server keeps running)"
	@for i in $$(seq 1 120); do \
		if lsof -nP -iTCP:$(PORT) -sTCP:LISTEN >/dev/null 2>&1; then \
			echo ""; \
			echo "✔ Server is UP"; \
			echo "  Address : localhost:$(PORT)  (game protocol — connect with void-client, not a browser)"; \
			echo "  Logs    : make logs"; \
			exit 0; \
		fi; \
		if ! kill -0 $$(cat $(PID_FILE)) 2>/dev/null; then \
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
	echo "⚠ Still not listening after 4 min. Check: make logs"

stop:
	@if [ ! -f "$(PID_FILE)" ]; then \
		echo "No pid file. Server not started by make."; \
		exit 1; \
	fi
	@pid=$$(cat $(PID_FILE)); \
	if kill -0 $$pid 2>/dev/null; then \
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
	else \
		echo "Process $$pid not running (stale pid file)."; \
	fi
	@rm -f $(PID_FILE)
	@# also clear any leftover listener on the game port
	@lsof -ti tcp:$(PORT) | xargs kill 2>/dev/null || true

restart: stop start

status:
	@if [ -f "$(PID_FILE)" ] && kill -0 $$(cat $(PID_FILE)) 2>/dev/null; then \
		echo "Status: RUNNING (pid $$(cat $(PID_FILE)))"; \
	else \
		echo "Status: STOPPED"; \
	fi
	@if lsof -nP -iTCP:$(PORT) -sTCP:LISTEN >/dev/null 2>&1; then \
		echo "Port $(PORT): LISTENING ✔"; \
	else \
		echo "Port $(PORT): not listening"; \
	fi
	@if [ -f "$(LOG_FILE)" ]; then \
		echo "--- last 5 log lines ---"; \
		tail -n 5 $(LOG_FILE); \
	fi

logs:
	@tail -n 50 -f $(LOG_FILE)
