package edu.uoc.pdp.core.dao;

import edu.uoc.pdp.db.entity.User;
import edu.uoc.pdp.db.entity.User_;

import javax.inject.Singleton;

@Singleton
public class UserDAO extends BaseUserDAO<User> {

    /**
     * Checks if there's an user with the specified email
     *
     * @param email User email
     * @return {@code true} if the email existes, {@code false} otherwise
     */
    public boolean existUserByEmail(String email) {
        return exists((root, builder) -> builder.equal(root.get(User_.EMAIL), email));
    }
}
