import { getAjax, postAjax } from "./util.js";

let stompClient = null;

$(document).ready(function () {
    const chatRoomId = $('#chatRoomId').val();
    const userName = $('#userName').val();

    connectWebSocket(chatRoomId, userName);

    $('#send').on('click', function() {
        handleSendMessage(chatRoomId, userName);
    });

    $('#exit').on('click', function() {
        handleLeaveChatRoom(chatRoomId, userName);
    });
});

function connectWebSocket(chatRoomId, userName) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log("WebSocket 연결 성공, 채팅방 ID:", chatRoomId);

        subscribeToChat(chatRoomId);
        subscribeToMembers(chatRoomId);

        // 구독 완료 후 서버에 알림 전송
        stompClient.send(`/pub/subscribed`, {}, JSON.stringify({ chatRoomId: chatRoomId, userName: userName }));

        handleJoinChatRoom(chatRoomId, userName);

    }, onWebSocketError);
}

function subscribeToChat(chatRoomId) {
    const CHAT_TOPIC = `/sub/chat/${chatRoomId}`;
    console.log("구독하는 채팅 토픽:", CHAT_TOPIC);

    stompClient.subscribe(CHAT_TOPIC, function(messageOutput) {
        console.log("채팅 메시지 수신:", messageOutput);
        onChatMessageReceived(messageOutput);
    });
}

function subscribeToMembers(chatRoomId) {
    const MEMBERS_TOPIC = `/sub/members/${chatRoomId}`;
    console.log("룸 아이디 확인:", chatRoomId);
    console.log("구독하는 멤버 리스트 토픽:", MEMBERS_TOPIC);

    stompClient.subscribe(MEMBERS_TOPIC, function (message) {
        console.log("멤버 리스트 메시지 수신:", message);
        onMembersMessageReceived(message);
    });
}

function onWebSocketError(error) {
    console.error("WebSocket 연결 오류:", error);
}

function onChatMessageReceived(messageOutput) {
    try {
        const message = JSON.parse(messageOutput.body);
        console.log("채팅 메시지 파싱:", message);
        if (message.type === 'chat') {
            displayChatMessage(message);
        }
    } catch (e) {
        console.error("메시지 파싱 오류:", e, messageOutput);
    }
}

function onMembersMessageReceived(messageOutput) {
    console.log("멤버 리스트 메시지 수신:", messageOutput);
    try {
        const message = JSON.parse(messageOutput.body);
        console.log("멤버 리스트 메시지 파싱:", message);
        if (message.members) {
            updateChatMembersList(message.members);
        }
    } catch (e) {
        console.error("멤버 리스트 메시지 파싱 오류:", e, messageOutput);
    }
}

function handleSendMessage(chatRoomId, userName) {
    const message = $('#message').val();
    try {
        stompClient.send(`/pub/chat/${chatRoomId}`, {}, JSON.stringify({
            'message': message,
            'sender': userName,
            'type': 'chat'
        }));
    } catch (e) {
        console.error("메시지 전송 오류:", e);
    }
    $('#message').val('');
}

function handleJoinChatRoom(chatRoomId, userName) {
    postAjax(`/chatroom/join?chatRoomId=${chatRoomId}&userName=${userName}`, null, function(response) {
        if (response) {
            console.log("채팅방 참가 성공, 서버에 입장 메시지 전송:", response);
            stompClient.send(`/pub/chat/${chatRoomId}`, {}, JSON.stringify(response));
        }
    }, function(error) {
        console.error("채팅방 참가 오류:", error);
    });
}

function handleLeaveChatRoom(chatRoomId, userName) {
    postAjax(`/chatroom/leave?chatRoomId=${chatRoomId}&userName=${userName}`, null, function(response) {
        if (response) {
            console.log("채팅방 퇴장 성공, 서버에 퇴장 메시지 전송:", response);
            stompClient.send(`/pub/chat/${chatRoomId}`, {}, JSON.stringify(response));
            subscribeToMembers(chatRoomId);
            stompClient.disconnect();
            window.location.href = '/';
        }
    }, function(error) {
        console.error("채팅방 퇴장 오류:", error);
    });
}

function updateChatMembersList(members) {
    const chatMembers = $('#chat-members');
    chatMembers.empty(); // 기존 멤버 리스트 초기화
    members.forEach(member => {
        chatMembers.append($('<li>').text(member));
    });
    console.log("멤버 리스트 업데이트:", members);
}

function displayChatMessage(message) {
    const chatBoxContent = $('#chat-box-content');
    const messageElement = $('<div class="chat-box-message">')
        .append($('<div class="chat-box-message-content">')
            .append($('<div class="chat-box-message-text">').append($('<p>').text(message.sender + ": " + message.message))))
        .append($('<div class="chat-box-message-time">').append($('<p>').text(new Date().toLocaleTimeString())));
    chatBoxContent.append(messageElement);
    chatBoxContent.scrollTop(chatBoxContent.prop("scrollHeight"));
    console.log("채팅 메시지 출력:", message.sender + ": " + message.message);
}
