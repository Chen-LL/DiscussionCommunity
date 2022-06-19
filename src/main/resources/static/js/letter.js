$(function(){
	$("#sendBtn").click(send_letter);
});

function send_letter() {
	$("#sendModal").modal("hide");
	var username = $('#recipient-name').val()
	var content = $('#message-text').val()
	$.post(
		CONTEXT_PATH + "/message",
		{'username': username, 'content': content},
		function (resp) {
			$("#hintBody").text(resp.msg)
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload()
			}, 2000);
		}
	)


}