import React, { lazy, Suspense } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Button, Dropdown, Menu, message, Space } from "antd";
import {
  CloseSquareOutlined,
  DownOutlined,
  MergeCellsOutlined,
  ShareAltOutlined
} from "@ant-design/icons";
import { reloadTable, updateTable } from "../services/samplesSlice";
import { setBaseUrl } from "../../../../utilities/url-utilities";
import { validateSamplesForRemove } from "../services/sample.utilities";

const MergeModal = lazy(() => import("./MergeModal"));
const RemoveModal = lazy(() => import("./RemoveModal"));

/**
 * React element to render a row of actions that can be performed on
 * samples in the table
 * @returns {JSX.Element}
 * @constructor
 */
export default function SamplesMenu() {
  const dispatch = useDispatch();

  const { projectId, selected } = useSelector(state => state.samples);

  const [mergeVisible, setMergeVisible] = React.useState(false);
  const [removedVisible, setRemovedVisible] = React.useState(false);
  const [sorted, setSorted] = React.useState({});

  /**
   * When a merge is completed, hide the modal and ask
   * the table to reset
   */
  const onMergeComplete = () => {
    setMergeVisible(false);
    dispatch(reloadTable());
  };

  const onRemoveComplete = () => {
    setRemovedVisible(false);
    dispatch(reloadTable());
  };

  /**
   * Format samples to share with other projects,
   * store minimal information in localStorage
   */
  const shareSamples = () => {
    if (selected.size === 0) return;

    const samples = [];
    Object.values(selected).forEach(({ id, sampleName: name, owner }) => {
      samples.push({ id, name, owner });
    });

    // Store them to window storage for later use.
    window.sessionStorage.setItem(
      "share",
      JSON.stringify({
        samples,
        projectId,
        timestamp: Date.now()
      })
    );

    // Redirect user to share page
    window.location.href = setBaseUrl(`/projects/${projectId}/share`);
  };

  const sortSampledFor = name => {
    if (name === "remove") {
      const validated = validateSamplesForRemove(selected, projectId);
      if (validated.valid.length > 0) {
        setSorted(validated);
        setRemovedVisible(true);
      } else
        message.error("No selected samples can be removed from this project");
    }
  };

  const toolsMenu = React.useMemo(() => {
    return (
      <Menu>
        <Menu.Item
          key="merge-menu"
          icon={<MergeCellsOutlined />}
          onClick={() => setMergeVisible(true)}
        >
          {i18n("SamplesMenu.merge")}
        </Menu.Item>
        <Menu.Item
          key="share-menu"
          icon={<ShareAltOutlined />}
          onClick={shareSamples}
        >
          {i18n("SamplesMenu.share")}
        </Menu.Item>
        <Menu.Item
          key="remove-menu"
          icon={<CloseSquareOutlined />}
          onClick={() => sortSampledFor("remove")}
        >
          {i18n("SamplesMenu.remove")}
        </Menu.Item>
      </Menu>
    );
  }, [selected]);

  return (
    <>
      <Space>
        <Dropdown overlay={toolsMenu}>
          <Button>
            {i18n("SamplesMenu.label")} <DownOutlined />
          </Button>
        </Dropdown>
      </Space>
      {mergeVisible && (
        <Suspense fallback={<span />}>
          <MergeModal
            visible={mergeVisible}
            onComplete={onMergeComplete}
            onCancel={() => setMergeVisible(false)}
            samples={selected}
          />
        </Suspense>
      )}
      {removedVisible && (
        <Suspense fallback={<span />}>
          <RemoveModal
            visible={removedVisible}
            onComplete={onRemoveComplete}
            onCancel={() => setRemovedVisible(false)}
            samples={sorted}
          />
        </Suspense>
      )}
    </>
  );
}
