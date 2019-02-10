export class ApiError extends Error {
  readonly status: number

  constructor(status: number, msg: string) {
    super(msg)
    this.status = status
  }
}