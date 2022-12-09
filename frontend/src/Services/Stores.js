import create from 'zustand'

// const exampleTurnOrder = [
//     {
//         playerID : "1239fdasd",
//         playerName : "Player 1",
//         rank : 1,
//         score : 20,
//         isCurrentTurn : true,
//         isTurnSkipped : false
//     },
//     {
//         playerID : "1240fdasd",
//         playerName : "Player 2",
//         rank : 3,
//         score : 50,
//         isCurrentTurn : false,
//         isTurnSkipped : true
//     },
//     {
//         playerID : "2239fdasd",
//         playerName : "Player 3",
//         rank : 2,
//         score : 28,
//         isCurrentTurn : false,
//         isTurnSkipped : false
//     }
// ]
export const useTurnOrder = create( (set) => ({
    scores : [],
    isPlayReversed : false,
    round : 0,
    updateTurnInfo : (turnInfo) => set( {scores : turnInfo} ),
    setIsPlayReversed : (newIsPlayReversed) => set( {isPlayReversed : newIsPlayReversed} ),
    setRound : (newRoundNum) => set( {round : newRoundNum} )
}))


export const usePlayerStore = create( (set) => ({
    cards : [],
    playerID : null,
    name : "",
    isSelectingSuit : false,
    isPickingUpTwo : false,
    setIsSelectingSuit : (isSuitSelectMode) => set( {isSelectingSuit : isSuitSelectMode} ),
    setIsPickingUpTwo : (isPickingUpTwoMode) => set( {isPickingUpTwo : isPickingUpTwoMode} ),
    setPlayerID : (newPlayerID) => set( {playerID : newPlayerID}),
    setPlayerName : (playerName) => set( {name : playerName}),
    update : (newHand) => set( {cards : newHand} )
}))

// const exampleOtherPlayerHands = [

//     {
//         playerID : "1240fdasd",
//         playerName : "Player 2",
//         numCards : 8
//     },
//     {
//         playerID : "2239fdasd",
//         playerName : "Player 3",
//         numCards : 3
//     }
// ];
//TODO: Need to fix other player hands because this implementation doesn't work
export const useGameBoardStore = create( (set) => ({
    numDrawPileCards : 0,
    otherPlayerHands : [],
    discardPile: [],
    updateOthPlayerHand: (playerID,numCards) => set( (state) =>state.otherPlayerHands.set(playerID,numCards) ),
    updateDiscardPile : (newDiscardPile) => set({discardPile : newDiscardPile}),
    updateNumDrawPile : (numCards) => set( {numDrawPileCards : numCards} )
}))

export const useGameStateStore = create( (set) => ({
    isGameStarted : false,
    isConnected : false,
    isRegistered : false,
    isGameEnded : false,
    setIsRegistered : (newIsRegistered) => set( {isRegistered : newIsRegistered} ),
    setIsConnected : (newIsClientConnected) => set( {isConnected : newIsClientConnected} ),
    setIsGameStarted : (newIsGameStarted) => set({isGameStarted : newIsGameStarted}),
    setIsGameEnded : (newIsGameEnded) => set({isGameEnded : newIsGameEnded})
}))

// const exampleMsg = [
//     "[Game] Player 1 turn",
//     "[Game] Player 2 plays a 2",
//     "[Game] Player 1 must play 2 cards or draw 2 cards",
//     "[Game] Player 1 turn",
//     "[Game] Player 2 plays a 2",
//     "[Game] Player 1 must play 2 cards or draw 2 cards",
//     "[Game] Player 1 turn",
//     "[Game] Player 2 plays a 2",
//     "[Game] Player 1 must play 2 cards or draw 2 cards",
//     "[Game] Player 1 turn",
//     "[Game] Player 2 plays a 2",
//     "[Game] Player 1 must play 2 cards or draw 2 cards"
// ]
// const exampleAlert = {
//     type: "BAD",
//     title : "Failed to Join Game",
//     message: "Game is Full",
//     isClosable : false
// }
export const useMessageStore = create( (set) => ({
    messages : [],
    alert : null,
    add : (newMessage) => set( (state) => ({
        messages : [...state.messages,newMessage]

    })),
    setAlert : (newAlert) => set( {alert: newAlert} ),
    clearAlert : () => set( (state) => ({alert: null}) )
}))

