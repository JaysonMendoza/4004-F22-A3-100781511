import create from 'zustand'

const exampleTurnOrder = [
    {
        playerID : "1239fdasd",
        playerName : "Player 1",
        rank : 1,
        score : 20,
        isCurrentTurn : true,
        isTurnSkipped : false
    },
    {
        playerID : "1240fdasd",
        playerName : "Player 2",
        rank : 3,
        score : 50,
        isCurrentTurn : false,
        isTurnSkipped : true
    },
    {
        playerID : "2239fdasd",
        playerName : "Player 3",
        rank : 2,
        score : 28,
        isCurrentTurn : false,
        isTurnSkipped : false
    }
]
export const useTurnOrder = create( (set) => ({
    scores : [...exampleTurnOrder],
    update : (turnOrder) => set( {scores : turnOrder} )
}))


export const usePlayerStore = create( (set) => ({
    cards : ["CLUBS_8","DIAMONDS_7","SPADES_QUEEN","SPADES_QUEEN","CLUBS_QUEEN","DIAMONDS_ACE","DIAMONDS_5","HEARTS_7","CLUBS_8","DIAMONDS_7","SPADES_QUEEN","SPADES_QUEEN","CLUBS_QUEEN","DIAMONDS_ACE","DIAMONDS_5","HEARTS_7"],
    playerID : null,
    name : "Player 1",
    setPlayerID : (newPlayerID) => set( {playerID : newPlayerID}),
    setPlayerName : (playerName) => set( {name : playerName}),
    update : (newHand) => set( {cards : newHand} )
}))

const exampleOtherPlayerHands = [

    {
        playerID : "1240fdasd",
        playerName : "Player 2",
        numCards : 8
    },
    {
        playerID : "2239fdasd",
        playerName : "Player 3",
        numCards : 3
    }
];

export const useGameBoardStore = create( (set) => ({
    numDrawPileCards : 0,
    otherPlayerHands : [...exampleOtherPlayerHands],
    discardPile: [],
    updateOthPlayerHand: (playerID,numCards) => set( (state) =>state.otherPlayerHands.set(playerID,numCards) ),
    updateDiscardPile : (newDiscardPile) => set({discardPile : newDiscardPile}),
    updateNumDrawPile : (numCards) => set( {numDrawPileCards : numCards} )
}))

export const useGameStateStore = create( (set) => ({
    isGameStarted : false,
    isConnected : false,
    isRegistered : false,
    setIsRegistered : (newIsRegistered) => set( {isRegistered : newIsRegistered} ),
    setIsConnected : (newIsClientConnected) => set( {isConnected : newIsClientConnected} ),
    setIsGameStarted : (newIsGameStarted) => set({isGameStarted : newIsGameStarted})
}))

const exampleMsg = [
    "[Game] Player 1 turn",
    "[Game] Player 2 plays a 2",
    "[Game] Player 1 must play 2 cards or draw 2 cards",
    "[Game] Player 1 turn",
    "[Game] Player 2 plays a 2",
    "[Game] Player 1 must play 2 cards or draw 2 cards",
    "[Game] Player 1 turn",
    "[Game] Player 2 plays a 2",
    "[Game] Player 1 must play 2 cards or draw 2 cards",
    "[Game] Player 1 turn",
    "[Game] Player 2 plays a 2",
    "[Game] Player 1 must play 2 cards or draw 2 cards"
]

export const useMessageStore = create( (set) => ({
    messages : [...exampleMsg],
    add : (message) => set( (state) => ({messages: state.add(message)}) )
}))

