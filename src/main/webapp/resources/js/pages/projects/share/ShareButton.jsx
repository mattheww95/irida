import { Button, Menu } from "antd";
import React from "react";
import { useSelector } from "react-redux";
import { useShareSamplesWithProjectMutation } from "../../../apis/projects/samples";

export function ShareButton() {
  const { samples, owner, remove, projectId, currentProject } = useSelector(
    (state) => state.shareReducer
  );
  const [
    shareSamplesWithProject,
    { isLoading, isUpdating },
  ] = useShareSamplesWithProjectMutation();

  const shareSamples = () => {
    shareSamplesWithProject({
      sampleIds: samples.map((s) => s.id),
      owner,
      currentId: currentProject,
      targetId: projectId,
      remove,
    });
  };

  const disabled = samples.length === 0 || typeof projectId === "undefined";

  return (
    <div style={{ display: "flex", flexDirection: "row-reverse" }}>
      <Button
        disabled={disabled}
        onClick={() => shareSamples("share")}
        overlay={
          <Menu onClick={(e) => shareSamples(e.key)}>
            <Menu.Item key="move">{i18n("ShareButton.move")}</Menu.Item>
          </Menu>
        }
      >
        {i18n("ShareButton.copy")}
      </Button>
    </div>
  );
}
