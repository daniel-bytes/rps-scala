import React, { Component } from 'react';
import * as models from '../models/GameModels'
import GameEngine from '../services/GameEngine'
import GameService from '../services/GameService'
import GameToken from './GameToken'

export interface GameBoardProps {
  game: GameEngine
  gameService: GameService
}

export interface GameBoardState {
  game: GameEngine
  selectedToken: models.Token | undefined
  targetPoints: models.Point[]
}

export default class GameBoard extends Component<GameBoardProps, GameBoardState> {
  constructor(props: GameBoardProps) {
    super(props)
    this.state = { 
      game: props.game, 
      selectedToken: undefined,
      targetPoints: []
    }
  }

  get currentPlayerName(): string {
    if (this.state.game.model.isPlayerTurn) {
      return this.state.game.model.playerName
    } else {
      return this.state.game.model.otherPlayerName
    }
  }

  get currentGame(): GameEngine {
    return this.state.game
  }

  get currentGameId(): string {
    return this.currentGame.model.gameId
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

    if (t) {
      targets = this.currentGame.getTargetPoints(t.position)
    }

    this.setState({
      selectedToken: t,
      targetPoints: targets
    })
  }

  onMouseUp(t: models.Token | undefined, p: models.Point) {
    if (!this.canMove()) return
    if (!!this.state.selectedToken) {
      const move = { from: this.state.selectedToken.position, to: p }

      if (this.currentGame.isValidMove(move)) {
        return this.props.gameService.gameMoveAsync(this.currentGameId, move).then(result => {
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
    if (this.currentGame) {
      if (this.currentGame.model.isGameOver) {
        return `Game Over: ${this.currentGame.model.winnerName} wins!`
      }

      if (this.currentGame.canMove) {
        return `${this.currentPlayerName}'s Turn`
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
    return this.currentGame.canMove()
  }

  render() {
    const rows = this.getTableRows(this.state.game)

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
