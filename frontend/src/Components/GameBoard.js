import React from 'react'
import {Container, Row,Stack} from 'react-bootstrap'
import { useGameBoardStore, usePlayerStore } from '../Services/Stores';
import DeckArea from './DeckArea';
import Hand from './Hand';
import OtherHand from './OtherHand';
import PopUpMessage from './PopUpMessage';
import SuitChooser from './SuitChooser';

 function GameBoard(props) {
    const [otherPlayerHands] = useGameBoardStore((state) => [state.otherPlayerHands]);
    const [isSelectingSuit] = usePlayerStore((state) => [state.isSelectingSuit]);
    let suitChooserViewport; 
    let otherPlayerViews = [];

    if(isSelectingSuit) {
        suitChooserViewport = <SuitChooser/>
    }
    console.log("Gameboard otherplha: ",otherPlayerHands);
    otherPlayerHands.forEach( (othPlayerUpdate,playerID) => {
        let nameOthHand = "otherHand_"+playerID;
        console.log("GameBoard:: playerID:",playerID,", Data:",othPlayerUpdate);
        otherPlayerViews.push(<div><OtherHand playerID={playerID} playerName={othPlayerUpdate.playerName} name={nameOthHand} key={playerID} numCards={othPlayerUpdate.numCards}/></div>)
    });

    return (
        <Container>
            <Row>
                <Stack style={{height : '30vh'}}>
                    {otherPlayerViews}
                </Stack>
            </Row>
            
            <Row>
                <DeckArea  style={{height : '20vh'}}/>
            </Row>
            <Row>
                { isSelectingSuit ? <PopUpMessage show={isSelectingSuit} title="Select Suit" message={suitChooserViewport}/> : null }
                <Hand style={{height : '20vh'}}/>
            </Row>
        </Container>
    );

};

export default GameBoard;