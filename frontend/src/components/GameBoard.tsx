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

    return (
      <tr className='CanvasTableRow' key={`row:${r}`}>
        {cells}
      </tr>
    )
  }

  onMouseDown(t: models.Token | undefined) {
    this.props.applicationStore!.gameBeginMoveToken(t)
  }

  async onMouseUp(t: models.Token | undefined, p: models.Point) {
    await this.props.applicationStore!.gameMoveToPointAsync(p)
  }

  getTableCell(game: GameEngine, r: number, c: number): JSX.Element {
    const token = game.getToken({ x: c, y: r})
    const hasSelection = !!this.props.applicationStore!.selectedToken
    const isSelected = hasSelection && this.props.applicationStore!.selectedToken === token
    const isTarget = this.props.applicationStore!.targetPoints.some(
      p => p.x === c && p.y === r
    )

    return (
      <GameToken
        x={c}
        y={r}
        key={`GameToken:${r}:${c}`}
        isSelected={isSelected}
        isTarget={hasSelection && isTarget}
        token={token}
        onMouseDown={this.onMouseDown.bind(this)}
        onMouseUp={this.onMouseUp.bind(this)} />
    )
  }

  render() {
    const engine = this.props.applicationStore!.gameEngine!
    const rows = this.getTableRows(engine)

    return (
      <div className="container">
        <table className='CanvasTable table is-bordered is-fullwidth'>
          <tbody>
            {rows}
          </tbody>
        </table>
      </div>
    )
  }
}
