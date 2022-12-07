import React, {useEffect,useRef} from 'react'
import { Container,ListGroup, ListGroupItem } from 'react-bootstrap'
import { useMessageStore } from '../Services/Stores';

export default function Console(props) {
    const lastMsgRef = useRef();
    const [messages] = useMessageStore((state) => [state.messages]);
    let messageView = [];
    console.log("Console Messages",messages);
    for(let i=0;i<messages.length;++i) {
        if(i===messages.length-1) {
            messageView.push(<ListGroupItem ref={lastMsgRef} key={i}>{messages[i]}</ListGroupItem>)
        }
        else {
            messageView.push(<ListGroupItem key={i}>{messages[i]}</ListGroupItem>)
        }
        
    }

    useEffect(() => {
        if(messages.length>0) {
            lastMsgRef.current.scrollIntoView({behavior : 'smooth'})
        }
    });

    return (
        <Container>
            <ListGroup style={{overflowY : 'scroll', height : '30vh'}}>
                {messageView}
            </ListGroup>
        </Container>

    );
}