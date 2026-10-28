# Determine the operating system
ifeq ($(OS),Windows_NT)
    OS_TYPE := Windows
else
    OS_TYPE := $(shell uname -s)
endif

# Define Gradle wrapper command based on the operating system
ifeq ($(OS_TYPE),Windows)
    GRADLEW := gradlew
    SHELL_CMD := cmd /c
else
    GRADLEW := ./gradlew
    SHELL_CMD := sh
endif

# Targets
.PHONY: build clean run test format format-check update-git-hooks generate-version create-change-log

build: clean
	$(GRADLEW) build

clean:
	$(GRADLEW) clean

run:
	$(GRADLEW) bootRun

test:
	$(GRADLEW) clean && $(GRADLEW) test

format:
	$(GRADLEW) spotlessApply

format-check:
	$(GRADLEW) spotlessCheck

install-git-hooks:
	lefthook install

generate-version:
ifeq ($(OS_TYPE),Windows)
	$(SHELL_CMD) ci\version\generate-version.bat
else
	$(SHELL_CMD) ci/version/generate-version.sh
endif

create-change-log:
ifeq ($(OS_TYPE),Windows)
	$(SHELL_CMD) ci\version\create-change-log.bat
else
	$(SHELL_CMD) ci/version/create-change-log.sh
endif

db-evo:
	node db/main.js

db-evo-dev:
	node db/main.js -p=dev