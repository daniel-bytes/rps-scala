import React, { Component, MouseEventHandler } from 'react';
import * as models from '../models/GameModels'

export interface GameTokenProps {
  x: number
  y: number
  isSelected: boolean
  isTarget: boolean
  token: models.Token | undefined
  onMouseDown: (t: models.Token | undefined) => void
  onMouseUp: (t: models.Token | undefined, p: models.Point) => void
}

export default class GameToken extends Component<GameTokenProps> {
  getCssClass(): string {
    let cssClass = 'CanvasTableCell'
    
    if (this.isPlayerOwned) {
      cssClass += ' PlayerCell'
    }

    if (this.props.isSelected) {
      cssClass += ' SelectedCell'
    }

    if (this.props.isTarget) {
      cssClass += ' TargetCell'
    }

    return cssClass
  }

  getTokenType(): string {
    if (this.props.token) {
      switch (this.props.token.tokenType) {
        case 'other': return 'Player 2'
        default: return this.props.token.tokenType
      }
    }

    return ''
  }

  onMouseDown() {
    if (this.isPlayerOwned && this.props.onMouseDown) {
      this.props.onMouseDown(this.props.token)
    }
  }

  onMouseUp() {
    if (this.props.onMouseUp) {
      this.props.onMouseUp(this.props.token, this.point)
    }
  }
  
  render() {
    return (
      <td 
        className={this.getCssClass()} 
        onMouseDown={this.onMouseDown.bind(this)}
        onMouseUp={this.onMouseUp.bind(this)}
      >
        {this.getTokenType()}
      </td>
    )
  }

  get isPlayerOwned(): boolean {
    return !!this.props.token && this.props.token.playerOwned
  }

  get point(): models.Point {
    return { x: this.props.x, y: this.props.y }
  }
}
