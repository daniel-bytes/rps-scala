import SessionManager from './SessionManager'
import GameEngine from './GameEngine'
import * as models from '../models/GameModels'

export default class GameService {
  private readonly _session: SessionManager

  constructor(
    session: SessionManager
  ) {
    this._session = session
  }

  getCurrentGameId(): string | null {
    return this._session.getGameId()
  }

  setCurrentGameId(id: string | null) {
    return this._session.setGameId(id)
  }

  async listGamesAsync(): Promise<models.GamesOverview> {
    const response = await this.api.getAsync<models.GamesOverview>(this.rootUrl)
    console.log(`GameService#listGamesAsync ${JSON.stringify(response)}`)

    return response.body
  }

  async loadGameAsync(id: string): Promise<GameEngine | null> {
    const response = await this.api.maybeGetAsync<models.Game>(this.gameUrl(id))
    console.log(`GameService#loadGameAsync(${id}) ${JSON.stringify(response)}`)

    if (response.body) {
      this._session.setGameId(id)
      return new GameEngine(response.body)
    } else {
      this._session.setGameId(null)
      return null
    }
  }

  async gameMoveAsync(id: string, move: models.Move): Promise<GameEngine> {
    const response = await this.api.postAsync<models.Game>(this.moveUrl(id), move)
    console.log(`GameService#gameMoveAsync(${id}, ${JSON.stringify(move)}) ${JSON.stringify(response)}`)

    this._session.setGameId(response.body.gameId)
    return new GameEngine(response.body)
  }

  async createGameAsync(): Promise<GameEngine> {
    const response = await this.api.postAsync<models.Game>(this.rootUrl, {})
    console.log(`GameService#createGameAsync ${JSON.stringify(response)}`)

    this._session.setGameId(response.body.gameId)
    return new GameEngine(response.body)
  }

  async deleteGameAsync(id: string): Promise<GameEngine> {
    const response = await this.api.deleteAsync<models.Game>(this.gameUrl(id))
    console.log(`GameService#deleteGameAsync(${id}) ${JSON.stringify(response)}`)

    this._session.setGameId(null)
    return new GameEngine(response.body)
  }

  private get api() {
    return this._session.getApiClient()
  }

  private get rootUrl() { return '/games' }
  private gameUrl(id: string) { return `${this.rootUrl}/${id}` }
  private moveUrl(id: string) { return `${this.gameUrl(id)}/moves` }
}