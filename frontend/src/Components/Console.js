import React, {useEffect,useRef} from 'react'
import { Container,ListGroup, ListGroupItem } from 'react-bootstrap'
import { useMessageStore } from '../Services/Stores';

export default function console(props) {
    const lastMsgRef = useRef(null);
    const [messages] = useMessageStore((state) => [state.messages]);
    let messageView = [];
    for(let i=0;i<messages.length;++i) {
        if(i===messages.length-1) {
            messageView.push(<ListGroupItem ref={lastMsgRef} key={i}>{messages[i]}</ListGroupItem>)
        }
        else {
            messageView.push(<ListGroupItem key={i}>{messages[i]}</ListGroupItem>)
        }
        
    }

    useEffect(() => {
        lastMsgRef.current.scrollIntoView({behavior : 'smooth'})
    });

    return (
        <Container>
            <ListGroup style={{overflowY : 'scroll', height : '30vh'}}>
                {messageView}
            </ListGroup>
        </Container>

    );
}