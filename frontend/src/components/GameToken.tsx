import React, { Component, MouseEventHandler } from 'react';
import * as models from '../models/GameModels'
import './GameBoard.css'

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
    const text = this.props.token ? this.props.token.tokenType : ``

    return (
      <td 
        className={this.getCssClass()} 
        onMouseDown={this.onMouseDown.bind(this)}
        onMouseUp={this.onMouseUp.bind(this)}
      >
        {text}
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
