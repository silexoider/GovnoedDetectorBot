package ru.stn.telegram.govnoed.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ResourceBundle;

public interface LocalizationService {
    @Getter
    @RequiredArgsConstructor
    enum Message {
        MENU_MESSAGE("menu_message"),
        ZONE_ACTION_SUCCESS_MESSAGE("zone_action_success_message"),
        ZONE_ACTION_FAILURE_MESSAGE("zone_action_failure_message"),
        ZONE_ACTION_FAILURE_INVALID_TIMEZONE_MESSAGE("zone_action_failure_invalid_timezone_message"),
        ZONE_ACTION_FAILURE_INSUFFICIENT_PRIVILEGES_MESSAGE("zone_action_failure_insufficient_privileges_message"),
        VIEW_ZONE_EXISTS_MESSAGE("view_zone_exists_message"),
        VIEW_ZONE_ABSENT_MESSAGE("view_zone_absent_message"),
        VOTE_ACTION_MESSAGE("vote_action_message"),
        VOTE_ACTION_INVALID_DATE_MESSAGE("vote_action_invalid_date_message"),
        VOTE_ACTION_DENIED_MESSAGE("vote_action_denied_message"),
        VIEW_VOTE_MESSAGE("view_vote_message"),
        VIEW_VOTE_MESSAGE_NOT_VOTED("view_vote_message_not_voted"),
        REVOKE_MESSAGE("revoke_message"),
        UNABLE_TO_REVOKE_MESSAGE("unable_to_revoke_message"),
        SINGLE_WINNER_MESSAGE("single_winner_message"),
        MULTIPLE_WINNERS_MESSAGE("multiple_winners_message"),
        NO_WINNER_MESSAGE("no_winner_message"),
        SCORES_MESSAGE("scores_message"),
        SCORES_ENTRY("scores_entry"),
        SHOW_PRESENT_USER_MESSAGE("show_present_user_message"),
        SHOW_ABSENT_USER_MESSAGE("show_absent_user_message"),
        UNABLE_TO_RECOGNIZE_DATE_MESSAGE("unable_to_recognize_date_message"),
        INVALID_WINNER_DATE_MESSAGE("invalid_winner_date_message"),
        RIGGING_VOTE_ACTION_MESSAGE("rigging_vote_action_message"),
        RIGGING_VOTE_ACTION_DENIED_MESSAGE("rigging_vote_action_denied_message"),
        RIGGING_MESSAGE("rigging_message"),
        MENU_BUTTON_MESSAGE("menu_button_message"),
        VOTE_BUTTON_MESSAGE("vote_button_message"),
        REVOKE_BUTTON_MESSAGE("revoke_button_message"),
        WINNER_BUTTON_MESSAGE("winner_button_message"),
        SCORES_BUTTON_MESSAGE("scores_button_message");

        private final String name;
    }

    String getMessage(ResourceBundle resourceBundle, Message message);

    String getMenuMessage(ResourceBundle resourceBundle);
    String getZoneActionSuccessMessage(ResourceBundle resourceBundle);
    String getZoneActionFailureMessage(ResourceBundle resourceBundle);
    String getZoneActionFailureInvalidTimezoneMessage(ResourceBundle resourceBundle);
    String getZoneActionFailureInsufficientPrivilegesMessage(ResourceBundle resourceBundle);
    String getViewZoneExistsMessage(ResourceBundle resourceBundle);
    String getViewZoneAbsentMessage(ResourceBundle resourceBundle);
    String getVoteActionMessage(ResourceBundle resourceBundle);
    String getVoteActionInvalidDateMessage(ResourceBundle resourceBundle);
    String getVoteActionDeniedMessage(ResourceBundle resourceBundle);
    String getViewVoteMessage(ResourceBundle resourceBundle);
    String getViewVoteMessageNotVoted(ResourceBundle resourceBundle);
    String getRevokeMessage(ResourceBundle resourceBundle);
    String getUnableToRevokeMessage(ResourceBundle resourceBundle);
    String getSingleWinnerMessage(ResourceBundle resourceBundle);
    String getMultipleWinnersMessage(ResourceBundle resourceBundle);
    String getNoWinnerMessage(ResourceBundle resourceBundle);
    String getScoresMessage(ResourceBundle resourceBundle);
    String getScoresEntry(ResourceBundle resourceBundle);
    String getShowPresentUserMessage(ResourceBundle resourceBundle);
    String getShowAbsentUserMessage(ResourceBundle resourceBundle);
    String getUnableToRecognizeDateMessage(ResourceBundle resourceBundle);
    String getInvalidWinnerDateMessage(ResourceBundle resourceBundle);
    String getRiggingVoteActionMessage(ResourceBundle resourceBundle);
    String getRiggingVoteActionDeniedMessage(ResourceBundle resourceBundle);
    String getRiggingMessage(ResourceBundle resourceBundle);
    String getMenuButtonMessage(ResourceBundle resourceBundle);
    String getVoteButtonMessage(ResourceBundle resourceBundle);
    String getRevokeButtonMessage(ResourceBundle resourceBundle);
    String getWinnerButtonMessage(ResourceBundle resourceBundle);
    String getScoresButtonMessage(ResourceBundle resourceBundle);
}
