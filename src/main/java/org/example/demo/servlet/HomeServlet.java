package org.example.demo.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.demo.HttpMethod;
import org.example.demo.exception.InternalServerError;
import org.example.demo.handler.HomeHandler;
import org.example.demo.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet(name = "homeServlet", urlPatterns = "/")
public class HomeServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(HomeServlet.class);
    private HomeHandler homeHandler;
    private Router router;

    @Override
    public void init() {
        this.homeHandler = (HomeHandler) getServletContext().getAttribute("homeHandler");
        router = new Router();
        router.addRoute(HttpMethod.GET, "^/$", homeHandler::handleGetPosts);
        logger.info("HomeHandler {}", homeHandler);
        logger.info("HomeServlet.init finish!");
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (!router.route(request, response)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IOException e) {
            throw new InternalServerError(e.getMessage());
        }
    }
}
