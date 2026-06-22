package com.rohitlokhande.repository;

import com.rohitlokhande.model.Coin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinRepository extends JpaRepository<Coin,String> {
}
