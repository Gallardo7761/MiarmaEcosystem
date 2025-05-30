package net.miarma.api.miarmacraft.dao;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import net.miarma.api.common.db.DataAccessObject;
import net.miarma.api.common.db.DatabaseManager;
import net.miarma.api.common.db.QueryBuilder;
import net.miarma.api.common.http.QueryFilters;
import net.miarma.api.common.http.QueryParams;
import net.miarma.api.miarmacraft.entities.UserMetadataEntity;

import java.util.List;
import java.util.Map;

public class UserMetadataDAO implements DataAccessObject<UserMetadataEntity> {

	private final DatabaseManager db;
	
	public UserMetadataDAO(Pool pool) {
		this.db = DatabaseManager.getInstance(pool);
	}
	
	@Override
	public Future<List<UserMetadataEntity>> getAll() {
		return getAll(new QueryParams(Map.of(), new QueryFilters()));
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
	public Future<UserMetadataEntity> insert(UserMetadataEntity t) {
		Promise<UserMetadataEntity> promise = Promise.promise();
		String query = QueryBuilder.insert(t).build();
		db.executeOne(query, UserMetadataEntity.class,
			result -> promise.complete(result),
			promise::fail
		);
		return promise.future();
	}

	@Override
	public Future<UserMetadataEntity> update(UserMetadataEntity t) {
		Promise<UserMetadataEntity> promise = Promise.promise();
		String query = QueryBuilder.update(t).build();
		db.executeOne(query, UserMetadataEntity.class,
			result -> promise.complete(result),
			promise::fail
		);
		return promise.future();
	}

	@Override
	public Future<UserMetadataEntity> delete(Integer id) {
		Promise<UserMetadataEntity> promise = Promise.promise();
		UserMetadataEntity entity = new UserMetadataEntity();
		entity.setUser_id(id);
		String query = QueryBuilder.delete(entity).build();
		db.executeOne(query, UserMetadataEntity.class,
			result -> promise.complete(result),
			promise::fail
		);
		return promise.future();
	}

}
