import { v4 as uuidv4 } from 'uuid'
import { User } from '../models/UserModels'
import * as session from '../models/SessionModels'
import { GoogleAuthService } from './GoogleAuthService'
import { IApiClientFactory, IApiClient, ApiClient } from './ApiClient'

/**
 * Session store interface
 */
export interface ISessionService {  
  initializeAsync(): Promise<ISessionService>
  loginGoogleAsync(): Promise<ISessionService>
  loginAnonymousAsync(): Promise<ISessionService>
  
  getSessionState(): session.SessionState | null
  updateSessionState(state: session.SessionState): void
  clearSessionState(): void
}

/**
 * Session store implementation
 */
export class SessionService implements ISessionService, IApiClientFactory {
  private readonly _url = `/session`
  private readonly _sessionStateKey = `session-state`
  private readonly _google: GoogleAuthService

  constructor(google: GoogleAuthService) {
    this._google = google
  }

  public async initializeAsync(): Promise<ISessionService> {
    await this._google.initialize()
    await this.validateSessionAsync()

    return this
  }

  public async loginGoogleAsync(): Promise<ISessionService> {
    const user = await this._google.requestToken()
    const result = await this.loadGoogleTokenAsync(user)

    this.updateSessionState(result)
    
    return this
  }

  public async loginAnonymousAsync(): Promise<ISessionService> {
    const user = {
      id: uuidv4(),
      name: 'Player 1'
    }
    
    const result = await this.loadAnonymousTokenAsync(user)

    this.updateSessionState(result)
    
    return this
  }

  public getSessionState(): session.SessionState | null {
    const state = localStorage.getItem(this._sessionStateKey)

    if (state) {
      return JSON.parse(state) as session.SessionState
    } else {
      return null
    }
  }

  public updateSessionState(state: session.SessionState): void {
    localStorage.setItem(this._sessionStateKey, JSON.stringify(state))
  }

  public clearSessionState() {
    localStorage.removeItem(this._sessionStateKey)
  }

  public createApiClient(): IApiClient {
    return new ApiClient(this.getToken())
  }

  private getToken(): string | null {
    const session = this.getSessionState()

    if (session) {
      return session.token
    } else {
      return null
    }
  }

  /**
   * Creates a new token by POST-ing to the API server /google endpoint
   */
  private async loadGoogleTokenAsync(user: User): Promise<session.SessionState> {
    const request = {
      id: user.id,
      name: user.name,
      authCode: user.authCode
    }

    // fetching a token so we want to use a token-less API client
    const api = new ApiClient(null)
    const response = await api.postAsync<object>(this._url + '/google', request)
    
    if (response.status === 200) {
      const token = response.headers.get(`set-authorization`)

      if (token) {
        return { user, token, gameId: null }
      } else {
        throw new Error(`Auth token was null`)
      }
    } else {
      console.error(response)
      throw new Error(`Token request failed with ${response.status}`)
    }
  }

  /**
   * Creates a new token by POST-ing to the API server /anonymous endpoint
   */
  private async loadAnonymousTokenAsync(user: User): Promise<session.SessionState> {
    const request = {
      id: user.id,
      name: user.name
    }

    // fetching a token so we want to use a token-less API client
    const api = new ApiClient(null)
    const response = await api.postAsync<object>(this._url + '/anonymous', request)
    
    if (response.status === 200) {
      const token = response.headers.get(`set-authorization`)

      if (token) {
        return { user, token, gameId: null }
      } else {
        throw new Error(`Auth token was null`)
      }
    } else {
      console.error(response)
      throw new Error(`Token request failed with ${response.status}`)
    }
  }

  /**
   * Validates the current token stored in local storage.
   * If no token is stored, or the token is invalid, null is returned.
   * If token is valid, the token is returned.
   */
  private async validateSessionAsync(): Promise<session.SessionState | null> {
    const sessionState = this.getSessionState()

    if (sessionState) {
      const response = await this.createApiClient().getAsync<session.SessionResponse>(this._url)

      if (response.body.sessionId) {
        return sessionState
      } else {
        return null
      }
    } else {
      return null
    }
  }
}
