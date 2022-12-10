import React from 'react'
import { Container, Row, Col,Card, Button } from 'react-bootstrap'
import PlayingCard from './PlayingCard'
import { useGameBoardStore } from '../Services/Stores'
import { actionDrawCard } from '../Services/SocketHandler'
import { Cards } from './Cards';

export default function DeckArea(props) {
   const [numDrawPile,discardPile] = useGameBoardStore((state) => [state.numDrawPile,state.discardPile]);
    let topCard;
    console.log("Drawing discard pile",discardPile);
    if(discardPile.length>0) {
        topCard = <PlayingCard name='topDiscardedCard' cardEnum={discardPile[discardPile.length-1]} isSelected={false}/>;
    }
    else {
        topCard = <PlayingCard isSelected={false}/>
    }

   return (
    <Container>
        <Row>
            <Col style={{textAlign : 'right'}}>
                <h5 style={{color : 'white'}}>Deck</h5>
            </Col>
            <Col style={{textAlign : 'left'}}>
                <h5 style={{color : 'white'}}>Discard Pile</h5>
            </Col>
        </Row>
        <Row>
            <Col>
                <Card name={"drawPile"} onClick={() => actionDrawCard()} style={{backgroundColor : 'transparent',}}>
                    <Card.Img style={{width:'8vh'}} src={Cards["CARDBACK"]} alt={"Draw Pile"}/>
                </Card>
                <Button name='btnDrawCard' variant='primary' size='lg' onClick={() => actionDrawCard()}>Draw Card</Button>
                {/* <PlayingCard className="ml-auto" cardEnum="CARDBACK" onSelect={() => actionDrawCard()} isSelected={false}/> */}
            </Col>
            <Col>
                {topCard}
            </Col>
        </Row>
        <Row>
            <Col>
                <p>{numDrawPile}</p>
            </Col>
            <Col>
            </Col>
        </Row>
    </Container>
   );
}