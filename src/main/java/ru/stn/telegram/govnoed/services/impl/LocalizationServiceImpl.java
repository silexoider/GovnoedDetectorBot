package ru.stn.telegram.govnoed.services.impl;

import ru.stn.telegram.govnoed.services.LocalizationService;

import java.util.ResourceBundle;

public class LocalizationServiceImpl implements LocalizationService {
    @Override
    public String getMessage(ResourceBundle resourceBundle, Message message) {
        return resourceBundle.getString(message.getName());
    }

    @Override
    public String getMenuMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.MENU_MESSAGE);
    }

    @Override
    public String getZoneActionSuccessMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.ZONE_ACTION_SUCCESS_MESSAGE);
    }

    @Override
    public String getZoneActionFailureMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.ZONE_ACTION_FAILURE_MESSAGE);
    }

    @Override
    public String getZoneActionFailureInvalidTimezoneMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.ZONE_ACTION_FAILURE_INVALID_TIMEZONE_MESSAGE);
    }

    @Override
    public String getZoneActionFailureInsufficientPrivilegesMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.ZONE_ACTION_FAILURE_INSUFFICIENT_PRIVILEGES_MESSAGE);
    }

    @Override
    public String getViewZoneExistsMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.VIEW_ZONE_EXISTS_MESSAGE);
    }

    @Override
    public String getViewZoneAbsentMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.VIEW_ZONE_ABSENT_MESSAGE);
    }

    @Override
    public String getVoteActionMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.VOTE_ACTION_MESSAGE);
    }

    @Override
    public String getVoteActionInvalidDateMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.VOTE_ACTION_INVALID_DATE_MESSAGE);
    }

    @Override
    public String getVoteActionDeniedMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.VOTE_ACTION_DENIED_MESSAGE);
    }

    @Override
    public String getViewVoteMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.VIEW_VOTE_MESSAGE);
    }

    @Override
    public String getViewVoteMessageNotVoted(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.VIEW_VOTE_MESSAGE_NOT_VOTED);
    }

    @Override
    public String getRevokeMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.REVOKE_MESSAGE);
    }

    @Override
    public String getUnableToRevokeMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.UNABLE_TO_REVOKE_MESSAGE);
    }

    @Override
    public String getSingleWinnerMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.SINGLE_WINNER_MESSAGE);
    }

    @Override
    public String getMultipleWinnersMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.MULTIPLE_WINNERS_MESSAGE);
    }

    @Override
    public String getNoWinnerMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.NO_WINNER_MESSAGE);
    }

    @Override
    public String getScoresMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.SCORES_MESSAGE);
    }

    @Override
    public String getScoresEntry(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.SCORES_ENTRY);
    }

    @Override
    public String getShowPresentUserMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.SHOW_PRESENT_USER_MESSAGE);
    }

    @Override
    public String getShowAbsentUserMessage(ResourceBundle resourceBundle) {
        return getMessage(resourceBundle, Message.SHOW_ABSENT_USER_MESSAGE);
    }
}
