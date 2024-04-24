#!/usr/bin/env sh
# skip first line added by mpfilepicker subproject
./gradlew -q :composeApp:printConveyorConfig | sed 1d