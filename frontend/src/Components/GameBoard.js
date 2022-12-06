import React from 'react'
import {Container, Row,Stack} from 'react-bootstrap'
import { useGameBoardStore } from '../Services/Stores';
import DeckArea from './DeckArea';
import Hand from './Hand';
import OtherHand from './OtherHand';

 function GameBoard(props) {
    const [otherPlayerHands] = useGameBoardStore((state) => [state.otherPlayerHands]);
    let otherPlayerViews = [];

    for(const p of otherPlayerHands) {
        otherPlayerViews.push(<div><OtherHand name={p.playerName} numCards={p.numCards}/></div>)
    }

    return (
        <Container>
            <Row>
                <Stack gap={1}  style={{height : '30vh'}}>
                    {otherPlayerViews}
                </Stack>
            </Row>
            
            <Row>
                <DeckArea  style={{height : '20vh'}}/>
            </Row>
            <Row>
                <Hand style={{height : '20vh'}}/>
            </Row>
        </Container>
    );

};

export default GameBoard;