import React, { useEffect, useRef, useState } from "react";
import { Button, Checkbox, Form, Input, Select } from "antd";
import {
  createSynchronizedProject,
  getListOfRemoteApis,
  getProjectsForAPI,
} from "../../apis/remote-api/remote-api";
import { setBaseUrl } from "../../utilities/url-utilities";
import { RemoteApiStatus } from "../../pages/admin/components/remote-connections/RemoteApiStatus";
import { HelpPopover } from "../popovers";
import { SyncFrequencySelect } from "./SyncFrequencySelect";

/**
 * React form for creating a Synchronized Remote Project
 * @returns {JSX.Element}
 * @constructor
 */
export function CreateRemoteProjectSyncForm() {
  const [apis, setApis] = useState([]);
  const [selectedApi, setSelectedApi] = useState();
  const [newRemoteProjectUrlError, setNewRemoteProjectUrlError] =
    useState(null);
  const [projects, setProjects] = useState([]);
  const [connected, setConnected] = useState();
  const [manual, setManual] = useState(false);
  const [form] = Form.useForm();
  const apiRef = useRef();

  useEffect(() => {
    // Load all remote api's at mount time
    getListOfRemoteApis().then((list) => {
      setApis(list);
      if (list.length === 1) {
        form.setFieldsValue({ api: 0 });
        setSelectedApi(list[0]);
      }
      // Set user focus on the api select after at mount time
      apiRef.current.focus();
    });
  }, [form]);

  /**
   * Update the status of a specific api.
   * @param {number} value - index of the api in the apis list.
   */
  const updateApiStatus = (value) => setSelectedApi(apis[value]);

  const getApiProjects = () => {
    if (selectedApi.id) {
      getProjectsForAPI({ id: selectedApi.id })
        .then(setProjects)
        .then(() => setConnected(true));
    }
  };

  const createRemote = () => {
    form.validateFields().then(({ frequency, url }) => {
      setNewRemoteProjectUrlError(null);
      createSynchronizedProject({
        url,
        frequency,
      })
        .then(
          ({ id }) => (window.location.href = setBaseUrl(`/projects/${id}`))
        )
        .catch((error) => setNewRemoteProjectUrlError(error));
    });
  };

  return (
    <Form
      form={form}
      layout="vertical"
      initialValues={{
        frequency: 2,
      }}
    >
      <Form.Item
        name="api"
        label={i18n("NewProjectSync.api")}
        help={i18n("NewProjectSync.api.help")}
      >
        <Select
          className="t-api-select"
          ref={apiRef}
          showSearch
          onChange={updateApiStatus}
          placeholder={i18n("NewProjectSync.api.placeholder")}
        >
          {apis.map((api, index) => (
            <Select.Option key={`api-${api.id}`} value={index}>
              {api.name}
            </Select.Option>
          ))}
        </Select>
      </Form.Item>
      {selectedApi ? (
        <>
          <Form.Item label={i18n("NewProjectSync.api-status")}>
            <RemoteApiStatus api={selectedApi} onConnect={getApiProjects} />
          </Form.Item>
          {connected ? (
            <>
              <Form.Item label={i18n("NewProjectSync.project")} name="project">
                <Select
                  className="t-project-select"
                  onChange={(value) => form.setFieldsValue({ url: value })}
                  showSearch
                  options={projects}
                  placeholder={i18n("NewProjectSync.project.placeholder")}
                />
              </Form.Item>
              <Form.Item
                validateStatus={newRemoteProjectUrlError !== null && "error"}
                help={
                  newRemoteProjectUrlError !== null && newRemoteProjectUrlError
                }
                rules={[
                  {
                    required: true,
                    message: i18n("NewProjectSync.remoteUrl.required"),
                  },
                ]}
                label={
                  <span>
                    {i18n("NewProjectSync.remoteUrl")}
                    <Checkbox
                      className="t-remote-project-url-checkbox"
                      onChange={(e) => {
                        setManual(e.target.checked);
                        setNewRemoteProjectUrlError(null);
                      }}
                    >
                      {i18n("NewProjectSync.remoteUrl.manual")}
                      <HelpPopover
                        content={<div>{i18n("NewProjectSync.url.help")}</div>}
                      />
                    </Checkbox>
                  </span>
                }
                name="url"
              >
                <Input
                  className="t-project-url"
                  disabled={!manual}
                  onChange={() => setNewRemoteProjectUrlError(null)}
                />
              </Form.Item>
              <SyncFrequencySelect />
            </>
          ) : null}
          <div style={{ display: "flex", flexDirection: "row-reverse" }}>
            <Button
              type="primary"
              onClick={createRemote}
              className="t-sync-submit"
            >
              {i18n("NewProjectSync.submit")}
            </Button>
          </div>
        </>
      ) : null}
    </Form>
  );
}
