import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import axios from "axios"

const SOCK_SVR = "http://localhost:8080/crazy-eights-websocket"
const instance = axios.create({
    baseURL : SOCK_SVR
});
export const connect = async (name) => {
    const client = new Client(
        {
            webSocketFactory: () => {
                return new SockJS(`${SOCK_SVR}?name=${name}`);
            },
            reconnectDelay: 50000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: onWsConnected
        }
    );
    client.activate();
    
    // let response = await instance.post(
    //     "/app/joinGame",
    //     {
    //         name : "Player 1"
    //     }
    //     );
    // console.log(response);
    function onWsConnected(frame) {
        console.log(`Connected to Websocket @ ${SOCK_SVR}`)
        client.subscribe("/user/test",test);
    }
}



function test(val) {
    console.log("test recieved: "+val)
}