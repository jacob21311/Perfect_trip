package idv.tia201.g1.product.dao;

import idv.tia201.g1.order.entity.Order;
import idv.tia201.g1.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDao extends JpaRepository<Product, Integer> {


    //List<Product> getProductsByUserId(Integer userId);

    List<Product> getProductsByCompanyId(Integer companyId);

    List<Product> findAll();
}
