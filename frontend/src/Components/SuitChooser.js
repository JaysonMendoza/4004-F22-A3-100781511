import React from 'react';
import { CardGroup } from 'react-bootstrap';
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
        <CardGroup>
            <PlayingCard id="selectDiamonds" cardEnum="DIAMONDS_8" onSelect={onSelected} isSelected={false} />
            <PlayingCard id="selectClubs" cardEnum="CLUBS_8" onSelect={onSelected} isSelected={false} />
            <PlayingCard id="selectHearts" cardEnum="HEARTS_8" onSelect={onSelected} isSelected={false} />
            <PlayingCard id="selectSpades" cardEnum="SPADES_8" onSelect={onSelected} isSelected={false} />
        </CardGroup>
    );
}