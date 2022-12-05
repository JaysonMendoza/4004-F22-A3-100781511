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
        otherPlayerViews.push(<Row><OtherHand name={p.playerName} numCards={p.numCards}/></Row>)
    }

    return (
        <Container fluid>

            {otherPlayerViews}
            <Row>
                <DeckArea/>
            </Row>
            <Row>
                <Hand/>
            </Row>
        </Container>
    );

};

export default GameBoard;