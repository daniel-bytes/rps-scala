import React, { Component } from 'react';
import * as models from '../models/GameModels'
import GameEngine from '../services/GameEngine'
import GameService from '../services/GameService'
import GameToken from './GameToken'
import './GameBoard.css'

export interface GameBoardProps {
  gameService: GameService
}

export interface GameBoardState {
  game: GameEngine | null
  selectedToken: models.Token | undefined
  targetPoints: models.Point[]
}

export default class GameBoard extends Component<GameBoardProps, GameBoardState> {
  constructor(props: GameBoardProps) {
    super(props)
    this.state = { 
      game: null, 
      selectedToken: undefined,
      targetPoints: []
    }
  }

  async componentDidMount() {
    const game = await this.props.gameService.loadGameAsync()
    this.setState({ game })
  }

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
    if (!this.canMove()) return
    let targets: models.Point[] = []

    if (this.state.game && t) {
      targets = this.state.game.getTargetPoints(t.position)
    }

    this.setState({
      selectedToken: t,
      targetPoints: targets
    })
  }

  onMouseUp(t: models.Token | undefined, p: models.Point) {
    if (!this.canMove()) return
    if (!!this.state.selectedToken && !!this.state.game) {
      const move = { from: this.state.selectedToken.position, to: p }

      if (this.state.game.isValidMove(move)) {
        return this.props.gameService.gameMoveAsync(move).then(result => {
          this.setState({
            selectedToken: undefined,
            targetPoints: [],
            game: result
          })
        })
      }
    }
    
    this.setState({
      selectedToken: undefined,
      targetPoints: []
    })
  }

  getHeader() {
    if (this.state.game) {
      if (this.state.game.model.isGameOver) {
        return `Game Over: ${this.state.game.model.winnerId} wins!`
      }

      if (this.state.game.canMove) {
        return 'Your Turn'
      }
    }

    return ''
  }

  getTableCell(game: GameEngine, r: number, c: number): JSX.Element {
    const token = game.getToken({ x: c, y: r})
    const hasSelection = !!this.state.selectedToken
    const isSelected = hasSelection && this.state.selectedToken === token
    const isTarget = this.state.targetPoints.some(
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

  canMove(): boolean {
    return !!this.state.game && this.state.game.canMove()
  }

  render() {
    const rows = this.state.game ? this.getTableRows(this.state.game) : []

    return (
      <div className='Container'>
        <header className='CanvasHeader'>
          <h1>{this.getHeader()}</h1>
        </header>
        <section className='CanvasBody'>
          <table className='CanvasTable'>
            <tbody>
              {rows}
            </tbody>
          </table>
        </section>
        <footer></footer>
      </div>
    );
  }
}
