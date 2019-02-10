import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';
import GameBoard from './components/GameBoard'
import GameService from './services/GameService'
import SessionManager from './services/SessionManager'

interface Props {}
interface State { ready: boolean }

class App extends Component<Props, State> {
  private sessionManager = new SessionManager(`player1`)
  private gameService = new GameService('game1', this.sessionManager)

  constructor(props: Props) {
    super(props)
    this.state = { ready: false }
  }

  async componentDidMount() {
    await this.sessionManager.getTokenAsync()
    this.setState({ ready: true })

    const game = await this.gameService.loadGameAsync()
    console.log(game)
  }

  render() {
    const inner = this.state.ready ? (
      <div>Welcome!</div>
    ) : ( <div>Loading... </div> )

    return (
      <div className="App">
        <header className="AppBody">
          <GameBoard gameService={this.gameService} />
        </header>
      </div>
    );
  }
}

export default App;
