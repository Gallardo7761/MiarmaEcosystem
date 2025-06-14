package net.miarma.api.microservices.miarmacraft.dao;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import net.miarma.api.common.db.DataAccessObject;
import net.miarma.api.common.db.DatabaseManager;
import net.miarma.api.common.db.QueryBuilder;
import net.miarma.api.common.http.QueryFilters;
import net.miarma.api.common.http.QueryParams;
import net.miarma.api.microservices.miarmacraft.entities.PlayerEntity;

import java.util.List;
import java.util.Map;

public class PlayerDAO implements DataAccessObject<PlayerEntity, Integer> {

    private final DatabaseManager db;

    public PlayerDAO(Pool pool) {
        this.db = DatabaseManager.getInstance(pool);
    }

    @Override
    public Future<List<PlayerEntity>> getAll() {
        return getAll(new QueryParams(Map.of(), new QueryFilters()));
    }

    @Override
    public Future<PlayerEntity> getById(Integer integer) {
        Promise<PlayerEntity> promise = Promise.promise();
        String query = QueryBuilder
                .select(PlayerEntity.class)
                .where(Map.of("user_id", integer.toString()))
                .build();

        db.executeOne(query, PlayerEntity.class,
                promise::complete,
                promise::fail
        );

        return promise.future();
    }

    public Future<List<PlayerEntity>> getAll(QueryParams params) {
        Promise<List<PlayerEntity>> promise = Promise.promise();

        String query = QueryBuilder
                .select(PlayerEntity.class)
                .where(params.getFilters())
                .orderBy(params.getQueryFilters().getSort(), params.getQueryFilters().getOrder())
                .limit(params.getQueryFilters().getLimit())
                .offset(params.getQueryFilters().getOffset())
                .build();

        db.execute(query, PlayerEntity.class,
                list -> promise.complete(list.isEmpty() ? List.of() : list),
                promise::fail
        );

        return promise.future();
    }

    @Override
    public Future<PlayerEntity> insert(PlayerEntity t) {
        throw new UnsupportedOperationException("Insert not supported on view-based DAO");
    }

    @Override
    public Future<PlayerEntity> upsert(PlayerEntity playerEntity, String... conflictKeys) {
        throw new UnsupportedOperationException("Upsert not supported on view-based DAO");
    }

    @Override
    public Future<PlayerEntity> update(PlayerEntity t) {
        throw new UnsupportedOperationException("Insert not supported on view-based DAO");
    }

    @Override
    public Future<Boolean> delete(Integer id) {
        throw new UnsupportedOperationException("Insert not supported on view-based DAO");
    }

    @Override
    public Future<Boolean> exists(Integer integer) {
        Promise<Boolean> promise = Promise.promise();
        String query = QueryBuilder
                .select(PlayerEntity.class)
                .where(Map.of("user_id", integer.toString()))
                .build();

        db.executeOne(query, PlayerEntity.class,
                result -> promise.complete(result != null),
                promise::fail
        );

        return promise.future();
    }

}
