import React from 'react'
import {Container, Row } from 'react-bootstrap'
import RegistrationForm from './Components/Registration';
import PopUpMessage from './Components/PopUpMessage';
import GameBoard from './Components/GameBoard';
import 'bootstrap/dist/css/bootstrap.min.css';
import { useGameStateStore } from './Services/Stores';

import background from './Assets/green_background.png';
import HeadsUpDisplay from './Components/HeadsUpDisplay';
import AlertPopUp from './Components/AlertPopUp';

function App() {
  const [isGameStarted,isRegistered,isConnected,isGameEnded] = useGameStateStore((state) => [state.isGameStarted,state.isRegistered,state.isConnected,state.isGameEnded]);
  
  let viewport;

  if(!isConnected && !isGameEnded) {
    //Lost Connection
    viewport = <PopUpMessage show={true} title="Lost Connection" message="Game has lost connection! Attempting to reconnect..."/>;
  }
  else if(isGameStarted) {
    viewport=<GameBoard/>;
    
  }
  else if(isRegistered) {
    viewport = <PopUpMessage show={true} title="Waiting for Game Start" message="Please wait for all players to connect and game to begin!"/>;
  }
  else if(!isGameEnded) {
    viewport = <RegistrationForm/>;
    // viewport = <PlayingCard onClick={(e) => console.log("cardClick",e)} cardEnum="CLUBS_JACK" isSelected={false}/> .
    // viewport = <GameBoard/>
  }
  return (
    <div className="App">
      <Container style={{ backgroundImage : `url(${background})`, minHeight: '100vh', minWidth: '100vw',backgroundSize : 'cover'}}>
        <Row style={{height: '70vh'}}>
          {viewport}
        </Row>
        <Row style={{height: '30vh'}}>
          <AlertPopUp/>
          <HeadsUpDisplay/>
        </Row>
      </Container>
    </div>
  );
}

export default App;
