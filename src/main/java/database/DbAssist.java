package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DbAssist {

    private static final String AUDIO_BASE_PATH = "data/audiofiles/";

    public static boolean addSong(String title, String artist, String language, String genre, double rating, String fileName) {
        try (Connection conn = DbManager.getConnection()) {

            title = formatText(title);
            artist = formatText(artist);
            language = formatText(language);
            genre = formatText(genre);

            int artistId = getOrCreateId(conn, "artists", artist);
            int languageId = getOrCreateId(conn, "languages", language);
            int genreId = getOrCreateId(conn, "genres", genre);

            if (artistId == -1 || languageId == -1 || genreId == -1) return false;

            String sql = "INSERT INTO songs (title, artist_id, language_id, genre_id, rating, file_name) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, title);
            ps.setInt(2, artistId);
            ps.setInt(3, languageId);
            ps.setInt(4, genreId);
            ps.setDouble(5, rating);
            ps.setString(6, fileName);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("❌ Failed to add song: " + title + " - " + e.getMessage());
            return false;
        }
    }

    public static boolean updateSongById(int id, String title, String artist, String language, String genre, double rating) {
        String sql = "UPDATE songs SET title = ?, artist_id = ?, language_id = ?, genre_id = ?, rating = ? WHERE id = ?";

        try (Connection conn = DbManager.getConnection()) {

            title = formatText(title);
            artist = formatText(artist);
            language = formatText(language);
            genre = formatText(genre);

            int artistId = getOrCreateId(conn, "artists", artist);
            int languageId = getOrCreateId(conn, "languages", language);
            int genreId = getOrCreateId(conn, "genres", genre);

            if (artistId == -1 || languageId == -1 || genreId == -1) return false;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, title);
            stmt.setInt(2, artistId);
            stmt.setInt(3, languageId);
            stmt.setInt(4, genreId);
            stmt.setDouble(5, rating);
            stmt.setInt(6, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Failed to update song with ID " + id + ": " + e.getMessage());
            return false;
        }
    }

    private static int getOrCreateId(Connection conn, String table, String name) throws SQLException {
        String selectSql = "SELECT id FROM " + table + " WHERE name = ?";
        PreparedStatement selectStmt = conn.prepareStatement(selectSql);
        selectStmt.setString(1, name);
        ResultSet rs = selectStmt.executeQuery();

        if (rs.next()) return rs.getInt("id");

        // If not found, insert
        String insertSql = "INSERT INTO " + table + " (name) VALUES (?)";
        PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        insertStmt.setString(1, name);
        insertStmt.executeUpdate();
        ResultSet keys = insertStmt.getGeneratedKeys();
        return keys.next() ? keys.getInt(1) : -1;
    }

    public static int importFromCSV(String path) {
        int added = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length != 6) {
                    System.out.println("⚠️ Skipping malformed line: " + line);
                    continue;
                }

                String title = parts[0].trim();
                String artistFull = parts[1].trim();
                String artist = artistFull.contains("-") ? artistFull.split("-")[0].trim() : artistFull;
                String language = parts[2].trim();
                String genre = parts[3].trim();
                double rating = Double.parseDouble(parts[4].trim());
                String fileName = AUDIO_BASE_PATH + title + ".mp3";

                boolean success = addSong(title, artist, language, genre, rating, fileName);
                if (success) added++;
            }

        } catch (Exception e) {
            System.out.println("❌ Failed to read file: " + e.getMessage());
        }

        return added;
    }

    public static boolean deleteSongById(int songId) {
        String deleteSql = "DELETE FROM songs WHERE id = ?";
        String fetchIdsSql = "SELECT artist_id, language_id, genre_id FROM songs WHERE id = ?";

        try (Connection conn = DbManager.getConnection()) {
            conn.setAutoCommit(false);

            int artistId = -1, languageId = -1, genreId = -1;
            try (PreparedStatement fetchStmt = conn.prepareStatement(fetchIdsSql)) {
                fetchStmt.setInt(1, songId);
                ResultSet rs = fetchStmt.executeQuery();
                if (rs.next()) {
                    artistId = rs.getInt("artist_id");
                    languageId = rs.getInt("language_id");
                    genreId = rs.getInt("genre_id");
                } else {
                    return false;
                }
            }

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, songId);
                int affected = deleteStmt.executeUpdate();
                if (affected == 0) return false;
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM artists WHERE id = " + artistId + " AND NOT EXISTS (SELECT 1 FROM songs WHERE artist_id = " + artistId + ")");
                stmt.executeUpdate("DELETE FROM languages WHERE id = " + languageId + " AND NOT EXISTS (SELECT 1 FROM songs WHERE language_id = " + languageId + ")");
                stmt.executeUpdate("DELETE FROM genres WHERE id = " + genreId + " AND NOT EXISTS (SELECT 1 FROM songs WHERE genre_id = " + genreId + ")");
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Error deleting song with ID " + songId + ": " + e.getMessage());
            return false;
        }
    }


    // Capitalize the first letter of every word
    private static String formatText(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] words = input.toLowerCase().trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty())
                result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return result.toString().trim();
    }
}