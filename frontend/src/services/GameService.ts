import { IApiClientFactory } from "./ApiClient"
import * as models from "../models/GameModels"

export interface IGameService {
  listGamesAsync(): Promise<models.GamesOverview>
  loadGameAsync(id: string): Promise<models.Game | null>
  gameMoveAsync(id: string, move: models.Move): Promise<models.Game>
  createGameAsync(): Promise<models.Game>
  deleteGameAsync(id: string): Promise<void>
}

export class GameService implements IGameService {
  private readonly _apiClientFactory: IApiClientFactory

  constructor(apiClientFactory: IApiClientFactory) {
    this._apiClientFactory = apiClientFactory
  }

  public async listGamesAsync(): Promise<models.GamesOverview> {
    const response = await this.api.getAsync<models.GamesOverview>(this.rootUrl)
    return response.body
  }

  public async loadGameAsync(id: string): Promise<models.Game | null> {
    const response = await this.api.maybeGetAsync<models.Game>(this.gameUrl(id))
    return response.body
  }

  public async gameMoveAsync(id: string, move: models.Move): Promise<models.Game> {
    const response = await this.api.postAsync<models.Game>(this.moveUrl(id), move)
    return response.body
  }

  public async createGameAsync(): Promise<models.Game> {
    const response = await this.api.postAsync<models.Game>(this.rootUrl, {})
    return response.body
  }

  public async deleteGameAsync(id: string): Promise<void> {
    await this.api.deleteAsync<models.Game>(this.gameUrl(id))
  }

  private get api() {
    return this._apiClientFactory.createApiClient()
  }

  private get rootUrl() { return '/games' }
  private gameUrl(id: string) { return `${this.rootUrl}/${id}` }
  private moveUrl(id: string) { return `${this.gameUrl(id)}/moves` }
}
