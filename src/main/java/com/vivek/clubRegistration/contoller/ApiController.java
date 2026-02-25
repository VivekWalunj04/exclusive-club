package com.vivek.clubRegistration.contoller;

import com.vivek.clubRegistration.model.ApiResponse;
import com.vivek.clubRegistration.model.Member;          // ✅ FIXED: Added missing import
import com.vivek.clubRegistration.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final MemberService memberService;

    // ════════════════════════════════════════════════════════
    //  PUBLIC ENDPOINTS
    // ════════════════════════════════════════════════════════

    /**
     * POST /api/register
     * Register a new member (subject to all 4 rules)
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Member>> register(@Valid @RequestBody Member member) {
        Member saved = memberService.registerMember(member);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok(
                        "Registration submitted successfully! Your application is pending admin approval.",
                        saved
                ));
    }

    /**
     * GET /api/members
     * Get all members (all statuses)
     */
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<List<Member>>> getAllMembers() {
        return ResponseEntity.ok(ApiResponse.ok("All members", memberService.getAllMembers()));
    }

    /**
     * GET /api/members/approved
     * Get only approved members
     */
    @GetMapping("/members/approved")
    public ResponseEntity<ApiResponse<List<Member>>> getApprovedMembers() {
        return ResponseEntity.ok(ApiResponse.ok("Approved members", memberService.getApprovedMembers()));
    }

    /**
     * GET /api/members/pending
     * Get all pending applications
     */
    @GetMapping("/members/pending")
    public ResponseEntity<ApiResponse<List<Member>>> getPendingMembers() {
        return ResponseEntity.ok(ApiResponse.ok("Pending members", memberService.getPendingMembers()));
    }

    /**
     * GET /api/members/{id}
     * Get a single member by ID
     */
    @GetMapping("/members/{id}")
    public ResponseEntity<ApiResponse<Member>> getMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Member found", memberService.findById(id)));
    }

    /**
     * GET /api/stats
     * Get club statistics (capacity, counts per status)
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok("Club statistics", memberService.getStats()));
    }

    // ════════════════════════════════════════════════════════
    //  ADMIN ENDPOINTS
    // ════════════════════════════════════════════════════════

    /**
     * POST /api/admin/approve/{id}
     * Approve a pending member
     */
    @PostMapping("/admin/approve/{id}")
    public ResponseEntity<ApiResponse<Member>> approve(@PathVariable Long id) {
        Member updated = memberService.approveMember(id);
        return ResponseEntity.ok(ApiResponse.ok("Member approved successfully.", updated));
    }

    /**
     * POST /api/admin/reject/{id}?note=reason
     * Reject a pending member with optional note
     */
    @PostMapping("/admin/reject/{id}")
    public ResponseEntity<ApiResponse<Member>> reject(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Does not meet membership requirements") String note) {
        Member updated = memberService.rejectMember(id, note);
        return ResponseEntity.ok(ApiResponse.ok("Member rejected.", updated));
    }

    /**
     * DELETE /api/admin/delete/{id}
     * Delete a member record
     */
    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.ok("Member deleted successfully.", null));
    }
}