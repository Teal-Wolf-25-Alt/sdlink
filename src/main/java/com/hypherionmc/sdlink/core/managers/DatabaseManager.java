/*
 * This file is part of sdlink-core, licensed under the MIT License (MIT).
 * Copyright HypherionSA and Contributors
 */
package com.hypherionmc.sdlink.core.managers;

import com.hypherionmc.sdlink.core.database.HiddenPlayers;
import com.hypherionmc.sdlink.core.database.SDLinkAccount;
import io.jsondb.JsonDBTemplate;
import io.jsondb.annotation.Document;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author HypherionSA
 * Helper class to initialize the JSON database
 */
public final class DatabaseManager {

    public static final DatabaseManager INSTANCE = new DatabaseManager();

    private final JsonDBTemplate sdlinkDatabase = new JsonDBTemplate("sdlinkstorage", "com.hypherionmc.sdlink.core.database");

    private final Set<Class<?>> tables = new LinkedHashSet<>() {{
        add(SDLinkAccount.class);
        add(HiddenPlayers.class);
    }};

    DatabaseManager() {
        sdlinkDatabase.setupDB(tables);
    }

    public void initialize() {
        tables.forEach(t -> {
            if (!sdlinkDatabase.collectionExists(t)) {
                sdlinkDatabase.createCollection(t);
            }

            sdlinkDatabase.reloadCollection(t.getAnnotation(Document.class).collection());
        });
    }

    public void updateEntry(Object t) {
        sdlinkDatabase.upsert(t);
        reload(t.getClass());
    }

    public void deleteEntry(Object t) {
        sdlinkDatabase.remove(t);
        reload(t.getClass());
    }

    public void deleteEntry(Object t, Class<?> clazz) {
        sdlinkDatabase.remove(t, clazz);
        reload(t.getClass());
    }

    private void reload(Class<?> clazz) {
        sdlinkDatabase.reloadCollection(clazz.getAnnotation(Document.class).collection());
    }

    public <T> T findById(Object id, Class<T> entityClass) {
        reload(entityClass);
        return sdlinkDatabase.findById(id, entityClass);
    }

    public <T> List<T> getCollection(Class<T> entityClass) {
        reload(entityClass);
        return sdlinkDatabase.getCollection(entityClass);
    }

    public <T> List<T> findAll(Class<T> tClass) {
        reload(tClass);
        return sdlinkDatabase.findAll(tClass);
    }
}
