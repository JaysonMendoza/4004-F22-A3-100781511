import React from 'react'
import { CardGroup , Container, Row, Col} from 'react-bootstrap'
import PlayingCard from './PlayingCard'

export default function OtherHand(props) {
    
    let allCards = [];
    for(let i=0; i<props.numCards; ++i) {
        allCards.push(<PlayingCard cardEnum="CARDBACK"/>)
    }
    
    let nameOthHandCardGroup = "otherHandCardGroup_"+props.playerID;
    let nameOthHandName = "otherHandPlayerName_"+props.playerID;
    return (
        <Container>
            <Row>
                <Col>
                    <h3 name={nameOthHandName} style={{color : 'whitesmoke'}}>{props.playerName}</h3>
                </Col>
            </Row>
            <Row>
                <Col>
                    <CardGroup name={nameOthHandCardGroup}>
                        {allCards}
                    </CardGroup>
                </Col>
            </Row>
        </Container>
    );
}