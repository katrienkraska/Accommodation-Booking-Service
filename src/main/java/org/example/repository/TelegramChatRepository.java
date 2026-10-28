package org.example.repository;

import org.example.model.telegram.TelegramChat;
import org.example.model.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TelegramChatRepository extends JpaRepository<TelegramChat, Long> {
    Optional<TelegramChat> getTelegramChatByUserEmail(String email);

    List<TelegramChat> findAllByUser_Roles_Role(Role.RoleName roleName);
}
