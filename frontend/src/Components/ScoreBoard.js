import React from 'react';
import { ListGroup, ListGroupItem, Badge, Container, Row,Col } from 'react-bootstrap';
import { useTurnOrder, usePlayerStore } from '../Services/Stores';
export default function ScoreBoard(props) {
    const [turnOrder,isPlayReversed, round] = useTurnOrder((state) => [state.scores,state.isPlayReversed,state.round]);
    // const [playerName] = usePlayerStore( (state) => [state.name]);
    let listView = [];
    let currentTurnPlayer;
    for(const record of turnOrder) {
        listView.push(createListItem(record));
    }
    console.log("ScoreBoardList: ",listView);
    function createListItem(playerTurnInfo) {
        let badgetColour = playerTurnInfo.isCurrentTurn ? "light" : "primary";
        let badgetTextColor = playerTurnInfo.isCurrentTurn ? "dark" : null;
        if(playerTurnInfo.isCurrentTurn) {
            currentTurnPlayer = playerTurnInfo.playerName;
        }
        let namePlayerRank = "rank_"+playerTurnInfo.playerID;
        let namePlayerScore = "score_"+playerTurnInfo.playerID;
        let namePlayerName = "pname_"+playerTurnInfo.playerID;
        let namePlayerListItem = "listItem_"+playerTurnInfo.playerID;
        let item = <><div className="ms-2 me-auto"><div  name={namePlayerName} className="fw-bold">{playerTurnInfo.playerName}</div><p name={namePlayerScore}>{playerTurnInfo.score}</p></div><Badge name={namePlayerRank} bg={badgetColour} text={badgetTextColor} pill>{playerTurnInfo.rank}</Badge></>;
        if(playerTurnInfo.isCurrentTurn===true) {
            return <ListGroupItem name={namePlayerListItem} key={playerTurnInfo.playerID} id={playerTurnInfo.playerID} as="li" active className="d-flex justify-content-between align-items-start">{item}</ListGroupItem>
        }
        else if(playerTurnInfo.isTurnSkipped===true) {
            return <ListGroupItem name={namePlayerListItem} key={playerTurnInfo.playerID} id={playerTurnInfo.playerID} as="li" className="d-flex justify-content-between align-items-start" disabled>{item}</ListGroupItem>
        }
        else {
            return <ListGroupItem name={namePlayerListItem} key={playerTurnInfo.playerID} id={playerTurnInfo.playerID} as="li" className="d-flex justify-content-between align-items-start">{item}</ListGroupItem>
        }
    }

    return (
        <Container>
            <Row>
                <Col>
                    <h5 name='h5CurrentTurnPlayer' style={{color : 'whitesmoke'}}>Current Turn: {currentTurnPlayer}</h5>
                </Col>
                <Col>
                    <h5 name='h5RoundNumber' style={{color : 'whitesmoke'}}>Round: {round}</h5>
                </Col>
                <Col>
                    <h5 name='h5DirectionOfPlay' style={{color : 'whitesmoke'}}>Direction: {isPlayReversed ? "Reversed" : "Normal"}</h5>
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