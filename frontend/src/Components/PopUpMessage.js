import React, {useState} from 'react'
import { Modal, Button } from 'react-bootstrap';

export default function PopUpMessage(props) {
    const [isVisible,setVisible] = useState(props.show ? props.show : false);

    function closeButton(event) {
        setVisible(false);
    }
    console.log("Popup:",props.title);

    return (
        <>
            <Modal name="popMsg" show={isVisible} centered size='lg'>
                <Modal.Header name="popTitle"closeButton>
                    <Modal.Title>{props.title}</Modal.Title>
                </Modal.Header>
                <Modal.Body name="popMessage">
                    {props.message}
                </Modal.Body>
                <Modal.Footer>
                    {props.closeButtonText ? <Button name="btnPopClose" onClick={closeButton}>{props.closeButtonText}</Button> : null}
                </Modal.Footer>
            </Modal>
        </>

    );
}
