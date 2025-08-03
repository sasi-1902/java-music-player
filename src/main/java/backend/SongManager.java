package backend;

import database.DbManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SongManager {

    /**
     * Fetch song IDs based on a category type and its value.
     * 
     * @param category "artist", "language", or "genre"
     * @param name     Name to match exactly (case-insensitive)
     * @return List of matching song IDs
     */
    public static List<Integer> getSongsByCategory(String category, String name) {
        List<Integer> songIds = new ArrayList<>();

        String joinTable = switch (category.toLowerCase()) {
            case "artist" -> "artists";
            case "language" -> "languages";
            case "genre" -> "genres";
            default -> null;
        };

        String column = switch (category.toLowerCase()) {
            case "artist" -> "artist_id";
            case "language" -> "language_id";
            case "genre" -> "genre_id";
            default -> null;
        };

        if (joinTable == null || column == null) return songIds;

        String sql = """
            SELECT s.id
            FROM songs s
            JOIN %s t ON s.%s = t.id
            WHERE LOWER(t.name) = ?
        """.formatted(joinTable, column);

        try (Connection conn = DbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name.toLowerCase());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                songIds.add(rs.getInt("id"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching songs by %s: %s".formatted(category, e.getMessage()));
        }

        return songIds;
    }
}