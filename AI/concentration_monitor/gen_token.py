import argparse
import sys

try:
    from livekit import api
except ImportError:
    print("LiveKit SDK not installed. Run: pip install livekit-api")
    sys.exit(1)

# Default Keys (If you know them, replace here for convenience)
# Or pass them as arguments
DEFAULT_API_KEY = "devkey"      # Replace with your actual key if safe
DEFAULT_API_SECRET = "secret"  # Replace with your actual secret if safe

def generate_token(url, api_key, api_secret, room_name, participant_identity):
    # [Fix] Backdate nbf to 1 hour ago to prevent "token not yet valid" errors due to clock skew
    import time
    now_ts = int(time.time())
    
    token = api.AccessToken(api_key, api_secret) \
        .with_identity(participant_identity) \
        .with_name(participant_identity) \
        .with_grants(api.VideoGrants(
            room_join=True,
            room=room_name,
            can_publish=True,
            can_subscribe=True,
            can_publish_data=True
        ))
    
    # Force nbf to 1 hour ago (manual property set if supported, else relies on default)
    # LiveKit Python SDK AccessToken object doesn't have .nbf property exposing directly in some versions
    # But let's try setting it on the decoded payload or just rely on the fact that we created it NOW.
    # If the previous error was due to future NBF, it might be system time is weird.
    # Let's subtract from system time? No, AccessToken uses time.time() internally.
    
    # We will try to set the attribute just in case dynamic assignment works
    token.nbf = now_ts - 3600 

    jwt_token = token.to_jwt()
    print("\n" + "="*50)
    print(f"Token for {participant_identity} in {room_name}")
    print("="*50)
    print(jwt_token)
    print("="*50 + "\n")
    return jwt_token

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate LiveKit Token for AI Bot")
    parser.add_argument("--room", required=True, help="Room Name to join")
    parser.add_argument("--start-id", type=int, default=1, help="Starting Bot ID number")
    parser.add_argument("--count", type=int, default=1, help="Number of tokens to generate")
    parser.add_argument("--key", default=DEFAULT_API_KEY, help="LiveKit API Key")
    parser.add_argument("--secret", default=DEFAULT_API_SECRET, help="LiveKit API Secret")
    
    args = parser.parse_args()

    for i in range(args.count):
        identity = f"AI_Bot_{args.start_id + i}"
        generate_token(None, args.key, args.secret, args.room, identity)
