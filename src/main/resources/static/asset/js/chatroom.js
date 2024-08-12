import { postAjax } from "./util.js";

let stompClientChat;
let stompClientMember;

$(document).ready(function () {
    const chatRoomId = $('#chatRoomId').val();
    const userName = $('#userName').val();

    ensureSingleConnection(chatRoomId, userName);

    $('#send').on('click', function() {
        handleSendMessage(chatRoomId, userName);
    });

    $('#exit').on('click', function() {
        handleLeaveChatRoom(chatRoomId, userName);
    });
});

function ensureSingleConnection(chatRoomId, userName) {
    if (!stompClientChat || !stompClientMember) {
        connectWebSockets(chatRoomId, userName, function() {
            handleJoinChatRoom(chatRoomId, userName);
        });
    } else {
        console.log("이미 WebSocket이 연결되어 있습니다.");
    }
}

function connectWebSockets(chatRoomId, userName, callback) {
    const chatSocket = new SockJS('/ws/chat');
    const memberSocket = new SockJS('/ws/members');

    stompClientChat = Stomp.over(chatSocket);
    stompClientMember = Stomp.over(memberSocket);

    stompClientChat.connect({}, function(frame) {
        console.log("Connected to chat: " + frame);
        subscribeToChat(chatRoomId);

        stompClientMember.connect({}, function(frame) {
            console.log("Connected to members: " + frame);
            callback(); // WebSocket 연결이 모두 완료되면 콜백 호출
        }, function(error) {
            onWebSocketError(error);
        });
    }, function(error) {
        onWebSocketError(error);
    });
}

function subscribeToChat(chatRoomId) {
    const CHAT_TOPIC = `/sub/chat/${chatRoomId}`;
    console.log("구독하는 채팅 토픽:", CHAT_TOPIC);

    stompClientChat.subscribe(CHAT_TOPIC, function(messageOutput) {
        console.log("채팅 메시지 수신:", messageOutput);
        onChatMessageReceived(messageOutput);
    }, function(error) {
        console.error("채팅 메시지 구독 중 오류 발생:", error);
    });
}

async function subscribeToMember(chatRoomId) {
    const MEMBER_TOPIC = `/sub/members/${chatRoomId}`;
    console.log("구독하는 멤버 토픽:", MEMBER_TOPIC);

    stompClientMember.subscribe(MEMBER_TOPIC, function(messageOutput) {
        console.log("멤버 메시지 수신:", messageOutput);
        onMemberMessageReceived(messageOutput);
    }, function(error) {
        console.error("멤버 메시지 구독 중 오류 발생:", error);
    });
}

function onChatMessageReceived(messageOutput) {
    try {
        const message = JSON.parse(messageOutput.body);
        console.log("chat 메시지 파싱:", message);
        if (message.type === 'chat') {
            displayChatMessage(message);
        }
    } catch (e) {
        console.error("채팅 메시지 파싱 오류:", e, messageOutput);
    }
}

function onMemberMessageReceived(messageOutput) {
    try {
        const message = JSON.parse(messageOutput.body);
        console.log("member 메시지 파싱:", message);
        if (message.type === 'members') {
            displayMemberList(message.message);
        }
    } catch (e) {
        console.error("멤버 메시지 파싱 오류:", e, messageOutput);
    }
}

function displayChatMessage(message) {
    const chatBoxContent = $('#chat-box-content');
    const messageElement = $('<div class="chat-box-message">')
        .append($('<div class="chat-box-message-content">')
            .append($('<div class="chat-box-message-text">').append($('<p>').text((message.sender || "Unknown") + ": " + (message.message || "")))))
        .append($('<div class="chat-box-message-time">').append($('<p>').text(new Date().toLocaleTimeString())));

    chatBoxContent.append(messageElement);
    chatBoxContent.scrollTop(chatBoxContent.prop("scrollHeight"));
    console.log("채팅 메시지 출력:", message.sender + ": " + message.message);
}

function displayMemberList(members) {
    console.log("======멤버 리스트 출력=======:", members);
    const memberList = $('#chat-member');
    memberList.empty();
    if (Array.isArray(members)) {
        members.forEach(member => {
            const memberElement = $('<div class="chat-member">').append($('<p>').text(member));
            memberList.append(memberElement);
        });
    } else {
        console.error("멤버 리스트 형식 오류:", members);
    }
}

function handleSendMessage(chatRoomId, userName) {
    const message = $('#message').val();
    if (message.trim() === '') return;
    try {
        stompClientChat.send(`/pub/chat/${chatRoomId}`, {}, JSON.stringify({
            'message': message,
            'sender': userName,
            'type': 'chat'
        }));
    } catch (e) {
        console.error("메시지 전송 오류:", e);
    }
    $('#message').val('');
}

async function handleChatRoomAction(actionType, chatRoomId, userName) {
    const url = `/chatroom/${actionType}?chatRoomId=${chatRoomId}&userName=${userName}`;

    postAjax(url, null, function(response) {
        if (response) {
            console.log(`채팅방 ${actionType === 'join' ? '참가' : '퇴장'} 성공, 서버에 메시지 전송:`, response);

            const chatMessage = {
                'sender': 'System',
                'message': `${userName} 님이 ${actionType === 'join' ? '입장' : '퇴장'}하셨습니다.`,
                'type': 'chat'
            };

            const memberMessage = {
                'sender': 'System',
                'message': response.members ? response.members.toString() : userName,
                'type': 'members'
            };

            console.log("chatMessage:", chatMessage);
            console.log("memberMessage:", memberMessage);

            stompClientChat.send(`/pub/chat/${chatRoomId}`, {}, JSON.stringify(chatMessage));
            stompClientMember.send(`/pub/member/${chatRoomId}`, {}, JSON.stringify(memberMessage));

            if (actionType === 'leave') {
                stompClientChat.disconnect();
                stompClientMember.disconnect();
                window.location.href = '/';
            }
        }
    }, function(error) {
        console.error(`채팅방 ${actionType === 'join' ? '참가' : '퇴장'} 오류:`, error);
    });
}

async function handleJoinChatRoom(chatRoomId, userName) {
    console.log("handleJoinChatRoom 호출됨");
    await handleChatRoomAction('join', chatRoomId, userName);
    await subscribeToMember(chatRoomId);
    console.log("handleJoinChatRoom 완료됨");
}

function handleLeaveChatRoom(chatRoomId, userName) {
    handleChatRoomAction('leave', chatRoomId, userName);
}

function onWebSocketError(error) {
    console.error("WebSocket 연결 오류:", error);
}
