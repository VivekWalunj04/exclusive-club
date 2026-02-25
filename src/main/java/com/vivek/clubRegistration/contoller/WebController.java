package com.vivek.clubRegistration.contoller;

import com.vivek.clubRegistration.exception.RegistrationException;
import com.vivek.clubRegistration.model.Member;
import com.vivek.clubRegistration.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final MemberService memberService;

    // ── Home → Redirect to Register ────────────────────────
    @GetMapping("/")
    public String home() {
        return "redirect:/register";
    }

    // ── Show Registration Form ──────────────────────────────
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("member", new Member());
        model.addAttribute("stats", memberService.getStats());
        return "register";
    }

    // ── Handle Form Submission ──────────────────────────────
    @PostMapping("/register")
    public String submitRegisterForm(
            @Valid @ModelAttribute("member") Member member,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttrs) {

        // Step 1: Bean validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("stats", memberService.getStats());
            return "register";
        }

        // Step 2: Business rule validation
        try {
            Member saved = memberService.registerMember(member);
            redirectAttrs.addFlashAttribute("registeredMember", saved);
            return "redirect:/success";
        } catch (RegistrationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("errorRule", e.getRule());
            model.addAttribute("stats", memberService.getStats());
            return "register";
        }
    }

    // ── Success Page ────────────────────────────────────────
    @GetMapping("/success")
    public String successPage(Model model) {
        // If no flash attribute (direct URL access), redirect to register
        if (!model.containsAttribute("registeredMember")) {
            return "redirect:/register";
        }
        return "success";
    }

    // ═══════════════════════════════════════════════════════
    //  ADMIN WEB PANEL
    // ═══════════════════════════════════════════════════════

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("allMembers",     memberService.getAllMembers());
        model.addAttribute("pendingMembers", memberService.getPendingMembers());
        model.addAttribute("stats",          memberService.getStats());
        return "admin";
    }

    @PostMapping("/admin/approve/{id}")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            memberService.approveMember(id);
            redirectAttrs.addFlashAttribute("successMsg", "Member approved successfully! ✓");
        } catch (RegistrationException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/reject/{id}")
    public String reject(@PathVariable Long id,
                         @RequestParam(defaultValue = "Does not meet membership requirements") String note,
                         RedirectAttributes redirectAttrs) {
        try {
            memberService.rejectMember(id, note);
            redirectAttrs.addFlashAttribute("successMsg", "Member rejected.");
        } catch (RegistrationException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            memberService.deleteMember(id);
            redirectAttrs.addFlashAttribute("successMsg", "Member deleted.");
        } catch (RegistrationException e) {
            redirectAttrs.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin";
    }
}
