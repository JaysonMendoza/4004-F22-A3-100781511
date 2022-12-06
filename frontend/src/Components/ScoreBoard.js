import React from 'react';
import { ListGroup, ListGroupItem, Badge, Container, Row,Col } from 'react-bootstrap';
import { useTurnOrder } from '../Services/Stores';
export default function ScoreBoard(props) {
    const [turnOrder,isPlayReversed, round] = useTurnOrder((state) => [state.scores,state.isPlayReversed,state.round]);
    let listView = [];
    for(const record of turnOrder) {
        listView.push(createListItem(record));
    }

    function createListItem(playerTurnInfo) {
        let badgetColour = playerTurnInfo.isCurrentTurn ? "light" : "primary";
        let badgetTextColor = playerTurnInfo.isCurrentTurn ? "dark" : null;
        let item = <><div className="ms-2 me-auto"><div className="fw-bold">{playerTurnInfo.playerName}</div>{playerTurnInfo.score}</div><Badge bg={badgetColour} text={badgetTextColor} pill>{playerTurnInfo.rank}</Badge></>;
        if(playerTurnInfo.isCurrentTurn===true) {
            return <ListGroupItem key={playerTurnInfo.playerID} id={playerTurnInfo.playerID} as="li" active className="d-flex justify-content-between align-items-start">{item}</ListGroupItem>
        }
        else if(playerTurnInfo.isTurnSkipped===true) {
            return <ListGroupItem key={playerTurnInfo.playerID} id={playerTurnInfo.playerID} as="li" className="d-flex justify-content-between align-items-start" disabled>{item}</ListGroupItem>
        }
        else {
            return <ListGroupItem key={playerTurnInfo.playerID} id={playerTurnInfo.playerID} as="li" className="d-flex justify-content-between align-items-start">{item}</ListGroupItem>
        }
    }

    return (
        <Container>
            <Row>
                <Col>
                    <h5 style={{color : 'whitesmoke'}}>Round: {round}</h5>
                </Col>
                <Col>
                    <h5 style={{color : 'whitesmoke'}}>Direction: {isPlayReversed ? "Reversed" : "Normal"}</h5>
                </Col>
            </Row>
            <Row>
                <ListGroup as="ol" numbered>
                    {listView}
                </ListGroup>
            </Row>
        </Container>
    );
}