package com.gamehub.backend.persistence;

import com.gamehub.backend.domain.FriendRelationship;
import com.gamehub.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRelationshipRepository extends JpaRepository<FriendRelationship, Long> {
    List<FriendRelationship> findByUserAndStatus(User user, FriendRelationship.Status status);
    List<FriendRelationship> findByFriendAndStatus(User friend, FriendRelationship.Status status);
    List<FriendRelationship> findByUserAndStatusOrFriendAndStatus(User user, FriendRelationship.Status userStatus, User friend, FriendRelationship.Status friendStatus);
    List<FriendRelationship> findByUserAndFriendOrFriendAndUser(User user, User friend, User friendAsUser, User userAsFriend);

    boolean existsByUserAndFriend(User user, User friend);
    boolean existsByUserAndFriendAndStatus(User user, User friend, FriendRelationship.Status status);
    boolean existsByFriendAndStatus(User friend, FriendRelationship.Status status);
    boolean existsByUserAndStatus(User user, FriendRelationship.Status status);
}
