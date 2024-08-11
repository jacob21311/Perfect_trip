package idv.tia201.g1.chat.dao.impl;

import idv.tia201.g1.chat.dao.CustomChatParticipantDao;
import idv.tia201.g1.entity.ChatParticipant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static idv.tia201.g1.utils.Constants.*;

@Repository
public class CustomChatParticipantDaoImpl implements CustomChatParticipantDao {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ChatParticipant> findByChatIdToDTO(Long chatId) {
        String queryStr = "SELECT " +
                "    p.mapping_user_id AS user_id, " +
                "    CASE " +
                "        WHEN m.user_type = '" + ROLE_USER + "' THEN u.nickname " +
                "        WHEN m.user_type = '" + ROLE_COMPANY + "' THEN c.company_name " +
                "        WHEN m.user_type = '" + ROLE_ADMIN + "' THEN '平台管理員' " +
                "    END AS name, " +
                "    CASE " +
                "        WHEN m.user_type = '" + ROLE_USER + "' THEN u.avatar " +
                "        WHEN m.user_type = '" + ROLE_COMPANY + "' THEN '' " +
                "        WHEN m.user_type = '" + ROLE_ADMIN + "' THEN '' " +
                "    END AS avatar, " +
                "    m.user_type AS type, " +
                "    p.pinned, " +
                "    p.notify, " +
                "    p.unread_messages, " +
                "    p.last_reading_at " +
                "FROM chat_user_mappings m " +
                "LEFT JOIN user_master u ON " +
                "    m.ref_id = u.user_id AND m.user_type = '" + ROLE_USER + "' " +
                "LEFT JOIN company_master c ON " +
                "    m.ref_id = c.company_id AND m.user_type = '" + ROLE_COMPANY + "' " +
                "LEFT JOIN admin_master a ON " +
                "    m.ref_id = a.admin_id AND m.user_type = '" + ROLE_ADMIN + "' " +
                "JOIN chat_participants p ON " +
                "    p.mapping_user_id = m.mapping_user_id " +
                "JOIN chat_rooms r ON " +
                "    p.chat_id = r.chat_id " +
                "WHERE r.chat_id = :chatId";

        Query query = entityManager.createNativeQuery(queryStr);
        query.setParameter("chatId", chatId);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<ChatParticipant> list = new ArrayList<>();

        for (Object[] result : results) {
            ChatParticipant participant = getParticipant(result);
            list.add(participant);
        }

        return list;
    }

    @Override
    public List<Long> findChatIdByTypeAndRefId(String type, Integer refId, int size, Timestamp earliestTimestamp) {
        String queryStr = "";
        if (earliestTimestamp.getTime() == TIMESTAMP_MAX_VALUE) {
            // 表示第一次取得, 將所有釘選中的聊天室傳給前端
            queryStr += "SELECT pinned.chat_id " +
                    "FROM (SELECT p.chat_id " +
                    "      FROM chat_participants p " +
                    "               JOIN chat_user_mappings m ON " +
                    "          p.mapping_user_id = m.mapping_user_id " +
                    "      WHERE m.ref_id = :refId " +
                    "        AND m.user_type = :type " +
                    "        AND p.pinned = true " +
                    "      ORDER BY p.last_modified_date DESC) pinned " +
                    "UNION ";
        }
        queryStr += "SELECT unpinned.chat_id " +
                "FROM (SELECT p.chat_id " +
                "      FROM chat_participants p " +
                "               JOIN chat_user_mappings m ON " +
                "          p.mapping_user_id = m.mapping_user_id " +
                "      WHERE m.ref_id = :refId " +
                "        AND m.user_type = :type " +
                "        AND p.pinned = false " +
                "        AND p.last_modified_date <= :earliestTimestamp " +
                "      ORDER BY p.last_modified_date DESC " +
                "      LIMIT :size OFFSET 0) unpinned;";

        Query query = entityManager.createNativeQuery(queryStr);
        query.setParameter("refId", refId);
        query.setParameter("type", type);
        query.setParameter("earliestTimestamp", earliestTimestamp);
        query.setParameter("size", size);


        List<?> resultList = query.getResultList();

        return resultList.stream()
                .map(result -> {
                    if (result instanceof Number) {
                        return ((Number) result).longValue();
                    } else {
                        throw new RuntimeException("Unexpected result type: " + result.getClass().getName());
                    }
                })
                .toList();
    }

    @Override
    public Set<Long> findChatIdByTypeAndRefId(String type, Integer refId) {
        String queryStr = "SELECT p.chat_id " +
                "FROM chat_participants p " +
                "JOIN chat_user_mappings m ON p.mapping_user_id = m.mapping_user_id " +
                "WHERE m.ref_id = :refId AND m.user_type = :type ";

        Query query = entityManager.createNativeQuery(queryStr);
        query.setParameter("refId", refId);
        query.setParameter("type", type);

        @SuppressWarnings("unchecked")
        List<Number> resultList = query.getResultList();

        return resultList.stream()
                .map(Number::longValue)
                .collect(Collectors.toSet());
    }

    private static ChatParticipant getParticipant(Object[] result) {
        ChatParticipant participant = new ChatParticipant();
        participant.setMappingUserId((Long) result[0]);
        participant.setName((String) result[1]);
        participant.setAvatar((String) result[2]);
        participant.setType((String) result[3]);
        participant.setPinned((Boolean) result[4]);
        participant.setNotify((String) result[5]);
        participant.setUnreadMessages((Integer) result[6]);
        participant.setLastReadingAt((Timestamp) result[7]);
        return participant;
    }
}
