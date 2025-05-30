#!/bin/bash
cd /home/kavia/workspace/code-generation/rhythmease-36762-3ca20b46/rhythmease
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

