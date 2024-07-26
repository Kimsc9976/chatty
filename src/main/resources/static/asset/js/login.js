import { getAjax, postAjax, putAjax, deleteAjax, getParameterByName } from "./util.js";

$(document).ready(function() {

    $("#loginForm").submit(function(e) {
        e.preventDefault();
        login();
    });

});


function login() {
    const username = $("#username").val();
    const password = $("#password").val();
    const data = {
        username: username,
        password: password
    };
    postAjax("/login", data, function(response) {
        if (response.status === "success") {
            alert("로그인 성공");
            window.location.href = "/";
        } else {
            alert(response.message);
        }
    });
}
