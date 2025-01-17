/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.api.accounts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor(staticName = "of")
public final class DiscordUser {

    private String effectiveName;
    private String avatarUrl;
    private long userId;
    private String asMention;

}
