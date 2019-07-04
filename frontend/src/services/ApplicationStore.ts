import { observable, action, computed, runInAction } from 'mobx'
import { NavigationState } from '../models/NavigationModels'
import * as models from '../models/GameModels'
import { IGameService } from './GameService'
import { ISessionService } from './SessionService'
import GameEngine from './GameEngine'
import { ApiError } from '../errors/ApiError'

export interface IApplicationStore {
  readonly apiError: string | null
  readonly subtitle: string | null
  readonly isLoading: boolean
  readonly sessionInitialized: boolean
  readonly loggedIn: boolean
  readonly gameInProgress: boolean
  readonly navState: NavigationState

  readonly game: models.Game | null
  readonly gameEngine: GameEngine | null

  readonly selectedToken: models.Token | undefined
  readonly targetPoints: models.Point[]

  clearError(): void

  initializeSessionStoreAsync(): Promise<IApplicationStore>
  loginAsync(): Promise<IApplicationStore>
  logout(): IApplicationStore

  // Game app
  initializeGameAppAsync(): Promise<IApplicationStore>
  gameBeginMoveToken(token: models.Token | undefined): void
  gameMoveToPointAsync(point: models.Point): Promise<IApplicationStore>

  
  // Nav buttons
  endGameButtonPressedAsync(): Promise<IApplicationStore>
  signInButtonPressedAsync(): Promise<IApplicationStore>
  signOutButtonPressedAsync(): Promise<IApplicationStore>
}

/**
 * Main mobx store for the application
 */
export class ApplicationStore implements IApplicationStore {
  private readonly _gameStore: IGameService
  private readonly _sessionStore: ISessionService
  
  constructor(gameStore: IGameService, sessionStore: ISessionService) {
    this._gameStore = gameStore
    this._sessionStore = sessionStore
  }

  @observable
  public apiError: string | null = null

  @observable 
  public loadingCount = 0

  @observable 
  public selectedToken: models.Token | undefined = undefined

  @observable 
  public targetPoints: models.Point[] = []

  @observable 
  public game: models.Game | null = null

  @observable
  public sessionInitialized: boolean = false

  @observable
  public loggedIn: boolean = false

  @computed
  public get subtitle(): string | null {
    if (this.loggedIn) {
      if (this.game) {
        if (this.game.isGameOver) {
          return `Game Over: ${this.game.winnerName} wins!`
        } else if (this.game.isPlayerTurn) {
          return "Your Turn"
        }
      }
    }

    return ""
  }

  @computed
  public get gameInProgress(): boolean {
    return !!this.game
  }

  @computed
  public get isLoading(): boolean {
    return this.loadingCount > 0
  }

  @computed.struct
  public get gameEngine(): GameEngine | null {
    if (!this.game) return null
    
    return new GameEngine(this.game)
  }

  @computed
  public get navState(): NavigationState {
    if (!this.loggedIn) {
      return NavigationState.LoginPage
    } else if (this.game) {
      return NavigationState.PlayGamePage
    } else {
      return NavigationState.ListGamesPage
    }
  }

  @action.bound
  public clearError(): void {
    this.apiError = null
  }

  @action.bound
  public async initializeSessionStoreAsync(): Promise<IApplicationStore> {
    await this.apiAction(async () => {
      await this._sessionStore.initializeAsync()

      const sessionState = this._sessionStore.getSessionState()
      
      runInAction(() => { 
        this.sessionInitialized = true
        this.loggedIn = !!sessionState
      })
    })

    return this
  }

  @action.bound
  public async loginAsync(): Promise<IApplicationStore> {
    await this.apiAction(async () => {
      await this._sessionStore.loginAsync()

      runInAction(() => { 
        this.loggedIn = true 
      })
    })

    return this
  }

  @action.bound
  public logout(): IApplicationStore {
    this._sessionStore.clearSessionState()
    this.loggedIn = false
    return this
  }

  @action.bound
  public async initializeGameAppAsync(): Promise<IApplicationStore> {
    await this.apiAction(async () => {
      const overview = await this._gameStore.listGamesAsync()

      let game: models.Game | null = null
      const sessionState = this._sessionStore.getSessionState()

      // load game from session
      if (sessionState && sessionState.gameId) {
        game = await this._gameStore.loadGameAsync(sessionState.gameId)
      }

      if (!game) {
        // load game from overview
        for (let i = 0; i < overview.games.length; i++) {
          game = await this._gameStore.loadGameAsync(overview.games[i].id)
          if (game) break
        }
      }

      if (!game) {
        // create a new game
        game = await this._gameStore.createGameAsync()
      }

      runInAction(() => {
        this.game = game
        
        if (game) {
          this.setGameInSession(game)
        }
      })
    })

    return this
  }

  @action.bound
  public gameBeginMoveToken(token: models.Token | undefined): void {
    const engine = this.gameEngine!

    if (engine.canMove()) {
      let targets: models.Point[] = []

      if (token) {
        targets = engine.getTargetPoints(token.position)
      }

      this.selectedToken = token
      this.targetPoints = targets
    }
  }

  @action.bound
  public async gameMoveToPointAsync(point: models.Point): Promise<IApplicationStore> {
    await this.apiAction(async () => {
      const engine = this.gameEngine!
      
      if (engine.canMove() && this.selectedToken) {
        const move = { from: this.selectedToken.position, to: point }

        if (engine.isValidMove(move)) {
          const game = await this._gameStore.gameMoveAsync(engine.model.gameId, move)

          runInAction(() => {
            this.game = game
            this.selectedToken = undefined
            this.targetPoints = []
          })
        }
      }
    })

    return this
  }

  @action.bound
  public async endGameButtonPressedAsync(): Promise<IApplicationStore> {
    await this.apiAction(async () => {
      await this._gameStore.deleteGameAsync(this.game!.gameId)

      runInAction(() => {
        this.clearGameFromSession()
      })

      await this.initializeGameAppAsync()
    })
    return this
  }

  @action.bound
  public async signInButtonPressedAsync(): Promise<IApplicationStore> {
    await this.apiAction(async () => {
      await this._sessionStore.loginAsync()
      
      runInAction(() => {
        this.loggedIn = true
      })
    })
    return this
  }

  @action.bound
  public signOutButtonPressedAsync(): Promise<IApplicationStore> {
    return new Promise((resolve) => {
      runInAction(() => {
        this._sessionStore.clearSessionState()
        this.loggedIn = false
      })
      return resolve(this)
    })
  }

  private clearGameFromSession(): void {
    const sessionState = this._sessionStore.getSessionState()

    if (sessionState) {
      sessionState.gameId = null
      this._sessionStore.updateSessionState(sessionState)
    }

    this.game = null
  }

  private setGameInSession(game: models.Game): void {
    const sessionState = this._sessionStore.getSessionState()!
    sessionState.gameId = game.gameId
    this._sessionStore.updateSessionState(sessionState)
    this.game = game
  }

  private async apiAction(func: () => Promise<void>) {
    try {
      runInAction(() => {
        this.apiError = null
        this.loadingCount++
      })

      await func()
    } catch(e) {
      console.error(`API error: ${e.message}`)

      if (e instanceof ApiError) {
        if (e.status === 401) {
          this._sessionStore.clearSessionState()
        }
        
        runInAction(() => {
          this.apiError = e.code
        })
      }
    } finally {
      runInAction(() => {
        this.loadingCount--
      })
    }
  }
}
