package org.example.demo.repository;

import org.example.demo.db.DbConfig;
import org.example.demo.domain.Comment;
import org.example.demo.domain.User;
import org.example.demo.exception.InternalServerError;
import org.example.demo.model.CommentCreateDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommentRepository {
    private static final Logger logger = LoggerFactory.getLogger(CommentRepository.class);
    private DbConfig dbConfig;

    public CommentRepository(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public Comment saveComment(CommentCreateDao dao) {
        String sql = "INSERT INTO comments (post_id, writer_id, contents, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setLong(1, dao.getPostId());
            pstmt.setLong(2, dao.getWriterId());
            pstmt.setString(3, dao.getContents());
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                long id = generatedKeys.getLong(1);
                logger.info("Generated Comment ID: " + id);

                return getComment(id).get();
            } else {
                logger.error("Error getting saved comment");
                throw new InternalServerError("Error getting saved comment");
            }
        } catch (SQLException e) {
            logger.error("Error saving comments", e);
            throw new InternalServerError("Error saving comments " + e.getMessage());
        }
    }

    public void deleteComment(Long commentId) {
        String sql = "UPDATE comments SET is_present = false WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, commentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting comment", e);
            throw new InternalServerError("Error deleting comment " + e.getMessage());
        }
    }

    public Comment updateComment(Long commentId, String contents) {
        String sql = "UPDATE comments SET contents = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, contents);
            pstmt.setLong(2, commentId);
            pstmt.executeUpdate();

            return getComment(commentId).orElseThrow(() -> new InternalServerError("Error updating comment"));
        } catch (SQLException e) {
            logger.error("Error updating comment", e);
            throw new InternalServerError("Error updating comment " + e.getMessage());
        }
    }

    public Optional<Comment> getComment(Long commentId) {
        String sql = "SELECT c.*, u.user_id, u.name FROM comments c JOIN users u ON c.writer_id = u.id WHERE c.id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, commentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(createCommentFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting comment", e);
            throw new InternalServerError("Error getting comment " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Comment> getComments(Long postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.user_id, u.name FROM comments c JOIN users u ON c.writer_id = u.id WHERE c.post_id = ? ORDER BY c.created_at desc";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, postId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                comments.add(createCommentFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting comments", e);
        }
        return comments;
    }

    private Comment createCommentFromResultSet(ResultSet rs) throws SQLException {
        User writer = new User(
                rs.getLong("writer_id"),
                rs.getString("user_id"),
                null, // password는 보안상 가져오지 않음
                rs.getString("name"),
                null  // email도 필요 없다면 가져오지 않음
        );

        return new Comment(
                rs.getLong("id"),
                rs.getLong("post_id"),
                writer,
                rs.getString("contents"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    public List<Comment> getMoreComments(Long postId, Long lastCommentId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.user_id, u.name " +
                "FROM comments c " +
                "JOIN users u ON c.writer_id = u.id " +
                "WHERE c.post_id = ? AND c.id < ? AND c.is_present = true " +
                "ORDER BY c.created_at DESC;";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, postId);
            pstmt.setLong(2, lastCommentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                comments.add(createCommentFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting comments", e);
        }
        return comments;
    }

    public List<Comment> getPagedComments(long postId, long lastCommentId, int limit) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.user_id, u.name FROM comments c JOIN users u ON c.writer_id = u.id WHERE c.post_id = ? AND c.id > ? ORDER BY c.created_at desc LIMIT ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, postId);
            pstmt.setLong(2, lastCommentId);
            pstmt.setLong(3, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                comments.add(createCommentFromResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting comments", e);
        }
        return comments;
    }
}