package org.example.demo.repository;

import org.example.demo.db.DbConfig;
import org.example.demo.domain.Comment;
import org.example.demo.domain.Post;
import org.example.demo.domain.User;
import org.example.demo.model.PostCreateDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostRepository.class);
    private DbConfig dbConfig;

    public PostRepository(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public Optional<Post> getPost(Long postId) {
        String sql = "SELECT p.*,\n" +
                "       pu.id AS post_user_id, pu.user_id AS post_user_userid, pu.name AS post_user_name,\n" +
                "       c.id AS comment_id, c.contents AS comment_contents, c.created_at AS comment_created_at, c.is_present,\n" +
                "       cu.id AS comment_user_id, cu.user_id AS comment_user_userid, cu.name AS comment_user_name\n" +
                "FROM posts p\n" +
                "JOIN users pu ON p.writer_id = pu.id\n" +
                "LEFT JOIN (\n" +
                "    SELECT *\n" +
                "    FROM comments\n" +
                "    WHERE post_id = ? AND is_present = true\n" +
                "    ORDER BY created_at DESC\n" +
                "    LIMIT 5\n" +
                ") c ON p.id = c.post_id\n" +
                "LEFT JOIN users cu ON c.writer_id = cu.id\n" +
                "WHERE p.id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, postId);
            pstmt.setLong(2, postId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(createPostWithCommentsFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public void addPost(PostCreateDao dao) {
        String sql = "INSERT INTO posts (writer_id, title, contents, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, dao.getWriter());
            pstmt.setString(2, dao.getTitle());
            pstmt.setString(3, dao.getContents());
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                long id = generatedKeys.getLong(1);
                logger.info("Generated Post ID: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, u.user_id, u.name FROM posts p JOIN `users` u ON p.writer_id = u.id WHERE p.is_present = true ORDER BY p.created_at DESC";

        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                posts.add(createPostFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    private Post createPostFromResultSet(ResultSet rs) throws SQLException {
        User writer = new User(
                rs.getLong("writer_id"),
                rs.getString("user_id"),
                null, // password는 보안상 가져오지 않음
                rs.getString("name"),
                null  // email도 필요 없다면 가져오지 않음
        );

        return new Post(
                rs.getLong("id"),
                writer,
                rs.getString("title"),
                rs.getString("contents"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                null
        );
    }

    private Post createPostWithCommentsFromResultSet(ResultSet rs) throws SQLException {
        User writer = new User(
                rs.getLong("post_user_id"),  // 'writer_id' 대신 'post_user_id' 사용
                rs.getString("post_user_userid"),  // 'user_id' 대신 'post_user_userid' 사용
                null,
                rs.getString("post_user_name"),  // 'name' 대신 'post_user_name' 사용
                null
        );

        Long postId = rs.getLong("id");
        List<Comment> comments = new ArrayList<>();

        // Post 정보 설정
        Post post = new Post(
                postId,
                writer,
                rs.getString("title"),
                rs.getString("contents"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                comments
        );

        // Comment 정보 설정
        do {
            Long commentId = rs.getLong("comment_id");
            if (!rs.wasNull()) {  // commentId가 NULL이 아닌 경우에만 Comment 객체 생성
                User commentWriter = new User(
                        rs.getLong("comment_user_id"),
                        rs.getString("comment_user_userid"),
                        null,
                        rs.getString("comment_user_name"),
                        null
                );
                Comment comment = new Comment(
                        commentId,
                        postId,
                        commentWriter,  // 댓글 작성자 정보 사용
                        rs.getString("comment_contents"),
                        rs.getTimestamp("comment_created_at").toLocalDateTime()
                );
                comments.add(comment);
            }
        } while (rs.next());

        return post;
    }


    public void updatePost(Long id, String title, String contents) {
        String sql = "UPDATE posts SET title = ?, contents = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, contents);
            pstmt.setLong(3, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletePost(Long id) {
        String postSql = "UPDATE posts SET is_present = false WHERE id = ?";
        String commentSql = "UPDATE comments SET is_present = false WHERE post_id = ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(postSql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting post", e);
            e.printStackTrace();
        }

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(commentSql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting comments", e);
            e.printStackTrace();
        }
    }

    public List<Post> getPostsPaged(long page, int limit) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, u.user_id, u.name " +
                "FROM posts p " +
                "JOIN users u ON p.writer_id = u.id " +
                "WHERE p.is_present = true " +
                "ORDER BY p.created_at DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            pstmt.setLong(2, (page - 1) * limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(createPostFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching paged posts", e);
        }
        return posts;
    }

    public List<Post> getPagedPostWithCoveringIndex(long page, int limit) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, u.user_id, u.name " +
                "FROM ( " +
                "    SELECT id, writer_id, created_at " +
                "    FROM posts " +
                "    WHERE is_present = true " +
                "    ORDER BY created_at DESC " +
                "    LIMIT ? OFFSET ? " +
                ") AS subquery " +
                "JOIN posts p ON subquery.id = p.id " +
                "JOIN users u ON p.writer_id = u.id " +
                "ORDER BY p.created_at DESC";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            pstmt.setLong(2, (page - 1) * limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(createPostFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching paged posts", e);
        }
        return posts;
    }

    public int getTotalPostCount() {
        String sql = "SELECT COUNT(*) as cnt FROM posts p WHERE p.is_present = true";
        int cnt = 0;

        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                cnt = rs.getInt("cnt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cnt;
    }
}