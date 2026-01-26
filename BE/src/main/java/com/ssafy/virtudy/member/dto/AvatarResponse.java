package com.ssafy.virtudy.member.dto;

import com.ssafy.virtudy.member.domain.Avatar;

public record AvatarResponse(
        String hairFront,
        String hairBack,
        String hairColor,
        String eyes,
        String glasses,
        String outfit,
        String clothesColor
) {
    public static AvatarResponse from(Avatar avatar) {
        if (avatar == null) return null;
        return new AvatarResponse(
                avatar.getHairFront(),
                avatar.getHairBack(),
                avatar.getHairColor(),
                avatar.getEyes(),
                avatar.getGlasses(),
                avatar.getClothes(),
                avatar.getClothesColor()
        );
    }
}