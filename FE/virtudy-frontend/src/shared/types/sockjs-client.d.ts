declare module 'sockjs-client' {
    export default class SockJS {
        constructor(url: string, _reserved?: any, options?: any);
        onopen: () => void;
        onmessage: (e: { data: any }) => void;
        onclose: () => void;
        send(data: any): void;
        close(): void;
        readyState: number;
    }
}
