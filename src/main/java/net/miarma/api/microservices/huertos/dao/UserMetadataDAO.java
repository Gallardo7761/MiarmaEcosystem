package net.miarma.api.microservices.huertos.dao;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import net.miarma.api.common.db.DataAccessObject;
import net.miarma.api.common.db.DatabaseManager;
import net.miarma.api.common.db.QueryBuilder;
import net.miarma.api.common.http.QueryFilters;
import net.miarma.api.common.http.QueryParams;
import net.miarma.api.microservices.huertos.entities.UserMetadataEntity;

import java.util.List;
import java.util.Map;

public class UserMetadataDAO implements DataAccessObject<UserMetadataEntity, Integer> {

	private final DatabaseManager db;

	public UserMetadataDAO(Pool pool) {
		this.db = DatabaseManager.getInstance(pool);
	}

	@Override
	public Future<List<UserMetadataEntity>> getAll() {
		return getAll(new QueryParams(Map.of(), new QueryFilters()));
	}

	@Override
	public Future<UserMetadataEntity> getById(Integer id) {
		Promise<UserMetadataEntity> promise = Promise.promise();

		String query = QueryBuilder
				.select(UserMetadataEntity.class)
				.where(Map.of("user_id", id.toString()))
				.build();

		db.executeOne(query, UserMetadataEntity.class,
                promise::complete,
				promise::fail
		);

		return promise.future();
	}

	public Future<List<UserMetadataEntity>> getAll(QueryParams params) {
		Promise<List<UserMetadataEntity>> promise = Promise.promise();
		String query = QueryBuilder
				.select(UserMetadataEntity.class)
				.where(params.getFilters())
				.orderBy(params.getQueryFilters().getSort(), params.getQueryFilters().getOrder())
				.limit(params.getQueryFilters().getLimit())
				.offset(params.getQueryFilters().getOffset())
				.build();

		db.execute(query, UserMetadataEntity.class,
				list -> promise.complete(list.isEmpty() ? List.of() : list),
				promise::fail
		);

		return promise.future();
	}

	@Override
	public Future<UserMetadataEntity> insert(UserMetadataEntity user) {
		Promise<UserMetadataEntity> promise = Promise.promise();
		String query = QueryBuilder.insert(user).build();

		db.executeOne(query, UserMetadataEntity.class,
                promise::complete,
				promise::fail
		);

		return promise.future();
	}

	@Override
	public Future<UserMetadataEntity> upsert(UserMetadataEntity userMetadataEntity, String... conflictKeys) {
		Promise<UserMetadataEntity> promise = Promise.promise();
		String query = QueryBuilder.upsert(userMetadataEntity, conflictKeys).build();

		db.executeOne(query, UserMetadataEntity.class,
                promise::complete,
				promise::fail
		);

		return promise.future();
	}

	@Override
	public Future<UserMetadataEntity> update(UserMetadataEntity user) {
		Promise<UserMetadataEntity> promise = Promise.promise();
		String query = QueryBuilder.update(user).build();

		db.executeOne(query, UserMetadataEntity.class,
				_ -> promise.complete(user),
				promise::fail
		);

		return promise.future();
	}

	public Future<UserMetadataEntity> updateWithNulls(UserMetadataEntity user) {
		Promise<UserMetadataEntity> promise = Promise.promise();
		String query = QueryBuilder.updateWithNulls(user).build();

		db.executeOne(query, UserMetadataEntity.class,
				_ -> promise.complete(user),
				promise::fail
		);

		return promise.future();
	}

	@Override
	public Future<Boolean> delete(Integer id) {
		Promise<Boolean> promise = Promise.promise();
		UserMetadataEntity user = new UserMetadataEntity();
		user.setUser_id(id);

		String query = QueryBuilder.delete(user).build();

		db.executeOne(query, UserMetadataEntity.class,
				result -> promise.complete(result != null),
				promise::fail
		);

		return promise.future();
	}

	@Override
	public Future<Boolean> exists(Integer id) {
		Promise<Boolean> promise = Promise.promise();
		String query = QueryBuilder
				.select(UserMetadataEntity.class)
				.where(Map.of("user_id", id.toString()))
				.build();

		db.executeOne(query, UserMetadataEntity.class,
				result -> promise.complete(result != null),
				promise::fail
		);

		return promise.future();
	}
}
