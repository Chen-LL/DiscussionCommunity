$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
			CONTEXT_PATH + '/follow',
			{'entityType': 3, 'entityId': $(btn).val()},
			function (resp) {
				if (resp.code == 200) {
					$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
					$('#follower').text(resp.data)
				} else {
					alert(resp.msg)
				}
			}
		)
	} else {
		// 取消关注
		$.post(
			CONTEXT_PATH + '/follow/undo',
			{'entityType': 3, 'entityId': $(btn).val()},
			function (resp) {
				if (resp.code == 200) {
					$(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
					$('#follower').text(resp.data)
				} else {
					alert(resp.msg)
				}
			}
		)
	}
}