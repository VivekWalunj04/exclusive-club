package com.vivek.clubRegistration.repository;

import com.vivek.clubRegistration.model.Member;
import com.vivek.clubRegistration.model.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // ── Duplicate Check ─────────────────────────────────────
    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    // ── Status Queries ──────────────────────────────────────
    List<Member> findByStatusOrderByRegisteredAtDesc(MemberStatus status);

    List<Member> findAllByOrderByRegisteredAtDesc();

    long countByStatus(MemberStatus status);

    // ── Membership Type ─────────────────────────────────────
    List<Member> findByMembershipType(String membershipType);

    // ── Stats for dashboard ─────────────────────────────────
    @Query("SELECT m.membershipType, COUNT(m) FROM Member m WHERE m.status = 'APPROVED' GROUP BY m.membershipType")
    List<Object[]> countApprovedByType();

    void delete(Member member);
}