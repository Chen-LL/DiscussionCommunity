$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    $("#publishModal").modal("hide");
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();
    var url = CONTEXT_PATH + "/discuss-post";
    var data = {"title": title, "content": content};
	$.post(url, data, function (resp) {
		if (typeof(resp) == 'string') {
			resp = JSON.parse(resp)
		}
		$("#hintBody").text(resp.msg);
		$("#hintModal").modal("show");
		setTimeout(function(){
			$("#hintModal").modal("hide");
			if(resp.code == 200) {
				window.location.reload();
			}
		}, 2000);
	})

}