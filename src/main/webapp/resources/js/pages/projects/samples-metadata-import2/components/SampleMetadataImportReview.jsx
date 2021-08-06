import React from "react";
import { navigate } from "@reach/router"
import {
  Badge,
  Button,
  Space,
  Table,
  Tabs,
  Typography,
} from "antd";
import { SampleMetadataImportWizard } from "./SampleMetadataImportWizard";
import { useGetProjectSampleMetadataQuery } from "../../../../apis/metadata/metadata-import";
import { IconArrowLeft, IconArrowRight } from "../../../../components/icons/Icons";

const { Text } = Typography

/**
 * React component that displays Step #3 of the Sample Metadata Uploader.
 * This page is where the user reviews the metadata to be uploaded.
 * @returns {*}
 * @constructor
 */
export function SampleMetadataImportReview({ projectId }) {
  const { data } = useGetProjectSampleMetadataQuery(projectId);
  const { TabPane } = Tabs;

  const columns = data?.headers?.map((header) => {
    let item = { title: header, dataIndex: header };
    return item;
  });

  const foundDataSource = data?.found?.map((item, index) => {
    let newItem = { ...item, key: index };
    return newItem;
  });

  const missingDataSource = data?.missing?.map((item, index) => {
    let newItem = { ...item, key: index };
    return newItem;
  });

  return (
    <SampleMetadataImportWizard currentStep={2}>
      <Text>
        {i18n("SampleMetadataImportReview.description")}
      </Text>
      <Tabs type="card">
        <TabPane tab={<Space>Rows matching samples<Badge count={data?.found?.length} style={{ backgroundColor: 'green' }} /></Space>} key="1">
          <Table columns={columns} dataSource={foundDataSource} scroll={{ x: 1500 }} />
        </TabPane>
        <TabPane tab={<Space>Rows not matching samples<Badge count={data?.missing?.length} /></Space>} key="2">
          <Table columns={columns} dataSource={missingDataSource} scroll={{ x: 1500 }} />
        </TabPane>
      </Tabs>
      <div style={{display:'flex'}}>
        <Button icon={<IconArrowLeft />} onClick={() => navigate(-1)}> {i18n("SampleMetadataImportReview.back")}</Button>
        <Button style={{marginLeft:'auto'}}>
          {i18n("SampleMetadataImportReview.next")}
          <IconArrowRight />
        </Button>
      </div>
    </SampleMetadataImportWizard>
  );
}