package com.marimo.server.domain.order.entity;

import com.marimo.server.domain.order.dto.Account;
import com.marimo.server.domain.order.dto.SelectedOption;
import com.marimo.server.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "invitation_order")
public class InvitationOrderEntity extends BaseTimeEntity {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "option_list", columnDefinition = "jsonb", nullable = false)
    private List<SelectedOption> optionList;

    @Column(name = "groom_father_deceased", nullable = false)
    private Boolean groomFatherDeceased;

    @Column(name = "has_groom_father_christian_name", nullable = false)
    private Boolean hasGroomFatherChristianName;

    @Column(name = "groom_father_name", length = 30)
    private String groomFatherName;

    @Column(name = "groom_father_christian_name", length = 30)
    private String groomFatherChristianName;

    @Column(name = "groom_mother_deceased", nullable = false)
    private Boolean groomMotherDeceased;

    @Column(name = "has_groom_mother_christian_name", nullable = false)
    private Boolean hasGroomMotherChristianName;

    @Column(name = "groom_mother_name", length = 30)
    private String groomMotherName;

    @Column(name = "groom_mother_christian_name", length = 30)
    private String groomMotherChristianName;

    @Column(name = "bride_father_deceased", nullable = false)
    private Boolean brideFatherDeceased;

    @Column(name = "has_bride_father_christian_name", nullable = false)
    private Boolean hasBrideFatherChristianName;

    @Column(name = "bride_father_name", length = 30)
    private String brideFatherName;

    @Column(name = "bride_father_christian_name", length = 30)
    private String brideFatherChristianName;

    @Column(name = "bride_mother_deceased", nullable = false)
    private Boolean brideMotherDeceased;

    @Column(name = "has_bride_mother_christian_name", nullable = false)
    private Boolean hasBrideMotherChristianName;

    @Column(name = "bride_mother_name", length = 30)
    private String brideMotherName;

    @Column(name = "bride_mother_christian_name", length = 30)
    private String brideMotherChristianName;

    @Column(name = "wedding_venue_zone_code", length = 5, nullable = false)
    private String weddingVenueZoneCode;

    @Column(name = "wedding_venue_address", length = 100, nullable = false)
    private String weddingVenueAddress;

    @Column(name = "wedding_venue_detail_address", length = 100)
    private String weddingVenueDetailAddress;

    @Column(name = "paper_invitation_message", length = 200, nullable = false)
    private String paperInvitationMessage;

    @Column(name = "has_charter_bus", nullable = false)
    private Boolean hasCharterBus;

    @Column(name = "bus_stop_location", length = 100)
    private String busStopLocation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bus_stop_time_list", columnDefinition = "jsonb")
    private List<LocalTime> busStopTimeList;

    @Column(name = "has_reception", nullable = false)
    private Boolean hasReception;

    @Column(name = "reception_address", length = 100)
    private String receptionAddress;

    @Column(name = "reception_datetime")
    private LocalDateTime receptionDatetime;

    @Column(name = "has_mobile_invitation", nullable = false)
    private Boolean hasMobileInvitation;

    @Column(name = "mobile_invitation_url_slug", length = 20)
    private String mobileInvitationUrlSlug;

    @Column(name = "mobile_invitation_message", length = 200)
    private String mobileInvitationMessage;

    @Column(name = "has_gallery")
    private Boolean hasGallery;

    @Column(name = "has_contact_option")
    private Boolean hasContactOption;

    @Column(name = "groom_father_phone_number", length = 20)
    private String groomFatherPhoneNumber;

    @Column(name = "groom_mother_phone_number", length = 20)
    private String groomMotherPhoneNumber;

    @Column(name = "groom_phone_number", length = 20)
    private String groomPhoneNumber;

    @Column(name = "bride_father_phone_number", length = 20)
    private String brideFatherPhoneNumber;

    @Column(name = "bride_mother_phone_number", length = 20)
    private String brideMotherPhoneNumber;

    @Column(name = "bride_phone_number", length = 20)
    private String bridePhoneNumber;

    @Column(name = "has_gift_account")
    private Boolean hasGiftAccount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "groom_gift_account_list", columnDefinition = "jsonb")
    private List<Account> groomGiftAccountList;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bride_gift_account_list", columnDefinition = "jsonb")
    private List<Account> brideGiftAccountList;

    @Column(name = "has_calendar")
    private Boolean hasCalendar;

    @Column(name = "has_map_navigation")
    private Boolean hasMapNavigation;

    @Column(name = "has_guestbook")
    private Boolean hasGuestbook;

    @Column(name = "admin_password", length = 10)
    private String adminPassword;

    @Column(name = "has_rsvp")
    private Boolean hasRsvp;

    @Column(name = "has_primary_contact_field")
    private Boolean hasPrimaryContactField;

    @Column(name = "has_companion_field")
    private Boolean hasCompanionField;

    @Column(name = "has_meal_option_field")
    private Boolean hasMealOptionField;

    @Builder
    public InvitationOrderEntity(
            Long orderId,
            List<SelectedOption> optionList,
            Boolean groomFatherDeceased,
            Boolean hasGroomFatherChristianName,
            String groomFatherName,
            String groomFatherChristianName,
            Boolean groomMotherDeceased,
            Boolean hasGroomMotherChristianName,
            String groomMotherName,
            String groomMotherChristianName,
            Boolean brideFatherDeceased,
            Boolean hasBrideFatherChristianName,
            String brideFatherName,
            String brideFatherChristianName,
            Boolean brideMotherDeceased,
            Boolean hasBrideMotherChristianName,
            String brideMotherName,
            String brideMotherChristianName,
            String weddingVenueZoneCode,
            String weddingVenueAddress,
            String weddingVenueDetailAddress,
            String paperInvitationMessage,
            Boolean hasCharterBus,
            String busStopLocation,
            List<LocalTime> busStopTimeList,
            Boolean hasReception,
            String receptionAddress,
            LocalDateTime receptionDatetime,
            Boolean hasMobileInvitation,
            String mobileInvitationUrlSlug,
            String mobileInvitationMessage,
            Boolean hasGallery,
            Boolean hasContactOption,
            String groomFatherPhoneNumber,
            String groomMotherPhoneNumber,
            String groomPhoneNumber,
            String brideFatherPhoneNumber,
            String brideMotherPhoneNumber,
            String bridePhoneNumber,
            Boolean hasGiftAccount,
            List<Account> groomGiftAccountList,
            List<Account> brideGiftAccountList,
            Boolean hasCalendar,
            Boolean hasMapNavigation,
            Boolean hasGuestbook,
            String adminPassword,
            Boolean hasRsvp,
            Boolean hasPrimaryContactField,
            Boolean hasCompanionField,
            Boolean hasMealOptionField
    ) {
        this.orderId = orderId;
        this.optionList = optionList;
        this.groomFatherDeceased = groomFatherDeceased;
        this.hasGroomFatherChristianName = hasGroomFatherChristianName;
        this.groomFatherName = groomFatherName;
        this.groomFatherChristianName = groomFatherChristianName;
        this.groomMotherDeceased = groomMotherDeceased;
        this.hasGroomMotherChristianName = hasGroomMotherChristianName;
        this.groomMotherName = groomMotherName;
        this.groomMotherChristianName = groomMotherChristianName;
        this.brideFatherDeceased = brideFatherDeceased;
        this.hasBrideFatherChristianName = hasBrideFatherChristianName;
        this.brideFatherName = brideFatherName;
        this.brideFatherChristianName = brideFatherChristianName;
        this.brideMotherDeceased = brideMotherDeceased;
        this.hasBrideMotherChristianName = hasBrideMotherChristianName;
        this.brideMotherName = brideMotherName;
        this.brideMotherChristianName = brideMotherChristianName;
        this.weddingVenueZoneCode = weddingVenueZoneCode;
        this.weddingVenueAddress = weddingVenueAddress;
        this.weddingVenueDetailAddress = weddingVenueDetailAddress;
        this.paperInvitationMessage = paperInvitationMessage;
        this.hasCharterBus = hasCharterBus;
        this.busStopLocation = busStopLocation;
        this.busStopTimeList = busStopTimeList;
        this.hasReception = hasReception;
        this.receptionAddress = receptionAddress;
        this.receptionDatetime = receptionDatetime;
        this.hasMobileInvitation = hasMobileInvitation;
        this.mobileInvitationUrlSlug = mobileInvitationUrlSlug;
        this.mobileInvitationMessage = mobileInvitationMessage;
        this.hasGallery = hasGallery;
        this.hasContactOption = hasContactOption;
        this.groomFatherPhoneNumber = groomFatherPhoneNumber;
        this.groomMotherPhoneNumber = groomMotherPhoneNumber;
        this.groomPhoneNumber = groomPhoneNumber;
        this.brideFatherPhoneNumber = brideFatherPhoneNumber;
        this.brideMotherPhoneNumber = brideMotherPhoneNumber;
        this.bridePhoneNumber = bridePhoneNumber;
        this.hasGiftAccount = hasGiftAccount;
        this.groomGiftAccountList = groomGiftAccountList;
        this.brideGiftAccountList = brideGiftAccountList;
        this.hasCalendar = hasCalendar;
        this.hasMapNavigation = hasMapNavigation;
        this.hasGuestbook = hasGuestbook;
        this.adminPassword = adminPassword;
        this.hasRsvp = hasRsvp;
        this.hasPrimaryContactField = hasPrimaryContactField;
        this.hasCompanionField = hasCompanionField;
        this.hasMealOptionField = hasMealOptionField;
    }
}
