import React from "react";
import { useSelector } from "react-redux";
import { navigate } from "@reach/router";
import {
  List,
  notification,
  PageHeader,
  Skeleton,
  Table,
  Typography,
} from "antd";
import { addKeysToList } from "../../../utilities/http-utilities";

const { Text } = Typography;

/**
 * Component for viewing a metadata template.  This is for members who cannot
 * manage the current project.
 *
 * @param {number} id - identifier for the current template
 * @returns {JSX.Element}
 * @constructor
 */
export function MetadataTemplateMember({ id }) {
  const { templates, loading } = useSelector((state) => state.templates);
  const [template, setTemplate] = React.useState({});

  React.useEffect(() => {
    /*
    On mount we need to find the current template in the list of all templates.
    If it is not found the we redirect to all templates, if no templates at all
    are found then we redirect to the metadata fields page so the user can
    create one.
     */
    if (!loading) {
      const found = templates.find((template) => template.identifier === id);

      if (found) {
        setTemplate(found);
      } else if (templates.length === 0) {
        navigate(`../fields`).then(() =>
          notification.warn({ message: "__No templates found" })
        );
      } else {
        navigate(`../templates`).then(() =>
          notification.warn({ message: "__Template cannot be found" })
        );
      }
    }
  }, [id, templates]);

  return (
    <PageHeader title={template.name} onBack={() => navigate("./")}>
      <Skeleton loading={loading}>
        <List itemLayout="vertical" size="small">
          <List.Item>
            <List.Item.Meta
              title={<Text strong>{i18n("MetadataTemplate.label")}</Text>}
              description={template.name}
            />
          </List.Item>
          <List.Item>
            <List.Item.Meta
              title={<Text strong>{i18n("MetadataTemplate.description")}</Text>}
              description={template.description || ""}
            />
          </List.Item>
          <List.Item>
            <List.Item.Meta
              title={<Text strong>{i18n("MetadataTemplate.fields")}</Text>}
              description={
                <Table
                  pagination={false}
                  columns={[
                    { title: i18n("MetadataField.label"), dataIndex: "label" },
                    { title: i18n("MetadataField.type"), dataIndex: "type" },
                  ]}
                  dataSource={addKeysToList(template.fields || [], "field")}
                />
              }
            />
          </List.Item>
        </List>
      </Skeleton>
    </PageHeader>
  );
}
