package edu.ucsb.cs156.example.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HelpRequestsRepository extends CrudRepository<UCSBDate, Long> {}
