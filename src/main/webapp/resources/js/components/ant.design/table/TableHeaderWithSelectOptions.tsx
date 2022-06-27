import {EditOutlined} from "@ant-design/icons";
import {Button, Form, Popover, Select} from "antd";
import React from "react";
import TextWithHelpPopover from "../TextWithHelpPopover";

// TODO: WTF should I call this thing

type Props = {
  title: JSX.Element | string;
  options: string[];
  onChange: (value: string) => void;
  helpText: string;
};

/**
 * React component for the Ant Design Table that renders a popover containing a Select,
 * allowing the user to select a default value for all entries in the column.
 *
 * @returns {JSX.Element}
 * @constructor
 */
export const TableHeaderWithSelectOptions = React.forwardRef(
  function TableHeaderWithSelectOptions(
    { title, options = [], onChange, helpText }: Props,
    ref
  ): JSX.Element {
    const [form] = Form.useForm();

    React.useImperativeHandle(ref, () => ({
      resetSelect() {
        form.resetFields();
      },
    }));

    return (
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <TextWithHelpPopover text={title} help={helpText} />
        <Popover
          title={"Select value for all samples"}
          content={
            <Form form={form}>
              <Form.Item style={{ margin: 0 }} name="select">
                <Select style={{ display: "block" }} onChange={onChange}>
                  {options.map((option) => (
                    <Select.Option key={option}>{option}</Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Form>
          }
        >
          <Button
            size="small"
            type="text"
            icon={<EditOutlined style={{ color: `var(--grey-7)` }} />}
          />
        </Popover>
      </div>
    );
  }
);
