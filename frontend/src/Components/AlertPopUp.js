import React from 'react'
import { Modal, Button } from 'react-bootstrap';
import { useMessageStore } from '../Services/Stores';

export default function AlertPopUp(props) {
    const [alert, setAlert] = useMessageStore((state) => [state.alert,state.setAlert]);
    // const [isVisible,setVisible] = useState(false);
    let title="";
    let message="";
    let style = {};
    let isClosable=false;
    console.log("AlertPopUp alert: ",alert);
    let isVisible = alert!==null;
    if(alert) {
        title = alert.title;
        message = alert.message;
        isClosable = alert.isClosable;
        if(alert.type==='BAD') {
            style = {
                backgroundColor : 'red',
                color : 'white'
            }
        }
        else if(alert.type==='GOOD') {
            style = {
                backgroundColor : 'green',
                color : 'white'
            }
        }
        // setVisible(true);
    }



    function closeButton(event) {
        // setVisible(false);
        setAlert(null);
    }


    return (
        <>
            <Modal id="popAlert" show={isVisible} centered size='lg'>
                <Modal.Header style={style} id="alertTitle"closeButton>
                    <Modal.Title>{title}</Modal.Title>
                </Modal.Header>
                <Modal.Body id="alertMessage">
                    {message}
                </Modal.Body>
                <Modal.Footer>
                    { isClosable ? <Button id="btnAlertClose" onClick={closeButton}>OK</Button> : null }
                </Modal.Footer>
            </Modal>
        </>

    );
}
