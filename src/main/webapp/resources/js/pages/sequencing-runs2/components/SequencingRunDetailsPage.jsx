import React from "react";
import { useParams } from "react-router-dom";
import {
  useGetSequencingRunDetailsQuery,
  useGetSequencingRunFilesQuery
} from "../../../apis/sequencing-runs/sequencing-runs";
import { Badge, Button, Col, Descriptions, Row, Table, Typography } from "antd";
import { formatDate } from "../../../utilities/date-utilities";
import { PageWrapper } from "../../../components/page/PageWrapper";
import { setBaseUrl } from "../../../utilities/url-utilities";
import { IconDownloadFile } from "../../../../js/components/icons/Icons";
import { LinkButton } from "../../../components/Buttons/LinkButton";

/**
 * React component to display the sequencing run details page.
 * @returns {*}
 * @constructor
 */
export default function SequencingRunDetailsPage() {
  const isTechnician = window.TL._USER.systemRole === "ROLE_TECHNICIAN";
  const {runId} = useParams();
  const {data: run = {}} = useGetSequencingRunDetailsQuery(runId);
  const {
    data: files = [],
    isLoading: isFilesLoading
  } = useGetSequencingRunFilesQuery(runId);
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      align: 'right'
    },
    {
      title: 'Filename',
      dataIndex: 'fileName',
      key: 'fileName',
      render(text, item) {
        return <LinkButton
          text={text}
          href={setBaseUrl(`/sequencingRuns/${runId}/sequenceFiles/${item.sequencingObjectId}/file/${item.id}/summary`)}
        />
      },
    },
    {
      title: 'Size',
      dataIndex: 'fileSize',
      key: 'fileSize',
      align: 'right'
    },
    {
      title: 'Download',
      dataIndex: 'download',
      key: 'download',
      render(text, item) {
        return <Button shape="circle"
                       onClick={() => window.open(setBaseUrl(`/sequenceFiles/download/${item.sequencingObjectId}/file/${item.id}`), "_blank")}
                       icon={<IconDownloadFile/>}/>;
      },
      align: 'center'
    }
  ]

  return (
    <PageWrapper title={i18n("SequenceRunDetailsPage.title", runId)}>
      <Row justify="center" gutter={[0, 16]}>
        <Col span={18}>
          <Typography.Title level={5}>Details</Typography.Title>
        </Col>
        <Col span={18}>
          <Descriptions bordered>
            <Descriptions.Item
              label="Sequencer Type">{run.sequencerType}</Descriptions.Item>
            <Descriptions.Item
              label="Upload Status"><UploadStatusBadge
              status={run.uploadStatus}/></Descriptions.Item>
            <Descriptions.Item
              label="Upload User">
              {isTechnician ? run.userName : (<LinkButton
                  text={run.userName}
                  href={setBaseUrl(`/users/${run.userId}`)}/>
              )}
            </Descriptions.Item>
            <Descriptions.Item
              label="Created">{formatDate({date: run.createdDate})}</Descriptions.Item>
            <Descriptions.Item
              label="Description">{run.description}</Descriptions.Item>
          </Descriptions>
        </Col>
        {run.optionalProperties && (
          <>
            <Col span={18}>
              <Typography.Title level={5}>Additional
                Properties</Typography.Title>
            </Col>
            <Col span={18}>
              <Descriptions bordered>
                {Object.entries(run.optionalProperties).map(([key, value]) => (
                  <Descriptions.Item
                    label={key}>{value}</Descriptions.Item>
                ))}
              </Descriptions>
            </Col>
          </>
        )}
        <Col span={18}>
          <Typography.Title level={5}>Files</Typography.Title>
        </Col>
        <Col span={18}>
          <Table loading={isFilesLoading}
                 dataSource={files}
                 columns={columns}
                 scroll={{x: "max-content", y: 600}}
                 pagination={false}/>
        </Col>
      </Row>
    </PageWrapper>
  );
}

function UploadStatusBadge({status}) {
  switch (status) {
    case "ERROR":
      return <Badge status="error" text={status}/>;
    case "UPLOADING":
      return <Badge status="warning" text={status}/>;
    case "COMPLETE":
      return <Badge status="success" text={status}/>;
    default:
      return <Badge text={status}/>;
  }
}