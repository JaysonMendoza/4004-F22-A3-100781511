import React, {useState} from 'react'
import { CardGroup , Container, Row, Col, Button} from 'react-bootstrap'
import { usePlayerStore } from '../Services/Stores'
import PlayingCard from './PlayingCard'
import { actionPlayCard, actionRedoTurn } from '../Services/SocketHandler'

export default function Hand(props) {
    const [hand,playerName,isPickingUpTwo] = usePlayerStore((state) => [state.cards,state.name,state.isPickingUpTwo]);
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

    function redoTurn(event) {
        console.log("Redo Turn Clicked");
        setSelectedCard(null);
        actionRedoTurn();
    }

    function handlePlayCard(event) {
        actionPlayCard(selectedCard)
        setSelectedCard(null);
    }

    console.log("Hand selected card:",selectedCard);

    return (
        <Container>
            <Row>
                <Col>
                    <h2 name='txtHandPlayerName' style={{color : 'white'}}>{playerName}</h2>
                </Col>
                <Col>
                    {isPickingUpTwo ? <Button name='btnRedoTurn' variant='danger' size='lg' onClick={redoTurn}>Redo Turn</Button> : null}
                    {selectedCard ? <Button name='btnPlayCard' variant='info' size='lg' onClick={handlePlayCard}>Play Card</Button> : null}
                </Col>
            </Row>
            <Row className="g-10">
                <Col>
                    <CardGroup name="grpHandCards">
                        {allCards}
                    </CardGroup>
                </Col>
            </Row>
        </Container>
    );
}