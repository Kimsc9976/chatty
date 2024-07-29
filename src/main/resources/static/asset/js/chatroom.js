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
        console.log('====== 연결 성공 ======');
        console.log('연결된 프레임: ' + frame);
        console.log('=====================');
        // 메시지를 구독하여 처리
        stompClient.subscribe('/sub/chat/' + chatRoomId, function (messageOutput) {
            console.log("====== 메시지 수신 성공 ======");
            console.log("수신된 원시 메시지:", messageOutput);
            let message;
            try {
                message = JSON.parse(messageOutput.body);
                console.log("파싱된 메시지:", message);
            } catch (e) {
                console.error("====== 메시지 파싱 실패 ======");
                console.error("오류:", e);
                console.error("====== 메시지 파싱 실패 ======");
                message = { message: messageOutput.body, sender: "System" };
            }
            console.log("처리된 메시지: ", message);
            showMessageOutput(message);
        }, function (error) {
            console.error("====== 메시지 구독 실패 ======");
            console.error("오류:", error);
            console.error("====== 메시지 구독 실패 ======");
        });

        // 채팅방에 입장한 후 서버에 알리기
        joinChatRoom();
    }, function (error) {
        console.error("====== STOMP 연결 실패 ======");
        console.error("오류:", error);
        console.error("====== STOMP 연결 실패 ======");
    });
}

function sendMessage() {
    var message = $('#message').val();
    try {
        stompClient.send("/pub/chat/" + chatRoomId, {}, JSON.stringify({'message': message, 'sender': userName}));
        console.log('====== 메시지 전송 성공 ======');
        console.log('전송된 메시지:', message);
        console.log('==========================');
    } catch (e) {
        console.error("====== 메시지 전송 실패 ======");
        console.error("오류:", e);
        console.error("====== 메시지 전송 실패 ======");
    }
    $('#message').val(''); // 메시지 전송 후 입력 필드 비우기
}

function showMessageOutput(messageOutput) {
    console.log("====== 메시지 출력 함수 호출됨 ======");
    console.log("전달된 메시지:", messageOutput); // 추가 로그
    if (!messageOutput) {
        console.error("====== 메시지 출력 실패 ======");
        console.error("messageOutput이 undefined 또는 null입니다.");
        console.error("====== 메시지 출력 실패 ======");
        return;
    }

    console.log("====== 메시지 출력 성공 ======");
    console.log("출력할 메시지:", messageOutput); // 디버깅을 위한 추가 로그
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
        console.log("====== 채팅방 입장 성공 ======");
        console.log("Response 객체: ", response); // response 객체 구조 확인
        if (response && response.sender && response.message) {
            console.log(response.sender + "님이 입장하셨습니다.");
            console.log(response.message);
            // 입장 메시지를 서버에 전송 (입장한 사실을 다른 클라이언트에게 알리기 위함)
            stompClient.send("/pub/chat/" + chatRoomId, {}, JSON.stringify(response));
        } else {
            console.log("response 객체에 필요한 데이터가 없습니다.");
        }
        console.log("===========================");
    }, function(error) {
        console.error("====== 채팅방 입장 실패 ======");
        console.error("오류:", error);
        console.error("===========================");
    });
}

function leaveChatRoom() {
    postAjax(`/chatroom/leave?chatRoomId=${chatRoomId}&userName=${userName}`, null, function(response) {
        console.log("====== 채팅방 퇴장 성공 ======");
        console.log("Response 객체: ", response); // response 객체 구조 확인
        if (response && response.sender && response.message) {
            console.log(response.sender + "님이 퇴장하셨습니다.");
            console.log(response.message);
            // 퇴장 메시지를 서버에 전송 (퇴장한 사실을 다른 클라이언트에게 알리기 위함)
            stompClient.send("/pub/chat/" + chatRoomId, {}, JSON.stringify(response));
        } else {
            console.log("response 객체에 필요한 데이터가 없습니다.");
        }
        console.log("===========================");
    }, function(error) {
        console.error("====== 채팅방 퇴장 실패 ======");
        console.error("오류:", error);
        console.error("===========================");
    });
    if (stompClient) {
        stompClient.disconnect(function() {
            alert('채팅방을 나갔습니다.');
            window.location.href = '/'; // 메인 페이지로 리다이렉트
        });
    }
}
