import React, {useState} from 'react'
import { Modal, Image, Button } from 'react-bootstrap';
import {Cards} from './Cards';

export default function PopUpMessage(props) {
    const [isVisible,setVisible] = useState(props.show ? props.show : false);

    function closeButton(event) {
        setVisible(false);
    }
    console.log("Popup:",props.title);

    return (
        <>
            <Modal id="popAlert" show={isVisible} centered size='lg'>
                <Modal.Header id="popTitle"closeButton>
                    <Modal.Title>{props.title}</Modal.Title>
                </Modal.Header>
                <Modal.Body id="popMessage">
                    {props.message}
                </Modal.Body>
                <Modal.Footer>
                    {props.closeButtonText ? <Button id="btnPopClose" onClick={closeButton}>{props.closeButtonText}</Button> : null}
                </Modal.Footer>
            </Modal>
        </>

    );
}
