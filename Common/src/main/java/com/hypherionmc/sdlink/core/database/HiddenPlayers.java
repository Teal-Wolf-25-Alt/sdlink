package com.hypherionmc.sdlink.core.database;

import com.hypherionmc.sdlink.core.jsondb.annotations.Document;
import com.hypherionmc.sdlink.core.jsondb.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Document(collection = "hiddenplayers")
public final class HiddenPlayers {

    @Id
    private String identifier;
    private String displayName;
    private String type;

}
