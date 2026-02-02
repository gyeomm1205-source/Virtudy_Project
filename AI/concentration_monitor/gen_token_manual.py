import hmac
import hashlib
import base64
import json
import time
import argparse

def base64url_encode(data):
    return base64.urlsafe_b64encode(data).decode('utf-8').rstrip('=')

def generate_token(key, secret, room, identity):
    header = {
        "alg": "HS256",
        "typ": "JWT"
    }
    
    # Match Frontend Payload exactly
    # Frontend: nbf = now, exp = now + 3600
    now = int(time.time())
    
    payload = {
        "iss": key,
        "sub": identity,
        "video": {
            "room": room,
            "roomJoin": True,
            "canPublish": True,
            "canSubscribe": True,
            "canPublishData": True # AI needs this
        },
        "nbf": now - 300, # Backdate 5 mins just to be safe vs slight skew, but not 1 hr
        "exp": now + 3600,
        "name": identity
    }
    
    encoded_header = base64url_encode(json.dumps(header).encode('utf-8'))
    encoded_payload = base64url_encode(json.dumps(payload).encode('utf-8'))
    
    signature_input = f"{encoded_header}.{encoded_payload}"
    
    signature = hmac.new(
        secret.encode('utf-8'),
        signature_input.encode('utf-8'),
        hashlib.sha256
    ).digest()
    
    encoded_signature = base64url_encode(signature)
    
    return f"{signature_input}.{encoded_signature}"

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--room", required=True)
    parser.add_argument("--key", required=True)
    parser.add_argument("--secret", required=True)
    args = parser.parse_args()
    
    token = generate_token(args.key, args.secret, args.room, "AI_Bot_Manual")
    
    print("\n" + "="*50)
    print(f"Manual Token for {args.room}")
    print("="*50)
    print(token)
    print("="*50 + "\n")
    
    with open("token_utf8.txt", "w", encoding="utf-8") as f:
        f.write(token)
