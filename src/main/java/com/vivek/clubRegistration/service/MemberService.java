package com.vivek.clubRegistration.service;

import com.vivek.clubRegistration.exception.RegistrationException;
import com.vivek.clubRegistration.model.Member;
import com.vivek.clubRegistration.model.MemberStatus;
import com.vivek.clubRegistration.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Value("${club.max.members:100}")
    private int maxMembers;

    @Value("${club.min.age:18}")
    private int minAge;

    // ════════════════════════════════════════════════════════
    //  REGISTER — Enforces all 4 membership rules
    // ════════════════════════════════════════════════════════
    @Transactional
    public Member registerMember(Member member) {
        log.info("Processing registration for email: {}", member.getEmail());

        // ── Rule 1: No Duplicate Email ─────────────────────
        if (memberRepository.existsByEmail(member.getEmail().toLowerCase().trim())) {
            throw new RegistrationException(
                    "This email address is already registered. " +
                            "Each person may only register once.",
                    "DUPLICATE_EMAIL"
            );
        }

        // ── Rule 2: Age Restriction (18+) ──────────────────
        if (member.getAge() < minAge) {
            throw new RegistrationException(
                    "You must be at least " + minAge + " years old to join. " +
                            "Your current age is " + member.getAge() + ".",
                    "AGE_RESTRICTION"
            );
        }

        // ── Rule 3: Limited Membership Slots ───────────────
        long approvedCount = memberRepository.countByStatus(MemberStatus.APPROVED);
        if (approvedCount >= maxMembers) {
            throw new RegistrationException(
                    "The club has reached its maximum capacity of " + maxMembers +
                            " members. Please check back later.",
                    "CAPACITY_FULL"
            );
        }

        // ── Rule 4: Membership Approval Required ───────────
        // All new members start as PENDING — admin must approve
        member.setStatus(MemberStatus.PENDING);
        member.setEmail(member.getEmail().toLowerCase().trim());
        member.setMembershipType(member.getMembershipType().toUpperCase());
        member.setRegisteredAt(LocalDateTime.now());

        Member saved = memberRepository.save(member);
        log.info("Member registered successfully with ID: {} | Status: PENDING", saved.getId());
        return saved;
    }

    // ════════════════════════════════════════════════════════
    //  ADMIN: Approve a member
    // ════════════════════════════════════════════════════════
    @Transactional
    public Member approveMember(Long id) {
        Member member = findById(id);

        if (member.getStatus() == MemberStatus.APPROVED) {
            throw new RegistrationException("Member is already approved.");
        }

        // Re-check capacity before approving
        long approvedCount = memberRepository.countByStatus(MemberStatus.APPROVED);
        if (approvedCount >= maxMembers) {
            throw new RegistrationException(
                    "Cannot approve. Club has reached max capacity of " + maxMembers + " members.",
                    "CAPACITY_FULL"
            );
        }

        member.setStatus(MemberStatus.APPROVED);
        member.setApprovedAt(LocalDateTime.now());
        member.setAdminNote(null);
        log.info("Member ID {} approved.", id);

        // ✅ Fixed: was "S save" — corrected to "Member"
        return memberRepository.save(member);
    }

    // ════════════════════════════════════════════════════════
    //  ADMIN: Reject a member
    // ════════════════════════════════════════════════════════
    @Transactional
    public Member rejectMember(Long id, String note) {
        Member member = findById(id);
        member.setStatus(MemberStatus.REJECTED);
        member.setAdminNote(note != null ? note : "Does not meet membership requirements.");
        log.info("Member ID {} rejected. Note: {}", id, note);
        return memberRepository.save(member);
    }

    // ════════════════════════════════════════════════════════
    //  ADMIN: Delete a member
    // ════════════════════════════════════════════════════════
    @Transactional
    public void deleteMember(Long id) {
        Member member = findById(id);
        memberRepository.delete(member);
        log.info("Member ID {} deleted.", id);
    }

    // ════════════════════════════════════════════════════════
    //  QUERIES
    // ════════════════════════════════════════════════════════
    public List<Member> getAllMembers() {
        return memberRepository.findAllByOrderByRegisteredAtDesc();
    }

    public List<Member> getPendingMembers() {
        return memberRepository.findByStatusOrderByRegisteredAtDesc(MemberStatus.PENDING);
    }

    public List<Member> getApprovedMembers() {
        return memberRepository.findByStatusOrderByRegisteredAtDesc(MemberStatus.APPROVED);
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new RegistrationException("Member not found with ID: " + id));
    }

    // ════════════════════════════════════════════════════════
    //  STATS
    // ════════════════════════════════════════════════════════
    public Map<String, Object> getStats() {
        long approved = memberRepository.countByStatus(MemberStatus.APPROVED);
        long pending  = memberRepository.countByStatus(MemberStatus.PENDING);
        long rejected = memberRepository.countByStatus(MemberStatus.REJECTED);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total",          approved + pending + rejected);
        stats.put("approved",       approved);
        stats.put("pending",        pending);
        stats.put("rejected",       rejected);
        stats.put("maxSlots",       maxMembers);
        stats.put("slotsRemaining", Math.max(0, maxMembers - approved));
        stats.put("capacityPct",    (int)((approved * 100.0) / maxMembers));
        return stats;
    }

    public int getMaxMembers() { return maxMembers; }
}