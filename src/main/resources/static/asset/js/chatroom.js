import { getAjax, postAjax } from "./util.js";

let stompClientChat = null;
let stompClientMembers = null;

$(document).ready(async function () {
    const chatRoomId = $('#chatRoomId').val();
    const userName = $('#userName').val();

    try {
        await ensureSingleConnection(chatRoomId, userName);
        $('#send').on('click', function() {
            handleSendMessage(chatRoomId, userName);
        });

        $('#exit').on('click', function() {
            handleLeaveChatRoom(chatRoomId, userName);
        });
    } catch (error) {
        console.error("WebSocket 연결 중 오류 발생:", error);
    }
});

async function ensureSingleConnection(chatRoomId, userName) {
    if (!stompClientChat) {
        await connectChatWebSocket(chatRoomId, userName);
    } else {
        console.log("이미 채팅 WebSocket이 연결되어 있습니다.");
    }

    if (!stompClientMembers) {
        await connectMembersWebSocket(chatRoomId);
    } else {
        console.log("이미 멤버 리스트 WebSocket이 연결되어 있습니다.");
    }
}

async function connectChatWebSocket(chatRoomId, userName) {
    return new Promise((resolve, reject) => {
        const chatSocket = new SockJS('/ws/chat');
        stompClientChat = Stomp.over(chatSocket);
        stompClientChat.connect({}, function(frame) {
            console.log("/ws/chat WebSocket 연결 성공, 채팅방 ID:", chatRoomId);
            if (stompClientChat) {
                subscribeToChat(chatRoomId);
                handleJoinChatRoom(chatRoomId, userName);
                resolve();
            } else {
                reject("stompClientChat 초기화 오류");
            }
        }, function(error) {
            onWebSocketError(error);
            reject(error);
        });
    });
}

async function connectMembersWebSocket(chatRoomId) {
    return new Promise((resolve, reject) => {
        const membersSocket = new SockJS('/ws/members');
        stompClientMembers = Stomp.over(membersSocket);
        stompClientMembers.connect({}, function(frame) {
            console.log("/ws/members WebSocket 연결 성공, 채팅방 ID:", chatRoomId);
            if (stompClientMembers) {
                subscribeToMembers(chatRoomId);
                resolve();
            } else {
                reject("stompClientMembers 초기화 오류");
            }
        }, function(error) {
            onWebSocketError(error);
            reject(error);
        });
    });
}

function subscribeToChat(chatRoomId) {
    if (!stompClientChat) {
        console.error("stompClientChat이 null입니다. 구독을 설정할 수 없습니다.");
        return;
    }

    const CHAT_TOPIC = `/sub/chat/${chatRoomId}`;
    console.log("구독하는 채팅 토픽:", CHAT_TOPIC);

    stompClientChat.subscribe(CHAT_TOPIC, function(messageOutput) {
        console.log("채팅 메시지 수신:", messageOutput);
        onChatMessageReceived(messageOutput);
    });
}

function subscribeToMembers(chatRoomId) {
    if (!stompClientMembers) {
        console.error("stompClientMembers가 null입니다. 구독을 설정할 수 없습니다.");
        return;
    }

    const MEMBERS_TOPIC = `/sub/members/${chatRoomId}`;
    console.log("구독하는 멤버 리스트 토픽:", MEMBERS_TOPIC);

    stompClientMembers.subscribe(MEMBERS_TOPIC, function(message) {
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
        console.log("chat 메시지 파싱:", message);
        if (message.type === 'chat') {
            displayChatMessage(message);
        }
    } catch (e) {
        console.error("채팅 메시지 파싱 오류:", e, messageOutput);
    }
}

function onMembersMessageReceived(messageOutput) {
    console.log("멤버 리스트 메시지 수신:", messageOutput);  // 추가 로그로 메시지 수신 확인
    try {
        const message = JSON.parse(messageOutput.body);
        console.log("members 메시지 파싱:", message);
        if (message.type === 'members' && Array.isArray(message.message)) {
            updateChatMembersList(message.message);
        } else {
            console.error("잘못된 멤버 리스트 형식:", message);
        }
    } catch (e) {
        console.error("멤버 리스트 메시지 파싱 오류:", e, messageOutput);
    }
}

function handleSendMessage(chatRoomId, userName) {
    const message = $('#message').val();
    if (message.trim() === '') return;  // 빈 메시지 방지
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

function handleJoinChatRoom(chatRoomId, userName) {
    postAjax(`/chatroom/join?chatRoomId=${chatRoomId}&userName=${userName}`, null, function(response) {
        if (response) {
            console.log("채팅방 참가 성공, 서버에 입장 메시지 전송:", response);
            stompClientChat.send(`/pub/chat/${chatRoomId}`, {}, JSON.stringify(response));
        }
    }, function(error) {
        console.error("채팅방 참가 오류:", error);
    });
}

function handleLeaveChatRoom(chatRoomId, userName) {
    postAjax(`/chatroom/leave?chatRoomId=${chatRoomId}&userName=${userName}`, null, function(response) {
        if (response) {
            console.log("채팅방 퇴장 성공, 서버에 퇴장 메시지 전송:", response);
            stompClientChat.send(`/pub/chat/${chatRoomId}`, {}, JSON.stringify(response));
            stompClientChat.disconnect(); // 채팅 WebSocket 연결 해제
            stompClientMembers.disconnect(); // 멤버 리스트 WebSocket 연결 해제
            window.location.href = '/';
        }
    }, function(error) {
        console.error("채팅방 퇴장 오류:", error);
    });
}

function updateChatMembersList(members) {
    const chatMembers = $('#chat-member');
    chatMembers.empty(); // 기존 멤버 리스트 초기화
    if (members && members.length > 0) {
        members.forEach(member => {
            chatMembers.append($('<li>').text(member));
        });
    } else {
        console.log("멤버 리스트가 비어 있습니다.");
    }
    console.log("멤버 리스트 업데이트:", members);
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
