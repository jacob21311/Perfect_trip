package idv.tia201.g1.user.service.impl;

import idv.tia201.g1.dto.UserLoginRequest;
import idv.tia201.g1.dto.UserRegisterRequest;
import idv.tia201.g1.entity.User;
import idv.tia201.g1.user.dao.UserDao;
import idv.tia201.g1.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    private final static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    public UserDao userDao;

    @Override
    public Integer register(UserRegisterRequest userRegisterRequest) {

        // 檢查註冊的 email
        User user = userDao.findByUsername(userRegisterRequest.getUsername());

        if (user != null) {
            log.warn("該 userName {} 已經被註冊", userRegisterRequest.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // 使用 MD5 生成密碼的雜湊值
        String hashedPassword = DigestUtils.md5DigestAsHex(userRegisterRequest.getPassword().getBytes());
        userRegisterRequest.setPassword(hashedPassword);

        // 創建帳號
        return createUser(userRegisterRequest);

    }

    @Override
    public User login(UserLoginRequest userLoginRequest) {

        User user = userDao.findByUsername(userLoginRequest.getUsername());

        // 檢查帳號
        if (user == null) {
            log.warn("該 userName {} 尚未註冊", userLoginRequest.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // 使用 MD5 生成密碼的雜湊值
        String hashedPassword = DigestUtils.md5DigestAsHex(userLoginRequest.getPassword().getBytes());

        // 檢查密碼
        if (!user.getPassword().equals(hashedPassword)) {
            log.warn("該 userName {} 的密碼不正確", userLoginRequest.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return user;

    }

    @Override
    public Integer createUser(UserRegisterRequest userRegisterRequest) {

        User user = new User();
        user.setUsername(userRegisterRequest.getUsername());
        user.setPassword(userRegisterRequest.getPassword());
        user.setChange_id(0);
        user.setCreatedDate(new Date());
        user.setLastModifiedDate(new Date());

        User savedUser = userDao.save(user);
        return savedUser.getUserId();

    }

    @Override
    public User findByUserId(Integer userId) {

        return userDao.findByUserId(userId);

    }

    @Override
    public User findByUsername(String username) {

        return userDao.findByUsername(username);

    }

}