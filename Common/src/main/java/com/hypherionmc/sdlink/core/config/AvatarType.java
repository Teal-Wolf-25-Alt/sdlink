/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.config;

/**
 * @author HypherionSA
 * The type of User Icon/Avatar that will be used for Discord Messages
 */
public enum AvatarType {
    FACE("https://vzge.me/face/512/{uuid}?no=ears"),
    FACE_EARS("https://vzge.me/face/512/{uuid}"),
    FRONT("https://vzge.me/front/256/{uuid}?no=ears"),
    FRONT_EARS("https://vzge.me/front/256/{uuid}"),
    FRONT_FULL("https://vzge.me/frontfull/384/{uuid}?no=ears"),
    FRONT_FULL_EARS("https://vzge.me/frontfull/384/{uuid}"),
    HEAD("https://vzge.me/head/256/{uuid}?no=ears"),
    HEAD_EARS("https://vzge.me/head/256/{uuid}"),
    BUST("https://vzge.me/bust/256/{uuid}?no=ears"),
    BUST_EARS("https://vzge.me/bust/256/{uuid}"),
    FULL("https://vzge.me/full/256/{uuid}?no=ears"),
    FULL_EARS("https://vzge.me/full/256/{uuid}"),
    /*AVATAR removed, use FACE instead*/
    MC_HEAD("https://mc-heads.net/head/{uuid}/512"),
    PLAYER("https://mc-heads.net/player/{uuid}/512"),
    COMBO("https://mc-heads.net/combo/{uuid}/512");

    private final String url;

    AvatarType(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return this.url;
    }

    public String resolve(String uuid) {
        return this.url.replace("{uuid}", uuid);
    }
}
