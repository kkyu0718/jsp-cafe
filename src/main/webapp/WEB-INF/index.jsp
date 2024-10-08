<%@ page import="org.example.demo.domain.Post" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="kr" xmlns:jsp="http://java.sun.com/JSP/Page">


<jsp:include page="${pageContext.request.contextPath}/components/header.jsp"/>
<body>
<jsp:include page="${pageContext.request.contextPath}/components/nav.jsp"/>

<div class="container" id="main">
    <div class="col-md-12 col-sm-12 col-lg-10 col-lg-offset-1">
        <div class="panel panel-default qna-list">
            <ul class="list">
                <%
                    List<Post> posts = (List<Post>) request.getAttribute("posts");
                    for (Post post : posts) {
                %>
                <li>
                    <div class="wrap">
                        <div class="main">
                            <strong class="subject">
                                <a href="${pageContext.request.contextPath}/posts/<%=post.getId()%>"><%=post.getTitle()%>
                                </a>
                            </strong>
                            <div class="auth-info">
                                <i class="icon-add-comment"></i>
                                <span class="time"><%=post.getCreatedAt()%></span>
                                <a href="${pageContext.request.contextPath}/users/<%=post.getWriter().getId()%>"
                                   class="author"><%=post.getWriter().getName()%>
                                </a>
                            </div>
                            <div class="reply" title="댓글">
                                <i class="icon-reply"></i>
                                <%--                                <span class="point">댓글 개수</span>--%>
                            </div>
                        </div>
                    </div>
                </li>
                <%
                    }
                %>
            </ul>
            <div class="row">
                <div class="col-md-3"></div>
                <div class="col-md-6 text-center">
                    <ul class="pagination center-block" style="display:inline-block;">
                        <li<c:if test="${currentPage == 1}"> class="disabled"</c:if>>
                            <a href="<c:if test="${currentPage > 1}">${pageContext.request.contextPath}?page=${currentPage - 1}</c:if>"
                               aria-label="Previous">
                                <span aria-hidden="true">&laquo;</span>
                            </a>
                        </li>

                        <c:choose>
                            <c:when test="${totalPages <= 5}">
                                <c:set var="startPage" value="1"/>
                                <c:set var="endPage" value="${totalPages}"/>
                            </c:when>
                            <c:otherwise>
                                <c:set var="startPage"
                                       value="${Math.max(1, Math.min(currentPage - 2, totalPages - 4))}"/>
                                <c:set var="endPage" value="${Math.min(totalPages, startPage + 4)}"/>
                            </c:otherwise>
                        </c:choose>

                        <c:forEach begin="${startPage}" end="${endPage}" var="i">
                            <li<c:if test="${currentPage == i}"> class="active"</c:if>>
                                <a href="${pageContext.request.contextPath}?page=${i}">${i}</a>
                            </li>
                        </c:forEach>

                        <c:if test="${endPage < totalPages}">
                            <li class="disabled"><span>...</span></li>
                            <li><a href="${pageContext.request.contextPath}?page=${totalPages}">${totalPages}</a></li>
                        </c:if>

                        <li<c:if test="${currentPage == totalPages}"> class="disabled"</c:if>>
                            <a href="<c:if test="${currentPage < totalPages}">${pageContext.request.contextPath}?page=${currentPage + 1}</c:if>"
                               aria-label="Next">
                                <span aria-hidden="true">&raquo;</span>
                            </a>
                        </li>
                    </ul>
                </div>
                <div class="col-md-3 qna-write">
                    <a href="${pageContext.request.contextPath}/posts/form" class="btn btn-primary pull-right"
                       role="button">질문하기</a>
                </div>
            </div>
        </div>
    </div>
</div>

<!--login modal-->
<!--
<div id="loginModal" class="modal fade" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
  <div class="modal-content">
      <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
          <h2 class="text-center"><img src="https://lh5.googleusercontent.com/-b0-k99FZlyE/AAAAAAAAAAI/AAAAAAAAAAA/eu7opA4byxI/photo.jpg?sz=100" class="img-circle"><br>Login</h2>
      </div>
      <div class="modal-body">
          <form class="form col-md-12 center-block">
              <div class="form-group">
                  <label for="userId">사용자 아이디</label>
                  <input class="form-control" name="userId" placeholder="User ID">
              </div>
              <div class="form-group">
                  <label for="password">비밀번호</label>
                  <input type="password" class="form-control" name="password" placeholder="Password">
              </div>
              <div class="form-group">
                  <button class="btn btn-primary btn-lg btn-block">로그인</button>
                  <span class="pull-right"><a href="#registerModal" role="button" data-toggle="modal">회원가입</a></span>
              </div>
          </form>
      </div>
      <div class="modal-footer">
          <div class="col-md-12">
          <button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
      </div>  
      </div>
  </div>
  </div>
</div>
-->

<!--register modal-->
<!--
<div id="registerModal" class="modal fade" tabindex="-1" role="dialog" aria-hidden="true">
  <div class="modal-dialog">
  <div class="modal-content">
      <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
          <h2 class="text-center"><img src="https://lh5.googleusercontent.com/-b0-k99FZlyE/AAAAAAAAAAI/AAAAAAAAAAA/eu7opA4byxI/photo.jpg?sz=100" class="img-circle"><br>회원가입</h2>
      </div>
      <div class="modal-body">
          <form class="form col-md-12 center-block">
              <div class="form-group">
                  <label for="userId">사용자 아이디</label>
                  <input class="form-control" id="userId" name="userId" placeholder="User ID">
              </div>
              <div class="form-group">
                  <label for="password">비밀번호</label>
                  <input type="password" class="form-control" id="password" name="password" placeholder="Password">
              </div>
              <div class="form-group">
                  <label for="name">이름</label>
                  <input class="form-control" id="name" name="name" placeholder="Name">
              </div>
              <div class="form-group">
                  <label for="email">이메일</label>
                  <input type="email" class="form-control" id="email" name="email" placeholder="Email">
              </div>
            <div class="form-group">
              <button class="btn btn-primary btn-lg btn-block">회원가입</button>
            </div>
          </form>
      </div>
      <div class="modal-footer">
          <div class="col-md-12">
          <button class="btn" data-dismiss="modal" aria-hidden="true">Cancel</button>
      </div>  
      </div>
  </div>
  </div>
</div>
-->

<!-- script references -->
<jsp:include page="/components/footer.jsp"/>

</body>
</html>