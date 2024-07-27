package click.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/*
        웹 소켓 설정 클래스

    웹 소켓 서버를 활성화하고 메시지 브로커를 사용할 수 있도록 설정 (STOMP 프로토콜)
    STOMP 프로토콜 : Simple Text Oriented Messaging Protocol
    STOMP 프로토콜은 메시지 기반의 통신을 할 수 있도록 도와주는 프로토콜 (메시지를 보내고 받을 수 있음)


        브로커 패턴

    프로듀서 : 메시지를 생성하는 클라이언트 (메시지를 보내는 역할)
    ->
    브로커 : 메시지를 중계하는 역할을 하는 서버 (메시지를 받아서 다른 클라이언트에게 전달)
    ->
    컨슈머 : 메시지를 소비하는 클라이언트 (메시지를 받아서 처리)

        Apache Kafka

    Apache Kafka는 브로커 패턴을 사용하는 메시징 시스템
    프로듀서가 메시지를 생성하면 브로커가 메시지를 받아서 컨슈머에게 전달
    컨슈머는 메시지를 받아서 처리하는 역할을 함

    topic : 메시지를 보내고 받을 수 있는 주제
    partition : 토픽을 나누어서 메시지를 저장하는 단위
    offset : 메시지의 위치를 나타내는 값

        redis pub/sub


 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 클라이언트에서 접속할 웹 소켓 엔드포인트 설정 : /ws로 접속하면 웹 소켓 연결을 할 수 있음
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub/chat");
        registry.setApplicationDestinationPrefixes("/pub/chat");
    }

}
