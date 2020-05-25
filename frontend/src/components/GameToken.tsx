import React, { Component } from 'react'
import * as models from '../models/GameModels'
import { IApplicationStore } from '../services/ApplicationStore'
import GameEngine from '../services/GameEngine'

export interface GameTokenProps {
  id: string
  point: models.Point
  isSelected: boolean
  isTarget: boolean
  isPlayer: boolean
  isOtherPlayer: boolean
  token: models.Token | undefined
  applicationStore?: IApplicationStore
  onMouseDown: (t: models.Token | undefined, p: models.Point) => void
}

export default class GameToken extends Component<GameTokenProps> {
  getCssClass(): string {
    const applicationStore = this.props.applicationStore!
    const game = applicationStore.game!

    let cssClass = 'canvas-table-cell'
    
    const isTarget = applicationStore.targetPoints.some(p => GameEngine.pointsEqual(p, this.props.point))
    
    const hasTargets = this.props.token && 
      GameEngine.isMovableTokenType(this.props.token) && 
      GameEngine.getTargetPoints(game, this.props.point).length > 0

    if (isTarget || hasTargets) {
      cssClass += ' cell-movable'
    }

    if (this.props.isSelected) {
      cssClass += ' selected-cell'
    } else if (this.props.isTarget) {
      cssClass += ' target-cell'
    } else if (this.props.isPlayer) {
      cssClass += ' player-cell'
    } else if (this.props.isOtherPlayer) {
      cssClass += ' other-player-cell'
    }

    return cssClass
  }

  getTokenType(): string {
    if (this.props.token) {
      switch (this.props.token.tokenType) {
        case models.TokenType.other: return 'Player 2'
        default: return this.props.token.tokenType.toString()
      }
    }

    return ''
  }

  onMouseDown() {
    if (this.props.onMouseDown) {
      this.props.onMouseDown(this.props.token, this.props.point)
    }
  }
  
  render() {
    return (
      <td 
        id={this.props.id}
        className={this.getCssClass()} 
        onMouseDown={this.onMouseDown.bind(this)}
      >
        {this.getTokenType()}
      </td>
    )
  }

  get isPlayerOwned(): boolean {
    return !!this.props.token && this.props.token.playerOwned
  }

  get point(): models.Point {
    return this.props.point
  }
}
