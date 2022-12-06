import React, {useState} from 'react'
import { CardGroup , Container, Row, Col, Button, CardDeck} from 'react-bootstrap'
import { usePlayerStore } from '../Services/Stores'
import PlayingCard from './PlayingCard'
import { actionPlayCard } from '../Services/SocketHandler'

export default function Hand(props) {
    const [hand,playerName] = usePlayerStore((state) => [state.cards,state.name]);
    const [selectedCard,setSelectedCard] = useState(null);
    
    let allCards = [];
  
    for(const cd of hand) {
        allCards.push(<PlayingCard cardEnum={cd} onSelect={cardSelected} isSelected={selectedCard===cd}/>)
    }
    
    function cardSelected(cardEnum) {
        if(cardEnum===selectedCard) {
            setSelectedCard(null);
        }
        else {
            setSelectedCard(cardEnum);
        }
    }

    return (
        <Container>
            <Row>
                <Col>
                    <h2 style={{color : 'white'}}>{playerName}</h2>
                </Col>
                <Col>
                    {selectedCard ? <Button onClick={(e) => actionPlayCard(selectedCard)}>Play Card</Button> : null}
                </Col>
            </Row>
            <Row className="g-10">
                <Col>
                    <CardGroup>
                        {allCards}
                    </CardGroup>
                </Col>
            </Row>
        </Container>
    );
}