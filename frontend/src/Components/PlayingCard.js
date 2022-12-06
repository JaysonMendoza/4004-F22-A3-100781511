import React from 'react';
import { Card, Button } from 'react-bootstrap';
import { Cards } from './Cards';
import blankCard from '../Assets/empty.png'

export default function PlayingCard(props) {
    let cardImage = props.cardEnum ? Cards[props.cardEnum] : blankCard;
    let cardImageAlt = props.cardEnum ? props.cardEnum : "No Card";
    let wSize;
    if(props.isSelected===true) {
        wSize = {width: '10vh', borderColour : 'red', border : '2px solid'};
    }
    else if(props.isSelected===false) {
        wSize = {width:'8vh'};
    }
    else {
        wSize = {width : '6vh'};
    }

    function handleClick(event) {
        console.log("Card Select:",props.cardEnum);
        if(props.onSelect) {
            props.onSelect(props.cardEnum);
        }
    }

    return (
        <Card id={props.cardEnum} className={props.className} onClick={handleClick} style={{backgroundColor : 'transparent',}}>
            <Card.Img style={wSize} src={cardImage} alt={cardImageAlt}/>
        </Card>
    );
}