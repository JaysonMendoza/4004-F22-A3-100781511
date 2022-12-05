import React, {useState} from 'react'
import { Modal, Image, Button } from 'react-bootstrap';
import {Cards} from './Cards';

export default function PopUpMessage(props) {
    const [isVisible,setVisible] = useState(props.show ? props.show : false);

    function closeButton(event) {
        setVisible(false);
    }

    return (
        <>
            <Modal show={isVisible} centered size='lg'>
                <Modal.Header closeButton>
                    <Modal.Title>{props.title}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {props.message}
                </Modal.Body>
                <Modal.Footer>
                    {props.closeButtonText ? <Button onClick={closeButton}>{props.closeButtonText}</Button> : null}
                </Modal.Footer>
            </Modal>
        </>

    );
}
