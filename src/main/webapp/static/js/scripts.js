String.prototype.format = function () {
    var args = arguments;
    return this.replace(/{(\d+)}/g, function (match, number) {
        return typeof args[number] != 'undefined' ? args[number] : match;
    });
};

function addAnswer(e) {
    e.preventDefault(); // Prevent the default form submission

    var queryString = $(".submit-write").serialize(); // Serialize the form data
    var url = $(".submit-write").attr("action"); // Get the form action URL

    $.ajax({
        type: 'POST',
        url: url,
        data: queryString,
        dataType: 'json',

        success: function (data) {
            // Assuming the response is in JSON format with necessary data
            var answerTemplate = $("#answerTemplate").html();

            data.forEach(function (comment) {
                var formattedTemplate = answerTemplate.format(
                    comment.writer.name,
                    comment.createdAt,
                    comment.contents,
                    comment.id
                );
                $(".qna-comment-slipp-articles form.submit-write").before(formattedTemplate); // Append each new comment before the comment form
            });

            $("textarea[name=contents]").val(""); // Clear the textarea
        },
        error: function (e) {
            console.log(e);
            alert("Error adding comment. Please try again.");
        }
    });
}

// Function to handle deleting a comment
function deleteAnswer(e) {
    e.preventDefault(); // Prevent the default form submission

    var deleteBtn = $(this);
    var url = deleteBtn.closest("form").attr("action"); // Get the form action URL

    $.ajax({
        method: 'DELETE',
        url: url,
        dataType: 'text',
        success: function () {
            console.log("Comment deleted successfully");
            deleteBtn.closest("article").remove(); // Remove the comment from the DOM
        },
        error: function (e) {
            if (e.status === 403) {
                alert("You do not have permission to delete this comment.");
                return;
            }
            console.log(e);
        }
    });
}

// Function to handle editing a post
function editPost(e) {
    e.preventDefault(); // Prevent the default link click

    var editLink = $(this);
    var url = editLink.attr("href"); // Get the edit URL

    $.ajax({
        method: 'GET',
        url: url,
        success: function () {
            window.location.href = url; // Redirect to the edit page on successful permission check
        },
        error: function (e) {
            if (e.status === 403) {
                alert("You do not have permission to edit this post.");
                return;
            }
            console.log(e);
        }
    });
}

function deletePost(e) {
    e.preventDefault(); // Prevent the default form submission

    var deleteForm = $(this);
    var url = deleteForm.attr("action"); // Get the form action URL

    $.ajax({
        type: 'POST',
        url: url,
        data: deleteForm.serialize(),
        success: function () {
            window.location.href = "/"; // Redirect to the home page on successful delete
        },
        error: function (e) {
            if (e.status === 403) {
                alert("You do not have permission to delete this post.");
                return;
            } else if (e.status === 400) {
                alert("다른 사람의 댓글이 있는 글은 지울 수 없습니다.");
                return;
            }
            console.log(e);
        }
    });
}

function loadComments() {
    $.ajax({
        url: `/api/posts/${postId}/comments?page=${currentPage}&size=15`,
        method: 'GET',
        success: function(response) {
            const comments = response.content;
            comments.forEach(comment => {
                $('#commentList').append(`
                    <article class="article" id="${comment.id}">
                        <div class="article-header">
                            <div class="article-header-thumb">
                                <img src="https://graph.facebook.com/v2.3/1324855987/picture" class="article-author-thumb" alt="">
                            </div>
                            <div class="article-header-text">
                                <a href="#" class="article-author-name">${comment.writer.name}</a>
                                <div class="article-header-time">${comment.createdAt}</div>
                            </div>
                        </div>
                        <div class="article-doc comment-doc">
                            <p>${comment.contents}</p>
                        </div>
                        <ul class="article-util-list">
                            <li>
                                <form class="delete-answer-form" action="/posts/${postId}/comments/${comment.id}" method="POST">
                                    <input type="hidden" name="_method" value="DELETE">
                                    <button type="submit" class="delete-answer-button">삭제</button>
                                </form>
                            </li>
                        </ul>
                    </article>
                `);
            });

            if (!response.last) {
                $('#loadMoreComments').show();
            } else {
                $('#loadMoreComments').hide();
            }

            currentPage++;
        },
        error: function(error) {
            console.error('댓글을 불러오는 데 실패했습니다:', error);
        }
    });
}

// Event delegation for dynamically added elements
$(document).ready(function () {
    $(".submit-write button[type='submit']").on("click", addAnswer); // Bind addAnswer function to submit button
    $(".qna-comment-slipp-articles").on("click", ".delete-answer-form button[type='submit']", deleteAnswer); // Bind deleteAnswer function to delete buttons

    // Add handlers for post edit and delete forms
    $("#deletePostForm").on("submit", deletePost);

    // Bind editPost function to the edit link
    $("#editPostLink").on("click", editPost);

    $("#loadMoreComments").click(function() {
        loadComments();
    })
});
