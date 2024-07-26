import { getAjax, postAjax, putAjax, deleteAjax, getParameterByName } from "./util.js";

$(document).ready(function() {

    $("#joinForm").submit(function(e) {
        e.preventDefault();
        join();
    });

});

function join() {
    const data = {
        username: $("#username").val(),
        password: $("#password").val(),
        email: $("#email").val()
    };

    console.log(data);

    postAjax("/user/join", data, function(response) {
        alert("회원가입이 완료되었습니다.");
        window.location.href = "/login";
    }, function(error) {
        alert(JSON.stringify(error));
    });
}

