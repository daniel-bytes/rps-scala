import React, { Component } from 'react'
import { observer, inject } from 'mobx-react'
import * as models from '../models/GameModels'
import GameEngine from '../services/GameEngine'
import GameToken from './GameToken'
import { IApplicationStore } from '../services/ApplicationStore'

export interface GameBoardProps {
  applicationStore?: IApplicationStore
}

@inject("applicationStore")
@observer
export default class GameBoard extends Component<GameBoardProps> {

  getTableRows(game: GameEngine): JSX.Element[] {
    let rows: JSX.Element[] = []

    if (game) {
      for (let i = 0; i < game.model.board.rows; i++) {
        const r = game.model.board.rows - i - 1
        rows.push(this.getTableRow(game, r))
      }
    }

    return rows
  }

  getTableRow(game: GameEngine, r: number): JSX.Element {
    let cells: JSX.Element[] = []

    for (let c = 0; c < game.model.board.columns; c++) {
      cells.push(this.getTableCell(game, r, c))
    }

    const id = `canvas-table-row:${r}`

    return (
      <tr className='canvas-table-row' key={id} id={id}>
        {cells}
      </tr>
    )
  }

  getTableCell(game: GameEngine, r: number, c: number): JSX.Element {
    const store = this.props.applicationStore!

    let isSelected = false
    const point = { x: c, y: r }
    const token = game.getToken(point)
    const hasSelection = !!store.selectedToken
                        
    const isTarget = this.props.applicationStore!.targetPoints.some(
      p => GameEngine.pointsEqual(point, p)
    )

    if (hasSelection && store.selectedToken && token) {
      isSelected = GameEngine.pointsEqual(store.selectedToken.position, token.position)
    }

    const id = `canvas-table-row:${r}:${c}`

    return (
      <GameToken
        point={point}
        id={id}
        key={id}
        isPlayer={!!token && token.playerOwned}
        isOtherPlayer={!!token && !token.playerOwned}
        isSelected={isSelected}
        isTarget={hasSelection && isTarget}
        token={token}
        applicationStore={this.props.applicationStore}
        onMouseDown={this.onMouseDown.bind(this)} />
    )
  }

  async onMouseDown(t: models.Token | undefined, p: models.Point) {
    await this.props.applicationStore!.gameTrySelectTokenAsync(t, p)
  }

  render() {
    const engine = this.props.applicationStore!.gameEngine!
    const rows = this.getTableRows(engine)
    
    return (
      <table id='canvas-table'>
        <tbody>
          {rows}
        </tbody>
      </table>
    )
  }
}
