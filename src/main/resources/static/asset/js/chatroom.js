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
    const socket = new SockJS('/ws'); // SockJS를 사용하여 WebSocket 연결
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        // 메시지를 구독하여 처리
        stompClient.subscribe('/sub/chat/' + chatRoomId, function (messageOutput) {
            let message;
            try {
                message = JSON.parse(messageOutput.body);
            } catch (e) {
                message = { message: messageOutput.body, sender: "System" };
            }
            showMessageOutput(message);
        }, function (error) {
            console.error("오류:", error);
        });
        joinChatRoom();
    }, function (error) {
        console.error("오류:", error);
    });
}

function sendMessage() {
    var message = $('#message').val();
    try {
        stompClient.send("/pub/chat/" + chatRoomId, {}, JSON.stringify({'message': message, 'sender': userName}));
    } catch (e) {
        console.error("오류:", e);
    }
    $('#message').val(''); // 메시지 전송 후 입력 필드 비우기
}

function showMessageOutput(messageOutput) {
    let messageText;
    if (messageOutput.sender === "System") {
        messageText = messageOutput.message;
    } else {
        messageText = messageOutput.sender + ": " + messageOutput.message;
    }
    appendMessage(messageText);
}

function appendMessage(messageText) {
    const chatBoxContent = $('#chat-box-content');
    const messageElement = $('<div class="chat-box-message">')
        .append(
            $('<div class="chat-box-message-content">')
                .append(
                    $('<div class="chat-box-message-text">')
                        .append($('<p>').text(messageText))
                )
                .append(
                    $('<div class="chat-box-message-time">')
                        .append($('<p>').text(new Date().toLocaleTimeString()))
                )
        );
    chatBoxContent.append(messageElement);
    chatBoxContent.scrollTop(chatBoxContent.prop("scrollHeight"));
}

function joinChatRoom() {
    postAjax(`/chatroom/join?chatRoomId=${chatRoomId}&userName=${userName}`, null, function(response) {
        if (response && response.sender && response.message) {
            stompClient.send("/pub/chat/" + chatRoomId, {}, JSON.stringify(response));
        } else {
            console.log("response 객체에 필요한 데이터가 없습니다.");
        }
    }, function(error) {
        console.error("오류:", error);
    });
}

function leaveChatRoom() {
    postAjax(`/chatroom/leave?chatRoomId=${chatRoomId}&userName=${userName}`, null, function(response) {
        if (response && response.sender && response.message) {
            stompClient.send("/pub/chat/" + chatRoomId, {}, JSON.stringify(response));
            if(stompClient) {
                stompClient.disconnect(function() {
                    alert('채팅방을 나갔습니다.');
                    window.location.href = '/';
                });
            }
        } else {
            console.log("response 객체에 필요한 데이터가 없습니다.");
        }
    }, function(error) {
        console.error("오류:", error);
    });
}
