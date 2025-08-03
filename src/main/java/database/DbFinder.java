package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DbFinder {

    public static List<Integer> findSongIdsByTitle(String title) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id FROM songs WHERE LOWER(title) = ?";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title.toLowerCase());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching song IDs: " + e.getMessage());
        }

        return ids;
    }

    public static Map<String, String> searchSong(int id) {
        String sql = """
            SELECT s.title, s.file_name, a.name AS artist, l.name AS language, g.name AS genre, s.rating
            FROM songs s
            JOIN artists a ON s.artist_id = a.id
            JOIN languages l ON s.language_id = l.id
            JOIN genres g ON s.genre_id = g.id
            WHERE s.id = ?
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, String> songData = new HashMap<>();
                songData.put("Title", rs.getString("title"));
                songData.put("Artist", rs.getString("artist"));
                songData.put("Language", rs.getString("language"));
                songData.put("Genre", rs.getString("genre"));
                songData.put("Rating", String.valueOf(rs.getDouble("rating")));
                songData.put("file_name", rs.getString("file_name"));
                return songData;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error searching song by ID: " + e.getMessage());
        }

        return null;
    }

    public static List<Map<String, String>> searchSongsByTitleLike(String query) {
        List<Map<String, String>> songs = new ArrayList<>();
        String sql = """
            SELECT s.id, s.title, a.name AS artist
            FROM songs s
            JOIN artists a ON s.artist_id = a.id
            WHERE LOWER(s.title) LIKE ?
            ORDER BY 
                CASE
                    WHEN LOWER(s.title) LIKE ? THEN 1
                    WHEN LOWER(s.title) LIKE ? THEN 2
                    ELSE 3
                END,
                LENGTH(s.title) ASC
        """;

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String q = "%" + query.toLowerCase() + "%";
            String qStart = query.toLowerCase() + "%";
            String qMiddle = "% " + query.toLowerCase() + "%";

            stmt.setString(1, q);
            stmt.setString(2, qStart);
            stmt.setString(3, qMiddle);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, String> song = new HashMap<>();
                song.put("ID", String.valueOf(rs.getInt("id")));
                song.put("Title", rs.getString("title"));
                song.put("Artist", rs.getString("artist"));
                songs.add(song);
            }

        } catch (SQLException e) {
            System.err.println("❌ Search error: " + e.getMessage());
        }

        return songs;
    }

    public static List<String> getAllArtists() {
        return fetchSingleColumnValues("artists");
    }

    public static List<String> getAllLanguages() {
        return fetchSingleColumnValues("languages");
    }

    public static List<String> getAllGenres() {
        return fetchSingleColumnValues("genres");
    }

    public static List<Integer> findSongsByCategory(String categoryType, String value) {
        List<Integer> ids = new ArrayList<>();
        String sql = """
            SELECT s.id FROM songs s
            JOIN artists a ON s.artist_id = a.id
            JOIN languages l ON s.language_id = l.id
            JOIN genres g ON s.genre_id = g.id
            WHERE LOWER(%s.name) = ?
        """.formatted(switch (categoryType.toLowerCase()) {
            case "artist" -> "a";
            case "language" -> "l";
            case "genre" -> "g";
            default -> throw new IllegalArgumentException("Invalid category");
        });

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value.toLowerCase());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Category search error: " + e.getMessage());
        }

        return ids;
    }

    private static List<String> fetchSingleColumnValues(String tableName) {
        List<String> values = new ArrayList<>();
        String sql = "SELECT name FROM " + tableName + " ORDER BY name ASC";

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                values.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching from " + tableName + ": " + e.getMessage());
        }

        return values;
    }

    public static List<Integer> getAllSongs() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id FROM songs";
    
        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
    
        } catch (SQLException e) {
            System.err.println("❌ Error fetching all song IDs: " + e.getMessage());
        }
    
        return ids;
    }
}