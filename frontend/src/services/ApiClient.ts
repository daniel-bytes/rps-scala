import * as errors from '../errors/ApiError'
import * as models from '../models/ApiModels'

/**
 * An HTTP JSON REST client
 */
export interface IApiClient {
  /**
   * Executes an HTTP GET request and converts the JSON response to the specified type
   * @param url The URL to request
   */
  getAsync<TRes>(url: string): Promise<models.ApiResponse<TRes>>

  /**
   * Executes an HTTP GET request and converts the JSON response to the specified type,
   * or null if a 404 is returned
   * @param url The URL to request
   */
  maybeGetAsync<TRes>(url: string): Promise<models.ApiResponse<TRes | null>>

  /**
   * Executes a JSON HTTP POST request and converts the response to the specified type
   * @param url The URL to request
   * @param req The request body
   */
  postAsync<TRes>(
    url: string, 
    req: object
  ): Promise<models.ApiResponse<TRes>> 

  /**
   * Executes a JSON HTTP PUT request and converts the response to the specified type
   * @param url The URL to request
   * @param req The request body
   */
  putAsync<TRes>(
    url: string, 
    req: object
  ): Promise<models.ApiResponse<TRes>> 

  /**
   * Executes an HTTP DELETE request and converts the JSON response to the specified type
   * @param url The URL to request
   */
  deleteAsync<TRes>(url: string): Promise<models.ApiResponse<TRes>>
}

/**
 * A factory for creating IApiClient instances from API tokens
 */
export interface IApiClientFactory {
  /**
   * Returns a new IApiClient instance
   */
  createApiClient(): IApiClient
}

/**
 * Implementation of IApiClient.  Not exported, need to use ApiClientFactory to get an instance.
 */
export class ApiClient implements IApiClient {
  private readonly _token: string | null

  constructor(token: string | null = null) {
    this._token = token
  }

  public async getAsync<TRes>(url: string): Promise<models.ApiResponse<TRes>> {
    const response = await fetch(url, {
      method: `GET`,
      headers: this.getHeaders()
    })

    return this.handleResponse<TRes>(response)
  }

  public async maybeGetAsync<TRes>(url: string): Promise<models.ApiResponse<TRes | null>> {
    const response = await fetch(url, {
      method: `GET`,
      headers: this.getHeaders()
    })

    try {
      return await this.handleResponse<TRes>(response)
    } catch(e) {
      if (e instanceof errors.ApiError && e.status === 404) {
        return {
          status: e.status,
          body: null,
          headers: e.headers
        }
      } else {
        throw e
      }
    }
  }

  public async postAsync<TRes>(
    url: string, 
    req: object
  ): Promise<models.ApiResponse<TRes>> 
  {
    const response = await fetch(url, {
      method: `POST`,
      headers: this.getHeaders(true),
      body: JSON.stringify(req)
    })

    return this.handleResponse<TRes>(response)
  }

  public async putAsync<TRes>(
    url: string, 
    req: object
  ): Promise<models.ApiResponse<TRes>> 
  {
    const response = await fetch(url, {
      method: `PUT`,
      headers: this.getHeaders(true),
      body: JSON.stringify(req)
    })

    return this.handleResponse<TRes>(response)
  }

  public async deleteAsync<TRes>(url: string): Promise<models.ApiResponse<TRes>> {
    const response = await fetch(url, {
      method: `DELETE`,
      headers: this.getHeaders()
    })

    return this.handleResponse<TRes>(response)
  }

  private getHeaders(isJson: boolean = false): Headers {
    let headers: Headers = new Headers()

    if (this._token) {
      headers.append(`Authorization`, this._token)
    }

    if (isJson) {
      headers.append(`Content-Type`, `application/json`)
    }

    return headers
  }

  private async handleResponse<TRes>(
    response: Response
  ): Promise<models.ApiResponse<TRes>> 
  {
    if (response.status < 300) {
      const body = await response.json() as TRes
      return {
        status: response.status,
        body: body,
        headers: new Map<string, string>(response.headers)
      }
    }

    const responseBody = await response.json()

    throw new errors.ApiError(
      response.status, 
      responseBody.code || 'unknown',
      new Map<string, string>(response.headers))
  }
}