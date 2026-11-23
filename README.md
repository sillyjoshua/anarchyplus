# Anarchy+

An operator-only management menu for Paper 26.3.

## Features
- `/anarchy` opens the admin inventory menu.
- Toggle PvP, Elytra usage, and End Crystal damage.
- Restart and stop controls with configurable broadcast messages.
- No anti-cheat or hack-blocking functionality is included.

## Requirements
- Paper 26.3
- Java 25
- Gradle (the Gradle wrapper can be generated with `gradle wrapper`)

## Build
Run from this folder:

```sh
gradle build
```

The plugin JAR will be at `build/libs/AnarchyPlus-1.0.0.jar`.

Copy that JAR into the server's `plugins` folder and restart the server.

## Important limitations
A Paper plugin cannot reliably detect or prohibit every client-side mod or hack.
Anarchy+ does not attempt to inspect players' clients. It simply adds no anti-cheat
restrictions of its own. Other anti-cheat plugins must be removed or configured
separately.

The restart button runs the console command configured as `messages.restart-command`
(default: `restart`). Your host/server wrapper must support that command. If it
does not, configure an appropriate restart command in `plugins/AnarchyPlus/config.yml`.
The stop button uses Paper's normal server shutdown API.

## Build automatically on GitHub
The project includes `.github/workflows/build.yml`. Push the extracted project to a
GitHub repository, open the Actions tab, select **Build Anarchy+**, and wait for the
workflow to finish. Download the `AnarchyPlus-1.0.0` artifact from the successful run.
The workflow uses Java 25 and Gradle on GitHub's runner.
