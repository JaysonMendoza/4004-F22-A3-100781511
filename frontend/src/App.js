import React from 'react'
import {Container} from 'react-bootstrap'
import RegistrationForm from './Components/Registration';
import WaitingForGameStart from './Components/WaitingToStart';
import GameBoard from './Components/GameBoard';
import 'bootstrap/dist/css/bootstrap.min.css';
import { useGameStateStore } from './Services/Stores';

import background from './Assets/green_background.png'

function App() {
  const [isGameStarted,isRegistered] = useGameStateStore((state) => [state.isGameStarted,state.isRegistered]);
  let viewport;
  if(isGameStarted) {
    viewport=<GameBoard/>;
  }
  else if(isRegistered) {
    viewport = <WaitingForGameStart/>;
  }
  else {
    viewport = <RegistrationForm/>;
  }
  return (
    <div className="App">
      <Container style={{ backgroundImage : `url(${background})`, minHeight: '100vh', minWidth: '100vw',backgroundSize : 'cover'}}>
        {viewport}
      </Container>
    </div>
  );
}

export default App;
