import React from 'react';
import { ListGroup, ListGroupItem, Badge } from 'react-bootstrap';
import { useTurnOrder } from '../Services/Stores';
export default function ScoreBoard(props) {
    const [turnOrder] = useTurnOrder((state) => [state.scores]);
    let listView = [];
    for(const record of turnOrder) {
        listView.push(createListItem(record));
    }

    function createListItem(playerTurnInfo) {
        let badgetColour = playerTurnInfo.isCurrentTurn ? "light" : "primary";
        let badgetTextColor = playerTurnInfo.isCurrentTurn ? "dark" : null;
        let item = <><div className="ms-2 me-auto"><div className="fw-bold">{playerTurnInfo.playerName}</div>{playerTurnInfo.score}</div><Badge bg={badgetColour} text={badgetTextColor} pill>{playerTurnInfo.rank}</Badge></>;
        if(playerTurnInfo.isCurrentTurn===true) {
            return <ListGroupItem as="li" active className="d-flex justify-content-between align-items-start">{item}</ListGroupItem>
        }
        else if(playerTurnInfo.isTurnSkipped===true) {
            return <ListGroupItem as="li" className="d-flex justify-content-between align-items-start" disabled>{item}</ListGroupItem>
        }
        else {
            return <ListGroupItem as="li" className="d-flex justify-content-between align-items-start">{item}</ListGroupItem>
        }
    }

    return (
        <ListGroup as="ol" numbered>
            {listView}
        </ListGroup>
    );
}