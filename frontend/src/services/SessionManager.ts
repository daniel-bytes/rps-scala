import * as models from '../models/SessionModels'
import ApiClient from './ApiClient'

/**
 * Service for managing a user's session token
 */
export default class SessionManager {
  private readonly _url = `/session`
  private readonly _name: string
  private readonly _key: string
  private readonly _api: ApiClient
  private _apiWithToken: ApiClient

  constructor(name: string) {
    this._name = name
    this._key = `session-token-${name}`
    this._api = new ApiClient()
    this._apiWithToken = new ApiClient(this.getToken())
  }

  hasToken(): boolean {
    return this.getToken() !== null
  }

  getApiClient(): ApiClient {
    return this._apiWithToken
  }

  /**
   * Gets a valid session token, either from local storage or
   * directly from the API server
   * @param forceRefresh If true, a new token will always be requested
   */
  async getTokenAsync(forceRefresh: boolean = false): Promise<string> {
    let token = null

    if (!forceRefresh) {
      token = await this.validateTokenAsync()
    }

    if (token) {
      console.log(`Session token valid for user [${this._name}]`)
      return token as string
    } else {
      return this.loadTokenAsync()
    }
  }

  /**
   * Returns a session token from local storage, or null if not found
   */
  private getToken(): string | null {
    return localStorage.getItem(this._key)
  }

  /**
   * Stores a token in local storage
   * @param token The session token
   */
  private setToken(token: string): string {
    localStorage.setItem(this._key, token)
    this._apiWithToken = new ApiClient(token)
    return token
  }

  /**
   * Creates a new token by POST-ing to the API server
   */
  private async loadTokenAsync(): Promise<string> {
    console.log(`Requesting new session token from server for user [${this._name}]`)

    const request = { name: this._name }
    const response = await this._api.postAsync<object>(this._url, request)
    const token = response.headers.get(`Set-Authorization`)

    if (token) {
      console.log(`Session token loaded for user [${this._name}]`)
      return this.setToken(token)
    } else {
      throw new Error(`Auth token was null`)
    }
  }

  /**
   * Validates the current token stored in local storage.
   * If no token is stored, or the token is invalid, null is returned.
   * If token is valid, the token is returned.
   */
  private async validateTokenAsync(): Promise<string | null> {
    console.log(`Checking for valid session token in local storage for user [${this._name}]`)

    if (!this.hasToken()) {
      return new Promise(res => res(null))
    }

    const response = await this._apiWithToken.getAsync<models.SessionResponse>(this._url)

    if (response.body.playerId) {
      return this.getToken()
    } else {
      return null
    }
  }
}