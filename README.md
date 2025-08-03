# Java Music Player (Swing + Gradle)
A Java-based desktop music player with GUI and SQLite backend, built using Swing and Gradle. 

A desktop music player built with Java Swing, featuring:
- User authentication (Admin and User roles)
- SQLite database integration for music metadata and login credentials
- Role-based dashboards for different user types
- Audio playback controls (Play, Pause, Stop)
- Gradle-based build system

## Tech Stack

- Java 17
- Swing (for GUI)
- SQLite (for database)
- Gradle (for build automation)

## Features

- Secure login system with role management
- GUI-based music player with audio controls
- Admin panel to manage song data
- User dashboard for personal playback interface
- Pre-packaged JAR and ZIP files for easy distribution

## Run Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/sasi-1902/java-music-player.git
   cd java-music-player


2. Build the project using Gradle:

   ```bash
   ./gradlew build


3. Run the application:

   ```bash
   java -cp build/libs/MAD_MusicPlayer-1.0.jar gui.Main
