import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"
import { useGameBoardStore,usePlayerStore,useTurnOrder,useMessageStore, useGameStateStore } from "./Stores"
const SOCK_SVR = "http://localhost:8080/ws"

const client = new Client(
    {
        webSocketFactory: () => {
            return new SockJS(`${SOCK_SVR}`);
        },
        reconnectDelay: 50000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: onWsConnected,
        debug : (out) => { console.log(out)},
        onUnhandledMessage: (out) => console.log("Unhandled: "+out)
    }
);

console.log("Constructed SocketHandler.js");
export const connect = async (name) => {
    console.log("Connecting to "+SOCK_SVR)
    client.activate();
}

function onWsConnected(frame) {

    console.log(`Connected to Websocket @ ${SOCK_SVR}`)
    console.log("Subscribing to topics.");
    useGameStateStore.getState().setIsConnected(true);
    
    client.subscribe("/topic/startGame",handleStartGame);
    client.subscribe("/user/queue/playerUpdated",handlePlayerUpdated);
    client.subscribe("/user/queue/playerRegistered",handlePlayerRegistered);
    client.subscribe("/user/queue/messageReceived",handleMessageReceived);
    client.subscribe("/topic/messageReceived",handleMessageReceived);
    client.subscribe("/topic/updateTurnOrder",handleUpdateTurnOrder);
    client.subscribe("/topic/updateGameBoard",handleUpdateGameBoard);
    client.subscribe("/user/queue/OtherPlayerUpdated",handleOtherPlayerUpdated);   
}


/**
 * TOPIC HANDLERS
 */

function handlePlayerRegistered(response) {
    console.log("handlePlayerRegistered():",response.body)
    let data = JSON.parse(response.body);
    if ( !(data.hasOwnProperty("playerID") && data.hasOwnProperty("cards")) ) {
        console.error("handlePlayerRegistered recieved malformed data.")
        return;
    }
    useGameStateStore.getState().setIsRegistered(true);
    handlePlayerUpdated(response);
}

function handlePlayerUpdated(response) {
    console.log("handlePlayerUpdated:",response.body)
    let data = JSON.parse(response.body);
    if ( !(data.hasOwnProperty("playerID") && data.hasOwnProperty("cards")) ) {
        console.error("handleUpdatePlayer recieved malformed data.")
        return;
    }

    if(usePlayerStore.getState().playerID===null) {
        usePlayerStore.getState().setPlayerID(data.playerID);
    }
    else if(data.playerID!==usePlayerStore.getState().playerID) {
        console.error("Update recieved for invalid player. Player "+usePlayerStore.getState().playerID+" cannot be updated with playerID "+response.playerID);
        return;
    }
    usePlayerStore.getState().update(data.cards);
    console.log("PlayerID: "+usePlayerStore.getState().playerID+" hand updated!"); 
}

function handleStartGame(response) {
    console.log("Starting game!");
    useGameStateStore.setIsGameStarted(true);
}

function handleUpdateTurnOrder(response) {
    console.log("Turn Order Update Recieved!");
    let data = JSON.parse(response.body);
    if ( !(data.hasOwnProperty("turnSequence") && data.hasOwnProperty("isPlayReversed") && data.hasOwnProperty("round") ) ) {
        console.error("handleMessageReceived recieved malformed data.")
        return;
    }
    useTurnOrder.getState().updateTurnInfo(data.turnSequence);
    useTurnOrder.getState().setIsPlayReversed(data.isPlayReversed);
    useTurnOrder.getState().setRound(data.round);
}

function handleUpdateGameBoard(response) {
    console.log("Game Board Update Recieved!");
    let data = JSON.parse(response.body);
    if ( !(data.hasOwnProperty("numCardsDrawPile") && data.hasOwnProperty("discardPile")) || isNaN(data.numCardsDrawPile)) {
        console.error("handleMessageReceived recieved malformed data.")
        return;
    }
    useGameBoardStore.getState().updateNumDrawPile(data.numCardsDrawPile)
    useGameBoardStore.getState().updateDiscardPile(data.discardPile);
}

function handleOtherPlayerUpdated(response) {
    console.log("Other Player Update Recieved!");
    let data = JSON.parse(response.body);
    if ( !(data.hasOwnProperty("playerID") && data.hasOwnProperty("numCards")) || isNaN(data.numCards)) {
        console.error("handleMessageReceived recieved malformed data.")
        return;
    }
    useGameBoardStore.getState().updateOthPlayerHand(data.playerID,data.numCards);
}

function handleMessageReceived(response) {
    let data = JSON.parse(response.body);
    if ( !(data.hasOwnProperty("senderName") && data.hasOwnProperty("message")) ) {
        console.error("handleMessageReceived recieved malformed data.")
        return;
    }
    let line = `${data.senderName}: ${data.message}`;
    useMessageStore.getState().add(line);
}

/**
 * ACTION HANDLERS (MUST EXPORT)
 */

export function actionRegisterPlayer(name) {
    client.publish({
        destination: "/app/joinGame",
        body : name
    });
}

export function actionPlayCard(cardEnum) {
    console.log("actionPlayCard:",cardEnum);
}

export function actionDrawCard() {
    console.log("actionDrawCard");
}



