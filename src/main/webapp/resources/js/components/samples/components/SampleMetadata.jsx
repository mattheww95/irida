import React from "react";
import { Button, Empty, List, notification, Popconfirm, Space } from "antd";
import { AddNewMetadata } from "./AddNewMetadata";
import {
  useGetSampleMetadataQuery,
  useRemoveSampleMetadataMutation,
} from "../../../apis/samples/samples";
import { ContentLoading } from "../../loader";
import { IconEdit, IconPlusCircle, IconRemove } from "../../icons/Icons";
import styled from "styled-components";
import { MetadataRolesProvider } from "../../../contexts/metadata-roles-context";
import { EditMetadata } from "./EditMetadata";

const StyledListMetadata = styled(List)`
  .ant-list-item {
    padding: 15px;
    div.ant-typography,
    .ant-typography p {
      margin-bottom: 0;
    }
    .ant-typography.ant-typography-edit-content {
      margin: 0;
    }
  }
`;
/**
 * React component to display metadata associated with a sample
 *
 * @param sampleId The sample identifier
 * @param isModifiable If the current user can modify the sample or not
 * @param projectId The project identifier
 * @returns {JSX.Element}
 * @constructor
 */
export function SampleMetadata({ sampleId, isModifiable, projectId }) {
  const {
    data = {},
    isLoading,
    refetch: refetchSampleMetadata,
  } = useGetSampleMetadataQuery(sampleId);
  const [removeSampleMetadata] = useRemoveSampleMetadataMutation();
  const [editModalVisible, setEditModalVisible] = React.useState(false);

  const [field, setField] = React.useState(null);
  const [fieldId, setFieldId] = React.useState(null);
  const [entry, setEntry] = React.useState(null);
  const [entryId, setEntryId] = React.useState(null);

  const removeMetadata = (field, entryId) => {
    removeSampleMetadata({
      field,
      entryId,
    })
      .then(({ data }) => {
        notification.success({ message: data.message });
        refetchSampleMetadata();
      })
      .catch((error) => {
        notification.error({ message: error });
      });
  };

  // Set visibility of edit metadata modal to false if user clicks cancel
  const onCancel = () => {
    setEditModalVisible(false);
  };

  // Set visibility of edit metadata modal to false if user clicks ok
  const onOk = () => {
    setEditModalVisible(false);
  };

  return (
    <>
      {isModifiable && (
        <MetadataRolesProvider>
          <AddNewMetadata
            sampleId={sampleId}
            refetch={refetchSampleMetadata}
            projectId={projectId}
          >
            <Button style={{ marginLeft: "15px" }} icon={<IconPlusCircle />}>
              {i18n("SampleMetadata.addNewMetadata")}
            </Button>
          </AddNewMetadata>
        </MetadataRolesProvider>
      )}
      <div>
        {!isLoading ? (
          Object.keys(data.metadata).length ? (
            <>
              <StyledListMetadata
                itemLayout="horizontal"
                dataSource={data.metadata
                  .slice()
                  .sort((a, b) =>
                    a.metadataTemplateField.localeCompare(
                      b.metadataTemplateField
                    )
                  )}
                renderItem={(item) => (
                  <List.Item className="t-sample-details-metadata-item">
                    <List.Item.Meta
                      title={
                        <span className="t-sample-details-metadata__field">
                          {item.metadataTemplateField}
                        </span>
                      }
                      description={
                        <span className="t-sample-details-metadata__entry">
                          {item.metadataEntry}
                        </span>
                      }
                    />
                    {isModifiable && (
                      <Space size="small" direction="horizontal">
                        <Button
                          shape="circle"
                          icon={
                            <IconEdit
                              onClick={() => {
                                setEditModalVisible(true);
                                setField(item.metadataTemplateField);
                                setFieldId(item.fieldId);
                                setEntryId(item.entryId);
                                setEntry(item.metadataEntry);
                              }}
                            />
                          }
                        />
                        <Popconfirm
                          placement={"topRight"}
                          title={`Are you sure you want to delete the entry for field ${item.metadataTemplateField}?`}
                          onConfirm={() =>
                            removeMetadata(
                              item.metadataTemplateField,
                              item.entryId
                            )
                          }
                          okText="Confirm"
                        >
                          <Button shape="circle" icon={<IconRemove />} />
                        </Popconfirm>
                      </Space>
                    )}
                  </List.Item>
                )}
              />
              <MetadataRolesProvider>
                <EditMetadata
                  sampleId={sampleId}
                  projectId={projectId}
                  metadataField={field}
                  metadataFieldId={fieldId}
                  metadataEntryId={entryId}
                  metadataEntry={entry}
                  refetch={refetchSampleMetadata}
                  visible={editModalVisible}
                  onCancel={() => onCancel()}
                  onOk={() => onOk()}
                ></EditMetadata>
              </MetadataRolesProvider>
            </>
          ) : (
            <Empty description={i18n("SampleDetails.no-metadata")} />
          )
        ) : (
          <ContentLoading />
        )}
      </div>
    </>
  );
}
