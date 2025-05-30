package net.miarma.api.huertos.services;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import net.miarma.api.common.Constants;
import net.miarma.api.common.Constants.HuertosUserRole;
import net.miarma.api.common.Constants.HuertosUserStatus;
import net.miarma.api.common.Constants.HuertosUserType;
import net.miarma.api.common.exceptions.BadRequestException;
import net.miarma.api.common.exceptions.ForbiddenException;
import net.miarma.api.common.exceptions.NotFoundException;
import net.miarma.api.common.exceptions.ValidationException;
import net.miarma.api.common.http.QueryParams;
import net.miarma.api.common.security.JWTManager;
import net.miarma.api.common.security.PasswordHasher;
import net.miarma.api.core.dao.UserDAO;
import net.miarma.api.core.entities.UserEntity;
import net.miarma.api.core.services.UserService;
import net.miarma.api.huertos.dao.MemberDAO;
import net.miarma.api.huertos.dao.UserMetadataDAO;
import net.miarma.api.huertos.entities.MemberEntity;
import net.miarma.api.huertos.entities.PreUserEntity;
import net.miarma.api.huertos.entities.UserMetadataEntity;
import net.miarma.api.huertos.validators.MemberValidator;

import java.util.List;

@SuppressWarnings("unused")
public class MemberService {

    private final UserDAO userDAO;
    private final UserMetadataDAO userMetadataDAO;
    private final MemberDAO memberDAO;
    private final UserService userService;
    private final MemberValidator memberValidator;

    public MemberService(Pool pool) {
        this.userDAO = new UserDAO(pool);
        this.memberDAO = new MemberDAO(pool);
        this.userMetadataDAO = new UserMetadataDAO(pool);
        this.userService = new UserService(pool);
        this.memberValidator = new MemberValidator();
    }
    
    public Future<JsonObject> login(String emailOrUserName, String password, boolean keepLoggedIn) {
        return userService.login(emailOrUserName, password, keepLoggedIn).compose(json -> {
            JsonObject loggedUserJson = json.getJsonObject("loggedUser");
            UserEntity user = Constants.GSON.fromJson(loggedUserJson.encode(), UserEntity.class);
            
            if (user == null) {
				return Future.failedFuture(new BadRequestException("Invalid credentials"));
			}
            
            if (user.getGlobal_status() != Constants.CoreUserGlobalStatus.ACTIVE) {
            	return Future.failedFuture(new ForbiddenException("User is not active"));
            }
            
            return userMetadataDAO.getAll().compose(metadataList -> {
                UserMetadataEntity metadata = metadataList.stream()
                    .filter(meta -> meta.getUser_id().equals(user.getUser_id()))
                    .findFirst()
                    .orElse(null);
                
                if (metadata.getStatus() != HuertosUserStatus.ACTIVE) {
					return Future.failedFuture(new ForbiddenException("User is not active"));
				}

                if (metadata == null) {
                    return Future.failedFuture(new NotFoundException("User metadata not found"));
                }

                MemberEntity member = new MemberEntity(user, metadata);

                return Future.succeededFuture(new JsonObject()
                    .put("token", json.getString("token"))
                    .put("member", new JsonObject(Constants.GSON.toJson(member)))
                );
            });
        });
    }
    
    public Future<List<MemberEntity>> getAll() {
		return memberDAO.getAll().compose(list -> {
			return Future.succeededFuture(list.stream()
					.filter(m -> !m.getType().equals(HuertosUserType.DEVELOPER))
					.toList());
	        });
	}

    public Future<List<MemberEntity>> getAll(QueryParams params) {
        return memberDAO.getAll(params).compose(list -> {
			return Future.succeededFuture(list.stream()
				.filter(m -> !m.getType().equals(HuertosUserType.DEVELOPER))
				.toList());
        });
    }

    public Future<MemberEntity> getById(Integer id) {
        return memberDAO.getAll().compose(list -> {
            MemberEntity member = list.stream()
                .filter(m -> m.getUser_id().equals(id))
                .findFirst()
                .orElse(null);
            return member != null ?
                Future.succeededFuture(member) :
                Future.failedFuture(new NotFoundException("Member with id " + id));
        });
    }

    public Future<MemberEntity> getByMemberNumber(Integer memberNumber) {
        return memberDAO.getAll().compose(list -> {
            MemberEntity member = list.stream()
                .filter(m -> m.getMember_number().equals(memberNumber))
                .findFirst()
                .orElse(null);
            return member != null ?
                Future.succeededFuture(member) :
                Future.failedFuture(new NotFoundException("Member with number " + memberNumber));
        });
    }

    public Future<MemberEntity> getByPlotNumber(Integer plotNumber) {
        return memberDAO.getAll().compose(list -> {
            MemberEntity member = list.stream()
                .filter(m -> m.getPlot_number().equals(plotNumber))
                .findFirst()
                .orElse(null);
            return member != null ?
                Future.succeededFuture(member) :
                Future.failedFuture(new NotFoundException("Member with plot number " + plotNumber));
        });
    }

    public Future<MemberEntity> getByEmail(String email) {
        return memberDAO.getAll().compose(list -> {
            MemberEntity member = list.stream()
                .filter(m -> m.getEmail().equals(email))
                .findFirst()
                .orElse(null);
            return member != null ?
                Future.succeededFuture(member) :
                Future.failedFuture(new NotFoundException("Member with email " + email));
        });
    }

    public Future<MemberEntity> getByDni(String dni) {
        return memberDAO.getAll().compose(list -> {
            MemberEntity member = list.stream()
                .filter(m -> m.getDni().equals(dni))
                .findFirst()
                .orElse(null);
            return member != null ?
                Future.succeededFuture(member) :
                Future.failedFuture(new NotFoundException("Member with dni " + dni));
        });
    }

    public Future<MemberEntity> getByPhone(String phone) {
        return memberDAO.getAll().compose(list -> {
            MemberEntity member = list.stream()
                .filter(m -> m.getPhone().equals(phone))
                .findFirst()
                .orElse(null);
            return member != null ?
                Future.succeededFuture(member) :
                Future.failedFuture(new NotFoundException("Member with phone " + phone));
        });
    }

    public Future<List<MemberEntity>> getWaitlist() {
        return memberDAO.getAll().map(list ->
            list.stream()
                .filter(m -> m.getType().equals(HuertosUserType.WAIT_LIST))
                .filter(m -> m.getStatus().equals(HuertosUserStatus.ACTIVE))
                .toList()
        );
    }

    public Future<Integer> getLastMemberNumber() {
        return memberDAO.getAll().map(list ->
            list.stream()
                .map(MemberEntity::getMember_number)
                .max(Integer::compareTo)
                .orElse(0)
        );
    }
    
    public Future<Boolean> hasCollaborator(String token) {
        Integer userId = JWTManager.getInstance().getUserId(token);

        return getById(userId).compose(member -> {
            Integer plotNumber = member.getPlot_number();

            if (plotNumber == null || plotNumber == 0) {
                return Future.succeededFuture(false);
            }

            return getAll().map(users -> 
                users.stream().anyMatch(u -> u.getType() == HuertosUserType.COLLABORATOR
                	&& u.getPlot_number() != null && u.getPlot_number().equals(plotNumber))
            );
        });
    }
    
    public Future<Boolean> hasGreenHouse(String token) {
    	Integer userId = JWTManager.getInstance().getUserId(token);

    	return getById(userId).map(user -> user.getType() == HuertosUserType.WITH_GREENHOUSE);
    }

    public Future<MemberEntity> updateRole(Integer userId, HuertosUserRole role) {
        return getById(userId).compose(member -> {
            member.setRole(role);
            return userMetadataDAO.update(UserMetadataEntity.fromMemberEntity(member))
                .compose(updated -> getById(userId));
        });
    }

    public Future<MemberEntity> updateStatus(Integer userId, HuertosUserStatus status) {
        return getById(userId).compose(member -> {
            member.setStatus(status);
            return userMetadataDAO.update(UserMetadataEntity.fromMemberEntity(member))
                .compose(updated -> getById(userId));
        });
    }

    public Future<MemberEntity> create(MemberEntity member) {
        return memberValidator.validate(member).compose(validation -> {
            if (!validation.isValid()) {
                return Future.failedFuture(new ValidationException(Constants.GSON.toJson(validation.getErrors())));
            }

            member.setPassword(PasswordHasher.hash(member.getPassword()));
            if (member.getEmail().isBlank()) member.setEmail(null);

            return userDAO.insert(UserEntity.fromMemberEntity(member)).compose(user -> {
                UserMetadataEntity metadata = UserMetadataEntity.fromMemberEntity(member);
                metadata.setUser_id(user.getUser_id());

                return userMetadataDAO.insert(metadata).compose(meta -> {
                    String baseName = member.getDisplay_name().split(" ")[0].toLowerCase();
                    String userName = baseName + member.getMember_number();

                    user.setUser_name(userName);

                    return userDAO.update(user).map(updatedUser -> new MemberEntity(updatedUser, meta));
                });
            });
        });
    }

    
    public Future<MemberEntity> createFromPreUser(PreUserEntity preUser) {
		MemberEntity memberFromPreUser = MemberEntity.fromPreUser(preUser);
		return memberValidator.validate(memberFromPreUser).compose(validation -> {
			if (!validation.isValid()) {
				return Future.failedFuture(new ValidationException(Constants.GSON.toJson(validation.getErrors())));
			}

			memberFromPreUser.setPassword(PasswordHasher.hash(memberFromPreUser.getPassword()));

			return userDAO.insert(UserEntity.fromMemberEntity(memberFromPreUser)).compose(user -> {
				UserMetadataEntity metadata = UserMetadataEntity.fromMemberEntity(memberFromPreUser);
				metadata.setUser_id(user.getUser_id());

				return userMetadataDAO.insert(metadata)
					.map(meta -> new MemberEntity(user, meta));
			});
		});	
	}

    public Future<MemberEntity> update(MemberEntity member) {
    	return getById(member.getUser_id()).compose(existing -> {
    		if (existing == null) {
    			return Future.failedFuture(new NotFoundException("Member in the database"));
    		}

    		return memberValidator.validate(member).compose(validation -> {
    			if (!validation.isValid()) {
    				return Future.failedFuture(new ValidationException(Constants.GSON.toJson(validation.getErrors())));
    			}

    			if (member.getPassword() != null && !member.getPassword().isEmpty() && 
						!member.getPassword().equals(existing.getPassword())) {
    				member.setPassword(PasswordHasher.hash(member.getPassword()));
    			} else {
    				member.setPassword(existing.getPassword());
    			}

    			return userDAO.update(UserEntity.fromMemberEntity(member)).compose(user -> {
    				return userMetadataDAO.updateWithNulls(UserMetadataEntity.fromMemberEntity(member))
    						.map(meta -> new MemberEntity(user, meta));
    			});
    		});
    	});
    }


    public Future<MemberEntity> delete(Integer userId) {
        return getById(userId).compose(member -> 
            userDAO.delete(userId).compose(deletedUser -> 
                userMetadataDAO.delete(member.getUser_id())
                    .map(deletedMetadata -> member)
            )
        );
    }
    
    public Future<MemberEntity> changeMemberStatus(Integer userId, HuertosUserStatus status) {
		return getById(userId).compose(member -> {
			member.setStatus(status);
			return userMetadataDAO.update(UserMetadataEntity.fromMemberEntity(member))
				.map(updated -> member);
		});
	}
    
    public Future<MemberEntity> changeMemberType(Integer userId, HuertosUserType type) {
		return getById(userId).compose(member -> {
			member.setType(type);
			return userMetadataDAO.update(UserMetadataEntity.fromMemberEntity(member))
				.map(updated -> member);
			});
    			
    }
}
