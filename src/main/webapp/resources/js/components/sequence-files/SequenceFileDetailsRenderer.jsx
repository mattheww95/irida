import React from "react";
import { Avatar, Button, List, Space } from "antd";
import { IconDownloadFile } from "../icons/Icons";
import { FastQC } from "../samples/components/fastqc/FastQC";
import { setFastQCModalData } from "../samples/components/fastqc/fastQCSlice";
import { useDispatch } from "react-redux";

/**
 * React component to display paired end file details
 *
 * @param file The file to display details for
 * @param fileObjectId The sequencingobject identifier
 * @param {function} download sequence file function
 * @param {function} get file processing state function
 * @returns {JSX.Element}
 * @constructor
 */
export function SequenceFileDetailsRenderer({
  file,
  fileObjectId,
  downloadSequenceFile = () => {},
  getProcessingState = () => {},
}) {
  const dispatch = useDispatch();

  return (
    <List.Item
      key={`file-${file.id}`}
      style={{ width: `100%` }}
      className="t-file-details"
    >
      <List.Item.Meta
        avatar={<Avatar size={`small`} icon={file.icon} />}
        title={
          <div style={{ display: "flex", justifyContent: "space-between" }}>
            <div>
              <Button
                type="link"
                style={{ padding: 0 }}
                className="t-file-label"
                onClick={() =>
                  dispatch(
                    setFastQCModalData({
                      fileLabel: file.label,
                      fileId: file.id,
                      sequencingObjectId: fileObjectId,
                      fastQCModalVisible: true,
                      processingState: file.processingState,
                    })
                  )
                }
              >
                {file.label}
              </Button>
              <FastQC />
            </div>

            <Space direction="horizontal" size="small">
              {getProcessingState(file.processingState)}
              <span className="t-file-size">{file.filesize}</span>
              <Button
                shape="circle"
                icon={<IconDownloadFile />}
                className="t-download-file-btn"
                onClick={() => {
                  downloadSequenceFile({
                    sequencingObjectId: fileObjectId,
                    sequenceFileId: file.id,
                  });
                }}
              />
            </Space>
          </div>
        }
      />
    </List.Item>
  );
}
