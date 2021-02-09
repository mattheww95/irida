import React from "react";
import { Modal, notification, Typography } from "antd";
import { VisibilityProvider } from "../../../contexts/visibility-context";
import { formatDate } from "../../../utilities/date-utilities";
import Markdown from "react-markdown";
import { LinkButton } from "../../../components/Buttons/LinkButton";
import { PriorityFlag } from "./PriorityFlag";
import { getAnnouncement } from "../../../apis/announcements/announcements";

const { Text } = Typography;
const { confirm } = Modal;

/**
 * Component to add a button which will open a modal to view a unread announcement.
 * @param {long} announcementID - the announcement identifier.
 * @param {string} announcementTitle - the announcement title to be displayed in the link.
 * @param {function} markAnnouncementAsRead - the function that marks an announcement as read.
 * @returns {*}
 * @constructor
 */
function ViewUnreadAnnouncementModal({
  announcementID,
  announcementTitle,
  markAnnouncementAsRead,
}) {
  function showAnnouncement(aID) {
    return getAnnouncement({ aID })
      .then((data) => {
        displayAnnouncement({ announcement: data });
      })
      .catch(({ message }) => {
        notification.error({ message });
      });
  }

  const displayAnnouncement = ({ announcement }) =>
    confirm({
      type: "info",
      width: `60%`,
      icon: <PriorityFlag hasPriority={announcement.priority} />,
      okText: `Read`,
      onOk() {
        markAnnouncementAsRead(announcement.identifier);
      },
      title: (
        <>
          <Text strong>{announcement.title}</Text>
          <br />
          <Text type="secondary" style={{ fontSize: `.8em` }}>
            {i18n(
              "ViewUnreadAnnouncement.details",
              announcement.user.username,
              formatDate({ date: announcement.createdDate })
            )}
          </Text>
        </>
      ),
      content: (
        <div style={{ overflowY: "auto", maxHeight: 600 }}>
          <Markdown source={announcement.message} />
        </div>
      ),
    });
  return (
    <>
      <LinkButton
        text={announcementTitle}
        onClick={() => showAnnouncement(announcementID)}
      />
    </>
  );
}
export default function ViewUnreadAnnouncement({
  announcementID,
  announcementTitle,
  markAnnouncementAsRead,
}) {
  return (
    <VisibilityProvider>
      <ViewUnreadAnnouncementModal
        announcementID={announcementID}
        announcementTitle={announcementTitle}
        markAnnouncementAsRead={markAnnouncementAsRead}
      />
    </VisibilityProvider>
  );
}
