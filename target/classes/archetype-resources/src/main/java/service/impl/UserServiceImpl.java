/**
 * 
 */
package ${package}.service.impl;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ${package}.model.User;
import ${package}.repository.UserRepository;
import ${package}.security.utils.Digests;
import ${package}.service.UserService;
import ${package}.service.impl.UserRealm.ShiroUser;
import ${package}.utils.Encodes;

/**
 * @author WMS
 * 
 */
@Service("UserService")
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository respository;
	
	public static final String HASH_ALGORITHM = "SHA-1";
	public static final int HASH_INTERATIONS = 1024;
	private static final int SALT_SIZE = 8;

	public UserRepository getRespository() {
		return respository;
	}

	public void setRespository(UserRepository respository) {
		this.respository = respository;
	}
	
	/**
	 * 设定安全的密码，生成随机的salt并经过1024次 sha-1 hash
	 */
	private void entryptPassword(User user) {
		byte[] salt = Digests.generateSalt(SALT_SIZE);
		user.setSalt(Encodes.encodeHex(salt));

		byte[] hashPassword = Digests.sha1(user.getPassword().getBytes(), salt, HASH_INTERATIONS);
		user.setPassword(Encodes.encodeHex(hashPassword));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ${package}.service.Userservice#save(${package}.model.User)
	 */
	@Override
	public void save(User user) {
		entryptPassword(user);
		this.respository.save(user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ${package}.service.Userservice#delete(${package}.model.User)
	 */
	@Override
	public void delete(User user) {
		// TODO Auto-generated method stub
		this.respository.delete(user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ${package}.service.Userservice#update(${package}.model.User)
	 */
	@Override
	public void update(User user) {

		this.respository.save(user);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ${package}.service.Userservice#findById(java.lang.String)
	 */
	@Override
	public User findById(String id) {

		return this.respository.findOne(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ${package}.service.Userservice#list(org.springframework.data.domain
	 * .Pageable)
	 */
	@Override
	public Page<User> list(Pageable pageable) {

		return this.respository.findAll(pageable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ${package}.service.Userservice#updateState(${package}.model.User)
	 */
	@Override
	public void updateState(User user) {

		this.save(user);
	}

	@Override
	public Page<User> findLike(Pageable pageable, String searchtype,
			String searchvalue, boolean searchscope) {
		Page<User> pusers = null;
		
		if(searchscope){
			searchvalue = "%"+searchvalue+"%";
		}
		if("userid".equalsIgnoreCase(searchtype)){
			pusers = this.respository.findByIdLike(searchvalue, pageable);
		}else if("username".equalsIgnoreCase(searchtype)){
			pusers = this.respository.findByNameLike(searchvalue, pageable);
		}
		return pusers;
	}

	@Override
	public User getCurrentUser() {
		ShiroUser shiroUser = (ShiroUser) SecurityUtils.getSubject()
				.getPrincipal();
		if (shiroUser == null) {
			return null;
		}
		return this.findById(shiroUser.loginName);
	}

}
