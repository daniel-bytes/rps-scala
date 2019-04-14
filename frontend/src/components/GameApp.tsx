import React, { Component } from 'react';
import GameBoard from './GameBoard'
import GameList from './GameList'
import {User} from '../models/UserModels'
import GameService from '../services/GameService'
import GameEngine from '../services/GameEngine'
import { GameOverview } from '../models/GameModels'

interface Props { 
  user: User, 
  gameService: GameService 
}

interface State { 
  ready: boolean, 
  game: GameEngine | null
  games: GameOverview[]
}

export default class GameApp extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = { ready: false, game: null, games: [] }
  }

  async createGame() {
    this.setState({ ready: false })
    const result = await this.props.gameService.createGameAsync()
    this.setState({ ready: true, game: result })
  }

  async loadGame(id: string) {
    this.setState({ ready: false })
    const result = await this.props.gameService.loadGameAsync(id)
    this.setState({ ready: true, game: result })
  }

  async loadGames() {
    this.setState({ ready: false, games: [] })
    const result = await this.props.gameService.listGamesAsync()
    this.setState({ ready: true, games: result.games })
  }

  async leaveGame() {
    this.props.gameService.setCurrentGameId(null)
    this.setState({ ready: false, game: null })

    await this.loadGames()
  }

  async deleteGame() {
    const id = this.props.gameService.getCurrentGameId()

    if (id) {
      this.setState({ ready: false })
      await this.props.gameService.deleteGameAsync(id)

      this.setState({ ready: true, game: null })
      await this.loadGames()
    }
  }

  async componentDidMount() {
    const id = this.props.gameService.getCurrentGameId()

    if (id) {
      await this.loadGame(id)
    } else {
      await this.loadGames()
    }
  }

  render() {
    if (this.state.ready) {
      if (this.state.game) {
        return (
          <div>
            <header className="AppBody">
              <div>
                <button id="exit-game" onClick={this.leaveGame.bind(this)}>Home</button>
                <button id="delete-game" onClick={this.deleteGame.bind(this)}>End Game</button>
              </div>
              <GameBoard 
                gameService={this.props.gameService} 
                game={this.state.game} 
              />
            </header>
          </div>
        )
      } else {
        return (
          <div>
            <header className="AppBody">
              <GameList 
                games={this.state.games}
                gameSelected={this.loadGame.bind(this)}
              />
            </header>
            <div>
              <button id="create-game" onClick={this.createGame.bind(this)}>Start New Game</button>
            </div>
          </div>
        )
      }
    } else {
      return ( <div>loading... </div> )
    }
  }
}
