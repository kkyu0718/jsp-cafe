package org.example.demo.repository;

import org.example.demo.domain.Post;
import org.example.demo.model.PostCreateDao;
import org.example.demo.model.UserCreateDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoveringIndexTest extends RepositoryTestSupport {
    UserRepository userRepository;
    PostRepository postRepository;

    @BeforeEach
    void setUp() {
        setDb();
        userRepository = new UserRepository(dbConfig);
        postRepository = new PostRepository(dbConfig);
        insertTestData();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    void compareQueryPerformance() throws SQLException {
        int iterations = 100;
        long page = 1000;
        int limit = 10;

        long originalTime = 0;
        long coveringIndexTime = 0;

        for (int i = 0; i < iterations; i++) {
            originalTime += measureOriginalQuery(page, limit);
            coveringIndexTime += measureCoveringIndexQuery(page, limit);
        }

        double avgOriginalTime = (double) originalTime / iterations;
        double avgCoveringIndexTime = (double) coveringIndexTime / iterations;

        System.out.println("Average time for original query: " + avgOriginalTime + " ms");
        System.out.println("Average time for covering index query: " + avgCoveringIndexTime + " ms");
        System.out.println("Performance improvement: " +
                ((avgOriginalTime - avgCoveringIndexTime) / avgOriginalTime * 100) + "%");

        // 결과가 동일한지 확인
        List<Post> originalPosts = postRepository.getPostsPaged(page, limit);
        List<Post> coveringIndexPosts = postRepository.getPagedPostWithCoveringIndex(page, limit);
        assertEquals(originalPosts.size(), coveringIndexPosts.size());
        for (int i = 0; i < originalPosts.size(); i++) {
            assertEquals(originalPosts.get(i).getId(), coveringIndexPosts.get(i).getId());
        }
    }

    private long measureOriginalQuery(long page, int limit) throws SQLException {
        long start = System.currentTimeMillis();
        postRepository.getPostsPaged(page, limit);
        return System.currentTimeMillis() - start;
    }

    private long measureCoveringIndexQuery(long page, int limit) throws SQLException {
        long start = System.currentTimeMillis();
        postRepository.getPagedPostWithCoveringIndex(page, limit);
        return System.currentTimeMillis() - start;
    }

    private void insertTestData() {
        // 사용자 추가
        for (int i = 0; i < 100; i++) {
            userRepository.addUser(new UserCreateDao("user" + i, "password" + i, "User " + i, "user" + i + "@example.com"));
        }

        // 게시물 추가
        for (int i = 0; i < 100000; i++) {
            PostCreateDao post = new PostCreateDao(
                    (long)(i % 100 + 1),  // writerId를 1부터 100까지의 값으로 설정
                    "Title " + i,
                    "Content " + i
            );
            postRepository.addPost(post);
        }
    }
}