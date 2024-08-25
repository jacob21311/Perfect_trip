package idv.tia201.g1.Room.dao;

import idv.tia201.g1.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomDao extends JpaRepository<Room, Integer> {


    @Query("SELECT DISTINCT r.roomType FROM Room r")
    List<String> findDistinctRoomTypes();


}
