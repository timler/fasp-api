/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.dao;

import cc.altius.FASP.model.Role;
import cc.altius.FASP.model.User;
import java.util.List;
import java.util.Map;

/**
 *
 * @author altius
 */
public interface UserDao {

    public Map<String, Object> checkIfUserExists(String username, String password);

    public int resetFailedAttemptsByUserId(int userId);

    public int updateFailedAttemptsByUserId(String username);

    public List<Role> getRoleList();

    public int addNewUser(User user);

    public List<User> getUserList();

}
