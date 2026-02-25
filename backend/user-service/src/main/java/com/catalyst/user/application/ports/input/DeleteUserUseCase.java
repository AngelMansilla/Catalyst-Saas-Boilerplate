package com.catalyst.user.application.ports.input;

import com.catalyst.user.domain.valueobject.UserId;

/**
 * Input port for deleting a user account (GDPR).
 */
public interface DeleteUserUseCase {

    /**
     * Permanently deletes a user and all associated data.
     * 
     * @param userId the ID of the user to delete
     */
    void deleteUser(UserId userId);
}
