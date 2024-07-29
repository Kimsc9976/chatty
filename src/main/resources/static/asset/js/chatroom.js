import { getAjax, postAjax, putAjax, deleteAjax, getParameterByName } from "./util.js";

let stompClient = null;
let chatRoomId = null;
let userName = null;

$(document).ready(function () {
    chatRoomId = $('#chatRoomId').val();
    userName = $('#userName').val();

    connect();

    $('#send').on('click', sendMessage);
    $('#exit').on('click', leaveChatRoom);
});

function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/sub/chat/' + chatRoomId, function (messageOutput) {
            console.log("Received raw message:", messageOutput);
            showMessageOutput(JSON.parse(messageOutput.body));
        });
        joinChatRoom();
    });
}

function sendMessage() {
    var message = $('#message').val();
    stompClient.send("/pub/chat/" + chatRoomId, {}, JSON.stringify({'message': message, 'sender': userName}));
    $('#message').val('');
}

function showMessageOutput(messageOutput) {
    const chatBoxContent = $('#chat-box-content');
    const messageElement = $('<div class="chat-box-message">')
        .append(
            $('<div class="chat-box-message-content">')
                .append(
                    $('<div class="chat-box-message-text">')
                        .append($('<p>').text(messageOutput.sender))
                        .append($('<p>').text(messageOutput.message))
                )
                .append(
                    $('<div class="chat-box-message-time">')
                        .append($('<p>').text(new Date().toLocaleTimeString()))
                )
        );
    chatBoxContent.append(messageElement);
    // 스크롤을 가장 아래로 이동
    chatBoxContent.scrollTop(chatBoxContent.prop("scrollHeight"));
}

function joinChatRoom() {
    postAjax(`/chatroom/join?chatRoomId=${chatRoomId}&userName=${userName}`, null, function(response) {
        console.log(response);
    }, function(error) {
        console.log(error);
    });
}

function leaveChatRoom() {
    postAjax(`/chatroom/leave?chatRoomId=${chatRoomId}&userName=${userName}`, null, function(response) {
        console.log(response);
    }, function(error) {
        console.log(error);
    });
    if (stompClient) {
        stompClient.disconnect(function() {
            alert('채팅방을 나갔습니다.');
            window.location.href = '/';
        });
    }
}
