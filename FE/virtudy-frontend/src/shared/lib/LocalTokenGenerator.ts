export class LocalTokenGenerator {
    // LiveKit Default Dev Keys
    private static API_KEY = 'devkey';
    private static API_SECRET = 'secret';

    static async generateToken(roomId: string, memberId: string): Promise<string> {
        const header = { alg: 'HS256', typ: 'JWT' };

        // Payload structure ensuring video permissions
        const payload = {
            iss: this.API_KEY,
            sub: memberId,
            video: {
                room: roomId,
                roomJoin: true,
                canPublish: true,
                canSubscribe: true,
            },
            nbf: Math.floor(Date.now() / 1000),
            exp: Math.floor(Date.now() / 1000) + 3600, // 1 hour
        };

        const encodedHeader = this.base64UrlEncode(JSON.stringify(header));
        const encodedPayload = this.base64UrlEncode(JSON.stringify(payload));

        const signature = await this.sign(`${encodedHeader}.${encodedPayload}`, this.API_SECRET);

        return `${encodedHeader}.${encodedPayload}.${signature}`;
    }

    private static base64UrlEncode(str: string): string {
        return btoa(str)
            .replace(/\+/g, '-')
            .replace(/\//g, '_')
            .replace(/=+$/, '');
    }

    private static async sign(data: string, secret: string): Promise<string> {
        const encoder = new TextEncoder();
        const keyData = encoder.encode(secret);
        const dataToSign = encoder.encode(data);

        const key = await crypto.subtle.importKey(
            'raw',
            keyData,
            { name: 'HMAC', hash: 'SHA-256' },
            false,
            ['sign']
        );

        const signature = await crypto.subtle.sign('HMAC', key, dataToSign);

        return this.base64UrlEncode(String.fromCharCode(...new Uint8Array(signature)));
    }
}
