import { observable, action, computed, runInAction } from 'mobx'
import { NavigationState, AuthenticationType } from '../models/NavigationModels'
import * as models from '../models/GameModels'
import { IGameService } from './GameService'
import { ISessionService } from './SessionService'
import GameEngine from './GameEngine'
import { ApiError } from '../errors/ApiError'

export interface IApplicationStore {
  readonly apiError: string | null
  readonly subtitle: string | null
  readonly combat: string[]
  readonly isLoading: boolean
  readonly sessionInitialized: boolean
  readonly loggedIn: boolean
  readonly gameInProgress: boolean
  readonly navState: NavigationState

  readonly game: models.Game | null
  readonly gameEngine: GameEngine | null

  readonly selectedToken: models.Token | undefined
  readonly targetPoints: models.Point[]

  readonly authenticationType: AuthenticationType

  clearError(): void

  initializeSessionStoreAsync(): Promise<IApplicationStore>
  loginGoogleAsync(): Promise<IApplicationStore>
  logout(): IApplicationStore

  // Game app
  initializeGameAppAsync(): Promise<IApplicationStore>
  gameTrySelectTokenAsync(token: models.Token | undefined, point: models.Point): Promise<IApplicationStore>
  gameMoveToPointAsync(point: models.Point): Promise<IApplicationStore>

  
  // Nav buttons
  endGameButtonPressedAsync(): Promise<IApplicationStore>
  signInGoogleButtonPressedAsync(): Promise<IApplicationStore>
  signInAnonymousButtonPressedAsync(): Promise<IApplicationStore>
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

  @observable
  public authenticationType: AuthenticationType = AuthenticationType.Anonymous

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
  public get combat(): string[] {
    if (this.loggedIn && this.game) {
      const g = this.game 

      return g.recentMoves.reduce<string[]>((prev, cur) => {
        if (cur.combatSummary) {
          const s = cur.combatSummary
          const them = g.otherPlayerName || 'AI'

          let str = cur.playerId === g.playerId 
            ? `Your ${s.attackerTokenType} attacked ${them}'s ${s.defenderTokenType}: `
            : `${them}'s ${s.attackerTokenType} attacked your ${s.defenderTokenType}: `

          if (s.winnerTokenType)
            str += `${s.winnerTokenType} wins!`
          else 
            str += "everybody loses"
          
          prev.push(str)
        }

        return prev
      }, [])
    }
    return []
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
  public async loginGoogleAsync(): Promise<IApplicationStore> {
    await this.apiAction(async () => {
      await this._sessionStore.loginGoogleAsync()

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
        this.selectedToken = undefined
        this.targetPoints = []
        
        if (game) {
          this.setGameInSession(game)
        }
      })
    })

    return this
  }

  @action.bound
  public gameTrySelectTokenAsync(token: models.Token | undefined, point: models.Point): Promise<IApplicationStore> {
    const engine = this.gameEngine!

    if (engine.canMove()) {
      if (this.selectedToken && GameEngine.pointsEqual(point, this.selectedToken.position)) {
        this.selectedToken = undefined;
        this.targetPoints = [];
        return new Promise(r => r(this));
      }

      if (token && token.playerOwned && engine.canMoveToken(token)) {
        this.selectedToken = token
        this.targetPoints = engine.getTargetPoints(token.position)
        return new Promise(r => r(this));
      }

      if (this.selectedToken && this.targetPoints.some(p => GameEngine.pointsEqual(p, point))) {
        return this.gameMoveToPointAsync(point)
      }
    }

    return new Promise(r => r(this));
  }

  @action.bound
  public async gameMoveToPointAsync(point: models.Point): Promise<IApplicationStore> {
    await this.apiAction(async () => {
      const engine = this.gameEngine!
      
      if (engine.canMove() && this.selectedToken) {
        const move = { 
          from: this.selectedToken.position, 
          to: point,
          version: engine.model.version
        }

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
  public async signInGoogleButtonPressedAsync(): Promise<IApplicationStore> {
    await this.apiAction(async () => {
      await this._sessionStore.loginGoogleAsync()
      
      runInAction(() => {
        this.loggedIn = true
      })
    })
    return this
  }

  @action.bound
  public async signInAnonymousButtonPressedAsync(): Promise<IApplicationStore> {
    await this.apiAction(async () => {
      await this._sessionStore.loginAnonymousAsync()
      
      runInAction(() => {
        this.loggedIn = true
      })
    })
    return this
  }

  @action.bound
  public async signOutButtonPressedAsync(): Promise<IApplicationStore> {
    await this.apiAction(async () => {
      await this._gameStore.deleteGameAsync(this.game!.gameId)
      
      runInAction(() => {
        this._sessionStore.clearSessionState()
        this.loggedIn = false
      })
    })
    return this
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
        } else if (e.status === 409) {
          // reload app on version conflict
          await this.initializeGameAppAsync()
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
