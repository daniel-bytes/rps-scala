import * as errors from '../errors/ApiError'
import * as models from '../models/ApiModels'

export default class ApiClient {
  private readonly _token: string | null

  constructor(token: string | null = null) {
    this._token = token
  }

  async getAsync<TRes>(url: string): Promise<models.ApiResponse<TRes>> {
    const response = await fetch(url, {
      method: `GET`,
      headers: this.getHeaders()
    })

    return this.handleResponse<TRes>(response)
  }

  async postAsync<TRes>(
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

  async putAsync<TRes>(
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

  async deleteAsync<TRes>(url: string): Promise<models.ApiResponse<TRes>> {
    const response = await fetch(url, {
      method: `DELETE`,
      headers: this.getHeaders()
    })

    return this.handleResponse<TRes>(response)
  }

  private getHeaders(isJson: boolean = false): Headers 
  {
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

    let message: string

    if (response.headers.get(`Content-Type`) === `application/json`) {
      const err = await response.json()
      message = err.message
    } else {
      message = await response.text()
    }

    throw new errors.ApiError(response.status, message)
  }
}