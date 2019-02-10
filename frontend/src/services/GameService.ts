import SessionManager from './SessionManager'
import GameEngine from './GameEngine'
import * as models from '../models/GameModels'

export default class GameService {
  private readonly _gameUrl: string
  private readonly _moveUrl: string
  private readonly _id: string
  private readonly _session: SessionManager

  constructor(
    id: string, 
    session: SessionManager
  ) {
    this._id = id
    this._session = session
    this._gameUrl = `/games/${this._id}`
    this._moveUrl = `${this._gameUrl}/moves`
  }

  async loadGameAsync(): Promise<GameEngine> {
    const response = await this.api.getAsync<models.Game>(this._gameUrl)
    return new GameEngine(response.body)
  }

  async gameMoveAsync(move: models.Move): Promise<GameEngine> {
    const response = await this.api.postAsync<models.Game>(this._moveUrl, move)
    return new GameEngine(response.body)
  }

  private get api() {
    return this._session.getApiClient()
  }
}