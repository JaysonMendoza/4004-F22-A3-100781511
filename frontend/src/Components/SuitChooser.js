import React from 'react';
import { Container, Row,CardGroup, ButtonGroup, Button } from 'react-bootstrap';
import PlayingCard from './PlayingCard';
import { usePlayerStore } from '../Services/Stores';
import { actionSelectSuit } from '../Services/SocketHandler';

export default function SuitChooser(props) {

    const [setIsSelectingSuit] = usePlayerStore((state) => [state.setIsSelectingSuit]);

    function onSelected(cardEnum) {
        setIsSelectingSuit(false);
        actionSelectSuit(cardEnum);
    }

    return (
        <Container>
            <Row>
                <CardGroup name='suitSelect'>
                    <PlayingCard cardEnum="DIAMONDS_8" onSelect={onSelected} isSelected={false} />
                    <PlayingCard cardEnum="CLUBS_8" onSelect={onSelected} isSelected={false} />
                    <PlayingCard cardEnum="HEARTS_8" onSelect={onSelected} isSelected={false} />
                    <PlayingCard cardEnum="SPADES_8" onSelect={onSelected} isSelected={false} />
                </CardGroup>
            </Row>
            <Row>
                <ButtonGroup>
                    <Button name="selectDiamonds" variant='primary' size='sm' onClick={() => onSelected("DIAMONDS_8")}>DIAMONDS</Button>
                    <Button name="selectClubs" variant='primary' size='sm' onClick={() => onSelected("CLUBS_8")}>CLUBS</Button>
                    <Button name="selectHearts" variant='primary' size='sm' onClick={() => onSelected("HEARTS_8")}>HEARTS</Button>
                    <Button name="selectSpades" variant='primary' size='sm' onClick={() => onSelected("SPADES_8")}>SPADES</Button>
                </ButtonGroup>
            </Row>

        </Container>


    );
}