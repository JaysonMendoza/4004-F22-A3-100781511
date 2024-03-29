import React from 'react'
import {Form,Button,Modal} from 'react-bootstrap'
import {actionRegisterPlayer} from '../Services/SocketHandler'
import {useGameStateStore} from '../Services/Stores'

function RegistrationForm(props) {
    const handleSubmit = (event) => {
        event.preventDefault();
        actionRegisterPlayer(event.target.formPlayerName.value);
    };

    const isRegistered = useGameStateStore((state) => state.isRegistered);
    console.log("Registration: show=",!isRegistered);

    return (
        <>
            <Modal show={!isRegistered} centered size='lg'>
                <Modal.Header closeButton>
                    <Modal.Title>Register and Join Game</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form onSubmit={handleSubmit}>
                        <Form.Group className="mb-3" controlId="formPlayerName">
                            <Form.Label>Enter Player Name:</Form.Label>
                            <Form.Control name="formPlayerName" type="text" placeholder="Enter Name" />
                            <Form.Text className="text-muted">This is how you appear on the scoreboard and in messages</Form.Text>
                        </Form.Group>
                        <Button name='btnRegister' variant="primary" type="submit">Register</Button>
                    </Form>
                </Modal.Body>
            </Modal>
        </>

    );
}

export default RegistrationForm;