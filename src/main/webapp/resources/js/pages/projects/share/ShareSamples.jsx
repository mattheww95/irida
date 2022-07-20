import { Alert, Checkbox, Collapse, Space, Typography } from "antd";
import React from "react";
import { useDispatch, useSelector } from "react-redux";
import ShareAssociated from "./ShareAssociated";
import { SharedSamplesList } from "./SharedSamplesList";
import { updatedLocked, updateMoveSamples } from "./shareSlice";
import { ExclamationCircleOutlined } from "@ant-design/icons";
import { SPACE_XS } from "../../../styles/spacing";

const { Panel } = Collapse;
/**
 * React component to review the samples to be shared with another project.
 *
 * @returns {JSX.Element}
 * @constructor
 */
export function ShareSamples({
  samples = [],
  targetProjectSampleIdsDuplicate = [],
  targetProjectSampleNamesDuplicate = [],
}) {
  const dispatch = useDispatch();
  const {
    associated,
    samples: originalSamples,
    locked,
    remove,
  } = useSelector((state) => state.shareReducer);

  return (
    <Space direction="vertical" style={{ width: `100%` }}>
      <Typography.Title level={5}>
        {i18n("ShareSamplesList.title")}
      </Typography.Title>
      {associated.length > 0 && <ShareAssociated />}
      {samples.length > 0 && (
        <>
          <SharedSamplesList list={samples} />
          <Checkbox
            className="t-move-checkbox"
            checked={remove}
            onChange={(e) => dispatch(updateMoveSamples(e.target.checked))}
          >
            <Typography.Text strong>
              {i18n("ShareSamples.checkbox.remove")}
            </Typography.Text>
          </Checkbox>
          <Checkbox
            className="t-lock-checkbox"
            checked={locked}
            onChange={(e) => dispatch(updatedLocked(e.target.checked))}
            disabled={remove}
          >
            <Typography.Text strong>
              {i18n("ShareSamples.checkbox.lock")}
            </Typography.Text>
          </Checkbox>
        </>
      )}
      {samples.length === 0 && (
        <Alert
          type="warning"
          className="t-no-sample-warning"
          showIcon
          message={i18n("ShareSamples.no-samples.message")}
          description={i18n("ShareSamples.no-samples.description")}
        />
      )}
      {originalSamples.length - samples.length > 0 &&
        targetProjectSampleIdsDuplicate.length > 0 && (
          <Collapse className="t-same-sample-ids-warning">
            <Panel
              header={
                <div>
                  <ExclamationCircleOutlined
                    style={{ color: `var(--gold-6)`, marginRight: SPACE_XS }}
                  />
                  {i18n("ShareSamples.some-samples-same-ids.message")}
                </div>
              }
              key="1"
              style={{ backgroundColor: `var(--grey-1)` }}
            >
              <SharedSamplesList
                list={targetProjectSampleIdsDuplicate}
                itemActionsRequired={false}
              />
            </Panel>
          </Collapse>
        )}
      {originalSamples.length - samples.length > 0 &&
        targetProjectSampleNamesDuplicate.length > 0 && (
          <Collapse className="t-same-sample-names-warning">
            <Panel
              header={
                <div>
                  <ExclamationCircleOutlined
                    style={{ color: `var(--gold-6)`, marginRight: SPACE_XS }}
                  />
                  {i18n("ShareSamples.some-samples-same-names.message")}
                </div>
              }
              key="1"
              style={{ backgroundColor: `var(--grey-1)` }}
            >
              <SharedSamplesList
                list={targetProjectSampleNamesDuplicate}
                itemActionsRequired={false}
              />
            </Panel>
          </Collapse>
        )}
    </Space>
  );
}
