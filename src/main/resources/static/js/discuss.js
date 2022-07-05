function like(obj, entityType, entityId, toUserId, postId) {
    $.post(
        CONTEXT_PATH + '/like',
        {'entityType': entityType, 'entityId': entityId, 'toUserId': toUserId, 'postId': postId},
        function (resp) {
            if (resp.code == 200) {
                $(obj).children('b').text(resp.data.likeStatus == 1 ? '已赞':'赞')
                $(obj).children('i').text(resp.data.likeCount)
            } else {
                if (typeof(resp) == 'string') {
                        resp = JSON.parse(resp)
                    }
                    alert(resp.msg)
            }
        }
    )
}

function addComment(obj) {
    var form = $(obj).parent().parent()
    var content = form.find("[name='content']").val().trim()
    if (content === '' || content.length === 0) {
        alert("请输入评论内容！")
        return;
    }
    form.submit()
}

function setType(obj, postId, type) {
    type = type == 0 ? 1 : 0;
    $.post(
        CONTEXT_PATH + '/discuss-post/' + postId + '/type/' + type,
        function (resp) {
            if (resp.code == 200) {
                $(obj).text(type == 0 ? '置顶' : '取消置顶')
            } else {
                if (typeof(resp) == 'string') {
                        resp = JSON.parse(resp)
                    }
                    alert(resp.msg)
            }
        }
    )
}

function setStatus(obj, postId, status) {
    if (status == 2) {
        var res = confirm('你确定要删除该帖子吗？')
        if (!res) {
            return;
        }
    } else {
        status = status == 0 ? 1 : 0;
    }
    $.post(
        CONTEXT_PATH + '/discuss-post/' + postId + '/status/' + status,
        function (resp) {
            if (resp.code == 200) {
                if (status == 2) {
                    window.location.href = CONTEXT_PATH
                } else {
                    $(obj).text(status == 0 ? '加精' : '取消加精')
                }
            } else {
                if (typeof(resp) == 'string') {
                        resp = JSON.parse(resp)
                    }
                    alert(resp.msg)
            }
        }
    )
}