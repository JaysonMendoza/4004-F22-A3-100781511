import React from 'react'
import { CardGroup , Container, Row, Col} from 'react-bootstrap'
import { Cards } from './Cards'
import PlayingCard from './PlayingCard'

export default function OtherHand(props) {
    
    let allCards = [];
    for(let i=0; i<props.numCards; ++i) {
        allCards.push(<PlayingCard cardEnum="CARDBACK"/>)
    }
    

    return (
        <Container>
            <Row>
                <Col>
                    <h3 style={{color : 'whitesmoke'}}>{props.name}</h3>
                </Col>
            </Row>
            <Row>
                <Col>
                    <CardGroup>
                        {allCards}
                    </CardGroup>
                </Col>
            </Row>
        </Container>
    );
}