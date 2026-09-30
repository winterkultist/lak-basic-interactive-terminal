#!/usr/bin/env bash

set -euo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
output_dir="$project_dir/out/production/lak-basic-interactive-terminal"

java_bin=""
javac_bin=""

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" && -x "$JAVA_HOME/bin/javac" ]]; then
    java_bin="$JAVA_HOME/bin/java"
    javac_bin="$JAVA_HOME/bin/javac"
elif [[ -x "$HOME/.sdkman/candidates/java/current/bin/java" \
        && -x "$HOME/.sdkman/candidates/java/current/bin/javac" ]]; then
    java_bin="$HOME/.sdkman/candidates/java/current/bin/java"
    javac_bin="$HOME/.sdkman/candidates/java/current/bin/javac"
else
    java_bin="$(command -v java || true)"
    javac_bin="$(command -v javac || true)"
fi

if [[ -z "$java_bin" || -z "$javac_bin" ]]; then
    printf '%s\n' "Could not find a JDK. Set JAVA_HOME or install Java with javac available." >&2
    exit 1
fi

shell_quote() {
    printf '%q' "$1"
}

terminal_command="cd $(shell_quote "$project_dir") && mkdir -p $(shell_quote "$output_dir") && $(shell_quote "$javac_bin") -d $(shell_quote "$output_dir") src/main/java/terminal/*.java && exec $(shell_quote "$java_bin") -cp $(shell_quote "$output_dir") terminal.Main"

osascript - "$terminal_command" <<'APPLESCRIPT'
on run argv
    tell application "Terminal"
        do script (item 1 of argv)
        activate
    end tell
end run
APPLESCRIPT
