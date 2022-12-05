import React from 'react'
import { Container,ListGroup, ListGroupItem } from 'react-bootstrap'
import { useMessageStore } from '../Services/Stores';

export default function console(props) {
    const [messages] = useMessageStore((state) => [state.messages]);
    let messageView = [];
    for(const msg of messages) {
        messageView.push(<ListGroupItem>{msg}</ListGroupItem>)
    }

    return (
        <Container>
            <ListGroup>
                {messageView}
            </ListGroup>
        </Container>

    );
}