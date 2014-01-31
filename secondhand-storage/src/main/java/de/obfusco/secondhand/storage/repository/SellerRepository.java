package de.obfusco.secondhand.storage.repository;

import de.obfusco.secondhand.storage.model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Integer> {

}
