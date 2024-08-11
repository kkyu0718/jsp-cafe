package org.example.demo.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.demo.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class HomeHandler {
    private static final Logger logger = LoggerFactory.getLogger(HomeHandler.class);
    private final PostRepository postRepository;

    public HomeHandler(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public void handleGetPosts(HttpServletRequest request, HttpServletResponse response, List<String> pathVariables) throws ServletException, IOException {
        logger.info("handleGetPosts");
        long page = Long.parseLong(request.getParameter("page") == null ? "1" : request.getParameter("page"));
        int limit = Integer.parseInt(request.getParameter("limit") == null ? "15" : request.getParameter("limit"));

        long totalPages = (long) Math.ceil(postRepository.getTotalPostCount() / (double) limit);
        logger.info("page: {}, limit: {}", page, limit);
        logger.info("totalPages: {}", totalPages);

        request.setAttribute("posts", postRepository.getPagedPostWithCoveringIndex(page, limit));
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.getRequestDispatcher("/WEB-INF/index.jsp").forward(request, response);
    }
}
