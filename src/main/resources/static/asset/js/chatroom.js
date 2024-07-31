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

async function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    try {
        stompClient.connect({}, async function (frame) {
            console.log('Connected: ' + frame);

            // 기본 채팅 메시지 구독
            stompClient.subscribe('/sub/chat/' + chatRoomId, function (messageOutput) {
                let message;
                try {
                    message = JSON.parse(messageOutput.body);
                } catch (e) {
                    console.error("JSON 파싱 오류:", e);
                    message = { message: messageOutput.body, sender: "System" };
                }
                showMessageOutput(message);
            }, function (error) {
                console.error("구독 오류:", error);
            });

            // 채팅방에 참가한 후에만 사용자 리스트 구독 설정
            await joinChatRoom();
            console.log("채팅방에 참가했습니다. 이제 멤버 구독을 시작합니다.");

            stompClient.subscribe('/sub/chat/' + chatRoomId + '/members', function (messageOutput) {
                console.log("구독 성공: /sub/chat/" + chatRoomId + "/members");

                if (!messageOutput.body) {
                    console.error("메시지가 비어있습니다.");
                    return;
                } else {
                    console.log("Members list: ", messageOutput.body);
                }

                try {
                    let parsedMessage = JSON.parse(messageOutput.body);
                    if (typeof parsedMessage === 'string') {
                        parsedMessage = JSON.parse(parsedMessage); // 이중 인코딩을 처리
                    }
                    console.log("Parsed members list: ", parsedMessage.content);
                    updateChatMembers(parsedMessage.content);
                } catch (e) {
                    console.error("JSON 파싱 오류:", e);
                }
            }, function (error) {
                console.error("구독 오류:", error);
            });

        }, function (error) {
            console.error("WebSocket 연결 오류:", error);
        });
    } catch (error) {
        console.error("Error during connection:", error);
    }
}

function sendMessage() {
    var message = $('#message').val();
    try {
        stompClient.send("/pub/chat/" + chatRoomId, {}, JSON.stringify({'message': message, 'sender': userName}));
    } catch (e) {
        console.error("오류:", e);
    }
    $('#message').val('');
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

async function joinChatRoom() {
    return new Promise((resolve, reject) => {
        postAjax(`/chatroom/join?chatRoomId=${chatRoomId}&userName=${userName}`, null, function(response) {
            if (response && response.sender && response.message) {
                stompClient.send("/pub/chat/" + chatRoomId, {}, JSON.stringify(response));
                resolve();  // 성공적으로 채팅방에 참가한 경우 resolve
            } else {
                reject("response 객체에 필요한 데이터가 없습니다.");  // 오류 처리
            }
        }, function(error) {
            reject("오류:", error);  // 오류 처리
        });
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

function updateChatMembers(members) {
    const chatMembers = $('#chat-members');
    chatMembers.empty();  // 기존 리스트를 비움
    members.forEach(member => {
        // 각 멤버에 대해 새로운 리스트 항목을 추가
        chatMembers.append($('<li>').text(member));
    });
}
