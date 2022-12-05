import React from 'react';
import { Card, Button } from 'react-bootstrap';
import { Cards } from './Cards';
import blankCard from '../Assets/empty.png'

export default function PlayingCard(props) {
    let cardImage = props.cardEnum ? Cards[props.cardEnum] : blankCard;
    let cardImageAlt = props.cardEnum ? props.cardEnum : "No Card";
    let wSize;
    let borderColour = props.isSelected ? "primary" : "light";
    if(props.isSelected===true) {
        wSize = {width: '8rem'};
    }
    else if(props.isSelected===false) {
        wSize = {width:'6rem'};
    }
    else {
        wSize = {width : '4rem'};
    }

    function handleClick(event) {
        console.log("Card Select:",props.cardEnum);
        if(props.onSelect) {
            props.onSelect(props.cardEnum);
        }
    }

    return (
        <Card style={wSize} onClick={handleClick} border={borderColour} bg={props.isSelected ? borderColour : null}>
            <Card.Img src={cardImage} alt={cardImageAlt}/>
        </Card>
    );
}