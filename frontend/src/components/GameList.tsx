import React, { Component } from 'react';
import {GameOverview} from '../models/GameModels'

interface Props { 
  games: GameOverview[], 
  gameSelected: (id: string) => void 
}
interface State {}

export default class GameList extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = { ready: false }
  }

  gameClick(id: string) {
    this.props.gameSelected(id)
  }

  render() {
    return (
      <div className="GameList">
        <ul>
          {this.props.games.map(g =>
            <li key={g.id}>
              <button onClick={() => this.gameClick(g.id)}>
              {g.isGameOver ? "Game Over" : "In Progress"}
              </button>
            </li>
          )}
        </ul>
      </div>
    )
  }
}
