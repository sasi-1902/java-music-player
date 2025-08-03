package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbManager {

    private static final String DB_URL = "jdbc:sqlite:data/songs.db";

    // Connect to the SQLite DB with a success/fail message
    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(DB_URL);
            System.out.println("✅ Connected to database.");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
            return null;
        }
    }

    // Used silently by backend code
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Create all necessary tables if they don't exist
    public static void createSchemaIfNeeded() {
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {

            // Artists table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS artists (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                );
            """);

            // Languages table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS languages (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                );
            """);

            // Genres table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS genres (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                );
            """);

            // Songs table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS songs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    artist_id INTEGER NOT NULL,
                    language_id INTEGER NOT NULL,
                    genre_id INTEGER NOT NULL,
                    rating REAL,
                    file_name TEXT NOT NULL,
                    FOREIGN KEY (artist_id) REFERENCES artists(id),
                    FOREIGN KEY (language_id) REFERENCES languages(id),
                    FOREIGN KEY (genre_id) REFERENCES genres(id)
                );
            """);

            System.out.println("✅ Database schema created or already exists.");

        } catch (SQLException e) {
            System.err.println("❌ Error creating schema: " + e.getMessage());
        }
    }
}