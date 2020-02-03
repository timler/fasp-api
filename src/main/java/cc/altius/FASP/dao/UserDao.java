/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc.altius.FASP.dao;

import cc.altius.FASP.model.CustomUserDetails;
import cc.altius.FASP.model.Role;
import cc.altius.FASP.model.User;
import java.util.List;
import java.util.Map;

/**
 *
 * @author altius
 */
public interface UserDao {

    public CustomUserDetails getCustomUserByUsername(String username);
    
    public Map<String, Object> checkIfUserExists(String username, String password);
    
    public List<String> getBusinessFunctionsForUserId(int userId);

    public int resetFailedAttemptsByUserId(int userId);

    public int updateFailedAttemptsByUserId(String username);

    public List<Role> getRoleList();

    public int addNewUser(User user);

    public List<User> getUserList();

    public User getUserByUserId(int userId);

    public int updateUser(User user);

    public String checkIfUserExistsByEmailIdAndPhoneNumber(User user, int page); // 1 add User , 2 Edit User

    public int unlockAccount(User user);

}
