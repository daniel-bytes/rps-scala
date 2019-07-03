import React, { Component } from 'react'
import { observer, inject } from 'mobx-react'
import { IApplicationStore } from '../services/ApplicationStore'

interface Props {
  applicationStore?: IApplicationStore
}

@inject("applicationStore")
@observer
export default class GameList extends Component<Props> {
  constructor(props: Props) {
    super(props)
    this.state = { ready: false }
  }

  render() {
    return (
      <div className="GameList">
        <ul>
          {this.props.applicationStore!.gamesOverview.games.map(g =>
            <li key={g.id}>
              <button className="button" onClick={() => this.props.applicationStore!.playGameButtonPressedAsync(g.id)}>
              {g.isGameOver ? "Game Over" : "In Progress"}
              </button>
            </li>
          )}
        </ul>
      </div>
    )
  }
}
