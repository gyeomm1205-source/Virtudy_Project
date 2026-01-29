export interface AvatarConfig {
    hairFront: string;
    hairBack: string;
    hairColor: string;
    eyes: string;
    glasses: string;
    outfit: string;
    clothesColor: string;
}

export interface User {
    userId: string;
    nickName: string;
    email: string;
    jobType: string;
    tier: string;
    avatar?: AvatarConfig;
    avatarImageUrl?: string;
    tierScore?: number; // UserProfile.vue uses this? User interface in common had tierScore?
}
// Note: common.types.ts seen in Step 760 DID NOT have tierScore.
// But UserProfile.vue has tierScore.
// I will stick to what common.types.ts had for now plus exact copy.
