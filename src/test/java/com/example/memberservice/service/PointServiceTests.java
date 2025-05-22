package com.example.memberservice.service;

import com.example.memberservice.dto.PrivilegeUseRequestDto;
import com.example.memberservice.entity.Member;
import com.example.memberservice.entity.MemberLevel;
import com.example.memberservice.entity.Privilege;
import com.example.memberservice.exception.*;
import com.example.memberservice.repository.MemberRepository;
import com.example.memberservice.repository.PointBalanceRepository;
import com.example.memberservice.repository.PointRecordRepository;
import com.example.memberservice.repository.PointTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceTests {

    @Mock
    private PointRecordRepository pointRecordRepository;
    @Mock
    private PointTransactionRepository pointTransactionRepository;
    @Mock
    private PointBalanceRepository pointBalanceRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PrivilegeService privilegeService;
    @Mock
    private PrivilegeUsageLogService privilegeUsageLogService;

    @InjectMocks
    private PointService pointService;

    private Member member;
    private Privilege earnPointsPrivilege;
    private Privilege discountPrivilege;
    private PrivilegeUseRequestDto earnPointsRequest;
    private PrivilegeUseRequestDto discountRequest;
    private MemberLevel memberLevel;

    @BeforeEach
    void setUp() {
        memberLevel = new MemberLevel();
        memberLevel.setLevel(MemberLevel.Level.GOLD);

        member = new Member();
        member.setId(1L);
        member.setName("Test User");
        member.setMemberLevel(memberLevel);

        earnPointsPrivilege = new Privilege();
        earnPointsPrivilege.setId(1L);
        earnPointsPrivilege.setName("EARN_100_POINTS");
        earnPointsPrivilege.setActive(true);
        earnPointsPrivilege.setBenefitActionType("EARN_POINTS");
        earnPointsPrivilege.setBenefitPointAmount(100);
        earnPointsPrivilege.setMinMemberLevel(MemberLevel.Level.BRONZE);

        earnPointsRequest = new PrivilegeUseRequestDto();
        earnPointsRequest.setPrivilegeId(1L);

        discountPrivilege = new Privilege();
        discountPrivilege.setId(2L);
        discountPrivilege.setName("DISCOUNT_10_PERCENT");
        discountPrivilege.setActive(true);
        discountPrivilege.setBenefitActionType("EXAMPLE_DISCOUNT");
        discountPrivilege.setBenefitPointAmount(10); // 10% discount
        discountPrivilege.setMinMemberLevel(MemberLevel.Level.SILVER);
        
        Map<String, Object> params = new HashMap<>();
        params.put("originalCost", 200.0); // Example cost

        discountRequest = new PrivilegeUseRequestDto();
        discountRequest.setPrivilegeId(2L);
        discountRequest.setParams(params);
    }

    // --- applyPrivilegeForPointBenefit Tests ---

    @Test
    void applyPrivilegeForPointBenefit_EarnPoints_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(1L)).thenReturn(earnPointsPrivilege);
        when(privilegeService.canMemberUsePrivilege(1L, 1L)).thenReturn(true);
        // earnPoints method is void, so no need to mock its direct return unless it throws exceptions in other scenarios
        doNothing().when(privilegeUsageLogService).logPrivilegeUsage(anyLong(), anyLong(), anyString());

        pointService.applyPrivilegeForPointBenefit(1L, earnPointsRequest);

        // Verify earnPoints was called (indirectly, by checking its side-effects or mocks if it called other services)
        // For this test, we verify that logPrivilegeUsage was called, which is a good indicator.
        // A more thorough test for earnPoints itself would be in its own test suite or if earnPoints called other mocked services.
        verify(privilegeUsageLogService, times(1)).logPrivilegeUsage(
            eq(1L), eq(1L), eq("Used privilege: EARN_100_POINTS to earn 100 points.")
        );
        // We can also use an ArgumentCaptor on `earnPoints` if we want to check the arguments passed to it.
        // Since `earnPoints` is a public method in the same class, we can spy on `pointService` or refactor `earnPoints` to a separate component.
        // For simplicity, here we assume `earnPoints` behaves correctly and logging is sufficient.
    }
    
    @Test
    void applyPrivilegeForPointBenefit_ExampleDiscount_Success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(2L)).thenReturn(discountPrivilege);
        when(privilegeService.canMemberUsePrivilege(1L, 2L)).thenReturn(true);
        doNothing().when(privilegeUsageLogService).logPrivilegeUsage(anyLong(), anyLong(), anyString());

        pointService.applyPrivilegeForPointBenefit(1L, discountRequest);

        int expectedDiscountPoints = (int) (200.0 * (10 / 100.0)); // 20 points

        verify(privilegeUsageLogService, times(1)).logPrivilegeUsage(
            eq(1L), eq(2L), eq("Used privilege: DISCOUNT_10_PERCENT for a discount equivalent to " + expectedDiscountPoints + " points on an item costing 200.0.")
        );
         // Similar to EARN_POINTS, verifying earnPoints was called for the discount.
    }


    @Test
    void applyPrivilegeForPointBenefit_PrivilegeNotFound_ThrowsPrivilegeNotFoundException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(1L)).thenThrow(new PrivilegeNotFoundException("Privilege not found"));

        assertThrows(PrivilegeNotFoundException.class, () -> {
            pointService.applyPrivilegeForPointBenefit(1L, earnPointsRequest);
        });
    }

    @Test
    void applyPrivilegeForPointBenefit_PrivilegeNotActive_ThrowsPrivilegeNotActiveException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(1L)).thenThrow(new PrivilegeNotActiveException("Privilege not active"));

        assertThrows(PrivilegeNotActiveException.class, () -> {
            pointService.applyPrivilegeForPointBenefit(1L, earnPointsRequest);
        });
    }

    @Test
    void applyPrivilegeForPointBenefit_NotAuthorized_ThrowsPrivilegeNotAuthorizedException() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(1L)).thenReturn(earnPointsPrivilege);
        when(privilegeService.canMemberUsePrivilege(1L, 1L)).thenReturn(false);

        assertThrows(PrivilegeNotAuthorizedException.class, () -> {
            pointService.applyPrivilegeForPointBenefit(1L, earnPointsRequest);
        });
    }

    @Test
    void applyPrivilegeForPointBenefit_EarnPoints_InvalidAmount_ThrowsIllegalStateException() {
        earnPointsPrivilege.setBenefitPointAmount(null); // Invalid amount
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(1L)).thenReturn(earnPointsPrivilege);
        when(privilegeService.canMemberUsePrivilege(1L, 1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            pointService.applyPrivilegeForPointBenefit(1L, earnPointsRequest);
        });
    }
    
    @Test
    void applyPrivilegeForPointBenefit_EarnPoints_ZeroAmount_ThrowsIllegalStateException() {
        earnPointsPrivilege.setBenefitPointAmount(0); // Invalid amount
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(1L)).thenReturn(earnPointsPrivilege);
        when(privilegeService.canMemberUsePrivilege(1L, 1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            pointService.applyPrivilegeForPointBenefit(1L, earnPointsRequest);
        });
    }

    @Test
    void applyPrivilegeForPointBenefit_UnsupportedActionType_ThrowsUnsupportedOperationException() {
        earnPointsPrivilege.setBenefitActionType("UNKNOWN_ACTION");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(1L)).thenReturn(earnPointsPrivilege);
        when(privilegeService.canMemberUsePrivilege(1L, 1L)).thenReturn(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            pointService.applyPrivilegeForPointBenefit(1L, earnPointsRequest);
        });
    }
    
    @Test
    void applyPrivilegeForPointBenefit_NullActionType_ThrowsUnsupportedOperationException() {
        earnPointsPrivilege.setBenefitActionType(null); // Null action type
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(1L)).thenReturn(earnPointsPrivilege);
        when(privilegeService.canMemberUsePrivilege(1L, 1L)).thenReturn(true);

        assertThrows(UnsupportedOperationException.class, () -> {
            pointService.applyPrivilegeForPointBenefit(1L, earnPointsRequest);
        });
    }

    @Test
    void applyPrivilegeForPointBenefit_ExampleDiscount_MissingParams_ThrowsIllegalArgumentException() {
        discountRequest.setParams(null); // Missing params
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(2L)).thenReturn(discountPrivilege);
        when(privilegeService.canMemberUsePrivilege(1L, 2L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            pointService.applyPrivilegeForPointBenefit(1L, discountRequest);
        });
    }

    @Test
    void applyPrivilegeForPointBenefit_ExampleDiscount_InvalidOriginalCostParam_ThrowsIllegalArgumentException() {
        Map<String, Object> params = new HashMap<>();
        params.put("originalCost", "not-a-number"); // Invalid type for originalCost
        discountRequest.setParams(params); 

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(2L)).thenReturn(discountPrivilege);
        when(privilegeService.canMemberUsePrivilege(1L, 2L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            pointService.applyPrivilegeForPointBenefit(1L, discountRequest);
        });
    }
    
    @Test
    void applyPrivilegeForPointBenefit_ExampleDiscount_InvalidDiscountPercentage_ThrowsIllegalStateException() {
        discountPrivilege.setBenefitPointAmount(null); // Invalid discount percentage
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(2L)).thenReturn(discountPrivilege);
        when(privilegeService.canMemberUsePrivilege(1L, 2L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            pointService.applyPrivilegeForPointBenefit(1L, discountRequest);
        });
    }

    @Test
    void applyPrivilegeForPointBenefit_ExampleDiscount_ZeroDiscountPercentage_ThrowsIllegalStateException() {
        discountPrivilege.setBenefitPointAmount(0); // Zero discount percentage
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(2L)).thenReturn(discountPrivilege);
        when(privilegeService.canMemberUsePrivilege(1L, 2L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            pointService.applyPrivilegeForPointBenefit(1L, discountRequest);
        });
    }

    @Test
    void applyPrivilegeForPointBenefit_ExampleDiscount_TooHighDiscountPercentage_ThrowsIllegalStateException() {
        discountPrivilege.setBenefitPointAmount(101); // > 100 discount percentage
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(privilegeService.verifyAndGetPrivilege(2L)).thenReturn(discountPrivilege);
        when(privilegeService.canMemberUsePrivilege(1L, 2L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            pointService.applyPrivilegeForPointBenefit(1L, discountRequest);
        });
    }


    // Add other PointService tests here if they exist, to keep them separate
    // For example, tests for redeemPoints and earnPoints (non-privilege related)
}
