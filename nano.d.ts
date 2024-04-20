declare type Handler<T> = (entity: T, event: any) => T

declare function resource<T>(resource: string, namespace?: string): ResourceOps<T> | { [prop: string]: PropertyOps<T> };

export interface PropertyOps<T> {
    compute(handler: Handler<T>, dependencies?: PropertyOps<unknown>[]): void
}

declare interface ResourceOps<T> {
    beforeCreate(handler: Handler<T>): void
}