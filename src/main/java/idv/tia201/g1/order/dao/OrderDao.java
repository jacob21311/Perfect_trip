package idv.tia201.g1.order.dao;

import idv.tia201.g1.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface OrderDao extends JpaRepository<Order, Integer> {
    List<Order> findByUserId(Integer userId);

    Order findByOrderId(Integer orderId);

    @Query("SELECT o FROM Order o " +
            "JOIN OrderDetail od ON od.orderId = o.orderId " +
            "JOIN Product p ON p.productId = od.productId " +
            "WHERE p.companyId = :companyId")
    List<Order> findByCompanyId(@Param("companyId") Integer companyId);

    @Query("SELECT SUM(p.price * od.quantity) FROM Order o " +
            "JOIN OrderDetail od ON od.orderId = o.orderId " +
            "JOIN Product p ON p.productId = od.productId " +
            "WHERE o.orderId = :orderId")
    Integer calculateTotalPrice(@Param("orderId") Integer orderId);

    @Query("SELECT MIN(pd.discountRate) " +
            "FROM ProductDiscount pd " +
            "WHERE pd.companyId = :companyId " +
            "AND pd.startDateTime <= :date " +
            "AND pd.endDateTime >= :date")
    Double getDiscountByCompanyIdAndDate(
            @Param("companyId") Integer companyId,
            @Param("date") Date date);

}
