package hotel.db.repository.discount;

import hotel.db.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    //check xem code voucher da ton tai hay chua.
    boolean existsByCodeAndIsDeletedFalse(String code);

    //check xem code da bi ton tai ngoai code cua chinh no hay chua (dung cho update)
    boolean existsByCodeAndDiscountIdNotAndIsDeletedFalse(String code, Long discountId);

    //lay tat ca discount chua bi xoa
    List<Discount> findAllByIsDeletedFalse();

    //Tim discount theo id de hien thi details
    Discount findDiscountByDiscountId(Long discountId);

    //soft delete
    @Modifying
    @Transactional
    @Query("UPDATE Discount d SET d.isDeleted = true WHERE d.discountId = :id")
    void softDeleteById(@Param("id") Long id);

}