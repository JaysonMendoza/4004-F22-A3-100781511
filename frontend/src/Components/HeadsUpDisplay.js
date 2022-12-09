import React from 'react'
import { Container, Row, Col } from 'react-bootstrap'
import ScoreBoard from './ScoreBoard';
import Console from './Console'

export default function HeadsUpDisplay(props) {

    return (
        <Container>
            <Row>
                <Col>
                    <ScoreBoard name='scoreBoard' />
                </Col>
                <Col>
                    <Console />
                </Col>
            </Row>
        </Container>
    );
}